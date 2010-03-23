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

import org.dhaven.jue.api.description.Description;
import org.dhaven.jue.api.event.Status;
import org.dhaven.jue.api.event.TestEvent;
import org.dhaven.jue.core.TestListenerSupport;

/**
 * An EventNode simply sends an event when it is time.
 */
public class EventNode extends DependencyTestNode {
    private final Description description;
    private final Status status;
    private final Throwable failure;

    public EventNode(Description description, Status status) {
        this(description, status, null);
    }

    public EventNode(Description description, Status status, Throwable failure) {
        this.description = description;
        this.status = status;
        this.failure = failure;
    }

    @Override
    protected void run(TestListenerSupport support) {
        support.fireTestEvent(new TestEvent(getDescription(), status, failure));
    }

    @Override
    public Description getDescription() {
        return this.description;
    }
}
