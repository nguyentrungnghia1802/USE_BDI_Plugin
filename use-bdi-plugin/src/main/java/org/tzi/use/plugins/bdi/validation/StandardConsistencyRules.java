package org.tzi.use.plugins.bdi.validation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.tzi.use.plugins.bdi.importer.AslDiagnostic;
import org.tzi.use.plugins.bdi.index.ActionCallSite;
import org.tzi.use.plugins.bdi.index.AgentObjectReference;
import org.tzi.use.plugins.bdi.index.DuplicatePlanLabel;
import org.tzi.use.plugins.bdi.index.PredicateReference;
import org.tzi.use.plugins.bdi.index.PredicateSignature;
import org.tzi.use.plugins.bdi.model.ir.AgentModel;
import org.tzi.use.plugins.bdi.model.ir.CompoundTermModel;
import org.tzi.use.plugins.bdi.model.ir.LiteralTermModel;
import org.tzi.use.plugins.bdi.model.ir.NumberTermModel;
import org.tzi.use.plugins.bdi.model.ir.PlanModel;
import org.tzi.use.plugins.bdi.model.ir.PlanStepModel;
import org.tzi.use.plugins.bdi.model.ir.SourceSpan;
import org.tzi.use.plugins.bdi.model.ir.StringTermModel;
import org.tzi.use.plugins.bdi.model.ir.TermModel;
import org.tzi.use.plugins.bdi.model.mapping.MappingBinding;
import org.tzi.use.plugins.bdi.model.mapping.MappingKind;
import org.tzi.use.plugins.bdi.model.mapping.MappingSourceId;
import org.tzi.use.plugins.bdi.model.mapping.MappingStaleness;
import org.tzi.use.plugins.bdi.model.mapping.MappingStalenessDetector;
import org.tzi.use.plugins.bdi.model.mapping.MappingStalenessReason;
import org.tzi.use.plugins.bdi.use.UmlClassRef;
import org.tzi.use.plugins.bdi.use.UmlObjectRef;
import org.tzi.use.plugins.bdi.use.UmlOperationRef;
import org.tzi.use.plugins.bdi.use.UseModelSnapshot;

/** Factory for the first static rule set from the documented MVP catalog. */
final class StandardConsistencyRules {
    private StandardConsistencyRules() {
    }

    static List<ConsistencyRule> create() {
        return List.of(
                rule("ASL-001", RulePhase.PARSE, StandardConsistencyRules::parseErrors),
                rule("ASL-002", RulePhase.PARSE, StandardConsistencyRules::unsupportedSyntax),
                rule("BDI-001", RulePhase.IR_WELL_FORMEDNESS, StandardConsistencyRules::duplicatePlanLabels),
                rule("BDI-002", RulePhase.IR_WELL_FORMEDNESS, StandardConsistencyRules::unsupportedGoals),
                rule("BDI-003", RulePhase.IR_WELL_FORMEDNESS, StandardConsistencyRules::invalidTriggers),
                rule("BDI-004", RulePhase.IR_WELL_FORMEDNESS, StandardConsistencyRules::invalidStepOrder),
                rule("REF-001", RulePhase.REFERENCE, StandardConsistencyRules::unresolvedReferences),
                rule("REF-002", RulePhase.REFERENCE, StandardConsistencyRules::unresolvedTestReferences),
                rule("MAP-001", RulePhase.MAPPING, StandardConsistencyRules::unmappedAgents),
                rule("MAP-002", RulePhase.MAPPING, StandardConsistencyRules::unmappedActions),
                rule("MAP-003", RulePhase.MAPPING, StandardConsistencyRules::staleMappings),
                rule("SIG-001", RulePhase.SIGNATURE, StandardConsistencyRules::arityMismatches),
                rule("SIG-002", RulePhase.SIGNATURE, StandardConsistencyRules::typeMismatches),
                rule("SIG-003", RulePhase.SIGNATURE, StandardConsistencyRules::unknownTypes),
                rule("OWN-001", RulePhase.SIGNATURE, StandardConsistencyRules::wrongOwners));
    }

