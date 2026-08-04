package org.tzi.use.plugins.bdi.model.mapping;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.tzi.use.plugins.bdi.index.ActionCallSite;
import org.tzi.use.plugins.bdi.index.AgentObjectReference;
import org.tzi.use.plugins.bdi.index.BdiIndex;
import org.tzi.use.plugins.bdi.index.PredicateReference;
import org.tzi.use.plugins.bdi.index.PredicateSignature;
import org.tzi.use.plugins.bdi.model.ir.AgentModel;
import org.tzi.use.plugins.bdi.use.UmlAttributeRef;
import org.tzi.use.plugins.bdi.use.UmlClassRef;
import org.tzi.use.plugins.bdi.use.UmlObjectRef;
import org.tzi.use.plugins.bdi.use.UmlOperationRef;
import org.tzi.use.plugins.bdi.use.UmlParameterRef;
import org.tzi.use.plugins.bdi.use.UseModelSnapshot;

/**
 * Produces explainable mapping candidates from stable names and signatures.
 * Suggestions never mutate a mapping document or a USE state.
 */
public final class MappingSuggestionService {
    public List<MappingSuggestion> suggest(
            List<AgentModel> agents,
            BdiIndex index,
            UseModelSnapshot uml) {
        Objects.requireNonNull(agents, "agents");
        Objects.requireNonNull(index, "index");
        Objects.requireNonNull(uml, "uml");
        List<MappingSuggestion> suggestions = new ArrayList<>();
        suggestions.addAll(suggestAgentClasses(agents, uml));
        suggestions.addAll(suggestAgentObjects(agents, uml));
        suggestions.addAll(suggestActionOperations(index, uml));
        suggestions.addAll(suggestReceiverBindings(index, uml));
        suggestions.addAll(suggestBeliefAttributes(index, uml));
        for (ActionCallSite action : index.allActionCallSites()) {
            for (MappingSuggestion operation : suggestActionOperations(action, uml.operations())) {
                if (operation.score() >= 0.5) {
                    UmlOperationRef target = findOperation(operation.target(), uml.operations()).orElse(null);
                    if (target != null) {
                        suggestions.addAll(suggestParameterBindings(action, target));
                    }
                }
            }
        }
        return distinctAndSorted(suggestions);
    }

    public List<MappingSuggestion> suggestAgentClasses(
            List<AgentModel> agents,
            UseModelSnapshot uml) {
        Objects.requireNonNull(agents, "agents");
        Objects.requireNonNull(uml, "uml");
        List<MappingSuggestion> suggestions = new ArrayList<>();
        for (AgentModel agent : agents) {
            String source = agentSource(agent);
            String stem = sourceStem(agent.source());
            for (UmlClassRef target : uml.classes()) {
                double score = nameScore(stem, target.name());
                if (score >= 0.2) {
                    suggestions.add(new MappingSuggestion(
                            MappingKind.AGENT_CLASS,
                            source,
                            target.reference(),
                            score,
                            reasons(stem, target.name(), "Agent source filename")));
                }
            }
        }
        return distinctAndSorted(suggestions);
    }

    public List<MappingSuggestion> suggestAgentObjects(
            List<AgentModel> agents,
            UseModelSnapshot uml) {
        Objects.requireNonNull(agents, "agents");
        Objects.requireNonNull(uml, "uml");
        List<MappingSuggestion> suggestions = new ArrayList<>();
        for (AgentModel agent : agents) {
            String source = agentSource(agent);
            String stem = sourceStem(agent.source());
            for (UmlObjectRef target : uml.objects()) {
                double score = nameScore(stem, target.name());
                if (score >= 0.2) {
                    suggestions.add(new MappingSuggestion(
                            MappingKind.AGENT_OBJECT,
                            source,
                            target.reference(),
                            score,
                            reasons(stem, target.name(), "Agent source filename")));
                }
            }
        }
        return distinctAndSorted(suggestions);
    }

    public List<MappingSuggestion> suggestActionOperations(
            BdiIndex index,
            UseModelSnapshot uml) {
        Objects.requireNonNull(index, "index");
        Objects.requireNonNull(uml, "uml");
        return index.allActionCallSites().stream()
                .flatMap(action -> suggestActionOperations(action, uml.operations()).stream())
                .collect(Collectors.collectingAndThen(Collectors.toList(), MappingSuggestionService::distinctAndSorted));
    }

