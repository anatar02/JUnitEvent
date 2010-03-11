/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.dhaven.jue.api;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * TODO: make it work reliably and test.  Assumes too much.
 */
public class ClassCollector {
    private Class<? extends Annotation>[] methodAnnotations;
    private String basePackage = "";
    private boolean excludeInnerClasses = false;
    private boolean recurse = false;
    private File basePath;
    private ClassLoader loader = null;

    public void setClassLoader(ClassLoader loader) {
        this.loader = loader;
    }

    ClassLoader getClassLoader() {
        if (null == loader) {
            loader = Thread.currentThread().getContextClassLoader();
        }

        return loader;
    }

    public void methodsHaveAnnotation(Class<? extends Annotation>... annotation) {
        methodAnnotations = annotation;
    }

    public void setBasePackage(String packageName) {
        basePackage = (null == packageName) ? "" : packageName;
    }

    public Class<?>[] collect() throws Exception {
        ArrayList<Class<?>> classes = new ArrayList<Class<?>>(20);

        if (null == basePath) {
            List<URL> paths = Collections.list(getClassLoader().getResources(basePackage.replace('.', '/')));

            for (URL path : paths) {
                addClassesFromPath(classes, path);
            }
        } else {
            handleFile(classes, basePath);
        }

        return classes.toArray(new Class<?>[classes.size()]);
    }

    private void addClassesFromPath(ArrayList<Class<?>> classes, URL url) throws Exception {
        if ("file".equals(url.getProtocol())) {
            handleFile(classes, new File(URLDecoder.decode(url.getFile(), "utf-8")));
        } else if ("jar".equals(url.getProtocol())) {
            String jarName = url.getFile().substring(0, url.getFile().indexOf("!"));
            handleJar(classes, new JarFile(jarName));
        }
    }

    private void handleFile(ArrayList<Class<?>> classes, File base) {
        File[] files = base.listFiles();

        checkListing(base, files);

        for (File file : files) {
            if (file.getName().endsWith(".class")) {
                String className = file.getPath();
                className = className.substring(className.indexOf("test-classes") + "test-classes".length() + 1);
                className = className.substring(0, className.length() - ".class".length());
                className = className.replace(File.separatorChar, '.');

                addClassIfMatches(classes, className);
            } else if (recurse && file.isDirectory()) {
                handleFile(classes, file);
            }
        }
    }

    private void checkListing(File base, File[] files) {
        if (null == files) {
            if (base.exists()) {
                throw new IllegalStateException(
                        base.isDirectory() ?
                                "Dang Java implementers thought it was a good idea " +
                                        "to return null instead of throw an exception when" +
                                        " it couldn't list files for a valid directory."
                                : base.getPath() + " is not a directory");
            } else {
                throw new IllegalStateException(base.getPath() + " does not exist");
            }
        }
    }

    private void handleJar(ArrayList<Class<?>> classes, JarFile file) throws IOException {
        for (JarEntry entry : Collections.list(file.entries())) {
            String className = entry.getName();
            className = className.substring(0, className.length() - ".class".length());
            className = className.replace('/', '.');

            if (className.startsWith(basePackage)) {
                if (recurse) {
                    addClassIfMatches(classes, className);
                } else if (className.substring(basePackage.length()).lastIndexOf('.') <= 0) {
                    addClassIfMatches(classes, className);
                }
            }
        }
    }

    private void addClassIfMatches(ArrayList<Class<?>> classes, String className) {
        Class<?> classToCheck;

        try {
            classToCheck = getClassLoader().loadClass(className);
        } catch (ClassNotFoundException cnfe) {
            System.out.println("Could not find class: " + className);
            return;
        }
        boolean matches = false;

        if (null != methodAnnotations) {
            matches = checkMethodAnnotations(classToCheck);
        }

        if (matches && excludeInnerClasses) {
            matches = classToCheck.getEnclosingClass() == null;
        }

        if (matches) {
            classes.add(classToCheck);
        }
    }

    private boolean checkMethodAnnotations(Class<?> classToCheck) {
        boolean matches = false;

        for (Method method : classToCheck.getMethods()) {
            for (Class<? extends Annotation> annotation : methodAnnotations) {
                matches = method.getAnnotation(annotation) != null;
                if (matches) break;
            }
            if (matches) break;
        }

        return matches;
    }

    public void noInternalClasses() {
        excludeInnerClasses = true;
    }

    public void recursiveSearch() {
        recurse = true;
    }

    public void setBasePath(File path) {
        assert path.exists() && path.isDirectory();
        basePath = path;
    }
}
