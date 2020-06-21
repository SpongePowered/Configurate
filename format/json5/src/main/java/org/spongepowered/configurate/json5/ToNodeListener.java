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

import org.antlr.v4.runtime.BufferedTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;

import java.util.List;

/**
 * TODO: Use actions and avoid generating a parse tree.
 */
class ToNodeListener extends Json5BaseListener {

    private final BufferedTokenStream tokens;
    private CommentedConfigurationNode position;

    ToNodeListener(final BufferedTokenStream tokens, final CommentedConfigurationNode root) {
        this.tokens = tokens;
        this.position = root;
    }

    private void applyCommentsToPosition(final ParserRuleContext ctx) {
        final List<Token> commentChannel = this.tokens.getHiddenTokensToLeft(ctx.getStart().getTokenIndex(), Json5Lexer.HIDDEN);
        if (commentChannel != null) {
            final Token comment = commentChannel.get(0);
            if (comment != null) {
                final String rawText = comment.getText();
                switch (comment.getType()) {
                    case Json5Lexer.BLOCK_COMMENT:
                        this.position.setComment(rawText.substring(2, rawText.length() - 2));
                        break;
                    case Json5Lexer.LINE_COMMENT:
                        this.position.setComment(rawText.substring(2));
                        break;
                    default: // no-op
                }
            }
        }
    }

    @Override
    public void enterCompound(final Json5Parser.CompoundContext ctx) {
        // only advance if we're an array, otherwise this is handled in memberName
        if (ctx.parent instanceof Json5Parser.ArrayContext) { // we're in an array
            this.position = this.position.appendListNode();
            applyCommentsToPosition(ctx);
        }
    }

    @Override
    public void exitCompound(final Json5Parser.CompoundContext ctx) {
        this.position = this.position.getParent();
        super.exitCompound(ctx);
    }

    @Override
    public void enterLiteral(final Json5Parser.LiteralContext ctx) {
        // only advance if we're an array, otherwise this is handled in memberName
        if (ctx.parent instanceof Json5Parser.ArrayContext) { // we're in an array
            this.position = this.position.appendListNode();
            applyCommentsToPosition(ctx);
        }
        this.position.setValue(ctx.getText()); // TODO: actually handle values
        this.position = this.position.getParent();
    }

    @Override
    public void enterMemberName(final Json5Parser.MemberNameContext ctx) { // a key in an object
        final @Nullable TerminalNode identifier = ctx.IdentifierName();
        if (identifier != null) { // plain name
            this.position = this.position.getNode(identifier.getText());
        } else { // quoted name
            this.position = this.position.getNode(Helpers.unquote(ctx.getText()));
        }
        applyCommentsToPosition(ctx);
    }

}
