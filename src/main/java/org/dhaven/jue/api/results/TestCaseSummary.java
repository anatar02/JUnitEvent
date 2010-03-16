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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.dhaven.jue.api.event.Status;
import org.dhaven.jue.api.event.TestEvent;

/**
 * A test summary instance will provide the end results of a test, and any
 * children tests.  For example, a TestCase has many individual tests.
 */
public class TestCaseSummary extends TestSummary {
    private Set<TestSummary> children = new HashSet<TestSummary>();
    private Status summaryStatus = Status.Started;

    public TestCaseSummary(TestEvent event) {
        super(event);
    }

    @Override
    public boolean passed() {
        return summaryStatus == Status.Passed;
    }

    @Override
    public boolean failed() {
        return summaryStatus == Status.Failed;
    }

    @Override
    public boolean terminated() {
        return summaryStatus == Status.Terminated;
    }

    @Override
    public boolean ignored() {
        return summaryStatus == Status.Ignored;
    }

    @SuppressWarnings({"ThrowableResultOfMethodCallIgnored"})
    public Throwable[] getFailures() {
        Collection<Throwable> failures = new ArrayList<Throwable>(children.size());

        if (isComplete()) {
            for (TestSummary child : children) {
                Throwable failure = child.getFailure();

                if (null != failure) {
                    failures.add(failure);
                }
            }
        }

        return failures.toArray(new Throwable[failures.size()]);
    }

    @Override
    public long processorTime() {
        long time = 0;

        for (TestSummary child : children) {
            time += child.processorTime();
        }

        return time;
    }

    public void addChild(TestSummary child) {
        children.add(child);

        if (summaryStatus == Status.Started) {
            summaryStatus = child.getStatus();
        }

        switch (child.getStatus()) {
            case Passed:
                if (summaryStatus != Status.Terminated && summaryStatus != Status.Failed) {
                    summaryStatus = Status.Passed;
                }
                break;

            case Terminated:
                summaryStatus = Status.Terminated;
                break;

            case Failed:
                summaryStatus = Status.Failed;
                break;
        }
    }

    private static final String SEPARATOR = "------------------------------------------------------------------\n";

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        for (TestSummary summary : children) {
            builder.append(summary);
        }

        builder.append(SEPARATOR);
        builder.append(getDescription()).append("\t");

        appendStatus(builder);

        float clock = threeDigitMS(elapsedTime());
        float processor = threeDigitMS(processorTime());
        builder.append("clock time: ").append(clock);
        builder.append("ms\tprocessor time: ").append(processor);
        builder.append("ms\n\n");

        return builder.toString();
    }
}