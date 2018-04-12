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
package ninja.leaping.configurate.loader;

/**
 * Options for header handling
 */
public enum HeaderMode {
    /**
     * Use the header loaded from an existing file, replacing any header set in the options
     */
    PRESERVE,
    /**
     * ignore any header present in input, and output a header if one has been set in options
     */
    PRESET,
    /**
     * Ignore any header present in input, and do not output any header
     */
    NONE
}
