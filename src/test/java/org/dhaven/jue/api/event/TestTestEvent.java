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

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ExecutionException;

import org.dhaven.jue.Test;
import org.dhaven.jue.api.description.Description;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.lessThan;

@SuppressWarnings({"ThrowableResultOfMethodCallIgnored", "ThrowableInstanceNeverThrown"})
public class TestTestEvent {
    @Test
    public void createTestEvent() {
        TestEvent event = new TestEvent(Description.JUEName, Status.Started);

        assertThat(event.getDescription(), equalTo(Description.JUEName));
        assertThat(event.getType(), equalTo(Description.JUEName.getType()));
        assertThat(event.getStatus(), equalTo(Status.Started));
        assertThat(event.getFailure(), equalTo(null));
    }

    @Test(expected = IllegalArgumentException.class)
    public void descriptionMustExist() {
        new TestEvent(null, Status.Started);
    }

    @Test(expected = IllegalArgumentException.class)
    public void statusMustExist() {
        new TestEvent(Description.JUEName, null);
    }

    @Test
    public void createTestEventWithFailure() {
        Throwable failure = new AssertionError("test");
        TestEvent event = new TestEvent(Description.JUEName, Status.Failed, failure);

        assertThat(event.getDescription(), equalTo(Description.JUEName));
        assertThat(event.getType(), equalTo(Description.JUEName.getType()));
        assertThat(event.getStatus(), equalTo(Status.Failed));
        assertThat(event.getFailure(), equalTo(failure));
    }

    @Test
    public void createInvocationExceptionWithCause() {
        Throwable cause = new IllegalArgumentException("Just because");
        Throwable failure = new InvocationTargetException(cause);
        TestEvent event = new TestEvent(Description.JUEName, Status.Failed, failure);

        assertThat(event.getFailure(), equalTo(cause));
    }

    @Test
    public void createExecutionExceptionWithCause() {
        Throwable cause = new IllegalArgumentException("Just because");
        Throwable failure = new ExecutionException(cause);
        TestEvent event = new TestEvent(Description.JUEName, Status.Failed, failure);

        assertThat(event.getFailure(), equalTo(cause));
    }

    @Test
    public void createExecutionExceptionWithNoCause() {
        Throwable failure = new ExecutionException(null);
        TestEvent event = new TestEvent(Description.JUEName, Status.Failed, failure);

        assertThat(event.getFailure(), equalTo(failure));
    }

    @Test
    public void checkNanosecondsReasonablyClose() {
        // within 1/10 of a millisecond
        assertThat((System.nanoTime() - new TestEvent(Description.JUEName, Status.Terminated).getNanoseconds()), lessThan(100L));
    }

    @Test
    public void stringFormattedProperly() {
        TestEvent event = new TestEvent(Description.JUEName, Status.Terminated);

        assertThat(event.toString(), equalTo("{JUE: Version 0.5 [System] [Terminated]: " + event.getNanoseconds() + "}"));
    }
}
