package ninja.leaping.configurate.yaml;

import ninja.leaping.configurate.ConfigurationLoader;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.SimpleConfigurationNode;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.charset.Charset;


/**
 * A loader for YAML-formatted configurations, using the snakeyaml library for parsing
 */
public class YAMLConfigurationLoader implements ConfigurationLoader {
    private final File file;

    private final Yaml yaml;

    public YAMLConfigurationLoader(File file) {
        this.file = file;
        this.yaml = new Yaml();
    }

    @Override
    public ConfigurationNode load() throws IOException {
        final SimpleConfigurationNode node = SimpleConfigurationNode.root();
        node.setValue(yaml.load(new InputStreamReader(new FileInputStream(file), Charset.forName("utf-8"))));
        return node;
    }

    @Override
    public void save(ConfigurationNode node) throws IOException {
        yaml.dump(node.getValue(), new OutputStreamWriter(new FileOutputStream(file), Charset.forName("utf-8")));
    }
}
