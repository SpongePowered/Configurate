package org.spongepowered.configurate.interfaces.processor;

import static org.spongepowered.configurate.interfaces.processor.TestUtils.EXPECT_CONFIG_AND_MAPPING;
import static org.spongepowered.configurate.interfaces.processor.TestUtils.testCompilation;

import org.junit.jupiter.api.Test;

public class DefaultValueTest {

    @Test
    void testCorrectDefaults() {
        testCompilation("test/defaults/CorrectDefaults", EXPECT_CONFIG_AND_MAPPING);
    }

    @Test
    void testMultipleDefaults() {
        testCompilation("test/defaults/MultipleDefaults", EXPECT_CONFIG_AND_MAPPING);
    }

}
