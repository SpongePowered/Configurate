package ninja.leaping.configurate;


import java.util.List;
import java.util.Map;

/**
 * A node in the configuration tree
 */
public interface ConfigurationNode {

    public Object getValue();
    public Object getValue(Object def);
    public ConfigurationNode setValue(Object value);

    public boolean hasListChildren();
    public boolean hasMapChildren();

    /**
     * Return an immutable copy of the list of children this node is aware of
     * @return The children currently attached to this node
     */
    public List<ConfigurationNode> getChildrenList();

    /**
     * Return an immutable copy of the mapping from key to node of every child this node is aware of
     *
     * @return Children nodes currently attached
     */
    public Map<Object, ConfigurationNode> getChildrenMap();

    /**
     *
     * @param key
     * @return
     */
    public ConfigurationNode getChild(Object key);
    public boolean removeChild(Object key);

    /**
     * Optional: returns null if the contents of this node are not a list
     * @return a new child created as the next entry in the list
     */
    public ConfigurationNode getAppendedChild();


    public ConfigurationNode getNode(Object... path);

    /**
     * Whether this node does not currently exist in the configuration structure.
     *
     * @return true if this node is not attached (this occurs primarily when the node has no set value)
     */
    public boolean isVirtual();
}
