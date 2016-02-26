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

package org.dhaven.jue.api.results;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;

import org.dhaven.jue.api.description.Description;
import org.dhaven.jue.api.description.Type;
import org.dhaven.jue.api.event.Status;
import org.dhaven.jue.api.event.TestEvent;
import org.dhaven.jue.api.event.TestListener;

/**
 * A test summary instance will provide the end results of a test, and any
 * children tests.  For example, a TestCase has many individual tests.
 */
public class TestSummary implements Summary, TestListener {
    private static final int START = 0;
    private static final int END = 1;
    private final TestEvent[] events = new TestEvent[2];
    private Description description;

    /**
     * Constructor used to initialize the summary with the first event.
     *
     * @param event the test event used to initialize the summary.
     */
    public TestSummary(TestEvent event) {
        if (null != event) {
            handleEvent(event);
        }
    }

    @Override
    public void handleEvent(TestEvent event) {
        if (null == description) {
            description = event.getDescription();
        }

        events[Status.Started == event.getStatus() ? START : END] = event;
    }

    @Override
    public Type getType() {
        int index = events[START] == null ? END : START;

        return events[index].getType();
    }

    @Override
    public boolean complete() {
        return events[START] != null && events[END] != null;
    }

    @Override
    public boolean passed() {
        return getStatus() == Status.Passed;
    }

    @Override
    public boolean failed() {
        return getStatus() == Status.Failed;
    }

    @Override
    public boolean terminated() {
        return getStatus() == Status.Terminated;
    }

    @Override
    public boolean ignored() {
        return getStatus() == Status.Ignored;
    }

    @Override
    public Iterable<Failure> getFailures() {
        Collection<Failure> failures = new ArrayList<Failure>(1);

        if (complete() && events[END].getFailure() != null) {
            failures.add(new Failure(getDescription(), events[END].getFailure()));
        }

        return failures;
    }

    @Override
    public long elapsedTime() {
        return complete() ? events[END].getNanoseconds() - events[START].getNanoseconds() : 0;
    }

    @Override
    public long processorTime() {
        return elapsedTime();
    }

    @Override
    public Description getDescription() {
        return description;
    }

    @Override
    public int compareTo(Summary other) {
        return description.compareTo(other.getDescription());
    }

    @Override
    public boolean equals(Object object) {
        return (object instanceof Summary) && description.equals(Summary.class.cast(object).getDescription());
    }

    @Override
    public int hashCode() {
        return description.hashCode();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(description).append("(");
        builder.append(String.format("%.3f", nanosecondsToMilliseconds(elapsedTime())));
        builder.append("ms):\t");
        appendStatus(builder);

        if (failed()) {
            builder.append("\n");

            StringWriter stackTrace = new StringWriter();
            for (Failure failure : getFailures()) {
                failure.getCause().printStackTrace(new PrintWriter(stackTrace));
            }
            builder.append(stackTrace);

            builder.append("\n");
        }

        return builder.toString();
    }

    static TestSummary create(TestEvent event) {
        return event.getType() != Type.Test
                ? new TestCaseSummary(event)
                : new TestSummary(event);
    }

    protected void appendStatus(StringBuilder builder) {
        if (failed()) {
            builder.append("FAILED...");
        } else {
            builder.append(getStatus().name());
        }

        builder.append('\n');
    }

    protected float nanosecondsToMilliseconds(long time) {
        return time / 1000000f;
    }

    @Override
    public Status getStatus() {
        return events[END] == null ?
                events[START] == null ? null : events[START].getStatus()
                : events[END].getStatus();
    }
}
