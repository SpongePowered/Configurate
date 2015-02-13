/**
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
package ninja.leaping.configurate.commented;

import com.google.common.base.Optional;
import ninja.leaping.configurate.SimpleConfigurationNode;

import java.util.List;
import java.util.Map;


/**
 * Represents a configuration node containing comments
 */
public class SimpleCommentedConfigurationNode extends SimpleConfigurationNode implements CommentedConfigurationNode {
    private String comment;

    public static SimpleCommentedConfigurationNode root() {
        return new SimpleCommentedConfigurationNode(null, null, null);
    }

    protected SimpleCommentedConfigurationNode(Object path, SimpleConfigurationNode root, SimpleConfigurationNode parent) {
        super(path, root, parent);
    }

    @Override
    public Optional<String> getComment() {
        return Optional.fromNullable(comment);
    }

    @Override
    public SimpleCommentedConfigurationNode setComment(String comment) {
        attachIfNecessary();
        this.comment = comment;
        return this;
    }

    @Override
    protected SimpleCommentedConfigurationNode createNode(Object path) {
        return new SimpleCommentedConfigurationNode(path, root, this);
    }

    @Override
    public SimpleCommentedConfigurationNode setValue(Object value) {
        if (value instanceof CommentedConfigurationNode && ((CommentedConfigurationNode) value).getComment().isPresent()) {
            setComment(((CommentedConfigurationNode) value).getComment().get());
        }
        return (SimpleCommentedConfigurationNode)super.setValue(value);
    }

    @Override
    public SimpleCommentedConfigurationNode getNode(Object... path) {
        return (SimpleCommentedConfigurationNode)super.getNode(path);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<? extends SimpleCommentedConfigurationNode> getChildrenList() {
        return (List<SimpleCommentedConfigurationNode>) super.getChildrenList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<Object, ? extends SimpleCommentedConfigurationNode> getChildrenMap() {
        return (Map<Object, SimpleCommentedConfigurationNode>) super.getChildrenMap();
    }

    @Override
    public SimpleCommentedConfigurationNode getAppendedNode() {
        return (SimpleCommentedConfigurationNode) super.getAppendedNode();
    }
}
