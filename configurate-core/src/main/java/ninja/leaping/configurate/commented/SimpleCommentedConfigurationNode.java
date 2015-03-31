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
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.SimpleConfigurationNode;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;


/**
 * Represents a configuration node containing comments
 */
public class SimpleCommentedConfigurationNode extends SimpleConfigurationNode implements CommentedConfigurationNode {
    private final AtomicReference<String> comment = new AtomicReference<>();

    public static SimpleCommentedConfigurationNode root() {
        return root(ConfigurationOptions.defaults());
    }

    public static SimpleCommentedConfigurationNode root(ConfigurationOptions options) {
        return new SimpleCommentedConfigurationNode(null, null, options);
    }

    protected SimpleCommentedConfigurationNode(Object path, SimpleConfigurationNode parent, ConfigurationOptions options) {
        super(path, parent, options);
    }

    @Override
    public Optional<String> getComment() {
        return Optional.fromNullable(comment.get());
    }

    @Override
    public SimpleCommentedConfigurationNode setComment(String comment) {
        attachIfNecessary();
        this.comment.set(comment);
        return this;
    }

    @Override
    public SimpleCommentedConfigurationNode getParent() {
        return (SimpleCommentedConfigurationNode) super.getParent();
    }

    @Override
    protected SimpleCommentedConfigurationNode createNode(Object path) {
        return new SimpleCommentedConfigurationNode(path, this, getOptions());
    }

    @Override
    public SimpleCommentedConfigurationNode setValue(Object value) {
        if (value instanceof CommentedConfigurationNode && ((CommentedConfigurationNode) value).getComment().isPresent()) {
            setComment(((CommentedConfigurationNode) value).getComment().get());
        }
        return (SimpleCommentedConfigurationNode)super.setValue(value);
    }

    @Override
    public SimpleCommentedConfigurationNode mergeValuesFrom(ConfigurationNode other) {
        if (other instanceof CommentedConfigurationNode) {
            Optional<String> otherComment = ((CommentedConfigurationNode) other).getComment();
            if (otherComment.isPresent()) {
                comment.compareAndSet(null, otherComment.get());
            }
        }
        return (SimpleCommentedConfigurationNode) super.mergeValuesFrom(other);
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SimpleCommentedConfigurationNode)) return false;
        if (!super.equals(o)) return false;

        SimpleCommentedConfigurationNode that = (SimpleCommentedConfigurationNode) o;

        if (!comment.equals(that.comment)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + comment.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "SimpleCommentedConfigurationNode{" +
                "super=" + super.toString() +
                "comment=" + comment +
                '}';
    }
}
