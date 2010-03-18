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
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

import org.dhaven.jue.Annotations;
import org.dhaven.jue.Ignore;

/**
 * Collect, filter and prepare the test classes.
 */
public class Request {
    private Set<Class<?>> testClasses = new HashSet<Class<?>>();
    ClassCollector collector = new ClassCollector();

    public Request(String... arguments) throws Exception {
        collector.methodsHaveAnnotation(Annotations.get());
        collector.noInternalClasses();
        collector.recursiveSearch();

        if (arguments.length == 0) {
            throw new IllegalArgumentException("No arguments, no tests.");
        } else if (arguments[0].contains("/") || arguments[0].contains("\\")) {
            File file = new File(arguments[0]).getCanonicalFile();
            URLClassLoader loader =
                    new URLClassLoader(new URL[]{file.toURI().toURL()});

            collector.setClassLoader(loader);
            collector.setBasePath(file);
        } else {
            collector.setBasePackage(arguments[0]);
        }

        testClasses.addAll(Arrays.asList(collector.collect()));
    }

    public ClassLoader getRequestClassLoader() {
        return collector.getClassLoader();
    }

    public Request(Class<?>... classes) {
        testClasses.addAll(Arrays.asList(classes));
    }

    public Collection<Class<?>> getTestClasses() {
        filterClasses();
        return testClasses;
    }

    private void filterClasses() {
        Iterator<Class<?>> it = testClasses.iterator();

        while (it.hasNext()) {
            Class<?> classToVerify = it.next();

            if (classToVerify.getAnnotation(Ignore.class) != null) {
                it.remove();
            }
        }
    }
}
