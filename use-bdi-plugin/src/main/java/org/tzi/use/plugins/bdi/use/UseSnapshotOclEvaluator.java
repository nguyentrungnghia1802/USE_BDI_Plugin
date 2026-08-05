package org.tzi.use.plugins.bdi.use;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.tzi.use.parser.soil.SoilCompiler;
import org.tzi.use.plugins.bdi.model.ir.CompoundTermModel;
import org.tzi.use.plugins.bdi.model.ir.LiteralTermModel;
import org.tzi.use.plugins.bdi.model.ir.NumberTermModel;
import org.tzi.use.plugins.bdi.model.ir.StringTermModel;
import org.tzi.use.plugins.bdi.model.ir.TermModel;
import org.tzi.use.plugins.bdi.validation.BoundedEffectResult;
import org.tzi.use.plugins.bdi.validation.BoundedEffectStatus;
import org.tzi.use.plugins.bdi.validation.OclSnapshotResult;
import org.tzi.use.plugins.bdi.validation.OclSnapshotStatus;
import org.tzi.use.plugins.bdi.validation.SnapshotOclEvaluator;
import org.tzi.use.uml.ocl.value.BooleanValue;
import org.tzi.use.uml.ocl.value.IntegerValue;
import org.tzi.use.uml.ocl.value.RealValue;
import org.tzi.use.uml.ocl.value.StringValue;
import org.tzi.use.uml.ocl.value.Value;
import org.tzi.use.uml.ocl.value.VarBindings;
import org.tzi.use.uml.sys.MObject;
import org.tzi.use.uml.sys.MSystem;
import org.tzi.use.uml.sys.soil.MStatement;

/**
 * USE-backed evaluator that only reads snapshots except within a disposable
 * variation used for an explicitly configured {@code soil:} effect.
 */
public final class UseSnapshotOclEvaluator implements SnapshotOclEvaluator {
    private static final String SOIL_PREFIX = "soil:";

    private final MSystem system;
    private final UseOclEvaluator evaluator;

    public UseSnapshotOclEvaluator(MSystem system) {
        this(system, new UseOclEvaluator());
    }

    UseSnapshotOclEvaluator(MSystem system, UseOclEvaluator evaluator) {
        this.system = Objects.requireNonNull(system, "system");
        this.evaluator = Objects.requireNonNull(evaluator, "evaluator");
    }

    @Override
    public List<OclSnapshotResult> evaluatePreconditions(
            UmlOperationRef operation,
            String receiverObject,
            List<TermModel> arguments) {
        Objects.requireNonNull(operation, "operation");
        Objects.requireNonNull(receiverObject, "receiverObject");
        arguments = List.copyOf(Objects.requireNonNull(arguments, "arguments"));
        if (arguments.size() != operation.parameters().size()) {
            return List.of(unknown(operation.reference(), "Argument count does not match mapped operation"));
        }

        MObject receiver = system.state().objectByName(receiverObject);
        if (receiver == null) {
            return List.of(unknown(operation.reference(), "Receiver object is absent from the current USE state: " + receiverObject));
        }
        VarBindings bindings = new VarBindings(system.varBindings());
        bindings.push("self", receiver.value());
        for (int index = 0; index < arguments.size(); index++) {
            Optional<Value> value = valueFor(arguments.get(index));
            if (value.isEmpty()) {
                return List.of(unknown(
                        operation.reference(),
                        "Cannot bind argument " + index + ": " + arguments.get(index).render()));
            }
            bindings.push(operation.parameters().get(index).name(), value.orElseThrow());
        }

        return operation.preconditions().stream()
                .map(condition -> toSnapshotResult(
                        condition.name(), evaluator.evaluate(system, condition.expression(), bindings)))
                .toList();
    }

    @Override
    public OclSnapshotResult evaluateExpression(String expression, String subject) {
        return toSnapshotResult(subject, evaluator.evaluate(system, expression));
    }

