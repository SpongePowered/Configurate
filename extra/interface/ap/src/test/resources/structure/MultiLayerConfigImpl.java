package structure;

import java.lang.Override;
import java.lang.String;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

/**
 * Automatically generated implementation of the config */
@ConfigSerializable
final class MultiLayerConfigImpl implements MultiLayerConfig {
    private String test;

    private MultiLayerConfig.SecondLayer second = new SecondLayerImpl();

    @Override
    public String test() {
        return test;
    }

    @Override
    public MultiLayerConfig.SecondLayer second() {
        return second;
    }

    /**
     * Automatically generated implementation of the config */
    @ConfigSerializable
    static final class SecondLayerImpl implements MultiLayerConfig.SecondLayer {
        private String test;

        private String test2;

        @Override
        public String test() {
            return test;
        }

        @Override
        public String test2() {
            return test2;
        }
    }
}
