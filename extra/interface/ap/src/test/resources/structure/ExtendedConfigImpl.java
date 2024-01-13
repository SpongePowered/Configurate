package structure;

import java.lang.Number;
import java.lang.Override;
import java.lang.String;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

/**
 * Automatically generated implementation of the config */
@ConfigSerializable
final class ExtendedConfigImpl implements ExtendedConfig {
    private String hi;

    private int hey;

    private String hello;

    @Override
    public String hi() {
        return hi;
    }

    @Override
    public Number hey(int value) {
        this.hey = value;
        return this.hey;
    }

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
