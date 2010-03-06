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

package org.dhaven.jue.api;

/**
 * Event thrown by the test harness
 */
public class TestEvent {
    private final String name;
    private final EventType type;
    private final TestStatus status;
    private final Throwable failure;

    public TestEvent(String testName, EventType eventType, TestStatus testStatus) {
        this(testName, eventType, testStatus, null);
    }

    public TestEvent(String testName, EventType eventType, TestStatus testStatus, Throwable exception) {
        name = testName;
        type = eventType;
        status = testStatus;

        // TODO: extract root cause if we have a reflection exception
        failure = exception;
    }

    public String getName() {
        return name;
    }

    public EventType getType() {
        return type;
    }

    public TestStatus getStatus() {
        return status;
    }

    public Throwable getFailure() {
        return failure;
    }
}
