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

import org.dhaven.jue.api.description.Describable;
import org.dhaven.jue.api.description.Description;

/**
 * The failure information provides the exact {@link Description} where the
 * failure occurred.  The purpose of this class is to provide as much
 * information as possible to pinpoint the cause of the problem.
 */
public class Failure implements Describable {
    private final Description description;
    private final Throwable cause;

    public Failure(Description description, Throwable failure) {
        if (null == description)
            throw new IllegalArgumentException("description must not be null");
        if (null == failure)
            throw new IllegalArgumentException("cause must not be null");
        this.description = description;
        this.cause = failure;
    }

    /**
     * Get the description associated with the failure.  The description
     * includes important information such as which run, and what set of
     * parameters were used for the test.
     *
     * @return the test description
     */
    @Override
    public Description getDescription() {
        return description;
    }

    /**
     * Get the exception thrown during this test.
     *
     * @return the cause of the failure.
     */
    public Throwable getCause() {
        return cause;
    }
}
