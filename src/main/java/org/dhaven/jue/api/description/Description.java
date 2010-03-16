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

package org.dhaven.jue.api.description;

/**
 * Describes a specific instance of a test.  If the test was run multiple times,
 * it includes the run number.  If the test takes parameters, the parameters
 * are provided.
 */
public class Description implements Comparable<Description> {
    private static final String PREFIX = "JUE: ";
    public static final Description JUEName = new Description(PREFIX + "Version 0.5", Type.System);
    private final String name;
    private final int run;
    private final int ofRuns;
    private final Object[] parameters;
    private final Type type;

    /**
     * Create a description with just a name.  By default, there is one run
     * out of one runs for this description.
     *
     * @param name the name of the test
     * @param type the event class of the description
     */
    public Description(String name, Type type) {
        this(name, type, 1, 1);
    }

    /**
     * Create a description with a name and run information.
     *
     * @param name   the name of the test
     * @param type   the event class of the description
     * @param run    the number of the run
     * @param ofRuns the total number of runs expected
     */
    public Description(String name, Type type, int run, int ofRuns) {
        this(name, type, run, ofRuns, new Object[0]);
    }

    /**
     * Create a description with name, run info, and parameters.
     *
     * @param name       the name of the test
     * @param type       the event class of the description
     * @param run        the number of the run
     * @param ofRuns     the total number of runs expected
     * @param parameters the number of parameters
     */
    public Description(String name, Type type, int run, int ofRuns, Object... parameters) {
        this.name = name;
        this.run = run;
        this.ofRuns = ofRuns;
        this.parameters = parameters;
        this.type = type;
    }

    /**
     * The name of the test.  The name is the only bit of information needed to
     * relate descriptions together.
     *
     * @return the name of the test
     */
    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }

    /**
     * The current run that this description relates to.
     *
     * @return the run number
     */
    public int getRun() {
        return run;
    }

    /**
     * The total number of runs expected for this test.
     *
     * @return the total number of runs
     */
    public int getTotalRuns() {
        return ofRuns;
    }

    /**
     * The set of parameters assigned to the test.
     *
     * @return the parameter set
     */
    public Object[] getParameters() {
        return parameters;
    }

    /**
     * Determine whether this description is related to another description.
     * Equality and relationship are different in that equality considers the
     * run number in addition to the name.  All the runs that correspond to the
     * same name are related to each other.
     *
     * @param other the other description object
     * @return <code>true</code> if the descriptions are related
     */
    public boolean relatedTo(Description other) {
        if (type == Type.Test && other.getType() == Type.Test) {
            return name.equals(other.name);
        }

        String thisBase = extractBase(name);
        String otherBase = extractBase(other.name);

        boolean compareTestCase = thisBase.equals(name) || otherBase.equals(other.name);
        return compareTestCase ? thisBase.equals(otherBase) : name.equals(other.name);
    }

    private String extractBase(String name) {
        int index = name.lastIndexOf('.');
        if (index < 0) return name;

        String firstLetter = name.substring(index + 1, index + 2);
        if (firstLetter.equals(firstLetter.toUpperCase())) return name;

        return name.substring(0, index);
    }

    @Override
    public boolean equals(Object foreign) {
        boolean isEqual = foreign instanceof Description;

        if (isEqual) {
            Description other = Description.class.cast(foreign);
            isEqual = name.equals(other.name);
            isEqual &= this.run == other.run;
        }

        return isEqual;
    }

    @Override
    public int hashCode() {
        return name.hashCode() << 3 + run;
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

    @Override
    public int compareTo(Description other) {
        int direction = name.compareTo(other.name);

        if (direction == 0) {
            direction = run - other.run;
        }

        return direction;
    }
}
