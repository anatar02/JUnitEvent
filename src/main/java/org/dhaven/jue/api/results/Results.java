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

package org.dhaven.jue.api.results;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

import org.dhaven.jue.api.description.Description;
import org.dhaven.jue.api.description.Type;
import org.dhaven.jue.api.event.Status;
import org.dhaven.jue.api.event.TestEvent;
import org.dhaven.jue.api.event.TestEventListener;

/**
 * Collects the results from the tests as they are run.  The results object
 * provides summary and simple analysis features such as providing the
 * difference between processor time and clock time.  Considering that the tests
 * are designed to be run in parallel, this is useful information.  If any
 * test was run multiple times, you will be able to find the average processor
 * times for those tests.
 */
public class Results implements TestEventListener {
    private TestCaseSummary runSummary;
    private Set<TestCaseSummary> testCases = new TreeSet<TestCaseSummary>();
    private Map<Description, TestSummary> collectedResults = new HashMap<Description, TestSummary>();

    /**
     * Check to see if all the results we were expecting came in.
     *
     * @return <code>true</code> if any pending results have {@link Status#Started}
     */
    public boolean complete() {
        boolean completed = !collectedResults.isEmpty();

        for (TestSummary summary : collectedResults.values()) {
            completed = completed && summary.isComplete();
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

        for (TestSummary summary : collectedResults.values()) {
            if (summary.getType() == Type.Test) {
                passed = passed && (summary.passed() || summary.ignored());
            }
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

        for (TestSummary summary : collectedResults.values()) {
            if (summary.failed()) {
                builder.append(summary.getDescription());
                builder.append("... Failed\n");

                StringWriter writer = new StringWriter();
                summary.getFailure().printStackTrace(new PrintWriter(writer));
                builder.append(writer.toString());
            }
        }

        return builder.toString();
    }

    @Override
    public void handleEvent(TestEvent event) {
        TestSummary summary = collectedResults.get(event.getDescription());

        if (null == summary) {
            summary = TestSummary.create(event);
        } else {
            summary.setEvent(event);
        }

        switch (summary.getType()) {
            case System:
                runSummary = TestCaseSummary.class.cast(summary);
                break;

            case TestCase:
                testCases.add(TestCaseSummary.class.cast(summary));
                runSummary.addChild(summary);
                break;

            case Test:
                for (TestCaseSummary caseSummary : testCases) {
                    Description testCase = caseSummary.getDescription();
                    Description test = summary.getDescription();
                    if (testCase.relatedTo(test)) {
                        caseSummary.addChild(summary);
                    }
                }

                break;

            default:
                throw new IllegalStateException("test summary event class is not handled: " + summary.getType());
        }

        collectedResults.put(event.getDescription(), summary);
    }

    public TestCaseSummary getRunSummary() {
        return runSummary;
    }

    public long getProcessorTime(Type type) {
        return totalTime(filterResults(type));
    }

    private Collection<TestSummary> filterResults(Type type) {
        ArrayList<TestSummary> summaries = new ArrayList<TestSummary>(collectedResults.size());

        for (TestSummary summary : collectedResults.values()) {
            if (summary.getType() == type) {
                summaries.add(summary);
            }
        }

        return summaries;
    }

    private static long totalTime(Collection<TestSummary> summaries) {
        int runningTime = 0;

        for (TestSummary summary : summaries) {
            runningTime += summary.elapsedTime();
        }

        return runningTime;
    }

    public int numberOfTestCases() {
        return filterResults(Type.TestCase).size();
    }

    public int numberOfTestsRun() {
        int numRun = 0;

        for (TestSummary summary : filterResults(Type.Test)) {
            if (!summary.ignored()) numRun++;
        }

        return numRun;
    }
}
