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

package org.dhaven.jue.core.internal.node;

import org.dhaven.jue.api.description.Describable;
import org.dhaven.jue.core.TestListenerSupport;

/**
 * The TestNode represents the smallest executable unit of a test.  Essentially,
 * test nodes can be prioritized and run concurrently.
 */
public interface TestNode extends Describable, Comparable<TestNode> {
    /**
     * Run the test code, passing in the listener support so that all the
     * listeners can be notified of the progress of the test as it is executed.
     * Returns false if the node could not be run yet, so it can be re-queued.
     *
     * @param support the {@link org.dhaven.jue.core.TestListenerSupport}
     * @return <code>true</code> if the node was run.
     */
    boolean attemptRun(TestListenerSupport support);

    void addSuccessor(TestNode dependencyTestNode);

    void addPredecessor(TestNode dependencyTestNode);

    void signalComplete(TestNode dependencyTestNode);
}