    public List<MappingSuggestion> suggestParameterBindings(
            ActionCallSite action,
            UmlOperationRef operation) {
        Objects.requireNonNull(action, "action");
        Objects.requireNonNull(operation, "operation");
        int argumentCount = action.signature().map(PredicateSignature::arity).orElse(0);
        List<MappingSuggestion> suggestions = new ArrayList<>();
        int count = Math.min(argumentCount, operation.parameters().size());
        for (int index = 0; index < count; index++) {
            UmlParameterRef parameter = operation.parameters().get(index);
            double score = argumentCount == operation.parameters().size() ? 1.0 : 0.65;
            suggestions.add(new MappingSuggestion(
                    MappingKind.PARAMETER,
                    actionSource(action) + "/argument/" + index,
                    operation.reference() + "#parameter:" + parameter.name(),
                    score,
                    List.of("Argument position " + index + " matches UML parameter order"),
                    Optional.of("argument[" + index + "]")));
        }
        return distinctAndSorted(suggestions);
    }

    public List<MappingSuggestion> suggestReceiverBindings(
            BdiIndex index,
            UseModelSnapshot uml) {
        Objects.requireNonNull(index, "index");
        Objects.requireNonNull(uml, "uml");
        List<MappingSuggestion> suggestions = new ArrayList<>();
        for (AgentObjectReference reference : allAgentReferences(index)) {
            for (UmlObjectRef object : uml.objects()) {
                double score = nameScore(reference.name(), object.name());
                if (score >= 0.2) {
                    suggestions.add(new MappingSuggestion(
                            MappingKind.RECEIVER_OBJECT,
                            receiverSource(reference),
                            object.reference(),
                            score,
                            reasons(reference.name(), object.name(), "Agent receiver reference")));
                }
            }
        }
        return distinctAndSorted(suggestions);
    }

    public List<MappingSuggestion> suggestBeliefAttributes(
            BdiIndex index,
            UseModelSnapshot uml) {
        Objects.requireNonNull(index, "index");
        Objects.requireNonNull(uml, "uml");
        List<MappingSuggestion> suggestions = new ArrayList<>();
        Set<PredicateSignature> beliefs = new TreeSet<>(Comparator
                .comparing(PredicateSignature::functor)
                .thenComparingInt(PredicateSignature::arity));
        for (PredicateReference reference : index.allPredicateReferences()) {
            if (reference.kind() == PredicateReference.PredicateReferenceKind.INITIAL_BELIEF) {
                beliefs.add(reference.signature());
            }
        }
        for (PredicateSignature belief : beliefs) {
            for (UmlAttributeRef attribute : uml.attributes()) {
                double score = nameScore(belief.functor(), attribute.name());
                if (score >= 0.2) {
                    suggestions.add(new MappingSuggestion(
                            MappingKind.BELIEF_ATTRIBUTE,
                            belief.toString(),
                            attribute.reference(),
                            score,
                            reasons(belief.functor(), attribute.name(),
                                    "Initial belief functor matches UML attribute")));
                }
            }
        }
        return distinctAndSorted(suggestions);
    }

    static String agentSource(AgentModel agent) {
        return normalizePath(agent.source());
    }

    static String actionSource(ActionCallSite action) {
        return normalizePath(action.sourceSpan().source())
                + "#plan:" + action.planLabel()
                + "#step:" + action.stepIndex();
    }

    private static String receiverSource(AgentObjectReference reference) {
        return normalizePath(reference.sourceSpan().source())
                + "#receiver:" + reference.name()
                + "#line:" + reference.sourceSpan().beginLine()
                + "#column:" + reference.sourceSpan().beginColumn();
    }

    private static List<MappingSuggestion> suggestActionOperations(
            ActionCallSite action,
            List<UmlOperationRef> operations) {
        String functor = action.signature()
                .map(PredicateSignature::functor)
                .orElseGet(() -> renderedFunctor(action.rendered()));
        int arity = action.signature().map(PredicateSignature::arity).orElse(-1);
        List<MappingSuggestion> suggestions = new ArrayList<>();
        for (UmlOperationRef operation : operations) {
            double name = nameScore(functor, operation.name());
            if (name == 0.0) {
                continue;
            }
            boolean arityMatches = arity >= 0 && arity == operation.parameters().size();
            double score = Math.min(1.0, name * 0.75 + (arityMatches ? 0.25 : 0.0));
            if (score >= 0.2) {
                List<String> reasons = new ArrayList<>(reasons(functor, operation.name(), "Action functor"));
                reasons.add(arityMatches
                        ? "Argument arity matches operation parameters"
                        : "Argument arity differs or is unavailable");
                suggestions.add(new MappingSuggestion(
                        MappingKind.ACTION_OPERATION,
                        actionSource(action),
                        operation.reference(),
                        score,
                        reasons));
            }
        }
        return distinctAndSorted(suggestions);
    }

