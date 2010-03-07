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

package org.dhaven.jue.api.event;

/**
 * The EventType identifies where an event is in the test cycle.  The event
 * types are listed in the logical order of how they are sent.
 */
public enum EventType {
    /**
     * Event is sent at the beginning of all testing.
     */
    StartRun,
    /**
     * Event is sent at the beginning of a test case.  A test case is a class
     * that contains several tests within it.
     */
    StartTestCase,
    /**
     * Event is sent as the beginning of an individual test.
     */
    StartTest,
    /**
     * Event is sent at the end of an individual test.
     */
    EndTest,
    /**
     * Event is sent at the end of a test case.  A test case is a class that
     * contains several tests within it.
     */
    EndTestCase,
    /**
     * Event is sent at the end of all testing.
     */
    EndRun
}
