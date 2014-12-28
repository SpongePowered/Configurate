package ninja.leaping.configurate;

import java.io.IOException;

/**
 * Loader for a specific configuration format
 */
public interface ConfigurationLoader {
    /**
     * Create a new configuration node populated with the appropriate data
     *
     * @return The newly constructed node
     */
    public ConfigurationNode load() throws IOException;

    /**
     * Save the contents of the given node tree to
     * @param node The node a save is being requested for
     */
    public void save(ConfigurationNode node) throws IOException;
}
