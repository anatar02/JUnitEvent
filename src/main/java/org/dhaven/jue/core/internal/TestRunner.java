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

package org.dhaven.jue.core.internal;

import org.dhaven.jue.core.TestListenerSupport;

/**
 * The test runner performs the scheduling and execution of the test plan.
 */
public interface TestRunner {
    /**
     * Initialize any thread pools, etc. using the configured TestListenerSupport.
     *
     * @param support the test listener support to use for every node
     */
    void start(TestListenerSupport support);

    /**
     * Execute the test plan as provided.
     *
     * @param plan the test plan
     */
    void execute(TestPlan plan);

    /**
     * Shutdown the test runner, waiting for any running tests to complete,
     * clean up any background threads, etc.
     */
    void shutdown();
}
