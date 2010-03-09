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

import java.util.LinkedList;
import java.util.List;

/**
 * Contains the logic necessary for blocking/ordering the test nodes.  Will also
 * handle archiving and restoring the ThreadContext.
 */
public abstract class AbstractTestNode implements TestNode {
    private List<AbstractTestNode> before = new LinkedList<AbstractTestNode>();
    private AbstractTestNode after;

    public AbstractTestNode(List<AbstractTestNode> befores, AbstractTestNode after) {
        before.addAll(befores);
        this.after = after;
    }

    @Override
    public int compareTo(TestNode node) {
        if (before.contains(node)) return -1;
        if (node.equals(after)) return 1;
        return 0;
    }
}
