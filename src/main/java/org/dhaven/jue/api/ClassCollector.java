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
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

interface CallBack<T> {
    void deliver(T object);
}

/**
 * The class collector is used to look through your classpath for classes that
 * satisfy your criteria.  The class collector is used by the JUnitEvent
 * internally to discover all the classes that are marked with the appropriate
 * annotations.  It can also be used by your tests to find all implementations
 * of an interface, or classes that extend base classes.
 */
public class ClassCollector implements CallBack<String> {
    private Class<? extends Annotation>[] methodAnnotations = null;
    private String basePackage = "";
    private boolean excludeInnerClasses = false;
    private boolean recurse = false;
    private File basePath;
    private ClassLoader loader = null;
    private List<Class> collectedClasses = new ArrayList<Class>(20);

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
        for (Scanner scanner : createScanners()) {
            scanner.scan();
        }

        return this.collectedClasses.toArray(new Class<?>[collectedClasses.size()]);
    }

    private Iterable<Scanner> createScanners() throws IOException {
        Collection<Scanner> scanners = new ArrayList<Scanner>(5);
        if (null != basePath) {
            scanners.add(new FileScanner(basePath, this));
            return scanners;
        }

        Enumeration<URL> basePaths = getClassLoader().getResources("");
        List<URL> paths = Collections.list(basePaths);

        for (URL base : paths) {
            if ("file".equals(base.getProtocol())) {
                File path = new File(URLDecoder.decode(base.getFile(), "utf-8"));

                // shouldn't be anything else, but better safe than
                // sorry.
                if (path.exists() && path.isDirectory()) {
                    scanners.add(new FileScanner(path, this));
                }
            } else if ("jar".equals(base.getProtocol())) {
                String jarName = base.getFile().substring(0, base.getFile().indexOf("!"));
                scanners.add(new JarScanner(new JarFile(jarName), this));
            }
        }

        return scanners;
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

    @Override
    public void deliver(final String className) {
        if (null != basePackage && !className.startsWith(basePackage)) {
            return;
        }

        if (!recurse && className.substring(basePackage == null ?
                0 : basePackage.length()).lastIndexOf('.') > 0) {
            return;
        }

        Class<?> classToCheck;

        try {
            classToCheck = Class.forName(className, true, getClassLoader());
        } catch (ClassNotFoundException exception) {
            System.out.println("Could not find class: " + className);
            return;
        }

        boolean matches = true;

        if (null != methodAnnotations) {
            matches = checkMethodAnnotations(classToCheck);
        }

        if (matches && excludeInnerClasses) {
            matches = classToCheck.getEnclosingClass() == null;
        }

        if (matches) {
            collectedClasses.add(classToCheck);
        }
    }

    private interface Scanner {
        void scan();
    }

    private final static class FileScanner implements Scanner {
        private final File root;
        private final CallBack<String> handler;

        public FileScanner(File root, CallBack<String> handler) {
            this.root = root;
            this.handler = handler;
        }

        public void scan() {
            scanDirectory(root);
        }

        public void scanDirectory(File dir) {
            File[] files = dir.listFiles();

            checkListing(dir, files);

            for (File file : files) {
                if (file.getName().endsWith(".class")) {
                    String className = file.getPath();

                    // should always be true...
                    if (className.startsWith(root.getPath())) {
                        className = className.substring(root.getPath().length() + 1);
                    }

                    className = className.substring(0, className.length() - ".class".length());
                    className = className.replace(File.separatorChar, '.');

                    handler.deliver(className);
                } else if (file.isDirectory()) {
                    scanDirectory(file);
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
    }

    private final static class JarScanner implements Scanner {
        private final JarFile jar;
        private final CallBack<String> handler;

        public JarScanner(JarFile jar, CallBack<String> handler) {
            this.jar = jar;
            this.handler = handler;
        }

        @Override
        public void scan() {
            for (JarEntry entry : Collections.list(jar.entries())) {
                String className = entry.getName();
                className = className.substring(0, className.length() - ".class".length());
                className = className.replace('/', '.');

                handler.deliver(className);
            }
        }
    }
}
