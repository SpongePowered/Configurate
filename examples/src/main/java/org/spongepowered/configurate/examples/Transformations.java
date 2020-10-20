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
package org.spongepowered.configurate.examples;

import static org.spongepowered.configurate.transformation.NodePath.path;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.transformation.ConfigurationTransformation;
import org.spongepowered.configurate.transformation.TransformAction;

import java.nio.file.Paths;

/**
 * An example of how to use transformations to migrate a configuration to a
 * newer schema version.
 *
 * <p>It's like DFU but not hot garbage! (and probably less PhD-worthy)</p>
 */
public final class Transformations {

    private static final int VERSION_LATEST = 2; // easy way to track the latest version, update as more revisions are added

    private Transformations() {}

    /**
     * Create a new builder for versioned configurations. This builder uses a
     * field in the node (by default {@code schema-version}) to determine the
     * current schema version (using -1 for no version present).
     *
     * @return versioned transformation
     */
    public static ConfigurationTransformation.Versioned create() {
        return ConfigurationTransformation.versionedBuilder()
                .addVersion(VERSION_LATEST, oneToTwo()) // syntax: target version, latest version
                .addVersion(1, zeroToOne())
                .addVersion(0, initialTransform())
                .build();
    }

    /**
     * A transformation. This one has multiple actions, and demonstrates how
     * wildcards work.
     *
     * @return created transformation
     */
    public static ConfigurationTransformation initialTransform() {
        return ConfigurationTransformation.builder()
                // Move the node at `serverVersion` to the location <code>{"server", "version"}</code>
                .addAction(path("serverVersion"), (path, value) -> {
                    return new Object[]{"server", "version"};
                })
                // For every direct child of the `section` node, set the value of its child `new-value` to something
                .addAction(path("section", ConfigurationTransformation.WILDCARD_OBJECT), (path, value) -> {
                    value.node("new-value").set("i'm a default");

                    return null; // don't move the value
                })
                .build();
    }

    public static ConfigurationTransformation zeroToOne() {
        return ConfigurationTransformation.builder()
                // oh, turns out we want to use a different format for this, so we'll change it again
                .addAction(path("server", "version"), (path, value) -> {
                    final @Nullable String val = value.getString();
                    if (val != null) {
                        value.set(val.replaceAll("-", "_"));
                    }
                    return null;
                })
                .build();
    }

    public static ConfigurationTransformation oneToTwo() {
        return ConfigurationTransformation.builder()
                .addAction(path("server", "version"), TransformAction.rename("release"))
                .build();
    }

    /**
     * Apply the transformations to a node.
     *
     * <p>This method also prints information about the version update that
     * occurred</p>
     *
     * @param node the node to transform
     * @param <N> node type
     * @return provided node, after transformation
     */
    public static <N extends ConfigurationNode> N updateNode(final N node) throws ConfigurateException {
        if (!node.virtual()) { // we only want to migrate existing data
            final ConfigurationTransformation.Versioned trans = create();
            final int startVersion = trans.version(node);
            trans.apply(node);
            final int endVersion = trans.version(node);
            if (startVersion != endVersion) { // we might not have made any changes
                System.out.println("Updated config schema from " + startVersion + " to " + endVersion);
            }
        }
        return node;
    }

    public static void main(final String[] args) throws ConfigurateException {
        if (args.length != 1) {
            System.err.println("Not enough arguments, usage: transformations <file>");
            System.err.println("Apply the test transformations to a single file");
        }
        final HoconConfigurationLoader loader = HoconConfigurationLoader.builder()
                .path(Paths.get(args[0]))
                .build();

        try {
            loader.save(updateNode(loader.load())); // tada
        } catch (final ConfigurateException ex) {
            // We try to update as much as possible, so could theoretically save a partially updated file here
            System.err.println("Failed to fully update node: " + ex.getMessage());
            ex.printStackTrace();
            System.exit(1);
        }
    }

}
