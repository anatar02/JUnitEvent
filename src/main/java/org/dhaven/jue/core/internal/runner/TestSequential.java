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

package org.dhaven.jue.core.internal.runner;

import org.dhaven.jue.api.description.Description;
import org.dhaven.jue.api.event.Status;
import org.dhaven.jue.api.event.TestEvent;
import org.dhaven.jue.core.TestListenerSupport;
import org.dhaven.jue.core.internal.TestCase;
import org.dhaven.jue.core.internal.TestNode;
import org.dhaven.jue.core.internal.TestPlan;

/**
 * Run the tests sequentially, just like would happen with traditional JUnit.
 */
public class TestSequential implements TestRunner {
    private TestListenerSupport support;

    @Override
    public void start(TestListenerSupport support) {
        this.support = support;
    }

    @Override
    public void execute(TestPlan plan) {
        support.fireTestEvent(new TestEvent(Description.JUEName, Status.Started));

        for (TestCase testCase : plan.export()) {
            executeTestCase(testCase);
        }

        support.fireTestEvent(new TestEvent(Description.JUEName, Status.Terminated));
    }

    private void executeTestCase(TestCase testCase) {
        support.fireTestEvent(new TestEvent(testCase.getDescription(), Status.Started));

        if (testCase.isEmpty()) {
            //noinspection ThrowableInstanceNeverThrown
            support.fireTestEvent(new TestEvent(testCase.getDescription(), Status.Failed,
                    new AssertionError("Test class does not have any tests: "
                            + testCase.getDescription().getName())));
        } else {
            for (TestNode node : testCase) {
                node.run(support);
            }

            support.fireTestEvent(new TestEvent(testCase.getDescription(), Status.Terminated));
        }
    }

    @Override
    public void shutdown() {
        // nothing to do
    }
}
