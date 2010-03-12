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

package org.dhaven.jue.api;

import java.util.TreeSet;

import static org.dhaven.jue.Annotations.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

public class TestDescription {
    @Test
    public void testDescriptionWithOnlyName() {
        Description description = new Description("Test Name");

        assertThat(description.getName(), equalTo("Test Name"));
        assertThat(description.getRun(), equalTo(1));
        assertThat(description.getTotalRuns(), equalTo(1));
        assertThat(description.getParameters(), equalTo(new Object[0]));
        assertThat(description.toString(), equalTo("Test Name"));
    }

    @Test
    public void testDescriptionWithMultipleRuns() {
        Description description = new Description("ThisTest", 3, 45);

        assertThat(description.getName(), equalTo("ThisTest"));
        assertThat(description.getRun(), equalTo(3));
        assertThat(description.getTotalRuns(), equalTo(45));
        assertThat(description.getParameters(), equalTo(new Object[0]));
        assertThat(description.toString(), equalTo("ThisTest:3-45"));
    }

    @Test
    public void testDescriptionWithParameters() {
        Description description = new Description("ParamTest", 1, 1, "First", 3);

        assertThat(description.getName(), equalTo("ParamTest"));
        assertThat(description.getRun(), equalTo(1));
        assertThat(description.getTotalRuns(), equalTo(1));
        assertThat(description.getParameters(), equalTo(new Object[]{"First", 3}));
        assertThat(description.toString(), equalTo("ParamTest(First,3)"));
    }

    @Test
    public void testDescriptionWithManyRunsAndParameters() {
        Description description = new Description("ParamTest", 4, 5, "First", 3);

        assertThat(description.getName(), equalTo("ParamTest"));
        assertThat(description.getRun(), equalTo(4));
        assertThat(description.getTotalRuns(), equalTo(5));
        assertThat(description.getParameters(), equalTo(new Object[]{"First", 3}));
        assertThat(description.toString(), equalTo("ParamTest:4-5(First,3)"));
    }

    @Test
    public void makeSureEqualityMatchesHashcode() {
        Description one = new Description("Test Name");
        Description two = new Description("Test Name");

        assertThat(one, equalTo(two));
        assertThat(one.hashCode(), equalTo(two.hashCode()));
    }

    @Test
    public void makeSureInequalityDoesNotMatcheHashcode() {
        Description one = new Description("Test Foo");
        Description two = new Description("Test Bar");

        assertThat(one, not(equalTo(two)));
        assertThat(one.hashCode(), not(equalTo(two.hashCode())));
    }

    @Test
    public void inequalityIncludesRunInformation() {
        Description one = new Description("Test Name", 1, 2);
        Description two = new Description("Test Name", 2, 2);

        assertThat(one, not(equalTo(two)));
        assertThat(one.hashCode(), not(equalTo(two.hashCode())));
    }

    @Test
    public void equalityIncludesRunInformation() {
        Description one = new Description("Test Name", 2, 2);
        Description two = new Description("Test Name", 2, 2);

        assertThat(one, equalTo(two));
        assertThat(one.hashCode(), equalTo(two.hashCode()));
    }

    @Test
    public void checkNaturalOrder() {
        TreeSet<Description> set = new TreeSet<Description>();
        set.add(new Description("Test Foo", 2, 2));
        set.add(new Description("Test Foo", 1, 2));
        set.add(new Description("Test Foo", 2, 2)); // should filter out this one
        set.add(new Description("Zed"));
        set.add(new Description("Bravo"));

        assertThat(set.size(), equalTo(4));
        assertThat(set.toArray(), equalTo(new Object[]{
                new Description("Bravo"),
                new Description("Test Foo", 1, 2),
                new Description("Test Foo", 2, 2),
                new Description("Zed")}));
    }

    @Test
    public void descriptionsAreRelatedWhenNamesAreSame() {
        Description one = new Description("Test Name", 1, 2);
        Description two = new Description("Test Name", 2, 2);
        Description three = new Description("Test Name");

        assertThat(one.relatedTo(two), equalTo(true));
        assertThat(two.relatedTo(three), equalTo(true));
        assertThat(one, not(equalTo(two)));
        assertThat(two, not(equalTo(three)));
    }

    @Test
    public void descriptionsAreNotRelatedWhenNamesAreDifferent() {
        Description one = new Description("Test Foo");
        Description two = new Description("Test Bar");

        assertThat(one.relatedTo(two), equalTo(false));
        assertThat(one, not(equalTo(two)));
    }

    @Test
    public void descriptionsAreRelatedWhenDotNotationIsUsed() {
        Description one = new Description("Test");
        Description two = new Description("Test.foo");

        assertThat(one.relatedTo(two), equalTo(true));
        assertThat(two.relatedTo(one), equalTo(true));
    }

    @Test
    public void descriptionsAreRelatedWhenMultiDotNotationIsUsed() {
        Description one = new Description("org.Test");
        Description two = new Description("org.Test.foo");

        assertThat(one.relatedTo(two), equalTo(true));
        assertThat(two.relatedTo(one), equalTo(true));
    }

    @Test
    public void descriptionsAreNotRelatedWhenDotNotationDiffers() {
        Description one = new Description("Test.bar");
        Description two = new Description("Test.foo");

        assertThat(one.relatedTo(two), equalTo(false));
        assertThat(two.relatedTo(one), equalTo(false));
    }

    @Test
    public void descriptionsAreNotRelatedWhenMultiDotNotationDiffers() {
        Description one = new Description("org.Test.bar");
        Description two = new Description("org.Test.foo");

        assertThat(one.relatedTo(two), equalTo(false));
        assertThat(two.relatedTo(one), equalTo(false));
    }
}
