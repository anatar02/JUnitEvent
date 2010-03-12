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

package org.dhaven.jue.core.internal;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.dhaven.jue.api.Description;
import org.dhaven.jue.api.Request;
import org.dhaven.jue.api.event.EventType;
import org.dhaven.jue.api.event.Status;
import org.dhaven.jue.core.internal.node.EventNode;
import org.dhaven.jue.core.internal.node.TestNode;

/**
 * Represents the test plan.
 */
public class TestPlan {
    private List<TestNode> testQueue = new LinkedList<TestNode>();
    private EventNode start = new EventNode(Description.JUEName, EventType.StartRun, Status.Running);
    private EventNode end = new EventNode(Description.JUEName, EventType.EndRun, Status.Terminated);

    // should only be created locally;

    private TestPlan() {
        start.addSuccessor(end);
        testQueue.add(start);
        testQueue.add(end);
    }

    /**
     * Create a test plan from a Request object.
     *
     * @param request the request object
     * @return the initialized test plan
     * @throws Exception when there was a problem constructing the plan.
     */
    public static TestPlan from(Request request) throws Exception {
        TestPlan plan = new TestPlan();

        for (Class<?> testCase : request.getTestClasses()) {
            Runner runner = new DefaultRunner();
            plan.addTests(runner.defineTests(testCase));
        }

        return plan;
    }

    public void addTests(Collection<? extends TestNode> tests) {
        for (TestNode node : tests) {
            node.addPredecessor(start);
            node.addSuccessor(end);
            testQueue.add(node);
        }
    }

    public Collection<? extends TestNode> export() {
        Collections.sort(testQueue);
        return testQueue;
    }
}
