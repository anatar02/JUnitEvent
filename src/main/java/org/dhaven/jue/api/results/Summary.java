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

import org.dhaven.jue.api.description.Describable;
import org.dhaven.jue.api.description.Type;
import org.dhaven.jue.api.event.Status;

/**
 * This interface represents what all summary types must provide.  Summaries
 * allow you to roll up and examine the results in detail, and are probably
 * easier to process than individual test events.  A summary is a composite
 * result from multiple test events, and in some cases even multiple summaries.
 */
public interface Summary extends Describable, Comparable<Summary> {
    /**
     * The type of summary that this represents.
     *
     * @return the type of summary
     * @see {@link Type}.
     */
    Type getType();

    /**
     * Determine if the summary has all the information it is expecting.
     *
     * @return <code>true</code> if all known information is provided
     */
    boolean complete();

    /**
     * Determine if all events represented by this summary have passed.
     *
     * @return <code>true</code> if all tests have passed
     */
    boolean passed();

    /**
     * Determine if the test was terminated before completion.  Termination
     * can be the result of a timeout or due to the user manually terminating
     * the test.
     *
     * @return <code>true</code> if the test terminated
     */
    boolean terminated();

    /**
     * Determine if the test was ignored.  This only pertains to a single test.
     * For test cases, an ignored test does not override {@link #passed()}.
     *
     * @return <code>true</code> if this test was ignored
     */
    boolean ignored();

    /**
     * Determine if any event represented by this summary have failed.
     *
     * @return <code>true</code> if any test has failed
     */
    boolean failed();

    /**
     * Get all the failures associated with this summary.  A summary is
     * responsible to roll up the failures from any of its children.
     *
     * @return a set of failures (array/list/whatever)
     */
    Iterable<Failure> getFailures();

    /**
     * Get the clock time associated with this summary.  The clock time
     * represents the difference between the first event and the last event
     * received for the summary.
     *
     * @return the elapsed time in nanoseconds
     */
    long elapsedTime();

    /**
     * Get the processor time associated with this summary.  The processor time
     * is the collected sum of the elapsed time of the detailed tests.
     *
     * @return the processor time in nanoseconds
     */
    long processorTime();

    /**
     * Get the status for the test.  The same information can be found out with
     * the {@link #passed()}, {@link #failed()}, {@link #ignored()}, and
     * {@link #terminated()} methods.
     *
     * @return the {@link Status} of this summary
     */
    Status getStatus();
}
