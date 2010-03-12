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

package org.dhaven.jue.core.internal.node;

import java.util.List;

import org.dhaven.jue.ListenerTester;
import org.dhaven.jue.api.Description;
import org.dhaven.jue.api.event.EventType;
import org.dhaven.jue.api.event.Status;
import org.dhaven.jue.api.event.TestEvent;
import org.dhaven.jue.core.TestEventListenerSupport;

import static org.dhaven.jue.Annotations.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class TestEventNode extends TestDependencyTestNode {
    private TestEventListenerSupport support;
    private ListenerTester listener;

    @Override
    protected EventNode createNode(int i) {
        Description description = new Description("DependencyTestNode: " + i);
        return new EventNode(description, EventType.EndTest, Status.Passed);
    }

    @Before
    public void setUpListener() {
        support = new TestEventListenerSupport();
        listener = new ListenerTester();
        support.addTestListener(listener);
    }

    @After
    public void tearDownListener() {
        support.removeTestListener(listener);
    }

    @Test
    public void sendStartRun() {
        EventNode node = new EventNode(new Description("start run"), EventType.StartRun, Status.Running);

        node.attemptRun(support);
        support.shutdown();

        List<TestEvent> events = listener.getEvents();
        assertThat(events.size(), equalTo(1));

        TestEvent event = events.get(0);
        assertThat(event.getType(), equalTo(EventType.StartRun));
        assertThat(event.getStatus(), equalTo(Status.Running));
    }

    @Test
    public void sendEndTestCase() {
        EventNode node = new EventNode(new Description("start run"), EventType.EndTestCase, Status.Terminated);

        node.attemptRun(support);
        support.shutdown();

        List<TestEvent> events = listener.getEvents();
        assertThat(events.size(), equalTo(1));

        TestEvent event = events.get(0);
        assertThat(event.getType(), equalTo(EventType.EndTestCase));
        assertThat(event.getStatus(), equalTo(Status.Terminated));
    }
}
