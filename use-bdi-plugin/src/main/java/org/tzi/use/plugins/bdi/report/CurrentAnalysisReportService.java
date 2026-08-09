package org.tzi.use.plugins.bdi.report;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.Optional;

import org.tzi.use.plugins.bdi.application.CurrentAnalysisSnapshot;

/** Converts and atomically exports an already-composed analysis snapshot. */
public final class CurrentAnalysisReportService {

    public ReportData toReportData(String projectName, CurrentAnalysisSnapshot snapshot) {
        Objects.requireNonNull(snapshot, "snapshot");
        return new ReportData(
                requireText(projectName, "projectName"),
                snapshot.versions().pluginVersion(),
                snapshot.versions().useVersion(),
                snapshot.timestamp(),
                snapshot.issueCount(),
                snapshot.mappingCount(),
                snapshot.configurationOrigin(),
                snapshot.modelHash(),
                Optional.of(snapshot.mappingHash()),
                snapshot.issues(),
                snapshot.suppressions());
    }

    public Path export(
            String projectName,
            CurrentAnalysisSnapshot snapshot,
            ReportFormat format,
            Path output,
            boolean overwrite) throws IOException {
        Objects.requireNonNull(format, "format");
        Objects.requireNonNull(output, "output");
        Path target = output.toAbsolutePath().normalize();
        if (Files.exists(target) && !overwrite) {
            throw new IOException("Report already exists and overwrite was not confirmed: " + target);
        }
        Path parent = target.getParent();
        if (parent == null) {
            throw new IOException("Report path has no parent directory: " + target);
        }
        Files.createDirectories(parent);
        Path temporary = Files.createTempFile(parent, ".bdi-report-", ".tmp");
        try {
            ReportData data = toReportData(projectName, snapshot);
            if (format == ReportFormat.JSON) {
                ReportExporter.exportJson(data, temporary);
            } else {
                HtmlReportExporter.exportHtml(data, temporary);
            }
            moveCompletedReport(temporary, target, overwrite);
            return target;
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    private static void moveCompletedReport(Path temporary, Path target, boolean overwrite) throws IOException {
        StandardCopyOption[] options = overwrite
                ? new StandardCopyOption[] {StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING}
                : new StandardCopyOption[] {StandardCopyOption.ATOMIC_MOVE};
        try {
            Files.move(temporary, target, options);
        } catch (AtomicMoveNotSupportedException error) {
            if (overwrite) {
                Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
            } else {
                Files.move(temporary, target);
            }
        }
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return value;
    }
}
