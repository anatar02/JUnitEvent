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

package org.dhaven.jue.core.internal.node;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import org.dhaven.jue.api.Description;
import org.dhaven.jue.core.TestEventListenerSupport;

import static org.dhaven.jue.Annotations.Ignore;
import static org.dhaven.jue.Annotations.Test;

/**
 * Codifies a discrete test.
 */
public class Testlet extends DependencyTestNode {
    private Description description;
    private boolean ignored;
    private Method method;
    private Object testCase;
    private List<Method> setup = new LinkedList<Method>();
    private List<Method> tearDown = new LinkedList<Method>();
    private Class<? extends Throwable> expected;

    public Testlet(Object instance, Method testMethod) {
        this(instance, testMethod, String.format("%s.%s",
                testMethod.getDeclaringClass().getName(),
                testMethod.getName()));
    }

    public Testlet(Object instance, Method testMethod, String testName) {
        testCase = instance;
        description = new Description(testName);
        method = testMethod;
        ignored = method.getAnnotation(Ignore.class) != null;

        Test annotation = method.getAnnotation(Test.class);
        expected = annotation.expected();
        if (Test.None.class.equals(expected)) {
            expected = null;
        }
    }

    public Description getDescription() {
        return description;
    }

    @SuppressWarnings({"ThrowableInstanceNeverThrown", "ThrowableResultOfMethodCallIgnored"})
    @Override
    public void run(TestEventListenerSupport support) {
        support.fireTestStarted(this);

        if (isIgnored()) {
            support.fireTestIgnored(this);
        }

        try {
            setup();
            method.invoke(testCase);
            if (null == expected) {
                support.fireTestPassed(this);
            } else {
                AssertionError failure = new AssertionError("Expected " + expected.getName() + " to be thrown.");
                support.fireTestFailed(this, failure);
            }
        } catch (Throwable throwable) {
            if (null != expected) {
                Throwable check = throwable;

                if (check instanceof InvocationTargetException) {
                    check = InvocationTargetException.class.cast(check).getCause();
                }

                if (check.getClass().isAssignableFrom(expected)) {
                    support.fireTestPassed(this);
                } else {
                    support.fireTestFailed(this, check);
                }
            } else {
                support.fireTestFailed(this, throwable);
            }
        } finally {
            try {
                tearDown();
            }
            catch (Exception e) {
                support.fireTestFailed(this, e);
            }
        }
    }

    private void tearDown() throws InvocationTargetException, IllegalAccessException {
        executeMethods(tearDown);
    }

    private void setup() throws InvocationTargetException, IllegalAccessException {
        executeMethods(setup);
    }

    private void executeMethods(List<Method> methods) throws InvocationTargetException, IllegalAccessException {
        for (Method method : methods) {
            method.invoke(testCase);
        }
    }

    public boolean isIgnored() {
        return ignored;
    }

    public void ignore() {
        ignored = true;
    }

    public void execute() {
        ignored = false;
    }

    public void addSetup(List<Method> setupMethods) {
        setup.addAll(setupMethods);
    }

    public void addTearDown(List<Method> tearDownMethods) {
        tearDown.addAll(tearDownMethods);
    }
}
