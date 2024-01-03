package test.defaults;

import java.lang.Override;
import java.lang.String;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

/**
 * Automatically generated implementation of the config */
@ConfigSerializable
final class MultipleDefaultsImpl implements MultipleDefaults {
    private int multipleSingle = 3;

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
