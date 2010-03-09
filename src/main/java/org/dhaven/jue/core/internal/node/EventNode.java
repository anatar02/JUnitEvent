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

import org.dhaven.jue.api.Description;
import org.dhaven.jue.api.event.EventType;
import org.dhaven.jue.api.event.Status;
import org.dhaven.jue.api.event.TestEvent;
import org.dhaven.jue.core.TestEventListenerSupport;

/**
 * An EventNode simply sends an event when it is time.
 */
public class EventNode extends DependencyTestNode {
    private Description description;
    private EventType type;
    private Status status;

    public EventNode(Description description, EventType type, Status status) {
        this.description = description;
        this.type = type;
        this.status = status;
    }

    @Override
    protected void run(TestEventListenerSupport support) {
        support.fireTestEvent(new TestEvent(getDescription(), type, status));
    }

    @Override
    public Description getDescription() {
        return this.description;
    }
}
