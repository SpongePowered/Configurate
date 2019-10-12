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
package ninja.leaping.configurate.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.google.common.base.Strings;

import java.io.IOException;

/**
 * An extension of {@link DefaultPrettyPrinter} which can be customised by loader settings.
 */
class ConfiguratePrettyPrinter extends DefaultPrettyPrinter {
    private static final long serialVersionUID = -3322746834998470769L;
    private final FieldValueSeparatorStyle style;

    public ConfiguratePrettyPrinter(int indent, FieldValueSeparatorStyle style) {
        this._objectIndenter = indent == 0 ? NopIndenter.instance : DefaultIndenter.SYSTEM_LINEFEED_INSTANCE.withIndent(Strings.repeat(" ", indent));
        this.style = style;
    }

    @Override
    public void writeObjectFieldValueSeparator(JsonGenerator jg) throws IOException {
        jg.writeRaw(style.getValue());
    }
}
