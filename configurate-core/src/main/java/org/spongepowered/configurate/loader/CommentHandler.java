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
package org.spongepowered.configurate.loader;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Collection;
import java.util.Optional;

/**
 * Interface specifying methods for handling abstract comments
 */
public interface CommentHandler {

    /**
     * Defines the handlers behaviour for reading comments.
     *
     * @param reader The reader
     * @return The comment
     * @throws IOException If any IO error occurs in the process
     */
    Optional<String> extractHeader(BufferedReader reader) throws IOException;

    /**
     * Converts the given lines into a comment
     *
     * @param lines The lines to make a comment
     * @return The transformed lines
     */
    Collection<String> toComment(Collection<String> lines);

}
