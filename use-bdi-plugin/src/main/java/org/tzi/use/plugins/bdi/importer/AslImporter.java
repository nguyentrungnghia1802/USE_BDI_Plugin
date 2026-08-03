package org.tzi.use.plugins.bdi.importer;

import java.nio.file.Path;
import java.util.List;

public interface AslImporter {
    AslImportResult importFiles(List<Path> sources) throws AslParseException;
}