    private static List<ConsistencyIssue> parseErrors(ValidationContext context) {
        return context.diagnostics().stream()
                .filter(diagnostic -> diagnostic.code().equals(AslDiagnostic.SYNTAX_ERROR_CODE))
                .map(diagnostic -> issue(
                        "ASL-001",
                        IssueSeverity.ERROR,
                        "AgentSpeak parser rejected the source: " + diagnostic.message(),
                        span(diagnostic),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        List.of("Parser diagnostic: " + diagnostic.message()),
                        "Fix the AgentSpeak syntax at the reported location.",
                        IssueCertainty.CONFIRMED))
                .toList();
    }

    private static List<ConsistencyIssue> unsupportedSyntax(ValidationContext context) {
        return context.agents().stream()
                .flatMap(agent -> agent.unsupportedFeatures().stream())
                .map(feature -> issue(
                        "ASL-002",
                        IssueSeverity.WARNING,
                        feature.message(),
                        feature.sourceSpan(),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        List.of("Unsupported kind: " + feature.kind(), "Subject: " + feature.subject()),
                        "Use a supported AgentSpeak subset or extend the normalizer with evidence.",
                        IssueCertainty.CONFIRMED))
                .toList();
    }

    private static List<ConsistencyIssue> duplicatePlanLabels(ValidationContext context) {
        return context.index().duplicatePlanLabels().stream()
                .map(duplicate -> duplicateIssue(duplicate))
                .toList();
    }

    private static ConsistencyIssue duplicateIssue(DuplicatePlanLabel duplicate) {
        SourceSpan first = duplicate.occurrences().get(0);
        return issue(
                "BDI-001",
                IssueSeverity.ERROR,
                "Duplicate explicit plan label '" + duplicate.label() + "'",
                first,
                Optional.of(duplicate.source().toString()),
                Optional.of(duplicate.label()),
                Optional.empty(),
                List.of("Occurrences: " + duplicate.occurrences().size(), "Source: " + duplicate.source()),
                "Give each explicit plan label a unique name in this agent source.",
                IssueCertainty.CONFIRMED);
    }

    private static List<ConsistencyIssue> unsupportedGoals(ValidationContext context) {
        return context.index().allPredicateReferences().stream()
                .filter(reference -> reference.kind() == PredicateReference.PredicateReferenceKind.INITIAL_GOAL
                        || reference.kind() == PredicateReference.PredicateReferenceKind.ACHIEVE_GOAL)
                .filter(reference -> context.index().supportingPlans(reference.signature()).isEmpty())
                .map(reference -> issue(
                        "BDI-002",
                        IssueSeverity.ERROR,
                        "Goal " + reference.signature() + " has no supporting achievement plan",
                        reference.sourceSpan(),
                        Optional.of(reference.sourceSpan().source().toString()),
                        optionalText(reference.planLabel()),
                        Optional.empty(),
                        List.of("Goal signature: " + reference.signature(),
                                "No plan trigger with +!" + reference.signature()),
                        "Add a matching achievement-triggered plan or correct the goal signature.",
                        IssueCertainty.CONFIRMED))
                .toList();
    }

    private static List<ConsistencyIssue> invalidTriggers(ValidationContext context) {
        return context.agents().stream()
                .flatMap(agent -> agent.plans().stream().map(plan -> new PlanLocation(agent, plan)))
                .filter(location -> !(location.plan().trigger().term() instanceof LiteralTermModel))
                .map(location -> issue(
                        "BDI-003",
                        IssueSeverity.ERROR,
                        "Plan trigger has no literal AgentSpeak signature",
                        location.plan().sourceSpan(),
                        Optional.of(MappingSourceId.agent(location.agent())),
                        optionalText(location.plan().label()),
                        Optional.empty(),
                        List.of("Trigger term: " + location.plan().trigger().term().render(),
                                "Trigger type: " + location.plan().trigger().type()),
                        "Use a literal belief, achievement, test, or signal trigger.",
                        IssueCertainty.CONFIRMED))
                .toList();
    }

    private static List<ConsistencyIssue> invalidStepOrder(ValidationContext context) {
        List<ConsistencyIssue> issues = new ArrayList<>();
        for (AgentModel agent : context.agents()) {
            for (PlanModel plan : agent.plans()) {
                int previousLine = 0;
                for (PlanStepModel step : plan.steps()) {
                    SourceSpan span = step.sourceSpan();
                    if (span.hasLinePosition() && previousLine > span.beginLine()) {
                        issues.add(issue(
                                "BDI-004",
                                IssueSeverity.ERROR,
                                "Plan steps are not ordered by their source locations",
                                span,
                                Optional.of(MappingSourceId.agent(agent)),
                                optionalText(plan.label()),
                                Optional.empty(),
                                List.of("Previous step line: " + previousLine,
                                        "Current step line: " + span.beginLine()),
                                "Restore the source order of plan body steps.",
                                IssueCertainty.CONFIRMED));
                    }
                    if (span.hasLinePosition()) {
                        previousLine = span.beginLine();
                    }
                }
            }
        }
        return List.copyOf(issues);
    }

