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

import org.dhaven.jue.api.event.EventType;
import org.dhaven.jue.api.event.Status;
import org.dhaven.jue.api.event.TestEvent;
import org.dhaven.jue.api.event.TestEventListener;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: berin.loritsch
 * Date: Mar 5, 2010
 * Time: 12:59:35 PM
 * To change this template use File | Settings | File Templates.
 */
public class Results implements TestEventListener {
    Map<String, TestEvent> collectedResults = new HashMap<String, TestEvent>();

    public boolean complete() {
        boolean completed = true;

        for (TestEvent event : collectedResults.values()) {
            completed = completed && (event.getStatus() != Status.Running);
        }

        return completed;
    }

    public boolean passed() {
        boolean passed = true;

        for (TestEvent event : collectedResults.values()) {
            passed = passed && (event.getStatus() == Status.Passed);
        }

        return passed;
    }

    public String failuresToString() {
        StringBuilder builder = new StringBuilder();

        for (TestEvent event : collectedResults.values()) {
            if (event.getStatus() == Status.Failed) {
                builder.append(event.getName());
                builder.append("... Failed\n");

                StringWriter writer = new StringWriter();
                event.getFailure().printStackTrace(new PrintWriter(writer));
                builder.append(writer.toString());
            }
        }

        return builder.toString();
    }

    @Override
    public void handleEvent(TestEvent event) {
        if (event.getType() == EventType.EndTest) {
            collectedResults.put(event.getName(), event);
        }
    }
}
