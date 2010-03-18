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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.dhaven.jue.Test;
import org.dhaven.jue.api.description.Description;
import org.dhaven.jue.core.TestListenerSupport;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class TestDependencyTestNode {
    @Test
    public void addingPredecessorHasReciprocalEffect() {
        DependencyTestNode one = createNode(1);
        DependencyTestNode two = createNode(2);

        two.addPredecessor(one);

        assertThat(one.isPredecessorOf(two), equalTo(true));
        assertThat(two.isSuccessorOf(one), equalTo(true));
    }

    @Test
    public void addingSuccessorHasReciprocalEffect() {
        DependencyTestNode one = createNode(1);
        DependencyTestNode two = createNode(2);

        one.addSuccessor(two);

        assertThat(one.isPredecessorOf(two), equalTo(true));
        assertThat(two.isSuccessorOf(one), equalTo(true));
    }

    @Test
    public void EnsureSortOrder() {
        DependencyTestNode two = createNode(2);
        DependencyTestNode three = createNode(3);
        DependencyTestNode one = createNode(1);

        one.addSuccessor(two);
        three.addPredecessor(one);
        three.addPredecessor(two);

        List<DependencyTestNode> list = Arrays.asList(two, three, one);
        Collections.sort(list);

        assertThat(list.get(0), equalTo(one));
        assertThat(list.get(1), equalTo(two));
        assertThat(list.get(2), equalTo(three));
    }

    @Test(expected = IllegalArgumentException.class)
    public void cannotAddSelfToPredecessor() {
        DependencyTestNode node = new TestNode(1);

        node.addPredecessor(node);
    }

    @Test(expected = IllegalArgumentException.class)
    public void cannotAddSelfToSuccessor() {
        DependencyTestNode node = createNode(1);

        node.addSuccessor(node);
    }

    protected DependencyTestNode createNode(int i) {
        return new TestNode(i);
    }

    private final static class TestNode extends DependencyTestNode {
        int num;

        public TestNode(int i) {
            num = i;
        }

        @Override
        protected void run(TestListenerSupport support) {
            //unneeded for tests
        }

        @Override
        public Description getDescription() {
            return null;// unneeded for tests
        }

        @Override
        public String toString() {
            return "DependencyTestNode: " + num;
        }
    }
}
