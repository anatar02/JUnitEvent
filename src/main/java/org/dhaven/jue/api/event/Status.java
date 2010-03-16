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
 * The status tells you the results of the test events as they are run.
 */
public enum Status {
    /**
     * The status at any start event, the event type is currently running.
     */
    Started,
    /**
     * The status when a non-test event ends or when a test ends early due
     * to being terminated by the user.
     */
    Terminated,
    /**
     * The test has been ignored, either through using the {@link @Ignore}
     * annotation or through a failed assumption.
     */
    Ignored,
    /**
     * The test passed its tests.
     */
    Passed,
    /**
     * The test failed.
     */
    Failed
}
