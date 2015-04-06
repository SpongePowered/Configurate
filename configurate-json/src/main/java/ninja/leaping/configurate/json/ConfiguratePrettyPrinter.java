package ninja.leaping.configurate.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.google.common.base.Strings;

import java.io.IOException;

class ConfiguratePrettyPrinter extends DefaultPrettyPrinter {

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
