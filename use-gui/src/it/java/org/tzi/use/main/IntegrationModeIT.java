package org.tzi.use.main;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.tzi.use.config.Options;

class IntegrationModeIT {
    @AfterEach
    void resetGlobalOptions() {
        Options.resetOptions();
    }

    @Test
    void invalidSpecificationReturnsInIntegrationTestMode(@TempDir Path tempDirectory) throws IOException {
        Path specification = Path.of("src/it/resources/testfiles/shell/t053.use")
                .toAbsolutePath()
                .normalize();
        Path commandFile = Files.createFile(tempDirectory.resolve("empty.cmd"));
        Path useHome = Path.of("use-core/target/classes").toAbsolutePath().normalize();

        assertDoesNotThrow(() -> Main.main(new String[] {
                "-nogui",
                "-noplugins",
                "-nr",
                "-t",
                "-it",
                "-q",
                "-H=" + useHome,
                specification.toString(),
                commandFile.toString()
        }));
    }
}
