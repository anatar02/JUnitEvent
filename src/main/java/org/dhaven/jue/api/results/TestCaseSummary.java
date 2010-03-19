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
public class TestCaseSummary extends TestSummary implements ParentSummary {
    private final Set<Summary> children = new HashSet<Summary>();

    public TestCaseSummary(TestEvent event) {
        super(event);
    }

    @Override
    public Status getStatus() {
        return evaluateStatus(getChildren());
    }

    protected Status evaluateStatus(Iterable<? extends Summary> children) {
        Status status = Status.Started;

        for (Summary child : children) {
            if (status == Status.Started) {
                status = child.getStatus();
            } else switch (child.getStatus()) {
                case Passed:
                    if (status != Status.Terminated && status != Status.Failed) {
                        status = Status.Passed;
                    }
                    break;

                case Terminated:
                    status = Status.Terminated;
                    break;

                case Failed:
                    status = Status.Failed;
                    break;
            }
        }

        return status;
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
    }

    @Override
    public Iterable<Summary> getChildren() {
        return children;
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

        float clock = nanosToMillis(elapsedTime());
        float processor = nanosToMillis(processorTime());
        builder.append("clock time: ").append(String.format("%.3f", clock));
        builder.append("ms\tprocessor time: ").append(String.format("%.3f", processor));
        builder.append("ms\n\n");

        return builder.toString();
    }
}
