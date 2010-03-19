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

import java.util.Collection;

import org.dhaven.jue.core.internal.node.TestNode;

/**
 * A planner controls the behavior of a test class.  A planner can enable new
 * annotations, as well as change the number of times your tests are called.
 */
public interface Planner {
    /**
     * Define the set of tests for a test case.  This method generates a set
     * of test nodes from the test case, all of which will be popped on to the
     * test queue and run by the test threads.
     *
     * @param testCase the test class to read
     * @return a set of test nodes read from that class
     * @throws Exception if there is a problem defining the tests
     */
    Collection<? extends TestNode> defineTests(Class<?> testCase) throws Exception;
}
