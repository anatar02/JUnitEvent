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

package org.dhaven.jue.core.internal.runner;

import org.dhaven.jue.Test;
import org.dhaven.jue.TestSemantics;
import org.dhaven.jue.api.Request;
import org.dhaven.jue.api.event.Status;
import org.dhaven.jue.api.results.Results;
import org.dhaven.jue.core.Engine;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class TestRunners {
    private Results runTestSemanticsWith(TestRunner runner, Class<?> testClass) throws Exception {
        Engine engine = new Engine();
        engine.setTestRunner(runner);
        Request request = new Request(testClass);

        return engine.process(request);
    }

    @Test
    public void runSemanticsWithSequentialRunner() throws Exception {
        assertThat(runTestSemanticsWith(new TestSequential(), TestSemantics.class).getStatus(),
                equalTo(Status.Passed));
    }

    @Test
    public void runSemanticsWithThreadPoolRunner() throws Exception {
        assertThat(runTestSemanticsWith(new TestThreadPool(), TestSemantics.class).getStatus(),
                equalTo(Status.Passed));
    }

    @Test
    public void runSemanticsWithForkJoinRunner() throws Exception {
        assertThat(runTestSemanticsWith(new TestForkJoinPool(), TestSemantics.class).getStatus(),
                equalTo(Status.Passed));
    }

    @Test
    public void runEmptyTestWithSequentialRunner() throws Exception {
        assertThat(runTestSemanticsWith(new TestSequential(), TestSemantics.EmptyTest.class).getStatus(),
                equalTo(Status.Failed));
    }

    @Test
    public void runEmptyTestWithThreadPoolRunner() throws Exception {
        assertThat(runTestSemanticsWith(new TestThreadPool(), TestSemantics.EmptyTest.class).getStatus(),
                equalTo(Status.Failed));
    }

    @Test
    public void runEmptyTestWithForkJoinRunner() throws Exception {
        assertThat(runTestSemanticsWith(new TestForkJoinPool(), TestSemantics.EmptyTest.class).getStatus(),
                equalTo(Status.Failed));
    }

    @Test(expected = IllegalArgumentException.class)
    public void assertThatRunnerMustExist() throws Exception {
        runTestSemanticsWith(null, TestSemantics.class);
    }
}
