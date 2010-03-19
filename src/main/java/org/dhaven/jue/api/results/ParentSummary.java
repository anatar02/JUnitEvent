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

package org.dhaven.jue.api.results;

/**
 * Common interface for composite summaries such as the TestCaseSummary.
 * Provides an easy to extend mechanism for different summary collection
 * strategies.
 */
public interface ParentSummary extends Summary {
    /**
     * Add a child summary to this one.  Adding a child may change the status
     * associated with the parent summary.
     *
     * @param child the child to add
     */
    void addChild(Summary child);

    /**
     * Get the set of children for this parent summary.
     *
     * @return the children
     */
    Iterable<Summary> getChildren();

    /**
     * The number of children associated with this summary.  This number only
     * reflects the number of direct children, and does not traverse a
     * hierarchy of summaries.
     *
     * @return the number of direct children
     */
    int size();
}
