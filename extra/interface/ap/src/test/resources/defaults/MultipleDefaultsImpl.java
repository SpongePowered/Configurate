package defaults;

import java.lang.Override;
import java.lang.String;
import org.spongepowered.configurate.interfaces.meta.defaults.DefaultNumeric;
import org.spongepowered.configurate.interfaces.meta.defaults.DefaultString;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

/**
 * Automatically generated implementation of the config */
@ConfigSerializable
final class MultipleDefaultsImpl implements MultipleDefaults {
    @DefaultNumeric(3)
    private int multipleSingle = 3;

    @DefaultString("Hey!")
    private String multipleOverride = "Hey!"; // this is expected as "Hey!" is handled before "Hello!"

    @Override
    public int multipleSingle() {
        return multipleSingle;
    }

    @Override
    public String multipleOverride() {
        return multipleOverride;
    }

    @Override
    public void multipleOverride(String x) {
        this.multipleOverride = x;
    }
}
