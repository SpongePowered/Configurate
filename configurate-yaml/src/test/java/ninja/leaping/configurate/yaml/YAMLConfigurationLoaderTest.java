package ninja.leaping.configurate.yaml;

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;

import static org.junit.Assert.*;

/**
 * Basic sanity checks for the loader
 */
public class YAMLConfigurationLoaderTest {
    @Test
    public void testSimpleLoading() throws IOException {
        URL url = getClass().getResource("/example.yml");
        ConfigurationLoader loader = new YAMLConfigurationLoader(url);
        ConfigurationNode node = loader.load();
        assertEquals("unicorn", node.getNode("test", "op-level").getValue());
        assertEquals("dragon", node.getNode("other", "op-level").getValue());
        assertEquals("dog park", node.getNode("other", "location").getValue());
    }
}
