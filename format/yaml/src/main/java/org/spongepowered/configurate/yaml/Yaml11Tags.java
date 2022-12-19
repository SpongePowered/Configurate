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
package org.spongepowered.configurate.yaml;

import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.loader.ParsingException;
import org.spongepowered.configurate.util.UnmodifiableCollections;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Standard types defined on the <a href="https://yaml.org/type/">yaml.org
 * tag repository</a>.
 *
 * @since 4.2.0
 */
final class Yaml11Tags {

    private Yaml11Tags() {
    }

    private static URI yamlOrg(final String specific) {
        return URI.create("tag:yaml.org,2002:" + specific);
    }

    /**
     * A binary data tag.
     *
     * @see <a href="https://yaml.org/type/binary.html">tag:yaml.org,2002:binary</a>
     * @since 4.2.0
     */
    public static final Tag.Scalar<byte[]> BINARY = new Tag.Scalar<byte[]>(
        yamlOrg("binary"),
        UnmodifiableCollections.toSet(byte[].class),
        null // base64 is not distinguishable from a normal string, require the tag to be provided explicitly
    ) {

        @Override
        public byte[] fromString(final String input) {
            return Base64.getDecoder().decode(input);
        }

        @Override
        public String toString(final byte[] own) {
            return Base64.getEncoder().encodeToString(own);
        }
    };

    /**
     * A boolean value.
     *
     * @implNote Canonically, these are y|n in YAML 1.1, but because YAML 1.2
     *     will only support true|false, we will treat those as the default
     *     output format.
     * @see <a href="https://yaml.org/type/bool.html">tag:yaml.org,2002:bool</a>
     * @since 4.2.0
     */
    public static final Tag.Scalar<Boolean> BOOL = new Tag.Scalar<Boolean>(
        yamlOrg("bool"),
        UnmodifiableCollections.toSet(Boolean.class),
        Pattern.compile("y|Y|yes|Yes|YES|n|N|no|No|NO"
            + "|true|True|TRUE|false|False|FALSE"
            + "|on|On|ON|off|Off|OFF")
    ) {
        private final Set<String> trues = UnmodifiableCollections.toSet(
            "y", "Y", "yes", "Yes", "YES",
            "true", "True", "TRUE",
            "on", "On", "ON"
        );

        @Override
        public Boolean fromString(final String input) {
            return this.trues.contains(input);
        }

        @Override
        public String toString(final Boolean own) {
            // YAML 1.2 is a lot more strict. Only emit the standard boolean values for forwards compatibility
            return own ? "true" : "false";
        }
    };

    /**
     * A floating-point number.
     *
     * @see <a href="https://yaml.org/type/float.html">tag:yaml.org,2002:float</a>
     * @since 4.2.0
     */
    public static final Tag.Scalar<Number> FLOAT = new Tag.Scalar<Number>(
        yamlOrg("float"),
        UnmodifiableCollections.toSet(Float.class, Double.class, BigDecimal.class),
        Pattern.compile("[-+]?([0-9][0-9_]*)?\\.[0-9.]*([eE][-+][0-9]+)?" // base 10
            + "|[-+]?[0-9][0-9_]*(:[0-5]?[0-9])+\\.[0-9]*" // base 60
            + "|[-+]?\\.(inf|Inf|INF)" // infinity
            + "|\\.(nan|NaN|NAN)") // not a number
    ) {

        @Override
        public Number fromString(final String input) {
            return Double.parseDouble(input);
        }

        @Override
        public String toString(final Number own) {
            return own.toString();
        }
    };

    /**
     * An integer.
     *
     * @see <a href="https://yaml.org/type/int.html">tag:yaml.org,2002:int</a>
     * @since 4.2.0
     */
    public static final Tag.Scalar<Number> INT = new Tag.Scalar<Number>(
        yamlOrg("int"),
        UnmodifiableCollections.toSet(Byte.class, Short.class, Integer.class, Long.class, BigInteger.class),
        Pattern.compile("[-+]?0b[0-1_]+" // base 2
            + "|[-+]?0[0-7_]+" // base 8
            + "|[-+]?(0|[1-9][0-9_]*)" // base 10
            + "|[-+]?0x[0-9a-fA-F_]+" // base 16
            + "|[-+]?[1-9][0-9_]*(:[0-5]?[0-9])+") // base 60
    ) {

        // todo: wrong
        @Override
        public Number fromString(final String input) {
            // handle leading +/-
            // if literal '0': return int
            // handle 0/0x/0b prefixes
            try {
                final long ret = Long.parseLong(input);
                if (ret >= Integer.MIN_VALUE && ret <= Integer.MAX_VALUE) {
                    return (int) ret;
                } else {
                    return ret;
                }
            } catch (final NumberFormatException ex) {
                return new BigInteger(input);
            }
        }

        @Override
        public String toString(final Number own) {
            // emit only number formats represented in yaml 1.2 core schema: base 10 or 16
            // todo: have a 'compatibility mode' that can be disabled to produce output that is valid yaml 1.1 but not valid 1.2?
            return own.toString();
        }
    };

