package org.tzi.use.plugins.bdi.model.mapping;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.tzi.use.plugins.bdi.index.ActionCallSite;
import org.tzi.use.plugins.bdi.index.AgentObjectReference;
import org.tzi.use.plugins.bdi.index.BdiIndex;
import org.tzi.use.plugins.bdi.index.BdiMetamodelVersion;
import org.tzi.use.plugins.bdi.index.PredicateReference;
import org.tzi.use.plugins.bdi.model.ir.AgentModel;
import org.tzi.use.plugins.bdi.model.ir.SourceSpan;
import org.tzi.use.plugins.bdi.use.UmlAttributeRef;
import org.tzi.use.plugins.bdi.use.UmlOperationRef;
import org.tzi.use.plugins.bdi.use.UmlParameterRef;
import org.tzi.use.plugins.bdi.use.UseModelSnapshot;

/** Detects stale mapping metadata and bindings without mutating a USE snapshot. */
public final class MappingStalenessDetector {
    public List<MappingStaleness> detect(
            List<AgentModel> agents,
            BdiIndex index,
            MappingDocument mapping,
            UseModelSnapshot uml) {
        Objects.requireNonNull(agents, "agents");
        Objects.requireNonNull(index, "index");
        Objects.requireNonNull(mapping, "mapping");
        Objects.requireNonNull(uml, "uml");

        List<MappingStaleness> findings = new ArrayList<>();
        Optional<SourceSpan> documentSource = mapping.bindings().stream()
                .map(binding -> sourceSpan(binding, agents, index))
                .flatMap(Optional::stream)
                .findFirst();
        if (!mapping.bdiMetamodelVersion().equals(BdiMetamodelVersion.CURRENT)) {
            findings.add(new MappingStaleness(
                    MappingStalenessReason.BDI_METAMODEL_VERSION_CHANGED,
                    Optional.empty(),
                    "Mapping uses BDI metamodel " + mapping.bdiMetamodelVersion()
                            + " but the plugin uses " + BdiMetamodelVersion.CURRENT,
                    documentSource,
                    List.of("Persisted BDI metamodel version differs from the current plugin version")));
        }
        if (!mapping.useFingerprint().equals("unknown") && !mapping.useFingerprint().equals(uml.fingerprint())) {
            findings.add(new MappingStaleness(
                    MappingStalenessReason.USE_FINGERPRINT_CHANGED,
                    Optional.empty(),
                    "USE model/state fingerprint changed since the mapping was saved",
                    documentSource,
                    List.of("Saved fingerprint: " + mapping.useFingerprint(),
                            "Current fingerprint: " + uml.fingerprint())));
        }
        Set<String> sources = sourceIds(agents, index);
        Set<String> targets = targetIds(uml);
        for (MappingBinding binding : mapping.bindings()) {
            Optional<SourceSpan> sourceSpan = sourceSpan(binding, agents, index);
            if (!sources.contains(binding.kind().name() + "\u0000" + binding.source())) {
                findings.add(new MappingStaleness(
                        MappingStalenessReason.SOURCE_MISSING,
                        Optional.of(binding),
                        "Mapping source no longer exists: " + binding.source(),
                        sourceSpan,
                        List.of("Mapping kind: " + binding.kind(), "Source: " + binding.source())));
            }
            if (!targets.contains(binding.kind().name() + "\u0000" + binding.target())) {
                findings.add(new MappingStaleness(
                        MappingStalenessReason.TARGET_MISSING,
                        Optional.of(binding),
                        "Mapping target no longer exists: " + binding.target(),
                        sourceSpan,
                        List.of("Mapping kind: " + binding.kind(), "Target: " + binding.target())));
            }
        }
        return findings.stream()
                .sorted(Comparator
                        .comparing((MappingStaleness finding) -> finding.reason().name())
                        .thenComparing(finding -> finding.binding().map(MappingBinding::key).orElse(""))
                        .thenComparing(MappingStaleness::message))
                .toList();
    }

