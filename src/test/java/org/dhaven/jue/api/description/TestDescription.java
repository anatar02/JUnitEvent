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

import java.util.TreeSet;

import org.dhaven.jue.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

public class TestDescription {
    @Test(expected = IllegalArgumentException.class)
    public void nameRequired() {
        new Description(null, Type.System);
    }

    @Test(expected = IllegalArgumentException.class)
    public void typeRequired() {
        new Description("name", null);
    }

    @Test
    public void testDescriptionWithOnlyName() {
        Description description = new Description("Test Name", Type.Test);

        assertThat(description.getName(), equalTo("Test Name"));
        assertThat(description.getRun(), equalTo(1));
        assertThat(description.getTotalRuns(), equalTo(1));
        assertThat(description.getParameters(), equalTo(new Object[0]));
        assertThat(description.toString(), equalTo("Test Name"));
    }

    @Test
    public void testDescriptionWithMultipleRuns() {
        Description description = new Description("ThisTest", Type.Test, 3, 45);

        assertThat(description.getName(), equalTo("ThisTest"));
        assertThat(description.getRun(), equalTo(3));
        assertThat(description.getTotalRuns(), equalTo(45));
        assertThat(description.getParameters(), equalTo(new Object[0]));
        assertThat(description.toString(), equalTo("ThisTest:3-45"));
    }

    @Test
    public void testDescriptionWithParameters() {
        Description description = new Description("ParameterTest", Type.Test, 1, 1, "First", 3);

        assertThat(description.getName(), equalTo("ParameterTest"));
        assertThat(description.getRun(), equalTo(1));
        assertThat(description.getTotalRuns(), equalTo(1));
        assertThat(description.getParameters(), equalTo(new Object[]{"First", 3}));
        assertThat(description.toString(), equalTo("ParameterTest(First,3)"));
    }

    @Test
    public void testDescriptionWithManyRunsAndParameters() {
        Description description = new Description("ParameterTest", Type.Test, 4, 5, "First", 3);

        assertThat(description.getName(), equalTo("ParameterTest"));
        assertThat(description.getRun(), equalTo(4));
        assertThat(description.getTotalRuns(), equalTo(5));
        assertThat(description.getParameters(), equalTo(new Object[]{"First", 3}));
        assertThat(description.toString(), equalTo("ParameterTest:4-5(First,3)"));
    }

    @Test
    public void ensureNotEqualWithOtherTypeOfObject() {
        Description one = new Description("test", Type.Test);
        Object two = "test";

        assertThat(one, not(equalTo(two)));
        assertThat(two, not(equalTo((Object) one)));
    }

    @Test
    public void makeSureEqualityMatchesHashCode() {
        Description one = new Description("Test Name", Type.Test);
        Description two = new Description("Test Name", Type.Test);

        assertThat(one, equalTo(two));
        assertThat(one.hashCode(), equalTo(two.hashCode()));
    }

    @Test
    public void makeSureInequalityDoesNotMatchHashCode() {
        Description one = new Description("Test Foo", Type.Test);
        Description two = new Description("Test Bar", Type.Test);

        assertThat(one, not(equalTo(two)));
        assertThat(one.hashCode(), not(equalTo(two.hashCode())));
    }

    @Test
    public void inequalityIncludesRunInformation() {
        Description one = new Description("Test Name", Type.Test, 1, 2);
        Description two = new Description("Test Name", Type.Test, 2, 2);

        assertThat(one, not(equalTo(two)));
        assertThat(one.hashCode(), not(equalTo(two.hashCode())));
    }

    @Test
    public void equalityIncludesRunInformation() {
        Description one = new Description("Test Name", Type.Test, 2, 2);
        Description two = new Description("Test Name", Type.Test, 2, 2);

        assertThat(one, equalTo(two));
        assertThat(one.hashCode(), equalTo(two.hashCode()));
    }

    @Test
    public void inequalityIncludesTypeInformation() {
        Description one = new Description("Test Name", Type.Test);
        Description two = new Description("Test Name", Type.TestCase);

        assertThat(one, not(equalTo(two)));
        assertThat(one.hashCode(), not(equalTo(two.hashCode())));
    }

    @Test
    public void equalityIncludesTypeInformation() {
        Description one = new Description("Test Name", Type.TestCase);
        Description two = new Description("Test Name", Type.TestCase);

        assertThat(one, equalTo(two));
        assertThat(one.hashCode(), equalTo(two.hashCode()));
    }

    @Test
    public void checkNaturalOrder() {
        TreeSet<Description> set = new TreeSet<Description>();
        set.add(new Description("Test Foo", Type.Test, 2, 2));
        set.add(new Description("Test Foo", Type.Test, 1, 2));
        set.add(new Description("Test Foo", Type.Test, 2, 2)); // should filter out this one
        set.add(new Description("Zed", Type.Test));
        set.add(new Description("Zed", Type.TestCase));
        set.add(new Description("Bravo", Type.Test));

        assertThat(set.size(), equalTo(5));
        assertThat(set.toArray(), equalTo(new Object[]{
                new Description("Zed", Type.TestCase),
                new Description("Bravo", Type.Test),
                new Description("Test Foo", Type.Test, 1, 2),
                new Description("Test Foo", Type.Test, 2, 2),
                new Description("Zed", Type.Test)}));
    }
}
