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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Created by IntelliJ IDEA.
 * User: berin.loritsch
 * Date: Mar 5, 2010
 * Time: 12:39:41 PM
 * To change this template use File | Settings | File Templates.
 */
public class ClassCollector {
    private Class<? extends Annotation>[] methodAnnotations;
    private String basePackage = "";
    private boolean excludeInnerClasses = false;
    private boolean recurse = false;

    private ClassLoader getClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }

    public void methodsHaveAnnotation(Class<? extends Annotation>... annotation) {
        methodAnnotations = annotation;
    }

    public void setBasePackage(String packageName) {
        basePackage = (null == packageName) ? "" : packageName;
    }

    public Class<?>[] collect() throws Exception {
        ArrayList<Class<?>> classes = new ArrayList<Class<?>>(20);
        List<URL> paths = Collections.list(getClassLoader().getResources(basePackage.replace('.', '/')));

        for (URL path : paths) {
            addClassesFromPath(classes, path);
        }

        return classes.toArray(new Class<?>[classes.size()]);
    }

    private void addClassesFromPath(ArrayList<Class<?>> classes, URL url) throws Exception {
        if ("file".equals(url.getProtocol())) {
            handleFileURL(classes, url);
        } else if ("jar".equals(url.getProtocol())) {
            handleJarURL(classes, url);
        }
    }

    private void handleFileURL(ArrayList<Class<?>> classes, URL url) {
        File base = new File(url.getFile());

        for (File file : base.listFiles()) {
            if (file.getName().endsWith(".class")) {
                String className = file.getPath();
                className = className.substring(className.indexOf("test-classes/") + "test-classes/".length());
                className = className.substring(0, className.length() - ".class".length());
                className = className.replace(File.separatorChar, '.');

                addClassIfMatches(classes, className);
            } else if (recurse && file.isDirectory()) {
                try {
                    handleFileURL(classes, file.toURI().toURL());
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void handleJarURL(ArrayList<Class<?>> classes, URL url) throws IOException {
        String jarName = url.getFile().substring(0, url.getFile().indexOf("!"));
        JarFile file = new JarFile(jarName);
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
        Class<?> classToCheck = null;

        try {
            classToCheck = getClassLoader().loadClass(className);
        } catch (ClassNotFoundException cnfe) {
            System.out.println("Could not find class: " + className);
            return;
        }
        boolean matches = false;

        if (null != methodAnnotations) {
            matches = matches || checkMethodAnnotations(classToCheck);
        }

        if (matches && excludeInnerClasses) {
            matches = matches && (classToCheck.getEnclosingClass() == null);
        }

        if (matches) {
            classes.add(classToCheck);
        }
    }

    private boolean checkMethodAnnotations(Class<?> classToCheck) {
        boolean matches = false;

        for (Method method : classToCheck.getMethods()) {
            for (Class<? extends Annotation> annotation : methodAnnotations) {
                matches = matches || method.getAnnotation(annotation) != null;
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
}
