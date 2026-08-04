package org.tzi.use.plugins.bdi.problems;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.tzi.use.plugins.bdi.application.BdiImportSnapshot;
import org.tzi.use.plugins.bdi.importer.AslDiagnostic;
import org.tzi.use.plugins.bdi.importer.AslDiagnosticSeverity;
import org.tzi.use.plugins.bdi.index.DuplicatePlanLabel;
import org.tzi.use.plugins.bdi.model.ir.AgentModel;
import org.tzi.use.plugins.bdi.model.ir.SourceSpan;
import org.tzi.use.plugins.bdi.model.ir.UnsupportedFeature;

/** Converts all retained import evidence into one deterministic problem list. */
public final class BdiProblemCollector {
    private BdiProblemCollector() {
    }

    public static List<BdiProblem> collect(BdiImportSnapshot snapshot) {
        List<BdiProblem> problems = new ArrayList<>();
        for (AslDiagnostic diagnostic : snapshot.diagnostics()) {
            problems.add(new BdiProblem(
                    diagnostic.code(),
                    diagnostic.severity() == AslDiagnosticSeverity.ERROR
                            ? BdiProblemSeverity.ERROR
                            : BdiProblemSeverity.WARNING,
                    diagnostic.source(),
                    diagnostic.line(),
                    diagnostic.column(),
                    diagnostic.message(),
                    "Import"));
        }
        for (AgentModel model : snapshot.models()) {
            for (UnsupportedFeature unsupported : model.unsupportedFeatures()) {
                SourceSpan span = unsupported.sourceSpan();
                problems.add(new BdiProblem(
                        unsupported.code(),
                        BdiProblemSeverity.WARNING,
                        span.source(),
                        span.hasLinePosition() ? span.beginLine() : 0,
                        span.hasColumnPosition() ? span.beginColumn() : 0,
                        unsupported.message(),
                        "Unsupported feature"));
            }
        }
        for (DuplicatePlanLabel duplicate : snapshot.index().duplicatePlanLabels()) {
            SourceSpan first = duplicate.occurrences().get(0);
            problems.add(new BdiProblem(
                    "BDI-INDEX-001",
                    BdiProblemSeverity.WARNING,
                    duplicate.source(),
                    first.hasLinePosition() ? first.beginLine() : 0,
                    first.hasColumnPosition() ? first.beginColumn() : 0,
                    "Duplicate plan label '" + duplicate.label() + "' ("
                            + duplicate.occurrences().size() + " occurrences)",
                    "Index validation"));
        }
        problems.sort(Comparator
                .comparing(BdiProblem::source)
                .thenComparingInt(problem -> problem.line() == 0 ? Integer.MAX_VALUE : problem.line())
                .thenComparing(BdiProblem::code)
                .thenComparing(BdiProblem::message));
        return List.copyOf(problems);
    }
}
