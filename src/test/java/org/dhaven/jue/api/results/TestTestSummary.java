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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.dhaven.jue.Before;
import org.dhaven.jue.Test;
import org.dhaven.jue.api.description.Description;
import org.dhaven.jue.api.description.Type;
import org.dhaven.jue.api.event.Status;
import org.dhaven.jue.api.event.TestEvent;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@SuppressWarnings({"ThrowableResultOfMethodCallIgnored"})
public class TestTestSummary {
    private TestEvent started;
    private TestEvent passed;
    private TestEvent ignored;
    private TestEvent terminated;
    private TestEvent failed;

    @Before
    public void setUpEvents() {
        Description description = new Description("My Test", Type.Test);
        started = new TestEvent(description, Status.Started);
        passed = new TestEvent(description, Status.Passed);
        ignored = new TestEvent(description, Status.Ignored);
        terminated = new TestEvent(description, Status.Terminated);
        failed = new TestEvent(description, Status.Failed, new AssertionError("Example"));
    }

    @Test
    public void testNotCompleteWithoutEndEvent() {
        TestSummary summary = new TestSummary(started);

        assertThat(summary.complete(), equalTo(false));
        assertThat(summary.getType(), equalTo(Type.Test));
    }

    @Test
    public void testNotCompleteWithoutBeginEvent() {
        TestSummary summary = new TestSummary(passed);

        assertThat(summary.complete(), equalTo(false));
        assertThat(summary.getType(), equalTo(Type.Test));
    }

    @Test
    public void testCompleteWithBothEvents() {
        TestSummary summary = new TestSummary(started);
        summary.handleEvent(passed);

        assertThat(summary.complete(), equalTo(true));
        assertThat(summary.getType(), equalTo(Type.Test));
    }

    @Test
    public void testEventOrderDoesNotMatter() {
        TestSummary summary = new TestSummary(passed);
        summary.handleEvent(started);

        assertThat(summary.complete(), equalTo(true));
        assertThat(summary.getType(), equalTo(Type.Test));
    }

    @Test
    public void testIncompleteStatusWithResultOnly() {
        TestSummary summary = new TestSummary(passed);

        assertThat(summary.getStatus(), equalTo(Status.Passed));
    }

    @Test
    public void testIncompleteStatusWithStartOnly() {
        TestSummary summary = new TestSummary(started);

        assertThat(summary.getStatus(), equalTo(Status.Started));
    }

    @Test
    public void testPasses() {
        TestSummary summary = new TestSummary(started);
        summary.handleEvent(passed);

        assertThat(summary.passed(), equalTo(true));
        assertThat(summary.ignored(), equalTo(false));
        assertThat(summary.failed(), equalTo(false));
        assertThat(summary.terminated(), equalTo(false));
        assertThat(summary.getStatus(), equalTo(Status.Passed));
        assertThat(summary.getFailures().iterator().hasNext(), equalTo(false));
    }

    @Test
    public void testIsIgnored() {
        TestSummary summary = new TestSummary(started);
        summary.handleEvent(ignored);

        assertThat(summary.passed(), equalTo(false));
        assertThat(summary.ignored(), equalTo(true));
        assertThat(summary.failed(), equalTo(false));
        assertThat(summary.terminated(), equalTo(false));
        assertThat(summary.getStatus(), equalTo(Status.Ignored));
        assertThat(summary.getFailures().iterator().hasNext(), equalTo(false));
    }

    @Test
    public void testWasTerminated() {
        TestSummary summary = new TestSummary(started);
        summary.handleEvent(terminated);

        assertThat(summary.passed(), equalTo(false));
        assertThat(summary.ignored(), equalTo(false));
        assertThat(summary.failed(), equalTo(false));
        assertThat(summary.terminated(), equalTo(true));
        assertThat(summary.getStatus(), equalTo(Status.Terminated));
        assertThat(summary.getFailures().iterator().hasNext(), equalTo(false));
    }

    @Test
    public void testFailed() {
        TestSummary summary = new TestSummary(started);
        summary.handleEvent(failed);

        assertThat(summary.passed(), equalTo(false));
        assertThat(summary.ignored(), equalTo(false));
        assertThat(summary.failed(), equalTo(true));
        assertThat(summary.terminated(), equalTo(false));
        assertThat(summary.getStatus(), equalTo(Status.Failed));

        assertThat(summary.getFailures().iterator().hasNext(), equalTo(true));
        assertThat(summary.getFailures().iterator().next().getCause(), equalTo(failed.getFailure()));
    }

    @Test
    public void testProcessorTimeIncompleteIsZero() {
        TestSummary summary = new TestSummary(passed);

        assertThat(summary.processorTime(), equalTo(0L));
        assertThat(summary.elapsedTime(), equalTo(0L));
    }

    @Test
    public void testProcessorTimeCompleteIsDifferenceOfStartAndEndTimes() {
        long elapsed = passed.getNanoseconds() - started.getNanoseconds();
        TestSummary summary = new TestSummary(started);
        summary.handleEvent(passed);

        assertThat(summary.processorTime(), equalTo(elapsed));
        assertThat(summary.elapsedTime(), equalTo(elapsed));
    }

    @Test
    public void naturalOrderFollowsDescriptions() {
        TestSummary one = new TestSummary(started);
        TestSummary two = new TestSummary(new TestEvent(new Description("Name", Type.Test), Status.Passed));
        TestSummary three = new TestSummary(new TestEvent(new Description("Alpha", Type.Test), Status.Passed));

        List<TestSummary> list = Arrays.asList(one, two, three);
        Collections.sort(list);

        assertThat(list, equalTo(Arrays.asList(three, one, two)));
    }

    @Test
    public void passedToString() {
        TestSummary summary = new TestSummary(started);
        summary.handleEvent(passed);
        float time = (passed.getNanoseconds() - started.getNanoseconds()) / 1000000f;

        assertThat(summary.toString(), equalTo(String.format("My Test(%.3fms):\tPassed\n", time)));
    }

    @Test
    public void terminatedToString() {
        TestSummary summary = new TestSummary(started);
        summary.handleEvent(terminated);
        float time = (terminated.getNanoseconds() - started.getNanoseconds()) / 1000000f;

        assertThat(summary.toString(), equalTo(String.format("My Test(%.3fms):\tTerminated\n", time)));
    }

    @Test
    public void ignoredToString() {
        TestSummary summary = new TestSummary(started);
        summary.handleEvent(ignored);
        float time = (ignored.getNanoseconds() - started.getNanoseconds()) / 1000000f;

        assertThat(summary.toString(), equalTo(String.format("My Test(%.3fms):\tIgnored\n", time)));
    }

    @Test
    public void failedToString() {
        TestSummary summary = new TestSummary(started);
        summary.handleEvent(failed);
        float time = (failed.getNanoseconds() - started.getNanoseconds()) / 1000000f;

        assertThat(summary.toString(), startsWith(String.format("My Test(%.3fms):\tFAILED...\n\n", time)));

        StringWriter writer = new StringWriter();
        failed.getFailure().printStackTrace(new PrintWriter(writer));

        assertThat(summary.toString(), endsWith(writer.toString() + "\n"));
    }
}
