package org.tzi.use.plugins.bdi.index;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.tzi.use.plugins.bdi.model.ir.AgentModel;
import org.tzi.use.plugins.bdi.model.ir.PlanModel;

/** Immutable lookup snapshot derived from one or more normalized agent models. */
public final class BdiIndex {
    private final String metamodelVersion;
    private final List<AgentModel> models;
    private final Map<PredicateSignature, List<PlanModel>> supportingPlans;
    private final Map<PredicateSignature, List<ActionCallSite>> actionCallSites;
    private final List<ActionCallSite> allActionCallSites;
    private final Map<PredicateSignature, List<PredicateReference>> predicateReferences;
    private final List<PredicateReference> allPredicateReferences;
    private final Map<String, List<AgentObjectReference>> agentReferences;
    private final Map<String, List<AgentObjectReference>> objectReferences;
    private final List<DuplicatePlanLabel> duplicatePlanLabels;

    BdiIndex(
            String metamodelVersion,
            List<AgentModel> models,
            Map<PredicateSignature, List<PlanModel>> supportingPlans,
            Map<PredicateSignature, List<ActionCallSite>> actionCallSites,
            List<ActionCallSite> allActionCallSites,
            Map<PredicateSignature, List<PredicateReference>> predicateReferences,
            List<PredicateReference> allPredicateReferences,
            Map<String, List<AgentObjectReference>> agentReferences,
            Map<String, List<AgentObjectReference>> objectReferences,
            List<DuplicatePlanLabel> duplicatePlanLabels) {
        this.metamodelVersion = requireText(metamodelVersion, "metamodelVersion");
        this.models = List.copyOf(Objects.requireNonNull(models, "models"));
        this.supportingPlans = immutableMap(supportingPlans);
        this.actionCallSites = immutableMap(actionCallSites);
        this.allActionCallSites = List.copyOf(Objects.requireNonNull(allActionCallSites, "allActionCallSites"));
        this.predicateReferences = immutableMap(predicateReferences);
        this.allPredicateReferences = List.copyOf(
                Objects.requireNonNull(allPredicateReferences, "allPredicateReferences"));
        this.agentReferences = immutableMap(agentReferences);
        this.objectReferences = immutableMap(objectReferences);
        this.duplicatePlanLabels = List.copyOf(
                Objects.requireNonNull(duplicatePlanLabels, "duplicatePlanLabels"));
    }

    public static BdiIndex empty() {
        return new BdiIndex(
                BdiMetamodelVersion.CURRENT,
                List.of(),
                Map.of(),
                Map.of(),
                List.of(),
                Map.of(),
                List.of(),
                Map.of(),
                Map.of(),
                List.of());
    }

    public String metamodelVersion() {
        return metamodelVersion;
    }

    public List<AgentModel> models() {
        return models;
    }

    public Map<PredicateSignature, List<PlanModel>> supportingPlansByGoal() {
        return supportingPlans;
    }

    public List<PlanModel> supportingPlans(PredicateSignature goal) {
        return supportingPlans.getOrDefault(Objects.requireNonNull(goal, "goal"), List.of());
    }

    public Map<PredicateSignature, List<ActionCallSite>> actionCallSitesBySignature() {
        return actionCallSites;
    }

    public List<ActionCallSite> actionCalls(PredicateSignature action) {
        return actionCallSites.getOrDefault(Objects.requireNonNull(action, "action"), List.of());
    }

    public List<ActionCallSite> allActionCallSites() {
        return allActionCallSites;
    }

    public Map<PredicateSignature, List<PredicateReference>> predicateReferencesBySignature() {
        return predicateReferences;
    }

    public List<PredicateReference> predicateReferences(PredicateSignature predicate) {
        return predicateReferences.getOrDefault(
                Objects.requireNonNull(predicate, "predicate"), List.of());
    }

    public List<PredicateReference> allPredicateReferences() {
        return allPredicateReferences;
    }

    public Map<String, List<AgentObjectReference>> agentReferencesByName() {
        return agentReferences;
    }

    public List<AgentObjectReference> agentReferences(String name) {
        return agentReferences.getOrDefault(requireText(name, "name"), List.of());
    }

    public Map<String, List<AgentObjectReference>> objectReferencesByName() {
        return objectReferences;
    }

    public List<AgentObjectReference> objectReferences(String name) {
        return objectReferences.getOrDefault(requireText(name, "name"), List.of());
    }

    public List<DuplicatePlanLabel> duplicatePlanLabels() {
        return duplicatePlanLabels;
    }

    private static String requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }

    private static <K, V> Map<K, List<V>> immutableMap(Map<K, List<V>> values) {
        Objects.requireNonNull(values, "values");
        Map<K, List<V>> copy = new LinkedHashMap<>();
        values.forEach((key, value) -> copy.put(
                Objects.requireNonNull(key, "map key"),
                List.copyOf(Objects.requireNonNull(value, "map value"))));
        return Collections.unmodifiableMap(copy);
    }
}
