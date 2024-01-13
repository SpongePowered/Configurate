package structure;

import java.lang.Override;
import java.lang.String;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

/**
 * Automatically generated implementation of the config */
@ConfigSerializable
final class BasicConfigImpl implements BasicConfig {
    private String hello;

    private String hi;

    @Override
    public String hello() {
        return hello;
    }

    @Override
    public void hi(String value) {
        this.hi = value;
    }

    @Override
    public String hello(String value) {
        this.hello = value;
        return this.hello;
    }
}
