package test;

import java.lang.Override;
import java.lang.String;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

/**
 * Automatically generated implementation of the config */
@ConfigSerializable
final class ExtendedConfigImpl implements ExtendedConfig {
    private String hi;

    private String hello;

    @Override
    public String hi() {
        return hi;
    }

    @Override
    public String hello() {
        return hello;
    }

    @Override
    public void hi(String value) {
        this.hi = value;
    }
}