    /**
     * A mapping merge.
     *
     * <p>This will not be supported in Configurate until reference-type nodes
     * are fully implemented.</p>
     *
     * @see <a href="https://yaml.org/type/merge.html">tag:yaml.org,2002:merge</a>
     * @since 4.2.0
     */
    public static final Tag.Scalar<?> MERGE = new Tag.Scalar<Object>(
        yamlOrg("merge"),
        UnmodifiableCollections.toSet(ConfigurationNode.class),
        Pattern.compile("<<")
    ) {

        // TODO: this can only really be implemented with full reference support
        // used as map key, where the next node will be a reference that should be merged in to this node

        @Override
        public Object fromString(final String input) throws ParsingException {
            throw new ParsingException(ParsingException.UNKNOWN_POS, ParsingException.UNKNOWN_POS, null, "Merge keys are not yet implemented", null);
        }

        @Override
        public String toString(final Object own) {
            return own.toString();
        }
    };

    /**
     * The value {@code null}.
     *
     * <p>Because Configurate has no distinction between a node with a
     * {@code null} value, and a node that does not exist, this tag will most
     * likely never be encountered in an in-memory representation.</p>
     *
     * @see <a href="https://yaml.org/type/null.html">tag:yaml.org,2002:null</a>
     * @since 4.2.0
     */
    public static final Tag.Scalar<Void> NULL = new Tag.Scalar<Void>(
        yamlOrg("null"),
        UnmodifiableCollections.toSet(Void.class, void.class),
        Pattern.compile("~"
            + "|null|Null|NULL"
            + "|$")
    ) {

        @Override
        public Void fromString(final String input) {
            return null;
        }

        @Override
        public String toString(final Void own) {
            return "null";
        }
    };

    /**
     * Any string.
     *
     * @see <a href="https://yaml.org/type/str.html">tag:yaml.org,2002:str</a>
     * @since 4.2.0
     */
    public static final Tag.Scalar<String> STR = new Tag.Scalar<String>(
        yamlOrg("str"),
        UnmodifiableCollections.toSet(String.class),
        Pattern.compile(".+") // empty scalar is NULL
    ) {
        @Override
        public String fromString(final String input) {
            return input;
        }

        @Override
        public String toString(final String own) {
            return own;
        }
    };

    /**
     * A timestamp, containing date, time, and timezone.
     *
     * @see <a href="https://yaml.org/type/timestamp.html">tag:yaml.org,2002:timestamp</a>
     * @since 4.2.0
     */
    public static final Tag.Scalar<ZonedDateTime> TIMESTAMP = new Tag.Scalar<ZonedDateTime>(
        yamlOrg("timestamp"),
        UnmodifiableCollections.toSet(ZonedDateTime.class),
        Pattern.compile("[0-9]{4}-[0-9]{2}-[0-9]{2}" // YYYY-MM-DD
            + "|[0-9]{4}" // YYYY
            + "-[0-9]{1,2}" // month
            + "-[0-9]{1,2}" // day
            + "([Tt]|[ \t]+)[0-9]{1,2}" // hour
            + ":[0-9]{1,2}" // minute
            + ":[0-9]{2}" // second
            + "(\\.[0-9]*)?" // fraction
            + "(([ \t]*)Z|[-+][0-9]{1,2}(:[0-9]{2})?)?") // time zone
    ) {
        @Override
        public ZonedDateTime fromString(final String input) {
            throw new UnsupportedOperationException("not yet implemented");
        }

        @Override
        public String toString(final ZonedDateTime own) {
            throw new UnsupportedOperationException("not yet implemented");
        }
    };

    /**
     * A mapping.
     *
     * @see <a href="https://yaml.org/type/map.html">tag:yaml.org,2002:map</a>
     * @since 4.2.0
     */
    public static final Tag.Mapping MAP = new Tag.Mapping(yamlOrg("map"), UnmodifiableCollections.toSet(Map.class));

    /**
     * A sequence.
     *
     * @see <a href="https://yaml.org/type/seq.html">tag:yaml.org,2002:seq</a>
     * @since 4.2.0
     */
    public static final Tag.Sequence SEQ = new Tag.Sequence(yamlOrg("seq"), UnmodifiableCollections.toSet(List.class, Set.class));

    static final TagRepository REPOSITORY = TagRepository.builder()
        .unresolvedTag(new Tag(URI.create("?"), UnmodifiableCollections.toSet(Object.class)) {})
        .stringTag(STR)
        .mappingTag(MAP)
        .sequenceTag(SEQ)
        .addTag(BINARY)
        .addTag(BOOL)
        .addTag(INT)
        .addTag(FLOAT)
        .addTag(NULL)
        .addTag(MERGE)
        .addTag(TIMESTAMP)
        .build();

}
