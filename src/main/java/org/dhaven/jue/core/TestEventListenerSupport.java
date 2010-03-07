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

package org.dhaven.jue.core;

import org.dhaven.jue.api.event.EventType;
import org.dhaven.jue.api.event.Status;
import org.dhaven.jue.api.event.TestEvent;
import org.dhaven.jue.api.event.TestEventListener;
import org.dhaven.jue.core.internal.Identifiable;

import java.util.LinkedList;
import java.util.List;

/**
 * Provide support for the Engine class to fire events as necessary.  Other
 * classes that need to fire tests will have a copy of this class.
 */
public class TestEventListenerSupport {
    private List<TestEventListener> listeners = new LinkedList<TestEventListener>();
    private static final String FRAMEWORK_NAME = "JUnit Events";

    /**
     * Add a test event listener.
     *
     * @param listener the listener to add
     */
    public void addTestListener(TestEventListener listener) {
        listeners.add(listener);
    }

    /**
     * Remove a test event listener.
     *
     * @param listener the listener to remove
     */
    public void removeTestListener(TestEventListener listener) {
        listeners.remove(listener);
    }

    /**
     * Send the test event to all the listeners.
     * TODO: Just add these to an event queue, and fire them off from the
     * TODO: event thread queue.
     *
     * @param testEvent the test event to send
     */
    private void fireTestEvent(TestEvent testEvent) {
        for (TestEventListener listener : listeners) {
            listener.handleEvent(testEvent);
        }
    }

    /**
     * Signal the start of a test run.
     */
    public void fireStartTestRun() {
        fireTestEvent(new TestEvent(FRAMEWORK_NAME, EventType.StartRun, Status.Running));
    }

    /**
     * Signal the end of a test run.
     */
    public void fireEndTestRun() {
        fireTestEvent(new TestEvent(FRAMEWORK_NAME, EventType.EndRun, Status.Terminated));
    }

    /**
     * Signal the start of a test case.
     *
     * @param testCase the test case that started
     */
    public void fireStartTestCase(Identifiable testCase) {
        fireTestEvent(new TestEvent(testCase.getName(), EventType.StartTestCase, Status.Running));
    }

    /**
     * Signal the end of a test case.
     *
     * @param testCase the test case that ended
     */
    public void fireEndTestCase(Identifiable testCase) {
        fireTestEvent(new TestEvent(testCase.getName(), EventType.EndTestCase, Status.Terminated));
    }

    /**
     * Signal the start of an individual test.
     *
     * @param test the test that started
     */
    public void fireTestStarted(Identifiable test) {
        fireTestEvent(new TestEvent(test.getName(), EventType.StartTest, Status.Running));
    }

    /**
     * Signal that a test was ignored.
     *
     * @param test the test that was ignored
     */
    public void fireTestIgnored(Identifiable test) {
        fireTestEvent(new TestEvent(test.getName(), EventType.EndTest, Status.Ignored));
    }

    /**
     * Signal that a test passed.
     *
     * @param test the test that passed.
     */
    public void fireTestPassed(Identifiable test) {
        fireTestEvent(new TestEvent(test.getName(), EventType.EndTest, Status.Passed));
    }

    /**
     * Signal that a test failed.
     *
     * @param test    the test that failed
     * @param failure the cause of the failure
     */
    public void fireTestFailed(Identifiable test, Throwable failure) {
        fireTestEvent(new TestEvent(test.getName(), EventType.EndTest, Status.Failed, failure));
    }
}
