/*
 * Configurate
 * Copyright (C) zml and Configurate contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.spongepowered.configurate.objectmapping;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.spongepowered.configurate.BasicConfigurationNode;
import org.spongepowered.configurate.objectmapping.meta.Constraint;
import org.spongepowered.configurate.objectmapping.meta.Matches;
import org.spongepowered.configurate.objectmapping.meta.Required;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.UUID;

@SuppressWarnings("NotNullFieldNotInitialized") // object mapper does initialization
public class ConstraintTest {

    // required //

    static class TestRequired {
        @Nullable UUID optional;
        @Required UUID mandatory;
    }

    @Test
    void testRequired() throws ObjectMappingException {
        final ObjectMapper<TestRequired> mapper = ObjectMapper.factory().get(TestRequired.class);

        assertAll(
            // optional present, required missing
            () -> {
                assertThrows(ObjectMappingException.class, () -> {
                    mapper.load(BasicConfigurationNode.root(n -> {
                        n.getNode("optional").setValue(UUID.randomUUID().toString());
                    }));
                });
            },
            // required present, optional missing
            () -> {
                final UUID expected = UUID.randomUUID();
                final TestRequired result = mapper.load(BasicConfigurationNode.root(n -> {
                    n.getNode("mandatory").setValue(expected);
                }));
                assertEquals(expected, result.mandatory);
                assertNull(result.optional);
            },
            // both present
            () -> {
                final UUID optionalVal = UUID.randomUUID();
                final UUID requiredVal = UUID.randomUUID();
                final TestRequired result = mapper.load(BasicConfigurationNode.root(n -> {
                    n.getNode("optional").setValue(optionalVal.toString());
                    n.getNode("mandatory").setValue(requiredVal.toString());
                }));
                assertEquals(optionalVal, result.optional);
                assertEquals(requiredVal, result.mandatory);
            }
        );
    }

    // pattern //

    static class TestPattern {
        @Matches("[a-z]+") String test;
    }

    @Test
    void testPattern() throws ObjectMappingException {
        final ObjectMapper<TestPattern> mapper = ObjectMapper.factory().get(TestPattern.class);

        assertAll(
                // Empty values are not tested
                () -> mapper.load(BasicConfigurationNode.root()),
                // Valid value loads without error
                () -> {
                    final TestPattern result = mapper.load(BasicConfigurationNode.root(n -> {
                        n.getNode("test").setValue("lowercase");
                    }));
                    assertEquals("lowercase", result.test);
                },
                // Invalid value throws ObjectMappingException
                () -> assertThrows(ObjectMappingException.class, () -> {
                    mapper.load(BasicConfigurationNode.root(n -> {
                        n.getNode("test").setValue("LOUD");
                    }));
                })
        );

    }

    // localized pattern //

    static class TestLocalizedPattern {
        @Matches(value = "Test", failureMessage = "configurate.test.matchfail") String pattern;
        @Matches(value = "[0-9.+-]+", failureMessage = "Value {0} is non-numeric") String numberLike;
    }

    @Test
    void testLocalizedPattern() throws ObjectMappingException {
        // load a bundle with fixed locale, to avoid regional dependence
        final ResourceBundle bundle = ResourceBundle.getBundle("org.spongepowered.configurate.objectmapping.messages", new Locale("en", "US"));

        // Create a mapper from a customized factory that does our localization
        final ObjectMapper<TestLocalizedPattern> mapper = ObjectMapper.factoryBuilder()
                .addConstraint(Matches.class, String.class, Constraint.localizedPattern(bundle))
                .build()
                .get(TestLocalizedPattern.class);

        Assertions.assertAll(
                // Matches both
                () -> {
                    final BasicConfigurationNode node = BasicConfigurationNode.root(n -> {
                        n.getNode("pattern").setValue("Test");
                        n.getNode("number-like").setValue("0.0.42+4");
                    });
                    final TestLocalizedPattern result = mapper.load(node);

                    assertEquals("Test", result.pattern);
                    assertEquals("0.0.42+4", result.numberLike);
                },
                // Fails one with localized key
                () -> {
                    final BasicConfigurationNode node = BasicConfigurationNode.root(n -> {
                        n.getNode("pattern").setValue("bad");
                        n.getNode("number-like").setValue("0.0.42+4");
                    });

                    assertEquals("failed for input string \"bad\" against pattern \"Test\"!", assertThrows(ObjectMappingException.class, () -> {
                        mapper.load(node);
                    }).getMessage());
                },
                // Fails second with non-localized passthrough
                () -> {
                    final BasicConfigurationNode node = BasicConfigurationNode.root(n -> {
                        n.getNode("pattern").setValue("Test");
                        n.getNode("number-like").setValue("invalid");
                    });

                    assertEquals("Value invalid is non-numeric", assertThrows(ObjectMappingException.class, () -> {
                        mapper.load(node);
                    }).getMessage());
                }
        );
    }

}
