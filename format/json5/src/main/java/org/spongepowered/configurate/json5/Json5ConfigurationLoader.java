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
package org.spongepowered.configurate.json5;

import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ConsoleErrorListener;
import org.antlr.v4.runtime.DefaultErrorStrategy;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.RepresentationHint;
import org.spongepowered.configurate.loader.AbstractConfigurationLoader;
import org.spongepowered.configurate.loader.CommentHandler;
import org.spongepowered.configurate.loader.CommentHandlers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;

// TODO: Package-private generated files -- we may have to provide our own templates (sigh)
// TODO: see https://github.com/antlr/antlr4/blob/master/tool/resources/org/antlr/v4/tool/templates/codegen/Java/Java.stg

/**
 * A loader for the <a href="https://json5.org">JSON5</a> language.
 *
 * <p>We use our own implementation, and aim to be fully complaint with the
 * spec. Within those bounds, any features will be considered.</p>
 */
public final class Json5ConfigurationLoader extends AbstractConfigurationLoader<CommentedConfigurationNode> {

    public static final RepresentationHint<NumberLayout> NUMBER_FORMAT = RepresentationHint.of("number_format", NumberLayout.class,
            NumberLayout.DECIMAL);

    public static Json5ConfigurationLoader.Builder builder() {
        return new Builder();
    }

    Json5ConfigurationLoader(final @NonNull Builder builder) {
        super(builder, new CommentHandler[]{CommentHandlers.SLASH_BLOCK, CommentHandlers.DOUBLE_SLASH});
    }

    @Override
    protected void loadInternal(final CommentedConfigurationNode node, final BufferedReader reader) throws IOException {
        final CharStream stream = CharStreams.fromReader(reader);
        final Json5Lexer lexer = new Json5Lexer(stream);
        final CommonTokenStream tokens = new CommonTokenStream(lexer);
        final Json5Parser parser = new Json5Parser(tokens);
        final ToNodeListener listener = new ToNodeListener(tokens, node);

        // ANTLR 4 Reference 13.7, improving speed
        // TODO: Use actions instead to avoid generating parse tree.
        parser.getInterpreter().setPredictionMode(PredictionMode.SLL);
        parser.removeErrorListeners();
        parser.setErrorHandler(new BailErrorStrategy());
        try {
            ParseTreeWalker.DEFAULT.walk(listener, parser.document());
            // success with the simpler method
        } catch (final ParseCancellationException ex) {
            // Reset node
            node.setValue(null);
            node.setComment(null);
            // Reset state
            tokens.seek(0);
            parser.reset();

            parser.addErrorListener(ConsoleErrorListener.INSTANCE); // TODO: Capture + throw all errors
            parser.setErrorHandler(new DefaultErrorStrategy());

            // with full prediction
            parser.getInterpreter().setPredictionMode(PredictionMode.LL);
            ParseTreeWalker.DEFAULT.walk(listener, parser.document());
        }
        for (Token token : tokens.getTokens()) {
            System.out.println(token);
        }
    }

    @Override
    protected void saveInternal(final ConfigurationNode node, final Writer writer) throws IOException {
        // TODO: writing
    }

    @Override
    public CommentedConfigurationNode createNode(final ConfigurationOptions options) {
        return CommentedConfigurationNode.root(options);
    }

    public static final class Builder extends AbstractConfigurationLoader.Builder<Builder> {

        // TODO: provide options to customize output

        @Override
        public @NonNull Json5ConfigurationLoader build() {
            return new Json5ConfigurationLoader(this);
        }
    }

}
