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

/**
 * Describes a specific instance of a test.  If the test was run multiple times,
 * it includes the run number.  If the test takes parameters, the parameters
 * are provided.
 */
public class Description {
    private final String name;
    private final int run;
    private final int ofRuns;
    private final Object[] parameters;

    public Description(String name) {
        this(name, 1, 1);
    }

    public Description(String name, int run, int ofRuns) {
        this(name, run, ofRuns, new Object[0]);
    }

    public Description(String name, int run, int ofRuns, Object... parameters) {
        this.name = name;
        this.run = run;
        this.ofRuns = ofRuns;
        this.parameters = parameters;
    }

    public String getName() {
        return name;
    }

    public int getRun() {
        return run;
    }

    public int getTotalRuns() {
        return ofRuns;
    }

    public Object[] getParameters() {
        return parameters;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(name);

        if (ofRuns > 1) {
            builder.append(":").append(run).append("-").append(ofRuns);
        }

        if (parameters.length > 0) {
            builder.append('(');
            boolean first = true;
            for (Object parameter : parameters) {
                if (first) first = false;
                else builder.append(',');

                builder.append(parameter);
            }
            builder.append(')');
        }

        return builder.toString();
    }
}
