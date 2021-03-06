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

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Phaser;

import org.dhaven.jue.api.description.Describable;
import org.dhaven.jue.api.event.Status;
import org.dhaven.jue.api.event.TestEvent;
import org.dhaven.jue.api.event.TestListener;

/**
 * Provide support for the Engine class to fire events as necessary.  Other
 * classes that need to fire tests will have a copy of this class.
 */
public final class TestListenerSupport {
    final ExecutorService service;
    private final Queue<TestListener> listeners = new LinkedList<TestListener>();
    private Phaser phaser = new Phaser(1);

    public TestListenerSupport() {
        service = Executors.newSingleThreadExecutor();
    }

    /**
     * Add a test event listener.
     *
     * @param listener the listener to add
     */
    public void addTestListener(TestListener listener) {
        listeners.add(listener);
    }

    /**
     * Remove a test event listener.
     *
     * @param listener the listener to remove
     */
    public void removeTestListener(TestListener listener) {
        listeners.remove(listener);
    }

    /**
     * Send the test event to all the listeners.
     *
     * @param testEvent the test event to send
     */
    public void fireTestEvent(final TestEvent testEvent) {
        phaser.register();
        service.execute(new Runnable() {
            @Override
            public void run() {
                for (TestListener listener : listeners) {
                    listener.handleEvent(testEvent);
                }

                phaser.arriveAndDeregister();
            }
        });
    }

    /**
     * Signal the start of an individual test.
     *
     * @param test the test that started
     */
    public void fireTestStarted(Describable test) {
        fireTestEvent(new TestEvent(test.getDescription(), Status.Started));
    }

    /**
     * Signal that a test was ignored.
     *
     * @param test the test that was ignored
     */
    public void fireTestIgnored(Describable test) {
        fireTestEvent(new TestEvent(test.getDescription(), Status.Ignored));
    }

    /**
     * Signal that a test passed.
     *
     * @param test the test that passed.
     */
    public void fireTestPassed(Describable test) {
        fireTestEvent(new TestEvent(test.getDescription(), Status.Passed));
    }

    /**
     * Signal that a test failed.
     *
     * @param test    the test that failed
     * @param failure the cause of the failure
     */
    public void fireTestFailed(Describable test, Throwable failure) {
        fireTestEvent(new TestEvent(test.getDescription(), Status.Failed, failure));
    }

    public void fireTestTerminated(Describable test) {
        fireTestEvent(new TestEvent(test.getDescription(), Status.Terminated));
    }

    public void await() {
        phaser.arriveAndAwaitAdvance();
        service.shutdown();
    }
}
