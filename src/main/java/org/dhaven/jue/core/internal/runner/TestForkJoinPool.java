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

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

import org.dhaven.jue.api.description.Description;
import org.dhaven.jue.api.event.Status;
import org.dhaven.jue.api.event.TestEvent;
import org.dhaven.jue.core.TestListenerSupport;
import org.dhaven.jue.core.internal.TestCase;
import org.dhaven.jue.core.internal.TestNode;
import org.dhaven.jue.core.internal.TestPlan;

/**
 * Implementation using Java 5's Fork/Join Pool.
 */
public class TestForkJoinPool implements TestRunner {
    private TestListenerSupport support;
    private ForkJoinPool service;

    @Override
    public void start(TestListenerSupport support) {
        this.support = support;
        this.service = new ForkJoinPool();
    }

    @Override
    public void execute(TestPlan plan) {
        service.invoke(new RunSolver(plan.export(), support));
    }

    @Override
    public void shutdown() {
        service.shutdown();
    }

    private static int getNumberOfProcessors() {
        return Runtime.getRuntime().availableProcessors();
    }

    private static class RunSolver extends RecursiveAction {
		private static final long serialVersionUID = -4093751896814113100L;
		private final Collection<TestCase> plan;
        private final TestListenerSupport support;

        public RunSolver(Collection<TestCase> plan, TestListenerSupport support) {
            this.plan = plan;
            this.support = support;
        }

        @Override
        protected void compute() {
            int threshold = plan.size() / TestForkJoinPool.getNumberOfProcessors();
            boolean runParallel = threshold > 2;

            support.fireTestEvent(new TestEvent(Description.JUEName, Status.Started));

            Collection<RecursiveAction> actions = new ArrayList<RecursiveAction>(plan.size());
            for (TestCase testCase : plan) {
                TestCaseRunner runner = new TestCaseRunner(testCase, support);
                if (runParallel) {
                    actions.add(runner);
                } else {
                    runner.compute();
                }
            }

            if (runParallel) {
                invokeAll(actions);
            }

            support.fireTestEvent(new TestEvent(Description.JUEName, Status.Terminated));
        }
    }

    private static class TestCaseRunner extends RecursiveAction {
		private static final long serialVersionUID = -6717716230263567796L;
		private final TestCase testCase;
        private final TestListenerSupport support;

        public TestCaseRunner(TestCase testCase, TestListenerSupport support) {
            this.testCase = testCase;
            this.support = support;
        }

        @Override
        protected void compute() {
            int threshold = testCase.size() / TestForkJoinPool.getNumberOfProcessors();
            boolean runParallel = threshold > 2;

            support.fireTestEvent(new TestEvent(testCase.getDescription(), Status.Started));

            if (testCase.isEmpty()) {
                //noinspection ThrowableInstanceNeverThrown
                support.fireTestEvent(new TestEvent(testCase.getDescription(), Status.Failed,
                        new AssertionError("Test class does not have any tests: "
                                + testCase.getDescription().getName())));
            } else {
                Collection<RecursiveAction> actions = new ArrayList<RecursiveAction>(testCase.size());
                for (TestNode node : testCase) {
                    NodeRunner runner = new NodeRunner(node, support);

                    if (runParallel) {
                        actions.add(runner);
                    } else {
                        runner.compute();
                    }
                }

                if (runParallel) {
                    invokeAll(actions);
                }

                support.fireTestEvent(new TestEvent(testCase.getDescription(), Status.Terminated));
            }
        }
    }

    private static class NodeRunner extends RecursiveAction {
		private static final long serialVersionUID = 6775236283531536301L;
		private TestNode node;
        private TestListenerSupport support;

        public NodeRunner(TestNode node, TestListenerSupport support) {
            this.node = node;
            this.support = support;
        }

        @Override
        protected void compute() {
            node.run(support);
        }
    }
}
