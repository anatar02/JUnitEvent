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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Collect, filter and prepare the test classes.
 */
public class Request {
    private Set<Class<?>> testClasses = new HashSet<Class<?>>();

    public Request(String... arguments) {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();

        for (String className : arguments) {
            try {
                testClasses.add(loader.loadClass(className));
            } catch (ClassNotFoundException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    }

    public Request(Class<?>... classes) {
        testClasses.addAll(Arrays.asList(classes));
    }

    public Collection<Class<?>> getTestClasses() {
        return testClasses;
    }
}
