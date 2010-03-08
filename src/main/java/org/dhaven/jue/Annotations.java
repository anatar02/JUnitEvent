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

import java.lang.annotation.*;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Container class with all the annotations in it.  Allows for very simple
 * enumeration of what get are in use.
 */
public class Annotations {
    /**
     * Get the list of core annotations.
     *
     * @return The list of annotations.
     */
    @SuppressWarnings("unchecked")
    public static Class<? extends Annotation>[] get() {
        Class<?>[] innerClasses = Annotations.class.getDeclaredClasses();
        return (Class<? extends Annotation>[]) innerClasses;
    }

    /**
     * Designate a method to be used for setup.
     */
    @Retention(RUNTIME)
    @Target(METHOD)
    @Documented
    @Inherited
    public static @interface Before {
    }

    /**
     * Designate a method to be used for cleanup.
     */
    @Retention(RUNTIME)
    @Target(METHOD)
    @Documented
    @Inherited
    public static @interface After {
    }

    /**
     * Mark a method as a test.
     */
    @Retention(RUNTIME)
    @Target(METHOD)
    @Documented
    @Inherited
    public static @interface Test {
    }

    /**
     * Mark a test as ignored.
     */
    @Retention(RUNTIME)
    @Target(METHOD)
    @Documented
    @Inherited
    public static @interface Ignore {
    }
}
