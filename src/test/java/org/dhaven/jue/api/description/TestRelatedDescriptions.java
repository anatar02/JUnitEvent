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

import org.dhaven.jue.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

@SuppressWarnings({"WeakerAccess"})
public class TestRelatedDescriptions {
    @Test
    public void descriptionsAreRelatedWhenNamesAndTypesAreSame() {
        Description one = new Description("Test Name", Type.Test, 1, 2);
        Description two = new Description("Test Name", Type.Test, 2, 2);
        Description three = new Description("Test Name", Type.Test);

        assertThat(one.relatedTo(two), equalTo(true));
        assertThat(two.relatedTo(three), equalTo(true));
        assertThat(one, not(equalTo(two)));
        assertThat(two, not(equalTo(three)));
    }

    @Test
    public void descriptionsAreNotRelatedWhenNamesAreDifferent() {
        Description one = new Description("Test Foo", Type.Test);
        Description two = new Description("Test Bar", Type.Test);

        assertThat(one.relatedTo(two), equalTo(false));
        assertThat(one, not(equalTo(two)));
    }

    @Test
    public void descriptionsAreRelatedWhenTestCaseIsRootOfTest() {
        Description one = new Description("Test", Type.TestCase);
        Description two = new Description("Test.foo", Type.Test);

        assertThat(one.relatedTo(two), equalTo(true));
        assertThat(two.relatedTo(one), equalTo(true));
    }

    @Test
    public void descriptionsAreRelatedWhenMultipleDotNotationIsUsed() {
        Description one = new Description("org.Test", Type.TestCase);
        Description two = new Description("org.Test.foo", Type.Test);

        assertThat(one.relatedTo(two), equalTo(true));
        assertThat(two.relatedTo(one), equalTo(true));
    }

    @Test
    public void descriptionsAreNotRelatedWhenMultipleDotNotationIsUsedAndBothAreTests() {
        Description one = new Description("org.Test", Type.Test);
        Description two = new Description("org.Test.foo", Type.Test);

        assertThat(one.relatedTo(two), equalTo(false));
        assertThat(two.relatedTo(one), equalTo(false));
    }

    @Test
    public void descriptionsAreNotRelatedWhenDotNotationDiffers() {
        Description one = new Description("Test.bar", Type.Test);
        Description two = new Description("Test.foo", Type.Test);

        assertThat(one.relatedTo(two), equalTo(false));
        assertThat(two.relatedTo(one), equalTo(false));
    }

    @Test
    public void descriptionsAreNotRelatedWhenMultipleDotNotationDiffers() {
        Description one = new Description("org.Test.bar", Type.Test);
        Description two = new Description("org.Test.foo", Type.Test);

        assertThat(one.relatedTo(two), equalTo(false));
        assertThat(two.relatedTo(one), equalTo(false));
    }

    @Test
    public void systemDescriptionRelatedToAllTypes() {
        Description one = Description.JUEName;
        Description two = new Description("com.mycom.TestCase", Type.TestCase);
        Description three = new Description("org.yourorg.Test", Type.Test);

        assertThat(one.relatedTo(two), equalTo(true));
        assertThat(one.relatedTo(three), equalTo(true));
        assertThat(two.relatedTo(one), equalTo(true));
        assertThat(three.relatedTo(one), equalTo(true));
    }
}
