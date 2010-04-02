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

package org.dhaven.jue.core.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import org.dhaven.jue.After;
import org.dhaven.jue.Before;
import org.dhaven.jue.Test;
import org.dhaven.jue.api.description.Description;
import org.dhaven.jue.api.description.Type;
import org.dhaven.jue.core.internal.node.Testlet;

/**
 * The Default Planner enables the core annotations found in the root package.
 */
public class DefaultPlanner implements Planner {
    @Override
    public TestCase defineTests(Class<?> testCase) throws Exception {
        Description caseDescription = new Description(testCase.getName(), Type.TestCase);
        TestCase testcase = new TestCase(caseDescription);

        List<Testlet> tests = new LinkedList<Testlet>();
        List<Method> setUpMethods = new LinkedList<Method>();
        List<Method> tearDownMethods = new LinkedList<Method>();

        for (Method method : testCase.getMethods()) {
            if (hasAnnotation(method, Test.class)) {
                // Ensure new object instance with each testlet
                Object instance = testCase.newInstance();
                Testlet testlet = new Testlet(instance, method);
                tests.add(testlet);
            }

            if (hasAnnotation(method, Before.class)) {
                setUpMethods.add(method);
            }

            if (hasAnnotation(method, After.class)) {
                tearDownMethods.add(method);
            }
        }

        for (Testlet testlet : tests) {
            testlet.addSetup(setUpMethods);
            testlet.addTearDown(tearDownMethods);
            testcase.addTest(testlet);
        }

        return testcase;
    }

    private static boolean hasAnnotation(Method method, Class<? extends Annotation> annotation) {
        return method.getAnnotation(annotation) != null;
    }
}