    private static List<ConsistencyIssue> unresolvedReferences(ValidationContext context) {
        Optional<UseModelSnapshot> uml = context.uml();
        if (uml.isEmpty()) {
            return List.of();
        }
        Set<String> objectNames = uml.orElseThrow().objects().stream()
                .map(UmlObjectRef::reference)
                .collect(Collectors.toSet());
        List<ConsistencyIssue> issues = new ArrayList<>();
        allReferences(context.index().agentReferencesByName().values()).stream()
                .filter(reference -> !reference.dynamic())
                .filter(reference -> context.mapping()
                        .find(MappingKind.RECEIVER_OBJECT, MappingSourceId.receiver(reference))
                        .map(MappingBinding::target)
                        .filter(objectNames::contains)
                        .isEmpty())
                .forEach(reference -> issues.add(issue(
                        "REF-001",
                        IssueSeverity.ERROR,
                        "Agent receiver '" + reference.name() + "' does not resolve to a current USE object",
                        reference.sourceSpan(),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        List.of("Reference origin: " + reference.origin(),
                                "Expected receiver mapping key: " + MappingSourceId.receiver(reference)),
                        "Map the receiver to an existing USE object or correct the receiver name.",
                        IssueCertainty.CONFIRMED)));
        allReferences(context.index().objectReferencesByName().values()).stream()
                .filter(reference -> !objectNames.contains(reference.name()))
                .forEach(reference -> issues.add(issue(
                        "REF-001",
                        IssueSeverity.ERROR,
                        "Named object reference '" + reference.name() + "' is absent from the current USE state",
                        reference.sourceSpan(),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.of(reference.name()),
                        List.of("Reference origin: " + reference.origin(), "Rendered term: " + reference.rendered()),
                        "Create/map the USE object or replace the AgentSpeak reference.",
                        IssueCertainty.POTENTIAL)));
        return List.copyOf(issues);
    }

    private static List<ConsistencyIssue> unresolvedTestReferences(ValidationContext context) {
        return context.index().allPredicateReferences().stream()
                .filter(reference -> reference.kind() == PredicateReference.PredicateReferenceKind.TEST)
                .filter(reference -> !isKnownTestReference(context, reference.signature()))
                .map(reference -> issue(
                        "REF-002",
                        IssueSeverity.WARNING,
                        "Test predicate " + reference.signature() + " has no retained belief or belief mapping",
                        reference.sourceSpan(),
                        Optional.empty(),
                        optionalText(reference.planLabel()),
                        Optional.empty(),
                        List.of("Test predicate: " + reference.signature()),
                        "Add a supporting belief/rule or map the predicate to a UML attribute.",
                        IssueCertainty.POTENTIAL))
                .toList();
    }

    private static boolean isKnownTestReference(ValidationContext context, PredicateSignature signature) {
        boolean initialBelief = context.index().predicateReferences(signature).stream()
                .anyMatch(reference -> reference.kind() == PredicateReference.PredicateReferenceKind.INITIAL_BELIEF);
        return initialBelief || context.mapping().find(MappingKind.BELIEF_ATTRIBUTE, MappingSourceId.belief(signature)).isPresent();
    }

    private static List<ConsistencyIssue> unmappedAgents(ValidationContext context) {
        return context.agents().stream()
                .filter(agent -> context.mapping().find(MappingKind.AGENT_CLASS, MappingSourceId.agent(agent)).isEmpty())
                .filter(agent -> context.mapping().find(MappingKind.AGENT_OBJECT, MappingSourceId.agent(agent)).isEmpty())
                .map(agent -> issue(
                        "MAP-001",
                        IssueSeverity.ERROR,
                        "Agent source has no UML class or object mapping",
                        SourceSpan.unknown(agent.source()),
                        Optional.of(MappingSourceId.agent(agent)),
                        Optional.empty(),
                        Optional.empty(),
                        List.of("Agent source: " + MappingSourceId.agent(agent)),
                        "Map the agent source to a UML class or a current UML object.",
                        IssueCertainty.CONFIRMED))
                .toList();
    }

