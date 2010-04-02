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
import org.dhaven.jue.api.event.TestListener;
import org.dhaven.jue.api.results.Results;
import org.dhaven.jue.core.internal.TestForkJoinPool;
import org.dhaven.jue.core.internal.TestPlan;
import org.dhaven.jue.core.internal.TestRunner;

/**
 * Central class for JUnit Events.  This runs the tests as they are.
 */
public final class Engine {
    private final TestListenerSupport listenerSupport = new TestListenerSupport();

    /**
     * Command line entry point for the test engine.
     *
     * @param arguments the command arguments to pass to the request.
     * @throws Exception if there was a problem in the engine
     */
    public static void main(String... arguments) throws Exception {
        // Set up the test engine
        Engine engine = new Engine();

        // Initialize the test environment
        Request request = new Request(arguments);

        // Get the results
        Results results = engine.process(request);

        System.out.println(results.passed() ? "All tests passed." : "Tests did not pass");

        System.out.println(results);
    }

    public Results process(Request request) throws Exception {
        Thread.currentThread().setContextClassLoader(request.getRequestClassLoader());
        TestRunner pool = new TestForkJoinPool();
        pool.start(listenerSupport);

        Results results = new Results();
        addTestListener(results);
        TestPlan plan = TestPlan.from(request);

        pool.execute(plan);

        pool.shutdown();
        listenerSupport.await();

        removeTestListener(results);
        return results;
    }

    public void removeTestListener(TestListener testListener) {
        listenerSupport.removeTestListener(testListener);
    }

    public void addTestListener(TestListener testListener) {
        listenerSupport.addTestListener(testListener);
    }
}
