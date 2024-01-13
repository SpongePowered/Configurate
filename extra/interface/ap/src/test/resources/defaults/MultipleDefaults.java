package defaults;

import org.spongepowered.configurate.interfaces.meta.defaults.DefaultBoolean;
import org.spongepowered.configurate.interfaces.meta.defaults.DefaultDecimal;
import org.spongepowered.configurate.interfaces.meta.defaults.DefaultNumeric;
import org.spongepowered.configurate.interfaces.meta.defaults.DefaultString;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

@ConfigSerializable
public interface MultipleDefaults {
    @DefaultBoolean(true)
    @DefaultDecimal(2)
    @DefaultNumeric(3)
    @DefaultString("Hi!")
    int multipleSingle();

    @DefaultString("Hey!")
    String multipleOverride();

    @DefaultString("Hello!")
    void multipleOverride(String x);
}
