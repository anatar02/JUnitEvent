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

package org.dhaven.jue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.dhaven.jue.api.description.Type;
import org.dhaven.jue.api.event.TestEvent;
import org.dhaven.jue.api.event.TestListener;

/**
 * Helper listener to extract the order of events.
 */
public class ListenerTester implements TestListener {
    private final List<TestEvent> events = new ArrayList<TestEvent>(Type.values().length);

    @Override
    public void handleEvent(TestEvent event) {
        events.add(event);
    }

    public Type[] getEventTypeOrder() {
        List<TestEvent> sortedOrder = getEvents();

        Type[] eventOrder = new Type[sortedOrder.size()];
        for (int i = 0; i < eventOrder.length; i++) {
            eventOrder[i] = sortedOrder.get(i).getType();
        }

        return eventOrder;
    }

    public List<TestEvent> getEvents() {
        Collections.sort(events, new Comparator<TestEvent>() {
            @Override
            public int compare(TestEvent o1, TestEvent o2) {
                return (int) (o1.getNanoseconds()
                        - o2.getNanoseconds());
            }
        });

        return events;
    }
}
