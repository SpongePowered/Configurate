package structure;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;

@ConfigSerializable
public interface BasicConfig {
    String hello();

    void hi(String value);

    String hello(String value);

    default String hey(String value) {
        return "Hello";
    }
}
