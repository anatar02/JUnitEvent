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

import org.dhaven.jue.api.*;
import org.dhaven.jue.core.internal.TestPlan;

/**
 * Central class for JUnit Events.  This runs the tests as they are.
 */
public class Engine {
    private TestEventListenerSupport listenerSupport = new TestEventListenerSupport();

    /**
     * Command line entry point for the test engine.
     *
     * @param arguments the command arguments to pass to the request.
     */
    public static void main(String... arguments) {
        // Set up the test engine
        Engine engine = new Engine();
        engine.addTestListener(new CommandLineListener());

        // Initialize the test environment
        Request request = new Request(arguments);

        // Get the results
        Results results = engine.process(request);

        System.out.println(results.passed() ? "All tests passed." : "Tests did not pass");
    }

    public Results process(Request request) {
        Results results = new Results();
        addTestListener(results);

        TestPlan plan = TestPlan.from(request);
        listenerSupport.fireTestEvent("JUnit Events", EventType.StartRun, TestStatus.Running);
        plan.execute(listenerSupport);
        listenerSupport.fireTestEvent("JUnit Events", EventType.EndRun, TestStatus.Terminated);

        removeTestListener(results);
        return results;
    }

    public void removeTestListener(TestEventListener testListener) {
        listenerSupport.removeTestListener(testListener);
    }

    public void addTestListener(TestEventListener testListener) {
        listenerSupport.addTestListener(testListener);
    }
}
