package test;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;

@ConfigSerializable
public interface BasicConfig {
    String hello();

    void hi(String value);
}
