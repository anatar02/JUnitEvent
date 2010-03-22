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
 * Describes a specific instance of a test.  The description includes whether
 * the description belongs to a test, a test case, or the framework.  Finally,
 * the description includes any additional information that pertains to its
 * associated test.  The full list of information you can find in the
 * description is:
 * <p/>
 * <ul>
 * <li>The name of the test</li>
 * <li>The the type of description</li>
 * <li>The run number for the test</li>
 * <li>The total number of runs for a test</li>
 * <li>The set of parameters associated with a test</li>
 * </ul>
 */
public class Description implements Comparable<Description> {
    /**
     * The root description for the all the tests and test cases.
     */
    public static final Description JUEName = new Description("JUE: Version 0.5", Type.System);

    private final String name;
    private final Type type;
    private final int run;
    private final int ofRuns;
    private final Object[] parameters;

    /**
     * Create a description with just a name and type.  By default, there is one
     * run out of one runs for this description.
     *
     * @param name the name of the test
     * @param type the event class of the description
     */
    public Description(String name, Type type) {
        this(name, type, 1, 1);
    }

    /**
     * Create a description with a name, type, and run information.
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
     * Create a description with name, type, run info, and parameters.
     *
     * @param name       the name of the test
     * @param type       the event class of the description
     * @param run        the number of the run
     * @param ofRuns     the total number of runs expected
     * @param parameters the number of parameters
     */
    public Description(String name, Type type, int run, int ofRuns, Object... parameters) {
        if (null == name)
            throw new IllegalArgumentException("Description name is required");
        if (null == type)
            throw new IllegalArgumentException("Description type is required");

        this.name = name;
        this.run = run;
        this.ofRuns = ofRuns;
        this.parameters = parameters;
        this.type = type;
    }

    /**
     * Get the name of the test.  The name is used to relate tests with each
     * other, as well as for reporting purposes.
     *
     * @return the name of the test
     */
    public String getName() {
        return name;
    }

    /**
     * Get the type of description.  The type information is used when
     * determining if two descriptions are related to each other.
     *
     * @return the description type
     */
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
     * Relationship is determined by only considering the type and name
     * information between two descriptions.  This can be useful when rolling
     * data up for several runs of a test.  Two descriptions are related when:
     * <p/>
     * <ul>
     * <li>either description is the root description {@link Description#JUEName}</li>
     * <li>one description is a test case and the other description starts with
     * the test case name.  (e.g. "org.TestCase.myTest" starts with "org.TestCase").</li>
     * <li>both test names are the same</li>
     * </li>
     *
     * @param other the other description object
     * @return <code>true</code> if the descriptions are related
     */
    public boolean relatedTo(Description other) {
        if (type == Type.System || other.getType() == Type.System) {
            return true;
        }

        if (type == Type.Test && other.getType() == Type.Test) {
            return name.equals(other.name);
        }

        Description testCase = (type == Type.TestCase) ? this : other;
        Description test = (type == Type.TestCase) ? other : this;

        return test.getName().startsWith(testCase.getName());
    }

    @Override
    public boolean equals(Object foreign) {
        boolean isEqual = foreign instanceof Description;

        if (isEqual) {
            Description other = Description.class.cast(foreign);
            isEqual = name.equals(other.name);
            isEqual &= type == other.type;
            isEqual &= this.run == other.run;
        }

        return isEqual;
    }

    @Override
    public int hashCode() {
        return (name.hashCode() << 3 + run) << 5 + type.ordinal();
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
        int direction = type.compareTo(other.type);

        if (direction == 0) {
            direction = name.compareTo(other.name);
        }

        if (direction == 0) {
            direction = run - other.run;
        }

        return direction;
    }
}
