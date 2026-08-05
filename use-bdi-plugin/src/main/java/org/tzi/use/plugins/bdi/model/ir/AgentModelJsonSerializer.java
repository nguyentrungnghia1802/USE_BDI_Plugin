package org.tzi.use.plugins.bdi.model.ir;

import java.nio.file.Path;
import java.util.Objects;

/** Deterministic JSON serializer used by golden IR tests and reports. */
public final class AgentModelJsonSerializer {
    public String serialize(AgentModel model) {
        return serialize(model, null);
    }

    public String serialize(AgentModel model, Path sourceRoot) {
        Objects.requireNonNull(model, "model");
        Path normalizedRoot = sourceRoot == null ? null : sourceRoot.toAbsolutePath().normalize();
        StringBuilder json = new StringBuilder();
        writeAgent(json, model, normalizedRoot);
        return json.toString();
    }

    private void writeAgent(StringBuilder json, AgentModel model, Path sourceRoot) {
        json.append('{');
        field(json, "source", quote(path(model.source(), sourceRoot)));
        comma(json);
        field(json, "parserVersion", quote(model.parserVersion()));
        comma(json);
        field(json, "beliefCount", Integer.toString(model.beliefCount()));
        comma(json);
        field(json, "goalCount", Integer.toString(model.goalCount()));
        comma(json);
        field(json, "planCount", Integer.toString(model.planCount()));
        comma(json);
        field(json, "beliefs", array(model.beliefs(), item -> writeBelief(item, sourceRoot)));
        comma(json);
        field(json, "goals", array(model.goals(), item -> writeGoal(item, sourceRoot)));
        comma(json);
        field(json, "plans", array(model.plans(), item -> writePlan(item, sourceRoot)));
        comma(json);
        field(json, "unsupportedFeatures",
                array(model.unsupportedFeatures(), item -> writeUnsupported(item, sourceRoot)));
        json.append('}');
    }

    private String writeBelief(BeliefModel belief, Path sourceRoot) {
        StringBuilder json = new StringBuilder("{");
        field(json, "literal", writeTerm(belief.literal(), sourceRoot));
        comma(json);
        field(json, "span", writeSpan(belief.sourceSpan(), sourceRoot));
        return json.append('}').toString();
    }

    private String writeGoal(GoalModel goal, Path sourceRoot) {
        StringBuilder json = new StringBuilder("{");
        field(json, "literal", writeTerm(goal.literal(), sourceRoot));
        comma(json);
        field(json, "span", writeSpan(goal.sourceSpan(), sourceRoot));
        return json.append('}').toString();
    }

    private String writePlan(PlanModel plan, Path sourceRoot) {
        StringBuilder json = new StringBuilder("{");
        field(json, "label", quote(plan.label()));
        comma(json);
        field(json, "trigger", writeTrigger(plan.trigger(), sourceRoot));
        comma(json);
        field(json, "context", plan.context().map(value -> writeContext(value, sourceRoot)).orElse("null"));
        comma(json);
        field(json, "steps", array(plan.steps(), value -> writeStep(value, sourceRoot)));
        comma(json);
        field(json, "span", writeSpan(plan.sourceSpan(), sourceRoot));
        return json.append('}').toString();
    }

    private String writeTrigger(TriggerModel trigger, Path sourceRoot) {
        StringBuilder json = new StringBuilder("{");
        field(json, "operator", quote(trigger.operator().name()));
        comma(json);
        field(json, "type", quote(trigger.type().name()));
        comma(json);
        field(json, "term", writeTerm(trigger.term(), sourceRoot));
        comma(json);
        field(json, "span", writeSpan(trigger.sourceSpan(), sourceRoot));
        return json.append('}').toString();
    }