    private static Optional<UmlOperationRef> findOperation(
            String reference,
            List<UmlOperationRef> operations) {
        return operations.stream().filter(operation -> operation.reference().equals(reference)).findFirst();
    }

    private static List<AgentObjectReference> allAgentReferences(BdiIndex index) {
        return index.agentReferencesByName().values().stream()
                .flatMap(Collection::stream)
                .filter(reference -> reference.kind() == AgentObjectReference.ReferenceKind.AGENT)
                .sorted(Comparator
                        .comparing((AgentObjectReference reference) -> normalizePath(reference.sourceSpan().source()))
                        .thenComparing(AgentObjectReference::name)
                        .thenComparing(AgentObjectReference::origin)
                        .thenComparingInt(reference -> reference.sourceSpan().beginLine()))
                .toList();
    }

    private static List<MappingSuggestion> distinctAndSorted(Collection<MappingSuggestion> suggestions) {
        Map<String, MappingSuggestion> unique = new LinkedHashMap<>();
        for (MappingSuggestion suggestion : suggestions) {
            String key = suggestion.kind().name() + "\u0000" + suggestion.source()
                    + "\u0000" + suggestion.target();
            MappingSuggestion previous = unique.get(key);
            if (previous == null || suggestion.score() > previous.score()) {
                unique.put(key, suggestion);
            }
        }
        return unique.values().stream()
                .sorted(Comparator
                        .comparing((MappingSuggestion suggestion) -> suggestion.kind().name())
                        .thenComparing(MappingSuggestion::source)
                        .thenComparing(Comparator.comparingDouble(MappingSuggestion::score).reversed())
                        .thenComparing(MappingSuggestion::target))
                .toList();
    }

    private static List<String> reasons(String left, String right, String basis) {
        List<String> reasons = new ArrayList<>();
        if (normalizeName(left).equals(normalizeName(right))) {
            reasons.add("Exact normalized name match");
        } else if (normalizeName(left).contains(normalizeName(right))
                || normalizeName(right).contains(normalizeName(left))) {
            reasons.add("One normalized name contains the other");
        } else {
            reasons.add("Shared normalized name tokens");
        }
        reasons.add(basis);
        return List.copyOf(reasons);
    }

    private static double nameScore(String left, String right) {
        String normalizedLeft = normalizeName(left);
        String normalizedRight = normalizeName(right);
        if (normalizedLeft.isEmpty() || normalizedRight.isEmpty()) {
            return 0.0;
        }
        if (normalizedLeft.equals(normalizedRight)) {
            return 1.0;
        }
        if (normalizedLeft.contains(normalizedRight) || normalizedRight.contains(normalizedLeft)) {
            return 0.75;
        }
        Set<String> leftTokens = tokens(left);
        Set<String> rightTokens = tokens(right);
        long shared = leftTokens.stream().filter(rightTokens::contains).count();
        if (shared == 0) {
            return 0.0;
        }
        return Math.min(0.6, 0.2 + 0.4 * shared / Math.max(leftTokens.size(), rightTokens.size()));
    }

    private static Set<String> tokens(String value) {
        return java.util.Arrays.stream(value
                        .replaceAll("([a-z0-9])([A-Z])", "$1 $2")
                        .split("[^A-Za-z0-9]+"))
                .map(String::toLowerCase)
                .filter(token -> !token.isBlank())
                .collect(Collectors.toCollection(TreeSet::new));
    }

    private static String normalizeName(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
    }

    private static String renderedFunctor(String rendered) {
        int end = rendered.indexOf('(');
        String functor = end < 0 ? rendered : rendered.substring(0, end);
        return functor.strip().replaceFirst("^\\.", "");
    }

    private static String sourceStem(Path source) {
        String filename = source.getFileName().toString();
        int extension = filename.lastIndexOf('.');
        return extension > 0 ? filename.substring(0, extension) : filename;
    }

    private static String normalizePath(Path source) {
        return source.toAbsolutePath().normalize().toString().replace('\\', '/');
    }
}