    private static List<ConsistencyIssue> unmappedActions(ValidationContext context) {
        return externalActions(context).stream()
                .filter(action -> context.mapping().find(MappingKind.ACTION_OPERATION, MappingSourceId.action(action)).isEmpty())
                .map(action -> issue(
                        "MAP-002",
                        IssueSeverity.ERROR,
                        "External action '" + action.rendered() + "' has no UML operation mapping",
                        action.sourceSpan(),
                        Optional.of(context.agentSource(action)),
                        optionalText(action.planLabel()),
                        Optional.empty(),
                        List.of("Action source: " + MappingSourceId.action(action)),
                        "Map the external action to a UML operation before signature/OCL checks.",
                        IssueCertainty.CONFIRMED))
                .toList();
    }

    private static List<ConsistencyIssue> staleMappings(ValidationContext context) {
        if (context.uml().isEmpty()) {
            return List.of();
        }
        return new MappingStalenessDetector().detect(
                        context.agents(), context.index(), context.mapping(), context.uml().orElseThrow())
                .stream()
                .filter(finding -> finding.reason() == MappingStalenessReason.SOURCE_MISSING
                        || finding.reason() == MappingStalenessReason.TARGET_MISSING)
                .map(finding -> staleIssue(context, finding))
                .toList();
    }

    private static ConsistencyIssue staleIssue(ValidationContext context, MappingStaleness finding) {
        MappingBinding binding = finding.binding().orElseThrow();
        SourceSpan span = finding.sourceSpan().orElseGet(() -> MappingSourceId.sourcePath(binding.source())
                .map(SourceSpan::unknown)
                .orElseGet(() -> SourceSpan.unknown(context.agents().isEmpty()
                        ? java.nio.file.Path.of("mapping.bdimap.json")
                        : context.agents().get(0).source())));
        return issue(
                "MAP-003",
                IssueSeverity.ERROR,
                finding.message(),
                span,
                Optional.empty(),
                Optional.empty(),
                Optional.of(binding.target()),
                finding.evidence(),
                "Refresh or remove the stale mapping binding.",
                IssueCertainty.CONFIRMED);
    }

    private static List<ConsistencyIssue> arityMismatches(ValidationContext context) {
        if (context.uml().isEmpty()) {
            return List.of();
        }
        return externalActions(context).stream()
                .flatMap(action -> operationFor(context, action).stream().map(operation -> new ActionOperation(action, operation)))
                .filter(pair -> pair.action().signature().isPresent())
                .filter(pair -> pair.action().signature().orElseThrow().arity() != pair.operation().parameters().size())
                .map(pair -> issue(
                        "SIG-001",
                        IssueSeverity.ERROR,
                        "Action arity does not match mapped operation parameters",
                        pair.action().sourceSpan(),
                        Optional.of(context.agentSource(pair.action())),
                        optionalText(pair.action().planLabel()),
                        Optional.of(pair.operation().reference()),
                        List.of("Action arity: " + pair.action().signature().orElseThrow().arity(),
                                "Operation parameter count: " + pair.operation().parameters().size()),
                        "Map an operation with matching arity or correct the action arguments.",
                        IssueCertainty.CONFIRMED))
                .toList();
    }

    private static List<ConsistencyIssue> typeMismatches(ValidationContext context) {
        if (context.uml().isEmpty()) {
            return List.of();
        }
        List<ConsistencyIssue> issues = new ArrayList<>();
        for (ActionCallSite action : externalActions(context)) {
            Optional<UmlOperationRef> operation = operationFor(context, action);
            Optional<TermModel> term = context.actionTerm(action);
            if (operation.isEmpty() || term.isEmpty()) {
                continue;
            }
            List<TermModel> arguments = arguments(term.orElseThrow());
            if (arguments.size() != operation.orElseThrow().parameters().size()) {
                continue;
            }
            for (int index = 0; index < arguments.size(); index++) {
                Optional<String> actualType = inferredType(arguments.get(index));
                String expectedType = operation.orElseThrow().parameters().get(index).type();
                if (actualType.isPresent() && !compatible(actualType.orElseThrow(), expectedType)) {
                    issues.add(issue(
                            "SIG-002",
                            IssueSeverity.ERROR,
                            "Argument " + index + " type " + actualType.orElseThrow()
                                    + " is incompatible with parameter type " + expectedType,
                            arguments.get(index).sourceSpan(),
                            Optional.of(context.agentSource(action)),
                            optionalText(action.planLabel()),
                            Optional.of(operation.orElseThrow().reference()),
                            List.of("Argument: " + arguments.get(index).render(),
                                    "Expected type: " + expectedType),
                            "Adjust the argument or map it to a compatible UML parameter.",
                            IssueCertainty.CONFIRMED));
                }
            }
        }
        return List.copyOf(issues);
    }

