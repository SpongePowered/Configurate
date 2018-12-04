package ninja.leaping.configurate.objectmapping;

import ninja.leaping.configurate.ConfigurationNode;

public interface FieldAdapter<V> {

    V deserialize(ConfigurationNode node);

    void serialize(V value, ConfigurationNode node);
}
