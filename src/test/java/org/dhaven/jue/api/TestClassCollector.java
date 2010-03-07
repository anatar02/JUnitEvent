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

import org.dhaven.jue.Annotations;
import org.dhaven.jue.api.collectorclasses.ClassWithAfterMethod;
import org.dhaven.jue.api.collectorclasses.ClassWithBeforeMethod;
import org.dhaven.jue.api.collectorclasses.ClassWithIgnoreMethod;
import org.dhaven.jue.api.collectorclasses.ClassWithTestMethod;

import static org.dhaven.jue.Annotations.Before;
import static org.dhaven.jue.Annotations.Test;
import static org.dhaven.jue.MethodScanner.hasMethodWithAnnotation;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItemInArray;

/**
 * Test the behavior of the class collector, so that we can easily create
 * suites of tests.
 */
public class TestClassCollector {
    private ClassCollector collector;

    @Before
    public void setUpClassCollector() {
        collector = new ClassCollector();
        collector.setBasePackage("org.dhaven.jue.api.collectorclasses");
    }

    @Test
    public void canFindClassesWithTestAnnotation() throws Exception {
        collector.methodsHaveAnnotation(Test.class);
        Class[] classes = collector.collect();

        assertThat(classes.length, equalTo(1));
        assertThat(classes[0], equalTo((Class) ClassWithTestMethod.class));
        assertThat(classes[0], hasMethodWithAnnotation(Test.class));
    }

    @Test
    public void canFindClassesWithMultipleAnnotations() throws Exception {
        collector.methodsHaveAnnotation(Annotations.get());
        Class[] classes = collector.collect();

        assertThat(classes.length, equalTo(4));
        assertThat(classes, hasItemInArray((Class) ClassWithAfterMethod.class));
        assertThat(classes, hasItemInArray((Class) ClassWithBeforeMethod.class));
        assertThat(classes, hasItemInArray((Class) ClassWithIgnoreMethod.class));
        assertThat(classes, hasItemInArray((Class) ClassWithTestMethod.class));
    }
}
