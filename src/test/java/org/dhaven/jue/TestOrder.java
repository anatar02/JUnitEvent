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

package org.dhaven.jue;

import org.dhaven.jue.api.Request;
import org.dhaven.jue.api.Results;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * Make sure the tests are run in order
 */
public class TestOrder {
    @Test
    public void checkOrder() {
        Request testRequest = new Request(InternalTest.class);
        Results results = testRequest.execute();
        assertThat(results.passed(), equalTo(true));
    }

    class InternalTest {
        boolean callFirstCalled = false;
        boolean actualTestCalled = false;
        boolean callLastCalled = false;

        @Before
        public void callFirst() {
            assertThat(callFirstCalled, equalTo(false));
            assertThat(actualTestCalled, equalTo(false));
            assertThat(callLastCalled, equalTo(false));
            callFirstCalled = true;
        }

        @Test
        public void actualTest() {
            assertThat(callFirstCalled, equalTo(true));
            assertThat(actualTestCalled, equalTo(false));
            assertThat(callLastCalled, equalTo(false));
            actualTestCalled = true;
        }

        @After
        public void callLast() {
            assertThat(callFirstCalled, equalTo(true));
            assertThat(actualTestCalled, equalTo(true));
            assertThat(callLastCalled, equalTo(false));
            callLastCalled = true;
        }
    }
}