    private String writeContext(ContextExpr context, Path sourceRoot) {
        StringBuilder json = new StringBuilder("{");
        if (context instanceof ContextLiteral literal) {
            field(json, "type", quote("literal"));
            comma(json);
            field(json, "literal", writeTerm(literal.literal(), sourceRoot));
            comma(json);
            field(json, "span", writeSpan(literal.sourceSpan(), sourceRoot));
        } else if (context instanceof ContextUnary unary) {
            field(json, "type", quote("unary"));
            comma(json);
            field(json, "operator", quote(unary.operator()));
            comma(json);
            field(json, "operand", writeContext(unary.operand(), sourceRoot));
            comma(json);
            field(json, "span", writeSpan(unary.sourceSpan(), sourceRoot));
        } else if (context instanceof ContextBinary binary) {
            field(json, "type", quote("binary"));
            comma(json);
            field(json, "operator", quote(binary.operator()));
            comma(json);
            field(json, "left", writeContext(binary.left(), sourceRoot));
            comma(json);
            field(json, "right", writeContext(binary.right(), sourceRoot));
            comma(json);
            field(json, "span", writeSpan(binary.sourceSpan(), sourceRoot));
        } else if (context instanceof ContextUnsupported unsupported) {
            field(json, "type", quote("unsupported"));
            comma(json);
            field(json, "feature", writeUnsupported(unsupported.feature(), sourceRoot));
            comma(json);
            field(json, "span", writeSpan(unsupported.sourceSpan(), sourceRoot));
        } else {
            throw new IllegalArgumentException("Unknown ContextExpr implementation: " + context.getClass());
        }
        return json.append('}').toString();
    }

    private String writeStep(PlanStepModel step, Path sourceRoot) {
        StringBuilder json = new StringBuilder("{");
        if (step instanceof ActionStepModel action) {
            field(json, "type", quote("action"));
            comma(json);
            field(json, "action", writeTerm(action.action(), sourceRoot));
        } else if (step instanceof InternalActionStepModel action) {
            field(json, "type", quote("internalAction"));
            comma(json);
            field(json, "action", writeTerm(action.action(), sourceRoot));
        } else if (step instanceof AchieveGoalStepModel achieve) {
            field(json, "type", quote("achieve"));
            comma(json);
            field(json, "goal", writeTerm(achieve.goal(), sourceRoot));
            comma(json);
            field(json, "newFocus", Boolean.toString(achieve.newFocus()));
        } else if (step instanceof TestStepModel test) {
            field(json, "type", quote("test"));
            comma(json);
            field(json, "condition", writeContext(test.condition(), sourceRoot));
        } else if (step instanceof BeliefUpdateStepModel update) {
            field(json, "type", quote("beliefUpdate"));
            comma(json);
            field(json, "operator", quote(update.operator().name()));
            comma(json);
            field(json, "focusPolicy", quote(update.focusPolicy().name()));
            comma(json);
            field(json, "belief", writeTerm(update.belief(), sourceRoot));
        } else if (step instanceof ConstraintStepModel constraint) {
            field(json, "type", quote("constraint"));
            comma(json);
            field(json, "condition", writeContext(constraint.condition(), sourceRoot));
        } else if (step instanceof UnsupportedStepModel unsupported) {
            field(json, "type", quote("unsupported"));
            comma(json);
            field(json, "feature", writeUnsupported(unsupported.feature(), sourceRoot));
        } else {
            throw new IllegalArgumentException("Unknown PlanStepModel implementation: " + step.getClass());
        }
        comma(json);
        field(json, "span", writeSpan(step.sourceSpan(), sourceRoot));
        return json.append('}').toString();
    }

