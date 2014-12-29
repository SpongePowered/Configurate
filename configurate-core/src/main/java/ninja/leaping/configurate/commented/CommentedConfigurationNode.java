package ninja.leaping.configurate.commented;

import com.google.common.base.Optional;
import ninja.leaping.configurate.ConfigurationNode;

import java.util.List;
import java.util.Map;

/**
 * A configuration node that is capable of having an attached comment
 */
public interface CommentedConfigurationNode extends ConfigurationNode {
    /**
     * Gets the current value for the comment. If the comment contains multiple lines, the lines will be split by \n
     *
     * @return the configuration's current comment
     */
    public Optional<String> getComment();

    /**
     * Sets the comment for this configuration.
     * @param comment
     */
    public void setComment(String comment);

    // Methods from superclass overridden to have correct return types

    @Override
    public List<? extends CommentedConfigurationNode> getChildrenList();
    @Override
    public Map<Object, ? extends CommentedConfigurationNode> getChildrenMap();
    @Override
    public CommentedConfigurationNode setValue(Object value);
    @Override
    public CommentedConfigurationNode getChild(Object key);
    @Override
    public CommentedConfigurationNode getAppendedChild();
    @Override
    public CommentedConfigurationNode getNode(Object... path);
}
