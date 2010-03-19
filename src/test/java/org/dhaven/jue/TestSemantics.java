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

import java.io.IOException;
import java.util.Arrays;

import org.dhaven.jue.api.Request;
import org.dhaven.jue.api.description.Type;
import org.dhaven.jue.api.results.Results;
import org.dhaven.jue.core.Engine;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

/**
 * Make sure the tests are run in order
 */
@SuppressWarnings({"ALL"})
public class TestSemantics {
    private Engine engine;
    private Request testsToRun;

    @Before
    public void setUpEngine() {
        testsToRun = new Request(InternalTest.class);
        engine = new Engine();
    }

    @Test
    public void checkMethodOrder() throws Exception {
        Results results = engine.process(testsToRun);
        assertThat("unexpected failure", results.passed(), equalTo(true));
    }

    @Test
    public void checkEventOrder() throws Exception {
        ListenerTester listenerTester = new ListenerTester();
        engine.addTestListener(listenerTester);

        engine.process(testsToRun);

        Type[] expected = expectedEventOrder();
        assertThat(listenerTester.getEventTypeOrder(), equalTo(expected));
    }

    private Type[] expectedEventOrder() {
        Type[] raw = Type.values();
        // when instrumented Type.values() is reversed.
        Arrays.sort(raw);
        Type[] expected = new Type[raw.length * 2];

        System.arraycopy(raw, 0, expected, 0, raw.length);

        for (int i = 0; i < raw.length; i++) {
            expected[expected.length - i - 1] = raw[i];
        }

        return expected;
    }

    @Test
    public void expectedExceptionIsPassing() throws Exception {
        testsToRun = new Request(ExceptionTest.class);

        Results results = engine.process(testsToRun);

        assertThat("unexpected failure", results.passed(), equalTo(true));
    }

    @Test
    public void wrongExceptionIsFailing() throws Exception {
        testsToRun = new Request(WrongExceptionTest.class);

        Results results = engine.process(testsToRun);

        assertThat("Did not fail test as expected", results.passed(), is(false));
    }

    @Test
    public void noExceptionWhenExpectedIsFailing() throws Exception {
        testsToRun = new Request(NoExceptionTest.class);

        Results results = engine.process(testsToRun);

        assertThat("Did not fail test as expected", results.passed(), is(false));
    }

    @Test
    public void testsAreInheritedFromSuperClasses() throws Exception {
        testsToRun = new Request(InheritanceTest.class);

        Results results = engine.process(testsToRun);

        assertThat(results.passed(), is(true));

        assertThat(results.numberOfTestCases(), is(1));
        assertThat(results.numberOfTestsRun(), is(2));
    }

    @Test
    public void ignoreTestsMarkedWithIgnoreAnnotation() throws Exception {
        testsToRun = new Request(IgnoreTest.class);

        Results results = engine.process(testsToRun);

        assertThat(results.toString(), results.passed(), is(true));

        assertThat(results.numberOfTestCases(), is(1));
        assertThat(results.numberOfTestsRun(), is(1));
    }

    @Test
    public void testsAreInheritedFromSuperClassesCheck2() throws Exception {
        testsToRun = new Request(InheritanceTest.class, InternalTest.class);

        Results results = engine.process(testsToRun);

        assertThat(results.passed(), is(true));

        assertThat(results.numberOfTestCases(), is(2));
        assertThat(results.numberOfTestsRun(), is(3));
    }

    /**
     * Internal test to ensure the order of @Before, @Test, and @After work OK.
     */
    public static class InternalTest {
        boolean callFirstCalled = false;
        boolean actualTestCalled = false;
        boolean callLastCalled = false;

        @Before
        public void callFirst() {
            assertThat("@Before already called in @Before", callFirstCalled, equalTo(false));
            assertThat("@Test already called in @Before", actualTestCalled, equalTo(false));
            assertThat("@After already called in @Before", callLastCalled, equalTo(false));
            callFirstCalled = true;
        }

        @Test
        public void actualTest() {
            assertThat("@Before not called yet in @Test", callFirstCalled, equalTo(true));
            assertThat("@Test already called in @Test", actualTestCalled, equalTo(false));
            assertThat("@After already called in @Test", callLastCalled, equalTo(false));
            actualTestCalled = true;
        }

        @After
        public void callLast() {
            assertThat("@Before not called yet in @Test", callFirstCalled, equalTo(true));
            assertThat("@Test not called yet in @After", actualTestCalled, equalTo(true));
            assertThat("@After already called in @After", callLastCalled, equalTo(false));
            callLastCalled = true;
        }
    }

    public static class IgnoreTest {
        @Ignore
        @Test
        public void ignoredTest() {
            throw new AssertionError("This should never be executed as a test");
        }

        @Test
        public void notIgnoredTest() {
            assertThat(true, equalTo(true));
        }
    }

    public static class ExceptionTest {
        @Test(expected = IllegalArgumentException.class)
        public void throwsExceptionOnPurpose() {
            throw new IllegalArgumentException();
        }
    }

    public static class WrongExceptionTest {
        @Test(expected = IllegalArgumentException.class)
        public void throwsWrongException() throws IOException {
            throw new IOException();
        }
    }

    public static class NoExceptionTest {
        @SuppressWarnings({"EmptyMethod"})
        @Test(expected = IllegalArgumentException.class)
        public void doesNotThrowException() {
        }
    }

    public static class InheritanceTest extends ExceptionTest {
        @Test
        public void secondTest() {
            assertThat(true, is(true));
        }
    }
}
