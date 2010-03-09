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

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.dhaven.jue.core.TestEventListenerSupport;

/**
 * Contains the logic necessary for blocking/ordering the test nodes.  Will also
 * handle archiving and restoring the ThreadContext.
 */
public abstract class DependencyTestNode implements TestNode {
    private final Set<TestNode> predecessors = new CopyOnWriteArraySet<TestNode>();
    private final Set<TestNode> successors = new CopyOnWriteArraySet<TestNode>();

    public void signalComplete(TestNode node) {
        predecessors.remove(node);
    }

    public void addPredecessor(TestNode predecessor) {
        if (this.equals(predecessor))
            throw new IllegalArgumentException("Cannot add self to predecessor");

        if (!predecessors.contains(predecessor)) {
            predecessors.add(predecessor);
            predecessor.addSuccessor(this);
        }
    }

    public void addSuccessor(TestNode successor) {
        if (this.equals(successor))
            throw new IllegalArgumentException("Cannot add self to predecessor");

        if (!successors.contains(successor)) {
            successors.add(successor);
            successor.addPredecessor(this);
        }
    }

    @Override
    public boolean attemptRun(TestEventListenerSupport support) {
        if (!predecessors.isEmpty()) {
            return false;
        }

        run(support);

        for (TestNode successor : successors) {
            successor.signalComplete(this);
        }

        successors.clear();

        return true;
    }

    protected abstract void run(TestEventListenerSupport support);

    @Override
    public int compareTo(TestNode node) {
        if (predecessors.contains(node)) return 1;
        if (successors.contains(node)) return -1;
        return 0;
    }

    boolean isPredecessorOf(DependencyTestNode node) {
        return successors.contains(node);
    }

    boolean isSuccessorOf(DependencyTestNode node) {
        return predecessors.contains(node);
    }
}