    private static Set<String> sourceIds(List<AgentModel> agents, BdiIndex index) {
        Set<String> sources = new HashSet<>();
        agents.forEach(agent -> {
            String source = MappingSourceId.agent(agent);
            sources.add(MappingKind.AGENT_CLASS.name() + "\u0000" + source);
            sources.add(MappingKind.AGENT_OBJECT.name() + "\u0000" + source);
        });
        for (ActionCallSite action : index.allActionCallSites()) {
            String actionSource = MappingSourceId.action(action);
            sources.add(MappingKind.ACTION_OPERATION.name() + "\u0000" + actionSource);
            int arity = action.signature().map(signature -> signature.arity()).orElse(0);
            for (int argument = 0; argument < arity; argument++) {
                sources.add(MappingKind.PARAMETER.name() + "\u0000"
                        + MappingSourceId.argument(action, argument));
            }
        }
        index.agentReferencesByName().values().stream()
                .flatMap(List::stream)
                .filter(reference -> reference.kind() == AgentObjectReference.ReferenceKind.AGENT)
                .forEach(reference -> sources.add(MappingKind.RECEIVER_OBJECT.name() + "\u0000"
                        + MappingSourceId.receiver(reference)));
        index.allPredicateReferences().stream()
                .filter(reference -> reference.kind() == PredicateReference.PredicateReferenceKind.INITIAL_BELIEF)
                .forEach(reference -> sources.add(MappingKind.BELIEF_ATTRIBUTE.name() + "\u0000"
                        + MappingSourceId.belief(reference.signature())));
        return Set.copyOf(sources);
    }

    private static Set<String> targetIds(UseModelSnapshot uml) {
        Set<String> targets = new HashSet<>();
        uml.classes().forEach(value -> targets.add(MappingKind.AGENT_CLASS.name() + "\u0000" + value.reference()));
        uml.objects().forEach(value -> {
            targets.add(MappingKind.AGENT_OBJECT.name() + "\u0000" + value.reference());
            targets.add(MappingKind.RECEIVER_OBJECT.name() + "\u0000" + value.reference());
        });
        for (UmlOperationRef operation : uml.operations()) {
            targets.add(MappingKind.ACTION_OPERATION.name() + "\u0000" + operation.reference());
            for (UmlParameterRef parameter : operation.parameters()) {
                targets.add(MappingKind.PARAMETER.name() + "\u0000"
                        + operation.reference() + "#parameter:" + parameter.name());
            }
        }
        for (UmlAttributeRef attribute : uml.attributes()) {
            targets.add(MappingKind.BELIEF_ATTRIBUTE.name() + "\u0000" + attribute.reference());
        }
        return Set.copyOf(targets);
    }

    private static Optional<SourceSpan> sourceSpan(
            MappingBinding binding,
            List<AgentModel> agents,
            BdiIndex index) {
        return switch (binding.kind()) {
            case AGENT_CLASS, AGENT_OBJECT -> agents.stream()
                    .filter(agent -> MappingSourceId.agent(agent).equals(binding.source()))
                    .map(agent -> SourceSpan.unknown(agent.source()))
                    .findFirst();
            case ACTION_OPERATION -> index.allActionCallSites().stream()
                    .filter(action -> MappingSourceId.action(action).equals(binding.source()))
                    .map(ActionCallSite::sourceSpan)
                    .findFirst();
            case PARAMETER -> index.allActionCallSites().stream()
                    .filter(action -> binding.source().startsWith(MappingSourceId.action(action) + "/argument/"))
                    .map(ActionCallSite::sourceSpan)
                    .findFirst();
            case RECEIVER_OBJECT -> index.agentReferencesByName().values().stream()
                    .flatMap(List::stream)
                    .filter(reference -> MappingSourceId.receiver(reference).equals(binding.source()))
                    .map(AgentObjectReference::sourceSpan)
                    .findFirst();
            case BELIEF_ATTRIBUTE -> index.allPredicateReferences().stream()
                    .filter(reference -> reference.kind() == PredicateReference.PredicateReferenceKind.INITIAL_BELIEF)
                    .filter(reference -> MappingSourceId.belief(reference.signature()).equals(binding.source()))
                    .map(PredicateReference::sourceSpan)
                    .findFirst();
        };
    }
}
