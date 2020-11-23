/*
 * Configurate
 * Copyright (C) zml and Configurate contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.spongepowered.configurate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.spongepowered.configurate.NodePath.path;

import org.junit.jupiter.api.Test;

public class NodePathTest {

    @Test
    void testPlus() {
        final NodePath one = path("hello");
        final NodePath two = path("world");
        final NodePath added = one.plus(two);

        assertEquals(path("hello", "world"), added);
    }

    @Test
    void testPlusEmpty() {
        final NodePath input = path("one", "two");
        assertEquals(input, input.plus(path()));
    }

    @Test
    void testEmptyPlus() {
        final NodePath input = path("one", "two");
        assertEquals(input, path().plus(input));
    }

    @Test
    void testWithAppendedChild() {
        final NodePath path = path("server", "port");
        assertEquals(path("server", "port", "default"), path.withAppendedChild("default"));
    }

    @Test
    void testWith() {
        final NodePath path = path("server", "port");
        assertEquals(path("client", "port"), path.with(0, "client"));
    }

}
