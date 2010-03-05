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

package org.dhaven.jue.core;

import org.dhaven.jue.api.Request;
import org.dhaven.jue.api.Results;
import org.dhaven.jue.api.TestEventListener;

/**
 * Created by IntelliJ IDEA.
 * User: berin.loritsch
 * Date: Mar 5, 2010
 * Time: 12:34:40 PM
 * To change this template use File | Settings | File Templates.
 */
public class Engine {
    public static void main(String... arguments) {
        Engine engine = new Engine();
        engine.addTestListener(new CommandLineListener());
        Request request = new Request(arguments);
        Results results = engine.process(request);

        System.exit(results.passed() ? 0 : -1);
    }

    private Results process(Request request) {
        return new Results();
    }

    private void addTestListener(TestEventListener testListener) {
        //To change body of created methods use File | Settings | File Templates.
    }
}
