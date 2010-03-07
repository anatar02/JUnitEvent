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

import org.dhaven.jue.api.event.EventType;
import org.dhaven.jue.api.event.Status;
import org.dhaven.jue.core.TestEventListenerSupport;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: berin
 * Date: Mar 6, 2010
 * Time: 10:05:45 PM
 * To change this template use File | Settings | File Templates.
 */
public class TestCase implements TestNode {
    private final Class<?> testCase;
    private final List<TestNode> children = new LinkedList<TestNode>();

    public TestCase(Class<?> test) {
        testCase = test;
    }

    void addChild(TestNode child) {
        children.add(child);
    }

    @Override
    public void run(TestEventListenerSupport support) throws Exception {
        support.fireTestEvent(getName(), EventType.StartTestCase, Status.Running);
        try {
            for (TestNode node : children) {
                node.run(support);
            }
        } finally {
            support.fireTestEvent(getName(), EventType.EndTestCase, Status.Terminated);
        }
    }

    @Override
    public String getName() {
        return testCase.getName();
    }
}
