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

import org.dhaven.jue.Ignore;
import org.dhaven.jue.api.event.EventType;
import org.dhaven.jue.api.event.Status;
import org.dhaven.jue.core.TestEventListenerSupport;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

/**
 * Codifies a discrete test.
 */
public class Testlet implements TestNode, Comparable<Testlet> {
    private String name;
    private boolean ignored;
    private Method method;
    private Object testCase;
    private List<Method> setup = new LinkedList<Method>();
    private List<Method> tearDown = new LinkedList<Method>();

    public Testlet(Object instance, Method testMethod) {
        this(instance, testMethod, String.format("%s.%s",
                testMethod.getDeclaringClass().getName(),
                testMethod.getName()));
    }

    public Testlet(Object instance, Method testMethod, String testName) {
        testCase = instance;
        name = testName;
        method = testMethod;
        ignored = method.getAnnotation(Ignore.class) != null;
    }

    public String getName() {
        return name;
    }

    @Override
    public void run(TestEventListenerSupport support) throws Exception {
        if (ignored) {
            support.fireTestEvent(getName(), EventType.EndTest, Status.Ignored);
            return;
        }

        try {
            support.fireTestEvent(getName(), EventType.StartTest, Status.Running);
            setup();
            method.invoke(testCase);
            support.fireTestEvent(getName(), EventType.EndTest, Status.Passed);
        } catch (Throwable throwable) {
            support.fireTestEvent(getName(), throwable);
        } finally {
            tearDown();
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

    @Override
    public int compareTo(Testlet other) {
        return 0;
    }

    public void addSetup(List<Method> setupMethods) {
        setup.addAll(setupMethods);
    }

    public void addTearDown(List<Method> tearDownMethods) {
        tearDown.addAll(tearDownMethods);
    }
}
