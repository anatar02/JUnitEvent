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

import org.dhaven.jue.core.TestEventListenerSupport;
import org.dhaven.jue.core.internal.node.TestNode;

import java.util.PriorityQueue;

/**
 * Provides the execution model for running the tests.
 */
public class TestThreadPool {
    private PriorityQueue<TestNode> workQueue = new PriorityQueue<TestNode>();
    private ThreadGroup group = new ThreadGroup("JUE:ThreadPool");
    private Thread[] threadList;
    private int multiplier = 1;

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
        workQueue.addAll(plan.export());
    }

    public void await() {
        while (workQueue.size() > 0) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    public void startup(TestEventListenerSupport support) {
        final int numThreads = getNumberOfThreads();
        threadList = new Thread[numThreads];

        for (int i = 0; i < numThreads; i++) {
            threadList[i] = new Thread(group, new TestPlanRunner(support));
            threadList[i].setDaemon(true);
            threadList[i].setPriority(Thread.NORM_PRIORITY);
            threadList[i].start();
        }
    }

    public void shutdown() {
        group.interrupt();
        for (Thread thread : threadList) {
            thread.interrupt();
        }
    }

    private class TestPlanRunner implements Runnable {
        private final TestEventListenerSupport support;

        public TestPlanRunner(TestEventListenerSupport support) {
            this.support = support;
        }

        @Override
        public void run() {
            while (!Thread.interrupted()) {
                TestNode node = workQueue.poll();

                // if there is no node, sleep
                if (!sleepOnNull(node)) {
                    // otherwise process the node
                    processNode(node);
                }
            }
        }

        private void processNode(TestNode node) {
            try {
                if (!node.attemptRun(support)) {
                    workQueue.offer(node);
                }
            } catch (InterruptedException ie) {
                support.fireTestTerminated(node);
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
    }
}
