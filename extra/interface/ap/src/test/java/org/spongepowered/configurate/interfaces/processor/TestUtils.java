package org.spongepowered.configurate.interfaces.processor;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

import com.google.common.io.Resources;
import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import javax.tools.StandardLocation;
import org.spongepowered.configurate.interfaces.Constants;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

final class TestUtils {

    private TestUtils() {
    }

    /**
     * Tests whether the compilation is successful, that the correct mappings
     * have been made and that the generated impl matches the expected impl.
     */
    static Compilation testCompilation(final String sourceResourceName) {
        final Compilation compilation =
            javac()
                .withProcessors(new ConfigImplementationGeneratorProcessor())
                .compile(JavaFileObjects.forResource(sourceResourceName + ".java"));

        final String targetResourceName = sourceResourceName + "Impl";
        final String targetSourceName = targetResourceName.replace('/', '.');

        assertThat(compilation).succeeded();
        assertThat(compilation)
            .generatedSourceFile(targetSourceName)
            .hasSourceEquivalentTo(JavaFileObjects.forResource(targetResourceName + ".java"));

        try {

            final String actualContent = compilation
                .generatedFile(StandardLocation.CLASS_OUTPUT, Constants.MAPPING_FILE)
                .orElseThrow(() -> new IllegalStateException("Expected the interface mappings file to be created"))
                .getCharContent(false)
                .toString();

            final List<String> expectedLines = readOrGenerateMappings(sourceResourceName, targetResourceName);

            assertIterableEquals(expectedLines, removeComments(actualContent));
        } catch (final IOException exception) {
            throw new RuntimeException(exception);
        }

        return compilation;
    }

    private static List<String> removeComments(final String content) {
        return Arrays.stream(content.split(System.lineSeparator()))
            .filter(line -> !line.startsWith("#"))
            .collect(Collectors.toList());
    }

    private static List<String> readOrGenerateMappings(final String sourceResourceName, final String targetResourceName) {
        try {
            final URL localMappings = Resources.getResource(sourceResourceName + ".properties");
            return Resources.asCharSource(localMappings, StandardCharsets.UTF_8).readLines();
        } catch (final IllegalArgumentException ignored) {
            System.out.println("Could not find resource " + sourceResourceName + ".properties, generating one");
            return Collections.singletonList(String.format(
                "%s=%s",
                sourceResourceName.replace('/', '.'),
                targetResourceName.replace('/', '.')
            ));
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

}
