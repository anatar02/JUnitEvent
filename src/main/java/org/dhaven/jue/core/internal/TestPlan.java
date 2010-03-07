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

import org.dhaven.jue.api.Request;
import org.dhaven.jue.api.event.EventType;
import org.dhaven.jue.api.event.Status;
import org.dhaven.jue.core.TestEventListenerSupport;

import java.util.Collection;
import java.util.PriorityQueue;

/**
 * Represents the test plan.
 */
public class TestPlan {
    PriorityQueue<Testlet> testQueue = new PriorityQueue<Testlet>();
    // should only be created locally;

    private TestPlan() {
    }

    /**
     * Create a test plan from a Request object.
     *
     * @param request the request object
     * @return the initialized test plan
     */
    public static TestPlan from(Request request) throws Exception {
        TestPlan plan = new TestPlan();

        for (Class<?> testCase : request.getTestClasses()) {
            Runner runner = new DefaultRunner();
            plan.addTests(runner.defineTests(testCase));
        }

        return plan;
    }

    public void addTest(Testlet test) {
        testQueue.add(test);
    }

    public void addTests(Collection<Testlet> tests) {
        testQueue.addAll(tests);
    }

    public void execute(TestEventListenerSupport listenerSupport) {
        for (Testlet testlet : testQueue) {
            listenerSupport.fireTestEvent(testlet.getName(), EventType.StartTest, Status.Running);
            if (testlet.isIgnored()) {
                listenerSupport.fireTestEvent(testlet.getName(), EventType.EndTest, Status.Ignored);
            } else {
                try {
                    testlet.call();
                    listenerSupport.fireTestEvent(testlet.getName(), EventType.EndTest, Status.Passed);
                } catch (InterruptedException ie) {
                    listenerSupport.fireTestEvent(testlet.getName(), EventType.EndTest, Status.Terminated);
                } catch (Throwable e) {
                    listenerSupport.fireTestEvent(testlet.getName(), e);
                }
            }
        }
    }
}
