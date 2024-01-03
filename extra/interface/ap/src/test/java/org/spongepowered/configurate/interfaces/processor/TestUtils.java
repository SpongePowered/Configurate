package org.spongepowered.configurate.interfaces.processor;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

import com.google.common.io.Resources;
import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.tools.StandardLocation;
import org.spongepowered.configurate.interfaces.Constants;

class TestUtils {

    static final int EXPECT_CONFIG_AND_MAPPING = 2;

    /**
     * Tests whether the compilation is successful and
     * that the correct mappings have been made
     */
    static Compilation testCompilation(final String sourceResourceName, final int expectedSourceCount) {
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
            final URL localMappings = Resources.getResource(sourceResourceName + ".properties");

            final String actualContent = compilation
                .generatedFile(StandardLocation.SOURCE_OUTPUT, Constants.MAPPING_FILE)
                .orElseThrow(() -> new IllegalStateException("Expected the interface mappings file to be created"))
                .getCharContent(false)
                .toString();

            final List<String> expectedLines =
                Resources
                    .asCharSource(localMappings, StandardCharsets.UTF_8)
                    .readLines();

            assertIterableEquals(expectedLines, removeComments(actualContent));
        } catch (final IOException exception) {
            throw new RuntimeException(exception);
        }

        if (expectedSourceCount != -1) {
            // can't use compilation.generatedSourceFiles because the mapping
            // file is written to the source output, but isn't a source file
            assertEquals(
                expectedSourceCount,
                compilation.generatedFiles().stream()
                    .filter(file -> file.getName().startsWith("/SOURCE_OUTPUT/"))
                    .count()
            );
        }
        return compilation;
    }

    static List<String> removeComments(final String content) {
        return Arrays.stream(content.split(System.lineSeparator()))
            .filter(line -> !line.startsWith("#"))
            .collect(Collectors.toList());
    }

}
