package org.spongepowered.configurate.interfaces.processor;

import static org.spongepowered.configurate.interfaces.processor.TestUtils.testCompilation;

import org.junit.jupiter.api.Test;

public class DefaultValueTest {

    @Test
    void testCorrectDefaults() {
        testCompilation("defaults/CorrectDefaults");
    }

    @Test
    void testMultipleDefaults() {
        testCompilation("defaults/MultipleDefaults");
    }

}
