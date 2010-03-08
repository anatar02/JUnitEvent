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

package org.dhaven.jue.api;

import org.dhaven.jue.api.event.EventType;
import org.dhaven.jue.api.event.Status;
import org.dhaven.jue.api.event.TestEvent;
import org.dhaven.jue.api.event.TestEventListener;
import org.dhaven.jue.core.internal.Description;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * Collects the results from the tests as they are run.  The results object
 * provides summary and simple analysis features such as providing the
 * difference between processor time and clock time.  Considering that the tests
 * are designed to be run in parallel, this is useful information.  If any
 * test was run multiple times, you will be able to find the average processor
 * times for those tests.
 * <p/>
 * TODO: test case/test summary as well
 */
public class Results implements TestEventListener {
    Map<Description, TestEvent> collectedResults = new HashMap<Description, TestEvent>();

    /**
     * Check to see if all the results we were expecting came in.
     *
     * @return <code>true</code> if any pending results have {@link Status#Running}
     */
    public boolean complete() {
        boolean completed = !collectedResults.isEmpty();

        for (TestEvent event : collectedResults.values()) {
            completed = completed && (event.getStatus() != Status.Running);
        }

        return completed;
    }

    /**
     * Check to see if all the tests returned passing results.
     *
     * @return <code>true</code> if all tests have {@link Status#Passed}
     */
    public boolean passed() {
        boolean passed = !collectedResults.isEmpty();

        for (TestEvent event : collectedResults.values()) {
            passed = passed && (event.getStatus() == Status.Passed);
        }

        return passed;
    }

    /**
     * List all failures as one big string.  Provides easy summary of what
     * needs to be fixed.
     *
     * @return The string of every failure, and its cause
     */
    @SuppressWarnings({"ThrowableResultOfMethodCallIgnored"})
    public String failuresToString() {
        StringBuilder builder = new StringBuilder();

        for (TestEvent event : collectedResults.values()) {
            if (event.getStatus() == Status.Failed) {
                builder.append(event.getDescription());
                builder.append("... Failed\n");

                StringWriter writer = new StringWriter();
                event.getFailure().printStackTrace(new PrintWriter(writer));
                builder.append(writer.toString());
            }
        }

        return builder.toString();
    }

    @Override
    public void handleEvent(TestEvent event) {
        // only recording tests...
        if (event.getType() == EventType.EndTest) {
            collectedResults.put(event.getDescription(), event);
        }
    }
}