    @Override
    public BoundedEffectResult simulateSoilEffect(String source) {
        if (source == null || !source.startsWith(SOIL_PREFIX) || source.substring(SOIL_PREFIX.length()).isBlank()) {
            return new BoundedEffectResult(BoundedEffectStatus.SKIPPED, List.of(
                    "No supported effect expression; use an ACTION_OPERATION mapping expression beginning with soil:"));
        }
        String soil = source.substring(SOIL_PREFIX.length()).trim();
        StringWriter output = new StringWriter();
        boolean variationStarted = false;
        try {
            system.beginVariation();
            variationStarted = true;
            MStatement statement = SoilCompiler.compileStatement(
                    system.model(),
                    system.state(),
                    system.getVariableEnvironment(),
                    soil,
                    "BDI bounded effect",
                    new PrintWriter(output),
                    false);
            if (statement == null) {
                return result(BoundedEffectStatus.UNKNOWN, output, "SOIL effect did not compile");
            }
            if (!system.execute(statement, false, false, false).wasSuccessfull()) {
                return result(BoundedEffectStatus.UNKNOWN, output, "SOIL effect execution failed");
            }
            boolean valid = system.state().check(new PrintWriter(output), false, false, false, List.of());
            return result(
                    valid ? BoundedEffectStatus.PASS : BoundedEffectStatus.INVARIANT_VIOLATED,
                    output,
                    valid ? "All active invariants hold in the bounded variation" : "An invariant failed in the bounded variation");
        } catch (Exception error) {
            return result(BoundedEffectStatus.UNKNOWN, output, error.getMessage() == null
                    ? error.getClass().getSimpleName()
                    : error.getMessage());
        } finally {
            if (variationStarted) {
                try {
                    system.endVariation();
                } catch (Exception error) {
                    throw new IllegalStateException("Could not restore the USE variation", error);
                }
            }
        }
    }

    private Optional<Value> valueFor(TermModel term) {
        if (term instanceof StringTermModel string) {
            return Optional.of(new StringValue(string.value()));
        }
        if (term instanceof NumberTermModel number) {
            try {
                return number.value().matches("[+-]?\\d+")
                        ? Optional.of(IntegerValue.valueOf(Integer.parseInt(number.value())))
                        : Optional.of(new RealValue(Double.parseDouble(number.value())));
            } catch (NumberFormatException error) {
                return Optional.empty();
            }
        }
        if (term instanceof LiteralTermModel literal && literal.arguments().isEmpty()) {
            if (literal.functor().equals("true") || literal.functor().equals("false")) {
                return Optional.of(BooleanValue.get(Boolean.parseBoolean(literal.functor())));
            }
            return Optional.ofNullable(system.state().objectByName(literal.functor())).map(MObject::value);
        }
        if (term instanceof CompoundTermModel compound && compound.arguments().isEmpty()) {
            return Optional.ofNullable(system.state().objectByName(compound.functor())).map(MObject::value);
        }
        return Optional.empty();
    }

    private static OclSnapshotResult toSnapshotResult(String subject, OclEvaluationResult result) {
        List<String> evidence = new ArrayList<>(result.diagnostics());
        evidence.add("Expression: " + result.expression());
        result.value().ifPresent(value -> evidence.add("Value: " + value));
        if (result.status() == OclEvaluationStatus.EVALUATED && result.value().filter("true"::equals).isPresent()) {
            return new OclSnapshotResult(subject, OclSnapshotStatus.PASS, evidence);
        }
        if (result.status() == OclEvaluationStatus.EVALUATED && result.value().filter("false"::equals).isPresent()) {
            return new OclSnapshotResult(subject, OclSnapshotStatus.FAIL, evidence);
        }
        evidence.add("OCL status: " + result.status());
        return new OclSnapshotResult(subject, OclSnapshotStatus.UNKNOWN, evidence);
    }

    private static OclSnapshotResult unknown(String subject, String evidence) {
        return new OclSnapshotResult(subject, OclSnapshotStatus.UNKNOWN, List.of(evidence));
    }

    private static BoundedEffectResult result(BoundedEffectStatus status, StringWriter output, String summary) {
        String detail = output.toString().trim();
        return new BoundedEffectResult(status, detail.isEmpty() ? List.of(summary) : List.of(summary, detail));
    }
}