    private static List<ConsistencyIssue> unknownTypes(ValidationContext context) {
        if (context.uml().isEmpty()) {
            return List.of();
        }
        List<ConsistencyIssue> issues = new ArrayList<>();
        for (ActionCallSite action : externalActions(context)) {
            Optional<UmlOperationRef> operation = operationFor(context, action);
            Optional<TermModel> term = context.actionTerm(action);
            if (operation.isEmpty() || term.isEmpty()) {
                continue;
            }
            List<TermModel> arguments = arguments(term.orElseThrow());
            if (arguments.size() != operation.orElseThrow().parameters().size()) {
                continue;
            }
            for (int index = 0; index < arguments.size(); index++) {
                if (inferredType(arguments.get(index)).isEmpty()) {
                    issues.add(issue(
                            "SIG-003",
                            IssueSeverity.WARNING,
                            "Argument " + index + " type cannot be inferred statically",
                            arguments.get(index).sourceSpan(),
                            Optional.of(context.agentSource(action)),
                            optionalText(action.planLabel()),
                            Optional.of(operation.orElseThrow().reference()),
                            List.of("Argument: " + arguments.get(index).render(),
                                    "Expected type: " + operation.orElseThrow().parameters().get(index).type()),
                            "Add a parameter binding or provide a runtime/snapshot type evidence.",
                            IssueCertainty.UNKNOWN));
                }
            }
        }
        return List.copyOf(issues);
    }

    private static List<ConsistencyIssue> wrongOwners(ValidationContext context) {
        if (context.uml().isEmpty()) {
            return List.of();
        }
        return externalActions(context).stream()
                .flatMap(action -> operationFor(context, action).stream().map(operation -> new ActionOperation(action, operation)))
                .flatMap(pair -> executingClass(context, pair.action()).stream()
                        .map(owner -> new ActionOwner(pair, owner)))
                .filter(pair -> !ownerCompatible(context.uml().orElseThrow(), pair.owner(), pair.operation().operation().ownerName()))
                .map(pair -> issue(
                        "OWN-001",
                        IssueSeverity.ERROR,
                        "Mapped operation belongs to " + pair.operation().operation().ownerName()
                                + " but the executing agent maps to " + pair.owner(),
                        pair.operation().action().sourceSpan(),
                        Optional.of(context.agentSource(pair.operation().action())),
                        optionalText(pair.operation().action().planLabel()),
                        Optional.of(pair.operation().operation().reference()),
                        List.of("Executing agent class: " + pair.owner(),
                                "Operation owner: " + pair.operation().operation().ownerName()),
                        "Map the agent/receiver to a compatible owner or choose an operation of its class.",
                        IssueCertainty.CONFIRMED))
                .toList();
    }

    private static Optional<UmlOperationRef> operationFor(ValidationContext context, ActionCallSite action) {
        return context.uml().flatMap(uml -> context.mapping()
                .find(MappingKind.ACTION_OPERATION, MappingSourceId.action(action))
                .flatMap(binding -> uml.operations().stream()
                        .filter(operation -> operation.reference().equals(binding.target()))
                        .findFirst()));
    }

    private static Optional<String> executingClass(ValidationContext context, ActionCallSite action) {
        String agentSource = context.agentSource(action);
        Optional<MappingBinding> classBinding = context.mapping().find(MappingKind.AGENT_CLASS, agentSource);
        if (classBinding.isPresent()) {
            return classBinding.map(MappingBinding::target);
        }
        return context.uml().flatMap(uml -> context.mapping()
                .find(MappingKind.AGENT_OBJECT, agentSource)
                .flatMap(binding -> uml.objects().stream()
                        .filter(object -> object.reference().equals(binding.target()))
                        .map(UmlObjectRef::className)
                        .findFirst()));
    }

