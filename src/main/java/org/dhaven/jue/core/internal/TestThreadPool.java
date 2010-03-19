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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;

import org.dhaven.jue.core.TestListenerSupport;
import org.dhaven.jue.core.internal.node.TestNode;

/**
 * Provides the execution model for running the tests.
 */
public class TestThreadPool {
    private final ThreadGroup group = new ThreadGroup("JUE:ThreadPool");
    private ThreadPoolExecutor service;
    private TestListenerSupport support;
    private CountDownLatch barrier;
    private ClassLoader classLoader;

    private int getNumberOfThreads() {
        return Runtime.getRuntime().availableProcessors();
    }

    public void execute(TestPlan plan) {
        Collection<? extends TestNode> testPlan = plan.export();
        barrier = new CountDownLatch(testPlan.size());

        for (TestNode node : testPlan) {
            service.execute(new NodeRunner(node, barrier, support));
        }
    }

    public void start(TestListenerSupport support) {
        this.classLoader = Thread.currentThread().getContextClassLoader();
        this.support = support;
        service = ThreadPoolExecutor.class.cast(
                Executors.newFixedThreadPool(getNumberOfThreads(),
                        new TestThreadFactory()));
    }

    public void shutdown() {
        try {
            barrier.await();
        } catch (InterruptedException e) {
            // do nothing
        }

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
            if (node.attemptRun(support)) {
                barrier.countDown();
            } else {
                service.execute(NodeRunner.this);
            }
        }
    }
}
