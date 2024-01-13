package structure;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;

@ConfigSerializable
public interface ExtendedConfig extends BasicConfig {
    String hi();

    Number hey(int value);
}