    private static boolean ownerCompatible(UseModelSnapshot uml, String executingClass, String operationOwner) {
        if (executingClass.equals(operationOwner)) {
            return true;
        }
        return uml.classes().stream()
                .filter(type -> type.reference().equals(executingClass))
                .map(UmlClassRef::parentNames)
                .anyMatch(parents -> parents.contains(operationOwner));
    }

    private static List<ActionCallSite> externalActions(ValidationContext context) {
        return context.index().allActionCallSites().stream()
                .filter(action -> action.kind() == ActionCallSite.ActionKind.EXTERNAL_ACTION)
                .toList();
    }

    private static List<AgentObjectReference> allReferences(
            Collection<List<AgentObjectReference>> references) {
        return references.stream().flatMap(List::stream)
                .sorted(Comparator
                        .comparing((AgentObjectReference reference) -> reference.sourceSpan().source().toString())
                        .thenComparingInt(reference -> reference.sourceSpan().beginLine())
                        .thenComparing(AgentObjectReference::name))
                .toList();
    }

    private static List<TermModel> arguments(TermModel action) {
        if (action instanceof LiteralTermModel literal) {
            return literal.arguments();
        }
        if (action instanceof CompoundTermModel compound) {
            return compound.arguments();
        }
        return List.of();
    }

    private static Optional<String> inferredType(TermModel term) {
        if (term instanceof StringTermModel) {
            return Optional.of("String");
        }
        if (term instanceof NumberTermModel number) {
            return Optional.of(number.value().matches("[+-]?\\d+") ? "Integer" : "Real");
        }
        if (term instanceof LiteralTermModel literal
                && (literal.functor().equals("true") || literal.functor().equals("false"))
                && literal.arguments().isEmpty()) {
            return Optional.of("Boolean");
        }
        return Optional.empty();
    }

    private static boolean compatible(String actual, String expected) {
        return normalizedType(actual).equals(normalizedType(expected));
    }

    private static String normalizedType(String type) {
        String compact = type.replaceAll("\\s+", "").toLowerCase(Locale.ROOT);
        if (compact.equals("int") || compact.endsWith("::integer")) {
            return "integer";
        }
        if (compact.equals("double") || compact.equals("float") || compact.endsWith("::real")) {
            return "real";
        }
        if (compact.endsWith("::string")) {
            return "string";
        }
        if (compact.endsWith("::boolean")) {
            return "boolean";
        }
        return compact;
    }

    private static SourceSpan span(AslDiagnostic diagnostic) {
        if (diagnostic.hasSourcePosition()) {
            return new SourceSpan(
                    diagnostic.source(), diagnostic.line(), diagnostic.column(), diagnostic.line(), diagnostic.column());
        }
        return SourceSpan.unknown(diagnostic.source());
    }

    private static Optional<String> optionalText(String value) {
        return value == null || value.isBlank() ? Optional.empty() : Optional.of(value);
    }

    private static ConsistencyIssue issue(
            String ruleId,
            IssueSeverity severity,
            String message,
            SourceSpan sourceSpan,
            Optional<String> agentId,
            Optional<String> planId,
            Optional<String> umlElementRef,
            List<String> evidence,
            String suggestedFix,
            IssueCertainty certainty) {
        return new ConsistencyIssue(
                ruleId,
                severity,
                IssueStatus.OPEN,
                message,
                agentId,
                planId,
                Optional.of(sourceSpan),
                umlElementRef,
                evidence,
                Optional.of(suggestedFix),
                certainty);
    }

    private static ConsistencyRule rule(String id, RulePhase phase, Function<ValidationContext, List<ConsistencyIssue>> evaluator) {
        return new CatalogRule(id, phase, evaluator);
    }

    private record CatalogRule(
            String id,
            RulePhase phase,
            Function<ValidationContext, List<ConsistencyIssue>> evaluator) implements ConsistencyRule {
        @Override
        public List<ConsistencyIssue> evaluate(ValidationContext context) {
            return List.copyOf(evaluator.apply(context));
        }
    }

    private record PlanLocation(AgentModel agent, PlanModel plan) {
    }

    private record ActionOperation(ActionCallSite action, UmlOperationRef operation) {
    }

    private record ActionOwner(ActionOperation operation, String owner) {
    }
}
