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

import org.dhaven.jue.core.TestEventListenerSupport;
import org.dhaven.jue.core.internal.node.TestNode;

/**
 * Provides the execution model for running the tests.
 */
public class TestThreadPool {
    private ThreadGroup group = new ThreadGroup("JUE:ThreadPool");
    private ThreadPoolExecutor service;
    private TestEventListenerSupport support;
    private CountDownLatch barrier;
    private int multiplier = 1;
    private ClassLoader classLoader;

    public void setMultiplier(int value) {
        multiplier = value;
    }

    public int getMultiplier() {
        return multiplier;
    }

    public int getNumberOfThreads() {
        return Runtime.getRuntime().availableProcessors() * multiplier;
    }

    public void execute(TestPlan plan) {
        Collection<? extends TestNode> testPlan = plan.export();
        barrier = new CountDownLatch(testPlan.size());

        for (TestNode node : testPlan) {
            service.execute(new NodeRunner(node, barrier, support));
        }
    }

    public void startup(TestEventListenerSupport support) {
        this.classLoader = Thread.currentThread().getContextClassLoader();
        this.support = support;
        service = ThreadPoolExecutor.class.cast(
                Executors.newFixedThreadPool(getNumberOfThreads(),
                        new TestThreadFactory()));

        service.setKeepAliveTime(5, TimeUnit.SECONDS);
        service.prestartAllCoreThreads();
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
        private final TestEventListenerSupport support;
        private final CountDownLatch barrier;

        public NodeRunner(TestNode node, CountDownLatch barrier, TestEventListenerSupport support) {
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
