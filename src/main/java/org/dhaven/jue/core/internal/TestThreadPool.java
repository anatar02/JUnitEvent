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

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.CyclicBarrier;

import org.dhaven.jue.core.TestEventListenerSupport;
import org.dhaven.jue.core.internal.node.TestNode;

/**
 * Provides the execution model for running the tests.
 */
public class TestThreadPool {
    private ThreadGroup group = new ThreadGroup("JUE:ThreadPool");
    private TestPlanThread[] threadList;
    private int currThread = 0;
    private int multiplier = 1;
    private CyclicBarrier barrier;

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
        for (TestNode node : plan.export()) {
            threadList[currThread].offer(node);
            currThread++;
            currThread = currThread % threadList.length;
        }

        for (TestPlanThread thread : threadList) {
            thread.planSubmitted();
        }
    }

    public void await() {
        try {
            barrier.await();
        } catch (Exception e) {
            // do nothing
        }
    }

    public void startup(TestEventListenerSupport support) {
        final int numThreads = getNumberOfThreads();
        barrier = new CyclicBarrier(numThreads + 1);
        threadList = new TestPlanThread[numThreads];

        for (int i = 0; i < numThreads; i++) {
            threadList[i] = new TestPlanThread(group, barrier, support);
            threadList[i].start();
        }
    }

    public void shutdown() {
        await();

        group.interrupt();
        for (Thread thread : threadList) {
            thread.interrupt();
        }
    }

    private static class TestPlanThread extends Thread {
        private Queue<TestNode> workQueue = new LinkedList<TestNode>();
        private static int threadNum = 1;
        private final TestEventListenerSupport support;
        private final CyclicBarrier barrier;
        private boolean countdown = false;

        public TestPlanThread(ThreadGroup group, CyclicBarrier barrier,
                              TestEventListenerSupport support) {
            super(group, group.getName() + "-" + nextThreadNum());
            this.support = support;
            this.barrier = barrier;
            setDaemon(true);
            setPriority(Thread.NORM_PRIORITY);
        }

        private static int nextThreadNum() {
            return threadNum++;
        }

        public boolean offer(TestNode node) {
            return workQueue.offer(node);
        }

        @Override
        public void run() {
            boolean process = true;
            while (process) {
                TestNode node = workQueue.poll();

                // if there is no node, sleep
                if (!sleepOnNull(node)) {
                    // otherwise process the node
                    processNode(node);
                }

                process = !Thread.interrupted();

                if (process && countdown) {
                    process = workQueue.size() > 0;
                }
            }

            try {
                barrier.await();
            } catch (Exception e) {
                // ignore
            }
        }

        private void processNode(TestNode node) {
            try {
                if (!node.attemptRun(support)) {
                    workQueue.offer(node);
                }
            } catch (Exception e) {
                support.fireTestFailed(node, e);
            }
        }

        private boolean sleepOnNull(TestNode node) {
            boolean sleep = node == null;

            if (sleep) {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    // silently ignore
                }
            }

            return sleep;
        }

        public void planSubmitted() {
            countdown = true;
        }
    }
}
