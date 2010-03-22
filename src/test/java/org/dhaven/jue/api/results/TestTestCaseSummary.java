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

import java.util.ArrayList;
import java.util.Collection;

import org.dhaven.jue.Test;
import org.dhaven.jue.api.description.Description;
import org.dhaven.jue.api.description.Type;
import org.dhaven.jue.api.event.Status;
import org.dhaven.jue.api.event.TestEvent;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class TestTestCaseSummary {
    @Test
    public void checkStatusWithNoChildren() {
        TestCaseSummary summary = new TestCaseSummary(null);

        assertThat(summary.size(), equalTo(0));
        assertThat(summary.getStatus(), equalTo(Status.Started));
    }

    @Test
    public void statusIsPassedWhenAllChildrenArePassed() {
        TestCaseSummary summary = new TestCaseSummary(null);

        summary.addChild(new ChildSummary(new Description("test-1", Type.Test), Status.Passed));
        summary.addChild(new ChildSummary(new Description("test-2", Type.Test), Status.Passed));
        summary.addChild(new ChildSummary(new Description("test-3", Type.Test), Status.Passed));

        assertThat(summary.size(), equalTo(3));
        assertThat(summary.getStatus(), equalTo(Status.Passed));
    }

    @Test
    public void statusIsPassedWhenAtLeastOneChildIsPassedAndOthersAreIgnored() {
        TestCaseSummary summary = new TestCaseSummary(null);

        summary.addChild(new ChildSummary(new Description("test-1", Type.Test), Status.Passed));
        summary.addChild(new ChildSummary(new Description("test-2", Type.Test), Status.Ignored));
        summary.addChild(new ChildSummary(new Description("test-3", Type.Test), Status.Ignored));

        assertThat(summary.size(), equalTo(3));
        assertThat(summary.getStatus(), equalTo(Status.Passed));
    }

    @Test
    public void statusIsIgnoredWhenAllChildrenAreIgnored() {
        TestCaseSummary summary = new TestCaseSummary(null);

        summary.addChild(new ChildSummary(new Description("test-1", Type.Test), Status.Ignored));
        summary.addChild(new ChildSummary(new Description("test-2", Type.Test), Status.Ignored));
        summary.addChild(new ChildSummary(new Description("test-3", Type.Test), Status.Ignored));

        assertThat(summary.size(), equalTo(3));
        assertThat(summary.getStatus(), equalTo(Status.Ignored));
    }

    @Test
    public void statusIsTerminatedWhenAtLeastOneChildIsTerminated() {
        TestCaseSummary summary = new TestCaseSummary(null);

        summary.addChild(new ChildSummary(new Description("test-1", Type.Test), Status.Passed));
        summary.addChild(new ChildSummary(new Description("test-2", Type.Test), Status.Terminated));
        summary.addChild(new ChildSummary(new Description("test-3", Type.Test), Status.Passed));

        assertThat(summary.size(), equalTo(3));
        assertThat(summary.getStatus(), equalTo(Status.Terminated));
    }

    @Test
    public void statusIsFailedWhenAtLeastOneChildIsFailed() {
        TestCaseSummary summary = new TestCaseSummary(null);

        summary.addChild(new ChildSummary(new Description("test-1", Type.Test), Status.Passed));
        summary.addChild(new ChildSummary(new Description("test-2", Type.Test), Status.Failed));
        summary.addChild(new ChildSummary(new Description("test-3", Type.Test), Status.Passed));

        assertThat(summary.size(), equalTo(3));
        assertThat(summary.getStatus(), equalTo(Status.Failed));
    }

    @Test
    public void statusIsFailedWhenBothTerminatedAndFailedChildrenExist() {
        TestCaseSummary summary = new TestCaseSummary(null);

        summary.addChild(new ChildSummary(new Description("test-1", Type.Test), Status.Terminated));
        summary.addChild(new ChildSummary(new Description("test-2", Type.Test), Status.Failed));
        summary.addChild(new ChildSummary(new Description("test-3", Type.Test), Status.Passed));

        assertThat(summary.size(), equalTo(3));
        assertThat(summary.getStatus(), equalTo(Status.Failed));
    }

    @Test
    public void statusIsFailedWhenBothTerminatedAndFailedChildrenExist_reverseOrder() {
        TestCaseSummary summary = new TestCaseSummary(null);

        summary.addChild(new ChildSummary(new Description("test-1", Type.Test), Status.Failed));
        summary.addChild(new ChildSummary(new Description("test-2", Type.Test), Status.Terminated));
        summary.addChild(new ChildSummary(new Description("test-3", Type.Test), Status.Passed));

        assertThat(summary.size(), equalTo(3));
        assertThat(summary.getStatus(), equalTo(Status.Failed));
    }

    @SuppressWarnings({"ThrowableInstanceNeverThrown", "ThrowableResultOfMethodCallIgnored"})
    @Test
    public void getSummaryOfAllFailures() {
        Description description = new Description("My Test", Type.TestCase);
        TestCaseSummary summary = new TestCaseSummary(new TestEvent(description, Status.Started));

        summary.addChild(new ChildSummary(new Description("test-1", Type.Test), new IllegalArgumentException("test-1")));
        summary.addChild(new ChildSummary(new Description("test-2", Type.Test), new IllegalArgumentException("test-2")));
        summary.addChild(new ChildSummary(new Description("test-3", Type.Test), new IllegalArgumentException("test-3")));

        summary.handleEvent(new TestEvent(description, Status.Terminated));

        int numberOfFailures = 0;
        for (Failure failure : summary.getFailures()) {
            assertThat(failure.getCause().getMessage(), equalTo(failure.getDescription().getName()));
            numberOfFailures++;
        }

        assertThat(numberOfFailures, equalTo(3));
    }

    @Test
    public void elapsedTimeIsFromFirstEventToCloseEvent() {
        Description description = new Description("My Test", Type.TestCase);
        TestEvent started = new TestEvent(description, Status.Started);
        TestCaseSummary summary = new TestCaseSummary(started);
        TestEvent ended = new TestEvent(description, Status.Terminated);
        summary.handleEvent(ended);

        assertThat(summary.elapsedTime(), equalTo(ended.getNanoseconds() - started.getNanoseconds()));
        assertThat(summary.processorTime(), equalTo(0L));
    }

    @Test
    public void processorTimeIsAdditionOfChildProcessorTimes() {
        TestCaseSummary summary = new TestCaseSummary(null);

        for (int i = 1; i <= 3; i++) {
            ChildSummary child = new ChildSummary(new Description("test-" + i, Type.Test), Status.Passed);
            child.elapsedTime(i * 100L);
            summary.addChild(child);
        }

        assertThat(summary.elapsedTime(), equalTo(0L));
        assertThat(summary.processorTime(), equalTo(600L));
    }

    protected static class ChildSummary implements Summary {
        private Description description;
        private Status status;
        private Collection<Failure> failures = new ArrayList<Failure>(1);
        private long elapsedTime;

        public ChildSummary(Description description, Status status) {
            this.description = description;
            this.status = status;
        }

        public ChildSummary(Description description, Throwable failure) {
            this.description = description;
            this.status = Status.Failed;
            this.failures.add(new Failure(description, failure));
        }

        @Override
        public Type getType() {
            return description.getType();
        }

        @Override
        public boolean complete() {
            return status != Status.Started;
        }

        @Override
        public boolean passed() {
            return status == Status.Passed;
        }

        @Override
        public boolean terminated() {
            return status == Status.Terminated;
        }

        @Override
        public boolean ignored() {
            return status == Status.Ignored;
        }

        @Override
        public boolean failed() {
            return status == Status.Failed;
        }

        @Override
        public Iterable<Failure> getFailures() {
            return failures;
        }

        public void elapsedTime(long value) {
            elapsedTime = value;
        }

        @Override
        public long elapsedTime() {
            return elapsedTime;
        }

        @Override
        public long processorTime() {
            return elapsedTime;
        }

        @Override
        public Status getStatus() {
            return status;
        }

        @Override
        public int compareTo(Summary o) {
            return description.compareTo(o.getDescription());
        }

        @Override
        public Description getDescription() {
            return description;
        }
    }
}
