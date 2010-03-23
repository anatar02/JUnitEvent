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

import org.dhaven.jue.Before;
import org.dhaven.jue.ListenerTester;
import org.dhaven.jue.Test;
import org.dhaven.jue.api.description.Describable;
import org.dhaven.jue.api.description.Description;
import org.dhaven.jue.api.description.Type;
import org.dhaven.jue.api.event.Status;
import org.dhaven.jue.api.event.TestEvent;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class TestTestListenerSupport {
    private TestListenerSupport support;
    private ListenerTester listener;

    @Before
    public void setUpListener() {
        support = new TestListenerSupport();
        listener = new ListenerTester();
        support.addTestListener(listener);
    }

    @Test
    public void eventGetsToListener() {
        support.fireTestEvent(new TestEvent(Description.JUEName, Status.Terminated));
        support.shutdown();

        assertThat(listener.getEvents().size(), equalTo(1));
        assertThat(listener.getEvents().get(0).getStatus(), equalTo(Status.Terminated));
    }

    @Test
    public void eventDoesNotGetToRemovedListener() {
        support.removeTestListener(listener);
        support.fireTestEvent(new TestEvent(Description.JUEName, Status.Terminated));
        support.shutdown();

        assertThat(listener.getEvents().size(), equalTo(0));
    }

    @Test
    public void testStartedEvent() {
        TestDescribable describable = new TestDescribable("Test Name", Type.Test);
        support.fireTestStarted(describable);
        support.shutdown();

        TestEvent event = listener.getEvents().get(0);
        assertThat(event.getDescription(), equalTo(describable.getDescription()));
        assertThat(event.getStatus(), equalTo(Status.Started));
    }

    @Test
    public void testPassedEvent() {
        TestDescribable describable = new TestDescribable("Test Name", Type.Test);
        support.fireTestPassed(describable);
        support.shutdown();

        TestEvent event = listener.getEvents().get(0);
        assertThat(event.getDescription(), equalTo(describable.getDescription()));
        assertThat(event.getStatus(), equalTo(Status.Passed));
    }

    @Test
    public void testIgnoredEvent() {
        TestDescribable describable = new TestDescribable("Test Name", Type.Test);
        support.fireTestIgnored(describable);
        support.shutdown();

        TestEvent event = listener.getEvents().get(0);
        assertThat(event.getDescription(), equalTo(describable.getDescription()));
        assertThat(event.getStatus(), equalTo(Status.Ignored));
    }

    @Test
    public void testTerminatedEvent() {
        TestDescribable describable = new TestDescribable("Test Name", Type.Test);
        support.fireTestTerminated(describable);
        support.shutdown();

        TestEvent event = listener.getEvents().get(0);
        assertThat(event.getDescription(), equalTo(describable.getDescription()));
        assertThat(event.getStatus(), equalTo(Status.Terminated));
    }


    @SuppressWarnings({"ThrowableInstanceNeverThrown", "ThrowableResultOfMethodCallIgnored"})
    @Test
    public void testFailedEvent() {
        TestDescribable describable = new TestDescribable("Test Name", Type.Test);
        Throwable failure = new CloneNotSupportedException("Test exception");
        support.fireTestFailed(describable, failure);
        support.shutdown();

        TestEvent event = listener.getEvents().get(0);
        assertThat(event.getDescription(), equalTo(describable.getDescription()));
        assertThat(event.getStatus(), equalTo(Status.Failed));
        assertThat(event.getFailure(), equalTo(failure));
    }

    private static class TestDescribable implements Describable {
        Description description;

        TestDescribable(String name, Type type) {
            description = new Description(name, type);
        }

        @Override
        public Description getDescription() {
            return description;
        }
    }
}
