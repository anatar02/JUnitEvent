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

package org.dhaven.jue.api.event;

import org.dhaven.jue.api.Describable;
import org.dhaven.jue.api.Description;

import java.lang.reflect.InvocationTargetException;

/**
 * The test event carries the data necessary for tools to listen to the progress
 * of the tests being run.  There is no guarantee for the order that events are
 * received, only for when they are created.  Each event is given a time stamp
 * in nanoseconds for when it was created.
 */
public class TestEvent implements Describable {
    private final Description description;
    private final EventType type;
    private final Status status;
    private final Throwable failure;
    private final long timeStamp;

    /**
     * Create a test event with the test name, event type, and status.
     *
     * @param testName   the test name
     * @param eventType  the {@link EventType}
     * @param testStatus the {@link Status}
     */
    public TestEvent(Description testName, EventType eventType,
                     Status testStatus) {
        this(testName, eventType, testStatus, null);
    }

    /**
     * Create a test event with all the parameters.  If the exception supplied
     * is an InvocationTargetException (thrown by the Java reflection API),
     * this constructor will extract the root cause if it exists.  In the
     * event that the InvocationTargetException does not have a cause, the
     * exception itself will be recorded as the cause.
     *
     * @param testName   the test name
     * @param eventType  the {@link EventType}
     * @param testStatus the {@link Status}
     * @param exception  the failure cause, if any
     */
    @SuppressWarnings({"ThrowableResultOfMethodCallIgnored"})
    public TestEvent(Description testName, EventType eventType,
                     Status testStatus, Throwable exception) {
        timeStamp = System.nanoTime();
        description = testName;
        type = eventType;
        status = testStatus;

        if (exception instanceof InvocationTargetException) {
            Throwable cause = InvocationTargetException.class
                    .cast(exception).getCause();

            failure = (cause != null) ? cause : exception;
        } else {
            failure = exception;
        }
    }

    /**
     * Get the test name.
     *
     * @return the test name
     */
    public Description getDescription() {
        return description;
    }

    /**
     * Get the event type.  Essentially, this tells where in the test cycle
     * that this event was thrown.
     *
     * @return the {@link EventType}
     */
    public EventType getType() {
        return type;
    }

    /**
     * Determine the status of the test relating to the event.
     *
     * @return the {@link Status}
     */
    public Status getStatus() {
        return status;
    }

    /**
     * Get the failure if there is one associated with the event.
     *
     * @return the causing exception
     */
    public Throwable getFailure() {
        return failure;
    }

    /**
     * Get the time stamp in nanoseconds.  The precision on most JVM millisecond
     * clocks is in the neighborhood of 10ms.  That's good enough to tell what
     * time it is, but not enough to know how long a test took.
     * <p/>
     * We use the nanosecond timer that is part of all modern JVMs.  The
     * nanosecond timer is much more precise, allowing for accurate timings.
     * To convert to milliseconds, divide the time stamp by 1 million.
     *
     * @return the time stamp in nanoseconds
     */
    public long getNanoseconds() {
        return timeStamp;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append('{').append(getDescription());
        builder.append(" [").append(getType().name()).append("] ");
        builder.append('[').append(getStatus().name()).append("]: ");
        builder.append(getNanoseconds()).append('}');

        return builder.toString();
    }
}
