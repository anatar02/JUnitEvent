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

import static org.dhaven.jue.Annotations.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItemInArray;

public class TestAnnotations {
    @Test
    public void testAnnotations() {
        Class[] list = Annotations.get();
        assertThat(list, hasItemInArray((Class) Test.class));
        assertThat(list, hasItemInArray((Class) Ignore.class));
        assertThat(list, hasItemInArray((Class) Before.class));
        assertThat(list, hasItemInArray((Class) After.class));
    }
}
