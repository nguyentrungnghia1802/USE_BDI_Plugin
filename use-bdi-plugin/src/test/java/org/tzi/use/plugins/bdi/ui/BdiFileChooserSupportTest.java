package org.tzi.use.plugins.bdi.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class BdiFileChooserSupportTest {
    @Test
    void discoversTheCurrentCheckoutFromANestedWorkingDirectory(@TempDir Path tempDir) throws Exception {
        Path checkout = Files.createDirectories(tempDir.resolve("checkout"));
        Files.createFile(checkout.resolve("pom.xml"));
        Files.createDirectory(checkout.resolve("use-bdi-plugin"));
        Path nestedDirectory = Files.createDirectories(checkout.resolve("use-bdi-plugin/demo/auction"));

        assertEquals(checkout, BdiFileChooserSupport.defaultDirectory(nestedDirectory));
    }

    @Test
    void keepsAnUnrelatedWorkingDirectoryAsTheFallback(@TempDir Path tempDir) throws Exception {
        Path workingDirectory = Files.createDirectories(tempDir.resolve("installed-distribution"));

        assertEquals(workingDirectory, BdiFileChooserSupport.defaultDirectory(workingDirectory));
    }
}
