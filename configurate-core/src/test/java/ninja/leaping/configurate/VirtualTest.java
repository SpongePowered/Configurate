package ninja.leaping.configurate;

import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.commented.SimpleCommentedConfigurationNode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class VirtualTest {

    @Test
    public void testConfigurationNode() {
        ConfigurationNode node = SimpleConfigurationNode.root();
        ConfigurationNode child = node.getNode("Child");
        child.ifExists(result -> assertTrue(result.isVirtual()));

        child.setValue("Value For Child");
        child.ifExists(result -> assertFalse(result.isVirtual()));
        child.ifExists(result -> assertEquals(result.getString(), "Value For Child"));
    }

    @Test
    public void testCommentedNode() {
        CommentedConfigurationNode node = SimpleCommentedConfigurationNode.root();
        CommentedConfigurationNode child = node.getNode("Child");
        child.ifExists(result -> assertTrue(result.isVirtual()));

        child.setValue("Value For Child");
        child.ifExists(result -> assertFalse(result.isVirtual()));
    }
}
