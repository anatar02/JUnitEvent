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

import org.dhaven.jue.Annotations.Test;
import org.dhaven.jue.api.description.Description;
import org.dhaven.jue.api.description.Type;
import org.dhaven.jue.api.event.Status;
import org.dhaven.jue.api.event.TestEvent;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;

@SuppressWarnings({"ThrowableInstanceNeverThrown"})
public class TestResults {
    private Results results = new Results();

    @Test
    public void completeWithNoEvents() {
        assertThat(results.complete(), is(false));
    }

    @Test
    public void completeWithARunningEvent() {
        results.handleEvent(new TestEvent(new Description("test", Type.Test), Status.Started));

        assertThat(results.complete(), is(false));
    }

    @Test
    public void completeWithPassedEvent() {
        results.handleEvent(new TestEvent(new Description("test", Type.Test), Status.Started));
        results.handleEvent(new TestEvent(new Description("test", Type.Test), Status.Passed));

        assertThat(results.complete(), is(true));
    }

    @Test
    public void passedWithNoEvents() {
        assertThat(results.passed(), is(false));
    }

    @Test
    public void passedWithPassedEvent() {
        results.handleEvent(new TestEvent(new Description("test", Type.Test), Status.Started));
        results.handleEvent(new TestEvent(new Description("test", Type.Test), Status.Passed));

        assertThat(results.passed(), is(true));
    }

    @Test
    public void passedWithFailedEvent() {
        results.handleEvent(new TestEvent(new Description("test", Type.Test), Status.Started));
        results.handleEvent(new TestEvent(new Description("test", Type.Test), Status.Failed, new IllegalArgumentException("just kidding")));

        assertThat(results.passed(), is(false));
    }

    @Test
    public void failuresToStringWithNoFailures() {
        assertThat(results.failuresToString(), is(""));
    }

    @Test
    public void failuresToStringWithOneFailure() {
        results.handleEvent(new TestEvent(new Description("test", Type.Test), Status.Started));
        results.handleEvent(new TestEvent(new Description("test", Type.Test), Status.Failed, new IllegalArgumentException("just kidding")));

        assertThat(results.failuresToString(), startsWith("test... Failed\njava.lang.IllegalArgumentException: just kidding"));
    }
}
