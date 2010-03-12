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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.dhaven.jue.api.event.EventClass;
import org.dhaven.jue.api.event.Status;
import org.dhaven.jue.api.event.TestEvent;
import org.dhaven.jue.api.event.TestEventListener;

/**
 * Collects the results from the tests as they are run.  The results object
 * provides summary and simple analysis features such as providing the
 * difference between processor time and clock time.  Considering that the tests
 * are designed to be run in parallel, this is useful information.  If any
 * test was run multiple times, you will be able to find the average processor
 * times for those tests.
 * <p/>
 * TODO: test case/test summary as well
 */
public class Results implements TestEventListener {
    private Map<Description, TestSummary> collectedResults = new HashMap<Description, TestSummary>();

    /**
     * Check to see if all the results we were expecting came in.
     *
     * @return <code>true</code> if any pending results have {@link Status#Running}
     */
    public boolean complete() {
        boolean completed = !collectedResults.isEmpty();

        for (TestSummary summary : collectedResults.values()) {
            completed = completed && summary.isComplete();
        }

        return completed;
    }

    /**
     * Check to see if all the tests returned passing results.
     *
     * @return <code>true</code> if all tests have {@link Status#Passed}
     */
    public boolean passed() {
        boolean passed = !collectedResults.isEmpty();

        for (TestSummary summary : collectedResults.values()) {
            if (summary.getEventClass() == EventClass.Test) {
                passed = passed && summary.passed();
            }
        }

        return passed;
    }

    /**
     * List all failures as one big string.  Provides easy summary of what
     * needs to be fixed.
     *
     * @return The string of every failure, and its cause
     */
    @SuppressWarnings({"ThrowableResultOfMethodCallIgnored"})
    public String failuresToString() {
        StringBuilder builder = new StringBuilder();

        for (TestSummary summary : collectedResults.values()) {
            if (summary.failed()) {
                builder.append(summary.getDescription());
                builder.append("... Failed\n");

                StringWriter writer = new StringWriter();
                summary.getFailure().printStackTrace(new PrintWriter(writer));
                builder.append(writer.toString());
            }
        }

        return builder.toString();
    }

    @Override
    public void handleEvent(TestEvent event) {
        TestSummary summary = collectedResults.get(event.getDescription());

        if (null == summary) {
            summary = new TestSummary(event);
        } else {
            summary.setEvent(event);
        }

        collectedResults.put(event.getDescription(), summary);
    }

    public long getProcessorTime(EventClass type) {
        return totalTime(filterResults(type));
    }

    private Collection<TestSummary> filterResults(EventClass type) {
        ArrayList<TestSummary> summaries = new ArrayList<TestSummary>(collectedResults.size());

        for (TestSummary summary : collectedResults.values()) {
            if (summary.getEventClass() == type) {
                summaries.add(summary);
            }
        }

        return summaries;
    }

    private static long totalTime(Collection<TestSummary> summaries) {
        int runningTime = 0;

        for (TestSummary summary : summaries) {
            runningTime += summary.elapsedTime();
        }

        return runningTime;
    }

    public int numberOfTestCases() {
        return filterResults(EventClass.TestCase).size();
    }

    public int numberOfTests() {
        return filterResults(EventClass.Test).size();
    }

    private static final class TestSummary implements Describable, Comparable<TestSummary> {
        private static final int START = 0;
        private static final int END = 1;
        private TestEvent[] events = new TestEvent[2];
        private Description description;

        public TestSummary(TestEvent event) {
            description = event.getDescription();
            setEvent(event);
        }

        public void setEvent(TestEvent event) {
            events[Status.Running == event.getStatus() ? START : END] = event;
        }

        public EventClass getEventClass() {
            int index = events[START] == null ? END : START;

            return events[index].getType().getType();
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

        @SuppressWarnings({"ThrowableResultOfMethodCallIgnored"})
        public Throwable getFailure() {
            return isComplete() ? events[END].getFailure() : null;
        }

        public long elapsedTime() {
            return isComplete() ? events[END].getNanoseconds() - events[START].getNanoseconds() : 0;
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
    }
}
