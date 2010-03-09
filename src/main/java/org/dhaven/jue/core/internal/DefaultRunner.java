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
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.dhaven.jue.Annotations;
import org.dhaven.jue.api.Description;
import org.dhaven.jue.api.event.EventType;
import org.dhaven.jue.api.event.Status;
import org.dhaven.jue.core.internal.node.EventNode;
import org.dhaven.jue.core.internal.node.TestNode;
import org.dhaven.jue.core.internal.node.Testlet;

/**
 * The Default Runner enables the core annotations found in {@link Annotations}.
 */
public class DefaultRunner implements Runner {
    @Override
    public Collection<? extends TestNode> defineTests(Class<?> testCase) throws Exception {
        Description caseDescription = new Description(testCase.getName());
        EventNode start = new EventNode(caseDescription, EventType.StartTestCase, Status.Running);
        EventNode end = new EventNode(caseDescription, EventType.EndTestCase, Status.Terminated);
        start.addSuccessor(end);

        List<Testlet> tests = new LinkedList<Testlet>();
        List<Method> befores = new LinkedList<Method>();
        List<Method> afters = new LinkedList<Method>();

        for (Method method : testCase.getDeclaredMethods()) {
            // Ensure new object instance with each testlet
            Object instance = testCase.newInstance();

            if (hasAnnotation(method, Annotations.Test.class)) {
                Testlet testlet = new Testlet(instance, method);
                testlet.addPredecessor(start);
                testlet.addSuccessor(end);
                tests.add(testlet);
            }

            if (hasAnnotation(method, Annotations.Before.class)) {
                befores.add(method);
            }

            if (hasAnnotation(method, Annotations.After.class)) {
                afters.add(method);
            }
        }

        List<TestNode> nodeList = new ArrayList<TestNode>(tests.size() + 2);
        nodeList.add(start);

        for (Testlet testlet : tests) {
            testlet.addSetup(befores);
            testlet.addTearDown(afters);
            nodeList.add(testlet);
        }

        nodeList.add(end);

        return nodeList;
    }

    private static boolean hasAnnotation(Method method, Class<? extends Annotation> annotation) {
        return method.getAnnotation(annotation) != null;
    }
}
