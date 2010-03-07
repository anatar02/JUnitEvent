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

import org.dhaven.jue.After;
import org.dhaven.jue.Before;
import org.dhaven.jue.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: berin
 * Date: Mar 5, 2010
 * Time: 8:09:37 PM
 * To change this template use File | Settings | File Templates.
 */
public class DefaultRunner implements Runner {
    @Override
    public Collection<TestNode> defineTests(Class<?> testCase) throws Exception {
        List<Testlet> tests = new LinkedList<Testlet>();
        List<Method> befores = new LinkedList<Method>();
        List<Method> afters = new LinkedList<Method>();
        Object instance = testCase.newInstance();

        for (Method method : testCase.getDeclaredMethods()) {
            if (hasAnnotation(method, Test.class)) {
                Testlet testlet = new Testlet(instance, method);
                tests.add(testlet);
            }

            if (hasAnnotation(method, Before.class)) {
                befores.add(method);
            }

            if (hasAnnotation(method, After.class)) {
                afters.add(method);
            }
        }

        TestCase testList = new TestCase(testCase);

        for (Testlet testlet : tests) {
            testlet.addSetup(befores);
            testlet.addTearDown(afters);

            testList.addChild(testlet);
        }

        return Arrays.asList(new TestNode[]{testList});
    }

    private static boolean hasAnnotation(Method method, Class<? extends Annotation> annotation) {
        return method.getAnnotation(annotation) != null;
    }
}
