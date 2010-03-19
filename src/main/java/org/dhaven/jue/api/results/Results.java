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

import org.dhaven.jue.api.description.Description;
import org.dhaven.jue.api.description.Type;
import org.dhaven.jue.api.event.Status;
import org.dhaven.jue.api.event.TestEvent;
import org.dhaven.jue.api.event.TestEventListener;

import java.util.*;

/**
 * Collects the results from the tests as they are run.  The results object
 * provides summary and simple analysis features such as providing the
 * difference between processor time and clock time.  Considering that the tests
 * are designed to be run in parallel, this is useful information.  If any
 * test was run multiple times, you will be able to find the average processor
 * times for those tests.
 */
public class Results extends TestCaseSummary implements TestEventListener {
    private Set<ParentSummary> testCases = new TreeSet<ParentSummary>();
    private Map<Description, TestSummary> collectedResults = new HashMap<Description, TestSummary>();

    /**
     * Create the results object.
     */
    public Results() {
        super(null);
    }

    /**
     * Check to see if all the results we were expecting came in.
     *
     * @return <code>true</code> if any pending results have {@link Status#Started}
     */
    @Override
    public boolean complete() {
        boolean completed = !collectedResults.isEmpty();

        for (Summary summary : collectedResults.values()) {
            completed = completed && summary.complete();
        }

        return completed;
    }

    @Override
    public boolean passed() {
        boolean passed = !collectedResults.isEmpty();

        for (Summary summary : collectedResults.values()) {
            if (summary.getType() == Type.Test) {
                passed = passed && (summary.passed() || summary.ignored());
            }
        }

        return passed;
    }

    @Override
    public void handleEvent(TestEvent event) {
        TestSummary summary = collectedResults.get(event.getDescription());

        if (null == summary) {
            summary = TestSummary.create(event);
        } else {
            summary.handleEvent(event);
        }

        switch (summary.getType()) {
            case System:
                super.handleEvent(event);
                break;

            case TestCase:
                testCases.add(TestCaseSummary.class.cast(summary));
                this.addChild(summary);
                break;

            case Test:
                for (ParentSummary caseSummary : testCases) {
                    Description testCase = caseSummary.getDescription();
                    if (testCase.relatedTo(summary.getDescription())) {
                        caseSummary.addChild(summary);
                    }
                }
                break;

            default:
                throw new IllegalStateException("test summary event class is not handled: " + summary.getType());
        }

        collectedResults.put(event.getDescription(), summary);
    }

    private Collection<Summary> filterResults(Type type) {
        ArrayList<Summary> summaries = new ArrayList<Summary>(collectedResults.size());

        for (TestSummary summary : collectedResults.values()) {
            if (summary.getType() == type) {
                summaries.add(summary);
            }
        }

        return summaries;
    }

    /**
     * Total number of test cases in this test run.
     *
     * @return the number of test cases executed
     */
    public int numberOfTestCases() {
        return filterResults(Type.TestCase).size();
    }

    /**
     * Total number of tests in this test run from all test cases.  Does not
     * count ignored tests.
     *
     * @return the total number of tests
     */
    public int numberOfTestsRun() {
        int numRun = 0;

        for (Summary summary : filterResults(Type.Test)) {
            if (!summary.ignored()) numRun++;
        }

        return numRun;
    }
}
