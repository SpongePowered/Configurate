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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import org.spongepowered.configurate.util.Strings;

import java.io.IOException;

/**
 * An extension of {@link DefaultPrettyPrinter} which can be customised by loader settings.
 */
class ConfiguratePrettyPrinter extends DefaultPrettyPrinter {

    private static final long serialVersionUID = -3322746834998470769L;
    private final FieldValueSeparatorStyle style;

    ConfiguratePrettyPrinter(final int indent, final FieldValueSeparatorStyle style) {
        if (indent == 0) {
            this._objectIndenter = NopIndenter.instance;
        } else {
            this._objectIndenter = DefaultIndenter.SYSTEM_LINEFEED_INSTANCE.withIndent(Strings.repeat(" ", indent));
        }
        this.style = style;
    }

    @Override
    public void writeObjectFieldValueSeparator(final JsonGenerator jg) throws IOException {
        jg.writeRaw(this.style.value());
    }

}
