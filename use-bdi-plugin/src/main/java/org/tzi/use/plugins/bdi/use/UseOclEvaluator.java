package org.tzi.use.plugins.bdi.use;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Optional;

import org.tzi.use.parser.ocl.OCLCompiler;
import org.tzi.use.uml.ocl.expr.Evaluator;
import org.tzi.use.uml.ocl.expr.Expression;
import org.tzi.use.uml.ocl.value.Value;
import org.tzi.use.uml.sys.MSystem;

/** Read-only bridge around USE's compiler and evaluator. */
public final class UseOclEvaluator {
    public OclEvaluationResult evaluate(MSystem system, String expression) {
        if (system == null) {
            throw new NullPointerException("system");
        }
        if (expression == null || expression.isBlank()) {
            return new OclEvaluationResult(
                    expression == null ? "" : expression,
                    OclEvaluationStatus.COMPILE_ERROR,
                    Optional.empty(),
                    Optional.empty(),
                    List.of("OCL expression must not be blank"));
        }

        StringWriter compileErrors = new StringWriter();
        PrintWriter errorWriter = new PrintWriter(compileErrors);
        Expression compiled = OCLCompiler.compileExpression(
                system.model(),
                system.state(),
                expression,
                "BDI plugin expression",
                errorWriter,
                system.varBindings());
        errorWriter.flush();
        if (compiled == null) {
            return new OclEvaluationResult(
                    expression,
                    OclEvaluationStatus.COMPILE_ERROR,
                    Optional.empty(),
                    Optional.empty(),
                    diagnostics(compileErrors.toString()));
        }

        try {
            Value result = new Evaluator(false).eval(compiled, system.state(), system.varBindings());
            return new OclEvaluationResult(
                    expression,
                    OclEvaluationStatus.EVALUATED,
                    Optional.ofNullable(result).map(Value::toString),
                    Optional.ofNullable(result).map(value -> value.type().toString()),
                    diagnostics(compileErrors.toString()));
        } catch (RuntimeException error) {
            return new OclEvaluationResult(
                    expression,
                    OclEvaluationStatus.EVALUATION_ERROR,
                    Optional.empty(),
                    Optional.empty(),
                    List.of(error.getMessage() == null ? error.getClass().getSimpleName() : error.getMessage()));
        }
    }

    private static List<String> diagnostics(String text) {
        String trimmed = text == null ? "" : text.trim();
        return trimmed.isEmpty() ? List.of() : List.of(trimmed);
    }
}
