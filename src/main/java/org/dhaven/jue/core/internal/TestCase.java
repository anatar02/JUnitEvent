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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.dhaven.jue.api.description.Describable;
import org.dhaven.jue.api.description.Description;
import org.dhaven.jue.core.internal.node.TestNode;

/**
 * Represent a test case with its set of tests built up inside.
 */
public class TestCase implements Describable, Iterable<TestNode> {
    private final Description description;
    private final List<TestNode> tests = new LinkedList<TestNode>();

    public TestCase(Description description) {
        this.description = description;
    }

    @Override
    public Description getDescription() {
        return description;
    }

    public void addTest(TestNode test) {
        tests.add(test);
    }

    public void removeTest(TestNode test) {
        tests.remove(test);
    }

    @Override
    public Iterator<TestNode> iterator() {
        return tests.iterator();
    }

    public int size() {
        return tests.size();
    }

    public boolean isEmpty() {
        return tests.isEmpty();
    }
}
