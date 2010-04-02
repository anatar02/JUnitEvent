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
import java.util.concurrent.*;

import org.dhaven.jue.api.description.Description;
import org.dhaven.jue.api.event.Status;
import org.dhaven.jue.core.TestListenerSupport;
import org.dhaven.jue.core.internal.node.EventNode;
import org.dhaven.jue.core.internal.node.TestNode;

/**
 * Provides the execution model for running the tests.
 */
public class TestThreadPool implements TestRunner {
    private final ThreadGroup group = new ThreadGroup("JUE:ThreadPool");
    private ThreadPoolExecutor service;
    private TestListenerSupport support;
    private ClassLoader classLoader;

    private int getNumberOfThreads() {
        return Runtime.getRuntime().availableProcessors();
    }

    public void execute(TestPlan plan) {
        Collection<TestCase> testPlan = plan.export();
        CountDownLatch latch = new CountDownLatch(testPlan.size());

        new EventNode(Description.JUEName, Status.Started).run(support);

        for (TestCase node : testPlan) {
            service.execute(new TestCaseRunner(node, latch, support));
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            // do nothing, we are interrupting the run
        }

        new EventNode(Description.JUEName, Status.Terminated).run(support);
    }

    public void start(TestListenerSupport support) {
        this.classLoader = Thread.currentThread().getContextClassLoader();
        this.support = support;
        service = new ThreadPoolExecutor(getNumberOfThreads(), Short.MAX_VALUE,
                60L, TimeUnit.SECONDS,
                new SynchronousQueue<Runnable>(),
                new TestThreadFactory());
    }

    public void shutdown() {
        service.shutdown();
    }

    private class TestThreadFactory implements ThreadFactory {
        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(group, r);
            thread.setDaemon(true);
            thread.setPriority(Thread.NORM_PRIORITY);
            thread.setContextClassLoader(classLoader);

            return thread;
        }
    }

    private class NodeRunner implements Runnable {
        private final TestNode node;
        private final TestListenerSupport support;
        private final CountDownLatch barrier;

        public NodeRunner(TestNode node, CountDownLatch barrier, TestListenerSupport support) {
            this.node = node;
            this.support = support;
            this.barrier = barrier;
        }

        @Override
        public void run() {
            node.run(support);
            barrier.countDown();
        }
    }

    private class TestCaseRunner implements Runnable {
        private final TestListenerSupport support;
        private final CountDownLatch barrier;
        private final TestCase testCase;

        public TestCaseRunner(TestCase testCase, CountDownLatch barrier, TestListenerSupport support) {
            this.testCase = testCase;
            this.barrier = barrier;
            this.support = support;
        }

        @Override
        public void run() {
            new EventNode(testCase.getDescription(), Status.Started).run(support);

            if (testCase.isEmpty()) {
                //noinspection ThrowableInstanceNeverThrown
                new EventNode(testCase.getDescription(), Status.Failed,
                        new AssertionError("Test class does not have any tests: "
                                + testCase.getDescription().getName())).run(support);
            } else {
                CountDownLatch latch = new CountDownLatch(testCase.size());

                for (TestNode node : testCase) {
                    service.execute(new NodeRunner(node, latch, support));
                }

                try {
                    latch.await();
                } catch (InterruptedException e) {
                    // do nothing, it was interrupted
                }

                new EventNode(testCase.getDescription(), Status.Terminated).run(support);
            }

            barrier.countDown();
        }
    }
}
