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

import org.dhaven.jue.api.description.Describable;
import org.dhaven.jue.api.description.Description;
import org.dhaven.jue.api.description.Type;
import org.dhaven.jue.api.event.Status;
import org.dhaven.jue.api.event.TestEvent;

/**
 * A test summary instance will provide the end results of a test, and any
 * children tests.  For example, a TestCase has many individual tests.
 */
public class TestSummary implements Describable, Comparable<TestSummary> {
    private static final int START = 0;
    private static final int END = 1;
    private TestEvent[] events = new TestEvent[2];
    private Description description;

    protected TestSummary(TestEvent event) {
        description = event.getDescription();
        setEvent(event);
    }

    public void setEvent(TestEvent event) {
        events[Status.Started == event.getStatus() ? START : END] = event;
    }

    public Type getType() {
        int index = events[START] == null ? END : START;

        return events[index].getType();
    }

    public boolean isComplete() {
        return events[START] != null && events[END] != null;
    }

    public boolean passed() {
        return isComplete() && events[END].getStatus() == Status.Passed;
    }

    public boolean failed() {
        return isComplete() && events[END].getStatus() == Status.Failed;
    }

    public boolean terminated() {
        return isComplete() && events[END].getStatus() == Status.Terminated;
    }

    public boolean ignored() {
        return isComplete() && events[END].getStatus() == Status.Ignored;
    }

    @SuppressWarnings({"ThrowableResultOfMethodCallIgnored"})
    public Throwable getFailure() {
        return isComplete() ? events[END].getFailure() : null;
    }

    public long elapsedTime() {
        return isComplete() ? events[END].getNanoseconds() - events[START].getNanoseconds() : 0;
    }

    public long processorTime() {
        return elapsedTime();
    }

    @Override
    public Description getDescription() {
        return description;
    }

    @Override
    public int compareTo(TestSummary other) {
        return description.compareTo(other.getDescription());
    }

    @Override
    public boolean equals(Object object) {
        return (object instanceof TestSummary) && description.equals(TestSummary.class.cast(object).getDescription());
    }

    @Override
    public int hashCode() {
        return description.hashCode();
    }

    @SuppressWarnings({"ThrowableResultOfMethodCallIgnored"})
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(description).append("(");
        builder.append(threeDigitMS(elapsedTime()));
        builder.append("ms):\t");
        appendStatus(builder);

        if (failed()) {
            builder.append("\n");

            StringWriter stackTrace = new StringWriter();
            getFailure().printStackTrace(new PrintWriter(stackTrace));
            builder.append(stackTrace);

            builder.append("\n");
        }

        return builder.toString();
    }

    public static TestSummary create(TestEvent event) {
        return event.getType() != Type.Test
                ? new TestCaseSummary(event)
                : new TestSummary(event);
    }

    protected void appendStatus(StringBuilder builder) {
        if (passed()) {
            builder.append("Passed\n");
        } else if (terminated()) {
            builder.append("Terminated\n");
        } else if (ignored()) {
            builder.append("Ignored\n");
        } else if (failed()) {
            builder.append("FAILED...\n");
        }
    }

    protected float threeDigitMS(long time) {
        float value = time / 1000000f;

        value = Math.round(value * 1000f) / 1000f;

        return value;
    }

    public Status getStatus() {
        return events[END] == null ? events[START].getStatus() : events[END].getStatus();
    }
}
