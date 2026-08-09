package org.tzi.use.plugins.bdi.casestudy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

class AuctionMetricsEvidenceTest {
    @Test
    void computesScopedClassificationMetricsForTheLabeledMutantCorpus() throws IOException {
        Path root = repositoryRoot();
        List<String> lines = Files.readAllLines(root.resolve(
                "docs/project/evidence/auction-metrics.csv"));
        assertEquals(5, lines.size());
        assertEquals("mutantId,family,ruleId,scope,baselineCount,mutantCount,delta,detected", lines.get(0));

        List<String[]> rows = lines.stream().skip(1)
                .map(line -> line.split(",", -1))
                .toList();
        assertEquals(4, rows.size());
        assertEquals(Set.of("MAP-003", "SIG-001", "REF-001", "OCL-001"),
                rows.stream().map(row -> row[2]).collect(Collectors.toSet()));
        assertTrue(rows.stream().allMatch(row -> "0".equals(row[4])));
        assertTrue(rows.stream().allMatch(row -> Integer.parseInt(row[5]) > 0));
        assertTrue(rows.stream().allMatch(row -> Integer.parseInt(row[6]) == Integer.parseInt(row[5])));

        long truePositives = rows.stream().filter(row -> "true".equals(row[7])).count();
        // Every row is an expected defect; the corpus has no clean negative rows.
        long falsePositives = 0;
        long falseNegatives = rows.stream().filter(row -> "false".equals(row[7])).count();
        assertEquals(4, truePositives);
        assertEquals(0, falsePositives);
        assertEquals(0, falseNegatives);

        double precision = truePositives / (double) (truePositives + falsePositives);
        double recall = truePositives / (double) (truePositives + falseNegatives);
        double f1 = 2.0 * precision * recall / (precision + recall);
        assertEquals(1.0, precision);
        assertEquals(1.0, recall);
        assertEquals(1.0, f1);

        String metricsDocument = Files.readString(root.resolve(
                "docs/project/evidence/auction-classification-metrics.md"));
        assertTrue(metricsDocument.contains("Precision | 1.000"));
        assertTrue(metricsDocument.contains("Recall | 1.000"));
        assertTrue(metricsDocument.contains("F1 | 1.000"));
    }

    private static Path repositoryRoot() {
        Path current = Path.of("").toAbsolutePath().normalize();
        while (current != null) {
            if (Files.isRegularFile(current.resolve("docs/project/16_PROJECT_COMPLETION_CHECKLIST.md"))) {
                return current;
            }
            current = current.getParent();
        }
        throw new IllegalStateException("Could not locate repository root from the test working directory");
    }
}
