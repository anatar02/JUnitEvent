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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Provide support for the Engine class to fire events as necessary.  Other
 * classes that need to fire tests will have a copy of this class.
 */
public class TestEventListenerSupport {
    private List<TestEventListener> listeners = new LinkedList<TestEventListener>();

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
     * Remove all the listeners at once.
     */
    void clearAllListeners() {
        Iterator<TestEventListener> it = listeners.iterator();
        while (it.hasNext()) {
            it.next();
            it.remove();
        }
    }

    /**
     * Creates a test event and fires the event to all listeners.
     *
     * @param name   the test name
     * @param type   the event type
     * @param status the test status
     */
    public void fireTestEvent(String name, EventType type, Status status) {
        fireTestEvent(new TestEvent(name, type, status));
    }

    /**
     * Send the test event to all the listeners.
     *
     * @param testEvent the test event to send
     */
    public void fireTestEvent(TestEvent testEvent) {
        for (TestEventListener listener : listeners) {
            listener.handleEvent(testEvent);
        }
    }

    public void fireTestEvent(String name, Throwable failure) {
        fireTestEvent(new TestEvent(name, EventType.EndTest, Status.Failed, failure));
    }
}
