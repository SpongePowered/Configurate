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
package org.spongepowered.configurate.jackson;

/**
 * Enumeration of field value separator styles.
 */
public enum FieldValueSeparatorStyle {

    /**
     * Style which uses spaces either side of the <code>:</code> character.
     */
    SPACE_BOTH_SIDES(" : "),

    /**
     * Style which uses a space after the <code>:</code> character.
     */
    SPACE_AFTER(": "),

    /**
     * Style which uses no spaces.
     */
    NO_SPACE(":");

    private final String decorationType;

    FieldValueSeparatorStyle(final String decorationType) {
        this.decorationType = decorationType;
    }

    /**
     * Get the literal separator for this type.
     *
     * @return literal separator value
     */
    public String getValue() {
        return this.decorationType;
    }

}
