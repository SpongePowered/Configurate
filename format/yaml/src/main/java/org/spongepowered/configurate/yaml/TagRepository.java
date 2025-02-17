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

import static java.util.Objects.requireNonNull;

import com.google.auto.value.AutoValue;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.util.UnmodifiableCollections;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * A collection of tags that are understood when reading a document.
 *
 * @since 4.2.0
 */
final class TagRepository {

    // fallback tag for each node type
    final Tag unresolvedTag;
    final Tag.Scalar<String> stringTag;
    final Tag.Sequence sequenceTag;
    final Tag.Mapping mappingTag;
    final List<Tag> tags;
    final Map<Class<?>, Tag> byErasedType;
    final Map<URI, Tag> byName;

    TagRepository(final Builder builder) {
        this.unresolvedTag = builder.unresolvedTag;
        this.stringTag = builder.stringTag;
        this.sequenceTag = builder.sequenceTag;
        this.mappingTag = builder.mappingTag;
        final List<Tag> allTags = new ArrayList<>(builder.otherTags);
        allTags.add(this.stringTag);
        allTags.add(this.sequenceTag);
        allTags.add(this.mappingTag);
        allTags.add(this.unresolvedTag);
        this.tags = UnmodifiableCollections.copyOf(allTags);
        this.byErasedType = UnmodifiableCollections.copyOf(builder.byErasedType);
        this.byName = UnmodifiableCollections.copyOf(builder.byName);
    }

    static TagRepository.Builder builder() {
        return new Builder();
    }

    /**
     * Determine the implicit tag for a scalar value.
     *
     * @param scalar scalar to test
     * @return the first matching tag
     * @since 4.2.0
     */
    public Tag.@Nullable Scalar<?> forInput(final String scalar) {
        for (final Tag tag : this.tags) {
            if (tag instanceof Tag.Scalar) {
                final @Nullable Pattern pattern = ((Tag.Scalar<?>) tag).pattern();
                if (pattern != null && pattern.matcher(scalar).matches()) {
                    return (Tag.Scalar<?>) tag;
                }
            }
        }

        return null;
    }

    /**
     * Resolve a tag by its URI.
     *
     * @param name the tag URI
     * @return a tag, if any is present
     * @since 4.2.0
     */
    public @Nullable Tag named(final URI name) {
        return this.byName.get(name);
    }

    /**
     * Resolve a tag by the Java type it represents.
     *
     * @param type the type used
     * @return a tag, if any is registered
     * @since 4.2.0
     */
    public @Nullable Tag byType(final Class<?> type) {
        return this.byErasedType.get(type);
    }

    /**
     * Analyze a node to determine what tag its value should have.
     *
     * @param node the node to analyze
     * @return a calculated tag
     * @since 4.2.0
     */
    @SuppressWarnings("rawtypes")
    AnalyzedTag analyze(final ConfigurationNode node) throws ConfigurateException {
        final @Nullable Tag explicit = node.ownHint(YamlConfigurationLoader.TAG);
        final @Nullable Tag calculated;
        boolean isUnambiguous;
        if (node.isMap()) {
            calculated = this.mappingTag;
            isUnambiguous = true;
        } else if (node.isList()) {
            calculated = this.sequenceTag;
            isUnambiguous = true;
        } else if (node.isNull()) {
            calculated = this.byType(void.class);
            isUnambiguous = true;
        } else {
            final @Nullable Object rawScalar = node.rawScalar();
            calculated = this.byType(rawScalar.getClass());
            isUnambiguous = true;
            if (calculated != null && calculated instanceof Tag.Scalar<?>) {
                final String serialized = ((Tag.Scalar) calculated).toString(rawScalar);
                for (final Tag tag : this.tags) {
                    if (tag != calculated && tag instanceof Tag.Scalar<?> && ((Tag.Scalar<?>) tag).pattern() != null) {
                        if (!tag.equals(this.stringTag) && ((Tag.Scalar<?>) tag).pattern().matcher(serialized).matches()) {
                            isUnambiguous = false;
                            break;
                        }
                    }
                }
            }

        }
        return AnalyzedTag.of(calculated == null ? this.unresolvedTag : calculated, explicit, isUnambiguous);
    }

    public Tag.Scalar<String> stringTag() {
        return this.stringTag;
    }

    public Tag.Sequence sequenceTag() {
        return this.sequenceTag;
    }

    public Tag.Mapping mappingTag() {
        return this.mappingTag;
    }

    public TagRepository.Builder toBuilder() {
        return new Builder(this);
    }

    /**
     * A combination of resolved tag, and whether the tag is the same as the tag
     * that would be implicitly calculated.
     *
     * @since 4.2.0
     */
    @AutoValue
    abstract static class AnalyzedTag {

        /**
         * Create a new resolved tag.
         *
         * @param resolved the resolved type
         * @param specified the specified type
         * @return the resolved tag
         * @since 4.2.0
         */
        static AnalyzedTag of(final Tag resolved, final @Nullable Tag specified, final boolean defaultForType) {
            return new AutoValue_TagRepository_AnalyzedTag(resolved, specified, defaultForType);
        }

