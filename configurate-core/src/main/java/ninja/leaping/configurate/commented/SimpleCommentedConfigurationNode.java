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
        return new SimpleCommentedConfigurationNode(new Object[0], null);
    }

    protected SimpleCommentedConfigurationNode(Object[] path, SimpleConfigurationNode root) {
        super(path, root);
    }

    @Override
    public Optional<String> getComment() {
        return Optional.fromNullable(comment);
    }

    @Override
    public void setComment(String comment) {
        attachIfNecessary();
        this.comment = comment;
    }

    @Override
    protected SimpleCommentedConfigurationNode createNode(Object[] path) {
        return new SimpleCommentedConfigurationNode(path, root);
    }

    @Override
    public SimpleCommentedConfigurationNode setValue(Object value) {
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
    public SimpleCommentedConfigurationNode getChild(Object key) {
        return (SimpleCommentedConfigurationNode) super.getChild(key);
    }

    @Override
    public SimpleCommentedConfigurationNode getAppendedChild() {
        return (SimpleCommentedConfigurationNode) super.getAppendedChild();
    }
}