    private String writeTerm(TermModel term, Path sourceRoot) {
        StringBuilder json = new StringBuilder("{");
        if (term instanceof LiteralTermModel literal) {
            field(json, "kind", quote("literal"));
            comma(json);
            field(json, "functor", quote(literal.functor()));
            comma(json);
            field(json, "arguments", array(literal.arguments(), value -> writeTerm(value, sourceRoot)));
            comma(json);
            field(json, "negated", Boolean.toString(literal.negated()));
            comma(json);
            field(json, "annotations", array(literal.annotations(), value -> writeTerm(value, sourceRoot)));
        } else if (term instanceof VariableTermModel variable) {
            field(json, "kind", quote("variable"));
            comma(json);
            field(json, "name", quote(variable.name()));
        } else if (term instanceof NumberTermModel number) {
            field(json, "kind", quote("number"));
            comma(json);
            field(json, "value", quote(number.value()));
        } else if (term instanceof StringTermModel string) {
            field(json, "kind", quote("string"));
            comma(json);
            field(json, "value", quote(string.value()));
        } else if (term instanceof CompoundTermModel compound) {
            field(json, "kind", quote("compound"));
            comma(json);
            field(json, "functor", quote(compound.functor()));
            comma(json);
            field(json, "arguments", array(compound.arguments(), value -> writeTerm(value, sourceRoot)));
        } else if (term instanceof ListTermModel list) {
            field(json, "kind", quote("list"));
            comma(json);
            field(json, "elements", array(list.elements(), value -> writeTerm(value, sourceRoot)));
            comma(json);
            field(json, "tail", list.tail().map(value -> writeTerm(value, sourceRoot)).orElse("null"));
        } else if (term instanceof SetTermModel set) {
            field(json, "kind", quote("set"));
            comma(json);
            field(json, "elements", array(set.elements(), value -> writeTerm(value, sourceRoot)));
        } else if (term instanceof ArithmeticTermModel arithmetic) {
            field(json, "kind", quote("arithmetic"));
            comma(json);
            field(json, "operator", quote(arithmetic.operator()));
            comma(json);
            field(json, "left", arithmetic.left().map(value -> writeTerm(value, sourceRoot)).orElse("null"));
            comma(json);
            field(json, "right", arithmetic.right().map(value -> writeTerm(value, sourceRoot)).orElse("null"));
        } else if (term instanceof UnsupportedTermModel unsupported) {
            field(json, "kind", quote("unsupported"));
            comma(json);
            field(json, "feature", writeUnsupported(unsupported.feature(), sourceRoot));
        } else {
            throw new IllegalArgumentException("Unknown TermModel implementation: " + term.getClass());
        }
        comma(json);
        field(json, "span", writeSpan(term.sourceSpan(), sourceRoot));
        return json.append('}').toString();
    }

    private String writeUnsupported(UnsupportedFeature feature, Path sourceRoot) {
        StringBuilder json = new StringBuilder("{");
        field(json, "code", quote(feature.code()));
        comma(json);
        field(json, "kind", quote(feature.kind()));
        comma(json);
        field(json, "subject", quote(feature.subject()));
        comma(json);
        field(json, "span", writeSpan(feature.sourceSpan(), sourceRoot));
        return json.append('}').toString();
    }

    private String writeSpan(SourceSpan span, Path sourceRoot) {
        StringBuilder json = new StringBuilder("{");
        field(json, "source", quote(path(span.source(), sourceRoot)));
        comma(json);
        field(json, "beginLine", Integer.toString(span.beginLine()));
        comma(json);
        field(json, "beginColumn", Integer.toString(span.beginColumn()));
        comma(json);
        field(json, "endLine", Integer.toString(span.endLine()));
        comma(json);
        field(json, "endColumn", Integer.toString(span.endColumn()));
        return json.append('}').toString();
    }

    private String path(Path value, Path sourceRoot) {
        Path normalized = value.toAbsolutePath().normalize();
        String rendered = sourceRoot != null && normalized.startsWith(sourceRoot)
                ? sourceRoot.relativize(normalized).toString()
                : normalized.toString();
        return rendered.replace('\\', '/');
    }

    private static void field(StringBuilder json, String name, String value) {
        json.append(quote(name)).append(':').append(value);
    }

    private static void comma(StringBuilder json) {
        json.append(',');
    }

    private static String quote(String value) {
        StringBuilder quoted = new StringBuilder(value.length() + 2).append('"');
        for (int index = 0; index < value.length(); index++) {
            char character = value.charAt(index);
            switch (character) {
                case '"' -> quoted.append("\\\"");
                case '\\' -> quoted.append("\\\\");
                case '\b' -> quoted.append("\\b");
                case '\f' -> quoted.append("\\f");
                case '\n' -> quoted.append("\\n");
                case '\r' -> quoted.append("\\r");
                case '\t' -> quoted.append("\\t");
                default -> {
                    if (character < 0x20) {
                        quoted.append(String.format("\\u%04x", (int) character));
                    } else {
                        quoted.append(character);
                    }
                }
            }
        }
        return quoted.append('"').toString();
    }

    private static <T> String array(Iterable<T> values, java.util.function.Function<T, String> serializer) {
        StringBuilder json = new StringBuilder("[");
        boolean first = true;
        for (T value : values) {
            if (!first) {
                json.append(',');
            }
            first = false;
            json.append(serializer.apply(value));
        }
        return json.append(']').toString();
    }
}
