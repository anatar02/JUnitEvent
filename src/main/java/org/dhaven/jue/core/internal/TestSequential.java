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

import org.dhaven.jue.api.description.Description;
import org.dhaven.jue.api.event.Status;
import org.dhaven.jue.core.TestListenerSupport;
import org.dhaven.jue.core.internal.node.EventNode;
import org.dhaven.jue.core.internal.node.TestNode;

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
        Collection<TestCase> testPlan = plan.export();

        new EventNode(Description.JUEName, Status.Started).run(support);

        for (TestCase testCase : testPlan) {
            new EventNode(testCase.getDescription(), Status.Started).run(support);

            if (testCase.isEmpty()) {
                //noinspection ThrowableInstanceNeverThrown
                new EventNode(testCase.getDescription(), Status.Failed,
                        new AssertionError("Test class does not have any tests: "
                                + testCase.getDescription().getName())).run(support);
            } else {
                for (TestNode node : testCase) {
                    node.run(support);
                }

                new EventNode(testCase.getDescription(), Status.Terminated).run(support);
            }
        }

        new EventNode(Description.JUEName, Status.Terminated).run(support);
    }

    @Override
    public void shutdown() {
        // nothing to do
    }
}
