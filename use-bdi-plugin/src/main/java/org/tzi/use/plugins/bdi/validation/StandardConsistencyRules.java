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
import org.tzi.use.plugins.bdi.model.ir.ContextBinary;
import org.tzi.use.plugins.bdi.model.ir.ContextExpr;
import org.tzi.use.plugins.bdi.model.ir.ContextLiteral;
import org.tzi.use.plugins.bdi.model.ir.ContextUnary;
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
                rule("OWN-001", RulePhase.SIGNATURE, StandardConsistencyRules::wrongOwners),
                rule("BEL-001", RulePhase.MAPPING, StandardConsistencyRules::unmappedBeliefs),
                rule("MSG-001", RulePhase.REFERENCE, StandardConsistencyRules::unknownMessageReceivers),
                rule("OCL-001", RulePhase.SNAPSHOT_OCL, StandardConsistencyRules::failedPreconditions),
                rule("OCL-002", RulePhase.SNAPSHOT_OCL, StandardConsistencyRules::unknownPreconditions),
                rule("CTX-001", RulePhase.SNAPSHOT_OCL, StandardConsistencyRules::contradictingContexts),
                rule("OCL-003", RulePhase.BOUNDED_SIMULATION, StandardConsistencyRules::violatedBoundedEffects),
                rule("OCL-004", RulePhase.BOUNDED_SIMULATION, StandardConsistencyRules::skippedBoundedEffects));
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

    private static List<ConsistencyIssue> unmappedBeliefs(ValidationContext context) {
        return context.agents().stream()
                .flatMap(agent -> agent.beliefs().stream().map(belief -> new BeliefLocation(agent, belief)))
                .filter(location -> context.mapping()
                        .find(MappingKind.BELIEF_ATTRIBUTE, beliefSource(location.belief().literal()))
                        .isEmpty())
                .map(location -> issue(
                        "BEL-001",
                        IssueSeverity.WARNING,
                        "Initial belief has no UML attribute mapping for cross-model checks",
                        location.belief().sourceSpan(),
                        Optional.of(MappingSourceId.agent(location.agent())),
                        Optional.empty(),
                        Optional.empty(),
                        List.of("Belief: " + location.belief().literal().render(),
                                "Expected mapping key: " + beliefSource(location.belief().literal())),
                        "Map the belief predicate to a UML attribute when it is used in a cross-model check.",
                        IssueCertainty.POTENTIAL))
                .toList();
    }

    private static List<ConsistencyIssue> unknownMessageReceivers(ValidationContext context) {
        if (context.uml().isEmpty()) {
            return List.of();
        }
        Set<String> objects = context.uml().orElseThrow().objects().stream()
                .map(UmlObjectRef::reference)
                .collect(Collectors.toSet());
        List<ConsistencyIssue> issues = new ArrayList<>();
        for (ActionCallSite action : context.index().allActionCallSites()) {
            if (action.kind() != ActionCallSite.ActionKind.INTERNAL_ACTION
                    || action.signature().filter(signature -> signature.functor().equals(".send")).isEmpty()) {
                continue;
            }
            Optional<TermModel> term = context.actionTerm(action);
            List<TermModel> arguments = term.map(StandardConsistencyRules::arguments).orElse(List.of());
            if (arguments.isEmpty()) {
                issues.add(messageReceiverIssue(context, action, "The .send action has no receiver argument", List.of()));
                continue;
            }
            Optional<String> source = receiverMappingSource(context, action, arguments.get(0));
            Optional<String> target = source.flatMap(key -> context.mapping()
                    .find(MappingKind.RECEIVER_OBJECT, key)
                    .map(MappingBinding::target));
            if (target.filter(objects::contains).isEmpty()) {
                issues.add(messageReceiverIssue(
                        context,
                        action,
                        "Message receiver does not resolve to a current USE object",
                        List.of("Receiver term: " + arguments.get(0).render(),
                                "Expected mapping key: " + source.orElse("<unindexed receiver>"))));
            }
        }
        return List.copyOf(issues);
    }

    private static ConsistencyIssue messageReceiverIssue(
            ValidationContext context,
            ActionCallSite action,
            String message,
            List<String> evidence) {
        return issue(
                "MSG-001",
                IssueSeverity.ERROR,
                message,
                action.sourceSpan(),
                Optional.of(context.agentSource(action)),
                optionalText(action.planLabel()),
                Optional.empty(),
                evidence.isEmpty() ? List.of("Action: " + action.rendered()) : evidence,
                "Map the .send receiver to an existing USE object or correct the receiver term.",
                IssueCertainty.CONFIRMED);
    }

    private static Optional<String> receiverMappingSource(
            ValidationContext context,
            ActionCallSite action,
            TermModel receiver) {
        return allReferences(context.index().agentReferencesByName().values()).stream()
                .filter(reference -> reference.sourceSpan().source().equals(action.sourceSpan().source()))
                .filter(reference -> reference.sourceSpan().beginLine() == action.sourceSpan().beginLine())
                .filter(reference -> reference.rendered().equals(receiver.render()))
                .findFirst()
                .map(MappingSourceId::receiver);
    }

    private static List<ConsistencyIssue> failedPreconditions(ValidationContext context) {
        return preconditionResults(context, OclSnapshotStatus.FAIL).stream()
                .map(result -> issue(
                        "OCL-001",
                        IssueSeverity.ERROR,
                        "Mapped operation precondition is false on the current USE snapshot",
                        result.action().sourceSpan(),
                        Optional.of(context.agentSource(result.action())),
                        optionalText(result.action().planLabel()),
                        Optional.of(result.operation().reference()),
                        result.result().evidence(),
                        "Change the action arguments/state or select an operation whose precondition holds.",
                        IssueCertainty.CONFIRMED))
                .toList();
    }

    private static List<ConsistencyIssue> unknownPreconditions(ValidationContext context) {
        List<ConsistencyIssue> issues = new ArrayList<>();
        for (ActionOperation pair : mappedActionsWithPreconditions(context)) {
            Optional<String> receiver = receiverObject(context, pair.action());
            if (context.ocl().isEmpty() || receiver.isEmpty()) {
                issues.add(issue(
                        "OCL-002",
                        IssueSeverity.WARNING,
                        "Mapped operation preconditions cannot be evaluated without a live snapshot receiver binding",
                        pair.action().sourceSpan(),
                        Optional.of(context.agentSource(pair.action())),
                        optionalText(pair.action().planLabel()),
                        Optional.of(pair.operation().reference()),
                        List.of("Expected Agent->Object mapping for: " + context.agentSource(pair.action())),
                        "Map the executing agent to a current USE object and keep the USE session open.",
                        IssueCertainty.UNKNOWN));
            }
        }
        preconditionResults(context, OclSnapshotStatus.UNKNOWN).forEach(result -> issues.add(issue(
                "OCL-002",
                IssueSeverity.WARNING,
                "Mapped operation precondition cannot be decided on the current snapshot",
                result.action().sourceSpan(),
                Optional.of(context.agentSource(result.action())),
                optionalText(result.action().planLabel()),
                Optional.of(result.operation().reference()),
                result.result().evidence(),
                "Provide a concrete receiver/argument binding and a valid current USE snapshot.",
                IssueCertainty.UNKNOWN)));
        return List.copyOf(issues);
    }

    private static List<PreconditionResult> preconditionResults(ValidationContext context, OclSnapshotStatus expected) {
        if (context.ocl().isEmpty()) {
            return List.of();
        }
        List<PreconditionResult> results = new ArrayList<>();
        for (ActionOperation pair : mappedActionsWithPreconditions(context)) {
            Optional<String> receiver = receiverObject(context, pair.action());
            Optional<TermModel> term = context.actionTerm(pair.action());
            if (receiver.isEmpty() || term.isEmpty()) {
                continue;
            }
            context.ocl().orElseThrow()
                    .evaluatePreconditions(pair.operation(), receiver.orElseThrow(), arguments(term.orElseThrow()))
                    .stream()
                    .filter(result -> result.status() == expected)
                    .forEach(result -> results.add(new PreconditionResult(pair.action(), pair.operation(), result)));
        }
        return List.copyOf(results);
    }

    private static List<ConsistencyIssue> contradictingContexts(ValidationContext context) {
        if (context.ocl().isEmpty()) {
            return List.of();
        }
        List<ConsistencyIssue> issues = new ArrayList<>();
        for (AgentModel agent : context.agents()) {
            Optional<String> receiver = context.mapping()
                    .find(MappingKind.AGENT_OBJECT, MappingSourceId.agent(agent))
                    .map(MappingBinding::target);
            if (receiver.isEmpty()) {
                continue;
            }
            for (PlanModel plan : agent.plans()) {
                Optional<String> expression = plan.context().flatMap(value -> contextExpression(context, value, receiver.orElseThrow()));
                if (expression.isEmpty()) {
                    continue;
                }
                OclSnapshotResult result = context.ocl().orElseThrow()
                        .evaluateExpression(expression.orElseThrow(), "Context " + plan.label());
                if (result.status() == OclSnapshotStatus.FAIL) {
                    issues.add(issue(
                            "CTX-001",
                            IssueSeverity.WARNING,
                            "Plan context is false on the current USE snapshot",
                            plan.context().orElseThrow().sourceSpan(),
                            Optional.of(MappingSourceId.agent(agent)),
                            optionalText(plan.label()),
                            Optional.empty(),
                            result.evidence(),
                            "Update the snapshot/belief mapping or choose a plan whose context holds.",
                            IssueCertainty.CONFIRMED));
                }
            }
        }
        return List.copyOf(issues);
    }

    private static Optional<String> contextExpression(ValidationContext context, ContextExpr contextExpr, String receiver) {
        if (contextExpr instanceof ContextLiteral literal) {
            return context.mapping()
                    .find(MappingKind.BELIEF_ATTRIBUTE, MappingSourceId.belief(
                            new PredicateSignature(literal.literal().functor(), literal.literal().arguments().size())))
                    .map(MappingBinding::target)
                    .flatMap(StandardConsistencyRules::attributeName)
                    .map(attribute -> receiver + "." + attribute);
        }
        if (contextExpr instanceof ContextUnary unary && unary.operator().equals("not")) {
            return contextExpression(context, unary.operand(), receiver).map(expression -> "not (" + expression + ")");
        }
        if (contextExpr instanceof ContextBinary binary) {
            Optional<String> left = contextExpression(context, binary.left(), receiver);
            Optional<String> right = contextExpression(context, binary.right(), receiver);
            String operator = switch (binary.operator()) {
                case "&", "and" -> "and";
                case "|", "or" -> "or";
                default -> "";
            };
            return left.isPresent() && right.isPresent() && !operator.isEmpty()
                    ? Optional.of("(" + left.orElseThrow() + ") " + operator + " (" + right.orElseThrow() + ")")
                    : Optional.empty();
        }
        return Optional.empty();
    }

    private static Optional<String> attributeName(String reference) {
        int separator = reference.lastIndexOf("::");
        return separator < 1 || separator + 2 >= reference.length()
                ? Optional.empty()
                : Optional.of(reference.substring(separator + 2));
    }

    private static String beliefSource(LiteralTermModel belief) {
        return MappingSourceId.belief(new PredicateSignature(belief.functor(), belief.arguments().size()));
    }

    private static List<ConsistencyIssue> violatedBoundedEffects(ValidationContext context) {
        if (context.ocl().isEmpty()) {
            return List.of();
        }
        List<ConsistencyIssue> issues = new ArrayList<>();
        for (ActionOperation pair : mappedActionsWithPreconditionsOrOperations(context)) {
            Optional<String> effect = effectSource(context, pair.action());
            if (effect.isEmpty()) {
                continue;
            }
            BoundedEffectResult result = context.ocl().orElseThrow().simulateSoilEffect(effect.orElseThrow());
            if (result.status() == BoundedEffectStatus.INVARIANT_VIOLATED) {
                issues.add(effectIssue("OCL-003", IssueSeverity.ERROR, "Bounded SOIL effect violates a USE invariant", context, pair, result));
            }
        }
        return List.copyOf(issues);
    }

    private static List<ConsistencyIssue> skippedBoundedEffects(ValidationContext context) {
        List<ConsistencyIssue> issues = new ArrayList<>();
        for (ActionOperation pair : mappedActionsWithPreconditionsOrOperations(context)) {
            Optional<String> effect = effectSource(context, pair.action());
            if (effect.isEmpty()) {
                issues.add(issue(
                        "OCL-004",
                        IssueSeverity.INFO,
                        "No bounded effect is specified; invariant check is skipped",
                        pair.action().sourceSpan(),
                        Optional.of(context.agentSource(pair.action())),
                        optionalText(pair.action().planLabel()),
                        Optional.of(pair.operation().reference()),
                        List.of("Add an ACTION_OPERATION mapping expression beginning with soil: to enable bounded simulation."),
                        "Specify a small SOIL effect or keep this action in static-only validation.",
                        IssueCertainty.UNKNOWN));
                continue;
            }
            if (context.ocl().isPresent()) {
                BoundedEffectResult result = context.ocl().orElseThrow().simulateSoilEffect(effect.orElseThrow());
                if (result.status() == BoundedEffectStatus.UNKNOWN) {
                    issues.add(effectIssue(
                            "OCL-004",
                            IssueSeverity.INFO,
                            "Bounded effect could not be simulated; invariant check is skipped",
                            context,
                            pair,
                            result));
                }
            }
        }
        return List.copyOf(issues);
    }

    private static ConsistencyIssue effectIssue(
            String ruleId,
            IssueSeverity severity,
            String message,
            ValidationContext context,
            ActionOperation pair,
            BoundedEffectResult result) {
        return issue(
                ruleId,
                severity,
                message,
                pair.action().sourceSpan(),
                Optional.of(context.agentSource(pair.action())),
                optionalText(pair.action().planLabel()),
                Optional.of(pair.operation().reference()),
                result.evidence(),
                "Review the soil: effect and the violated invariant before treating this action as executable.",
                ruleId.equals("OCL-003") ? IssueCertainty.CONFIRMED : IssueCertainty.UNKNOWN);
    }

    private static List<ActionOperation> mappedActionsWithPreconditions(ValidationContext context) {
        return mappedActionsWithPreconditionsOrOperations(context).stream()
                .filter(pair -> !pair.operation().preconditions().isEmpty())
                .toList();
    }

    private static List<ActionOperation> mappedActionsWithPreconditionsOrOperations(ValidationContext context) {
        if (context.uml().isEmpty()) {
            return List.of();
        }
        return externalActions(context).stream()
                .flatMap(action -> operationFor(context, action).stream().map(operation -> new ActionOperation(action, operation)))
                .toList();
    }

    private static Optional<String> receiverObject(ValidationContext context, ActionCallSite action) {
        return context.mapping().find(MappingKind.AGENT_OBJECT, context.agentSource(action)).map(MappingBinding::target);
    }

    private static Optional<String> effectSource(ValidationContext context, ActionCallSite action) {
        return context.mapping().find(MappingKind.ACTION_OPERATION, MappingSourceId.action(action))
                .flatMap(MappingBinding::expression)
                .filter(expression -> expression.startsWith("soil:"));
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
        List<String> normalizedEvidence = evidence == null || evidence.isEmpty()
                ? List.of("Rule generated no structured evidence; message: " + message)
                : evidence;
        return new ConsistencyIssue(
                ruleId,
                severity,
                IssueStatus.OPEN,
                message,
                agentId,
                planId,
                Optional.of(sourceSpan),
                umlElementRef,
                normalizedEvidence,
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

    private record BeliefLocation(AgentModel agent, org.tzi.use.plugins.bdi.model.ir.BeliefModel belief) {
    }

    private record ActionOperation(ActionCallSite action, UmlOperationRef operation) {
    }

    private record PreconditionResult(
            ActionCallSite action,
            UmlOperationRef operation,
            OclSnapshotResult result) {
    }

    private record ActionOwner(ActionOperation operation, String owner) {
    }
}
