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

package org.dhaven.jue;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

/**
 * Hamcrest matcher for methods in a class.
 */
public class MethodScanner extends TypeSafeMatcher<Class> {
    private final Class<? extends Annotation> annotation;

    private MethodScanner(Class<? extends Annotation> marker) {
        annotation = marker;
    }

    @Override
    public boolean matchesSafely(Class aClass) {
        boolean matches = false;

        for (Method method : aClass.getDeclaredMethods()) {
            matches = matches || method.getAnnotation(annotation) != null;
        }

        if (!aClass.getSuperclass().equals(Object.class)) {
            matches = matches || matchesSafely(aClass.getSuperclass());
        }

        return matches;
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("Contains method with annotation: ");
        description.appendValue(annotation.getName());
    }

    public static Matcher<Class> hasMethodWithAnnotation(Class<? extends Annotation> annotation) {
        return new MethodScanner(annotation);
    }
}
