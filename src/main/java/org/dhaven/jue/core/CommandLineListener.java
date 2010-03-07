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

package org.dhaven.jue.core;

import org.dhaven.jue.api.event.TestEvent;
import org.dhaven.jue.api.event.TestEventListener;

/**
 * Default listener for the command line access to the tool.
 */
public class CommandLineListener implements TestEventListener {
    @SuppressWarnings({"ThrowableResultOfMethodCallIgnored"})
    @Override
    public void handleEvent(TestEvent event) {
        switch (event.getStatus()) {
            case Passed:
            case Ignored:
                System.out.format("%s... %s\n", event.getName(), event.getStatus());
                break;

            case Failed:
                System.out.format("%s... %s\n", event.getName(), event.getStatus());
                event.getFailure().printStackTrace(System.out);
                break;

            default:
                System.out.format("%s (%s)\n", event.getName(), event.getType());
        }
    }
}
