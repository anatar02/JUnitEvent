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

package org.dhaven.jue.core;

import org.dhaven.jue.api.Request;
import org.dhaven.jue.api.Results;
import org.dhaven.jue.api.event.TestEventListener;
import org.dhaven.jue.core.internal.TestPlan;
import org.dhaven.jue.core.internal.TestThreadPool;

/**
 * Central class for JUnit Events.  This runs the tests as they are.
 */
public class Engine {
    private TestEventListenerSupport listenerSupport = new TestEventListenerSupport();
    private int threadsPerProcessor = 1;

    /**
     * Command line entry point for the test engine.
     *
     * @param arguments the command arguments to pass to the request.
     * @throws Exception if there was a problem in the engine
     */
    public static void main(String... arguments) throws Exception {
        // Set up the test engine
        Engine engine = new Engine();
        engine.setThreadsPerProcessor(10);
        engine.addTestListener(new CommandLineListener());

        // Initialize the test environment
        Request request = new Request(arguments);

        // Get the results
        Results results = engine.process(request);

        System.out.println(results.passed() ? "All tests passed." : "Tests did not pass");
    }

    public Results process(Request request) throws Exception {
        TestThreadPool pool = new TestThreadPool();
        pool.setMultiplier(threadsPerProcessor);
        pool.startup(listenerSupport);

        Results results = new Results();
        addTestListener(results);
        TestPlan plan = TestPlan.from(request);

        pool.execute(plan);

        pool.shutdown();
        listenerSupport.shutdown();

        removeTestListener(results);
        return results;
    }

    public int getThreadsPerProcessor() {
        return threadsPerProcessor;
    }

    public void setThreadsPerProcessor(int threads) {
        threadsPerProcessor = Math.min(Math.max(1, threads), 50);
    }

    public void removeTestListener(TestEventListener testListener) {
        listenerSupport.removeTestListener(testListener);
    }

    public void addTestListener(TestEventListener testListener) {
        listenerSupport.addTestListener(testListener);
    }
}