        AnalyzedTag() {
        }

        /**
         * Get the calculated tag, if any is present.
         *
         * <p>If no tag could be resolved, this will always return the parser's
         * <em>unresolved</em> tag.</p>
         *
         * @return the calculated tag
         * @since 4.2.0
         */
        public abstract Tag resolved();

        /**
         * Get the manually specified tag for this node.
         *
         * @return the specified tag
         * @since 4.2.0
         */
        public abstract @Nullable Tag specified();

        /**
         * Get whether this node's serialized scalar value unambiguously matched
         * a certain tag.
         *
         * @return whether the calculated tag unambiguously matches
         * @since 4.2.0
         */
        abstract boolean isUnambiguous();

        /**
         * Get the actual tag applicable to the analyzed node.
         *
         * <p>If a tag is explicitly specified, that tag will be returned.
         * Otherwise, the specified tag will be used.</p>
         *
         * @return the actual tag
         */
        public final Tag actual() {
            return this.specified() == null ? this.resolved() : this.specified();
        }

        /**
         * Get whether the provided tag is an implicit tag or not.
         *
         * <p>A tag is implicit when no type has been specified, or the resolved
         * type equals the specified type.</p>
         *
         * @return whether the tag is implicit.
         * @since 4.2.0
         */
        public final boolean implicit() {
            return this.specified() == null ? this.isUnambiguous() : Objects.equals(this.resolved(), this.specified());
        }

    }

    static final class Builder {
        private @MonotonicNonNull Tag unresolvedTag;
        private Tag.@MonotonicNonNull Scalar<String> stringTag;
        private Tag.@MonotonicNonNull Sequence sequenceTag;
        private Tag.@MonotonicNonNull Mapping mappingTag;
        private final List<Tag> otherTags = new ArrayList<>();
        private final Map<Class<?>, Tag> byErasedType = new HashMap<>();
        private final Map<URI, Tag> byName = new HashMap<>();

        Builder() {
        }

        Builder(final TagRepository existing) {
            this.unresolvedTag = existing.unresolvedTag;
            this.stringTag = existing.stringTag;
            this.sequenceTag = existing.sequenceTag;
            this.mappingTag = existing.mappingTag;
            this.otherTags.addAll(existing.tags);
            this.otherTags.remove(this.stringTag);
            this.otherTags.remove(this.sequenceTag);
            this.otherTags.remove(this.mappingTag);
            this.otherTags.remove(this.unresolvedTag);
            this.byErasedType.putAll(existing.byErasedType);
            this.byName.putAll(existing.byName);
        }

        Builder unresolvedTag(final Tag unresolvedTag) {
            // if (this.unresolvedTag != null)
            this.addTag0(this.unresolvedTag = requireNonNull(unresolvedTag, "unresolved"));
            return this;
        }

        Builder stringTag(final Tag.Scalar<String> string) {
            this.addTag0(this.stringTag = requireNonNull(string, "string"));
            return this;
        }

        Builder sequenceTag(final Tag.Sequence sequence) {
            this.addTag0(this.sequenceTag = requireNonNull(sequence, "sequence"));
            return this;
        }

        Builder mappingTag(final Tag.Mapping mapping) {
            this.addTag0(this.mappingTag = requireNonNull(mapping, "mapping"));
            return this;
        }

        /**
         * Add a tag to this repository.
         *
         * <p>This must not receive any tag that is already the string,
         * mapping, or sequence tags. If trying to register a tag that shares a
         * URL or supported types with an already-registered tag, this operation
         * will fail, unless that same tag instance is the one registered.</p>
         *
         * @param tag the tag to register
         * @return this builder
         * @since 4.2.0
         */
        Builder addTag(final Tag tag) {
            requireNonNull(tag, "tag");
            if (tag.equals(this.mappingTag) || tag.equals(this.sequenceTag) || tag.equals(this.stringTag) || tag.equals(this.unresolvedTag)) {
                throw new IllegalArgumentException("Tag " + tag
                    + " was already registered as one of the mapping, sequence, string, or unresolved tags!");
            }
            this.otherTags.add(tag);
            return this.addTag0(tag);
        }

        private Builder addTag0(final Tag tag) {
            for (final Class<?> clazz : tag.supportedTypes()) {
                this.byErasedType.put(clazz, tag);
            }
            this.byName.put(tag.tagUri(), tag);
            return this;
        }

        TagRepository build() {
            if (this.unresolvedTag == null) {
                throw new IllegalArgumentException("Unresolved tag not set");
            }
            if (this.stringTag == null) {
                throw new IllegalArgumentException("String tag not set");
            }
            if (this.mappingTag == null) {
                throw new IllegalArgumentException("Mapping tag not set");
            }
            if (this.sequenceTag == null) {
                throw new IllegalArgumentException("Sequence tag not set");
            }

            return new TagRepository(this);
        }

    }

}
