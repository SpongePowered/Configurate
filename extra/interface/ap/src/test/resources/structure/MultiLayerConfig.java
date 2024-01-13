package structure;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;

@ConfigSerializable
public interface MultiLayerConfig {
    String test();
    SecondLayer second();

    @ConfigSerializable
    public interface SecondLayer {
        String test();
        String test2();
    }
}
