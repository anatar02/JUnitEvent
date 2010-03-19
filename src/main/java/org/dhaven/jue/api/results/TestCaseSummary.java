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

import org.dhaven.jue.api.event.Status;
import org.dhaven.jue.api.event.TestEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * A test summary instance will provide the end results of a test, and any
 * children tests.  For example, a TestCase has many individual tests.
 */
public class TestCaseSummary extends TestSummary implements ParentSummary {
    private Set<Summary> children = new HashSet<Summary>();
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

    @Override
    @SuppressWarnings({"ThrowableResultOfMethodCallIgnored"})
    public Iterable<Failure> getFailures() {
        Collection<Failure> failures = new ArrayList<Failure>(children.size());

        if (complete()) {
            for (Summary child : children) {
                for (Failure failure : child.getFailures()) {
                    failures.add(failure);
                }
            }
        }

        return failures;
    }

    @Override
    public long processorTime() {
        long time = 0;

        for (Summary child : children) {
            time += child.processorTime();
        }

        return time;
    }

    @Override
    public void addChild(Summary child) {
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

    @Override
    public int size() {
        return children.size();
    }

    private static final String SEPARATOR = "------------------------------------------------------------------\n";

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        for (Summary summary : children) {
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
