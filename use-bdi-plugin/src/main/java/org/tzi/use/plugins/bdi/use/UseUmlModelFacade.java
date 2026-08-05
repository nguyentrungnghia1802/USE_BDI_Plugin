package org.tzi.use.plugins.bdi.use;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.tzi.use.uml.mm.MAggregationKind;
import org.tzi.use.uml.mm.MAssociation;
import org.tzi.use.uml.mm.MAssociationEnd;
import org.tzi.use.uml.mm.MAttribute;
import org.tzi.use.uml.mm.MClass;
import org.tzi.use.uml.mm.MClassInvariant;
import org.tzi.use.uml.mm.MClassifier;
import org.tzi.use.uml.mm.MModel;
import org.tzi.use.uml.mm.MOperation;
import org.tzi.use.uml.mm.MPrePostCondition;
import org.tzi.use.uml.ocl.expr.VarDecl;
import org.tzi.use.uml.sys.MLink;
import org.tzi.use.uml.sys.MObject;
import org.tzi.use.uml.sys.MObjectState;
import org.tzi.use.uml.sys.MSystem;
import org.tzi.use.uml.sys.MSystemState;

/**
 * Read-only adapter from the live USE model/session state to plugin-owned
 * immutable references.
 */
public final class UseUmlModelFacade {
    private final UseOclEvaluator oclEvaluator;

    public UseUmlModelFacade() {
        this(new UseOclEvaluator());
    }

    UseUmlModelFacade(UseOclEvaluator oclEvaluator) {
        this.oclEvaluator = Objects.requireNonNull(oclEvaluator, "oclEvaluator");
    }

    public UseModelSnapshot snapshot(MSystem system) {
        Objects.requireNonNull(system, "system");
        MModel model = system.model();
        MSystemState state = system.state();
        List<UmlClassRef> classes = classes(model);
        List<UmlAttributeRef> attributes = attributes(model);
        List<UmlAssociationRef> associations = associations(model);
        List<UmlOperationRef> operations = operations(model);
        List<UmlConstraintRef> classInvariants = classInvariants(model);
        List<UmlObjectRef> objects = objects(state);
        List<UmlLinkRef> links = links(state);
        UseModelSnapshot draft = new UseModelSnapshot(
                model.name(),
                model.filename(),
                classes,
                attributes,
                associations,
                operations,
                classInvariants,
                objects,
                links,
                "pending");
        return new UseModelSnapshot(
                draft.modelName(),
                draft.filename(),
                draft.classes(),
                draft.attributes(),
                draft.associations(),
                draft.operations(),
                draft.classInvariants(),
                draft.objects(),
                draft.links(),
                UseModelFingerprint.compute(draft));
    }

    public List<UmlClassRef> classes(MModel model) {
        Objects.requireNonNull(model, "model");
        return model.classes().stream()
                .sorted(Comparator.comparing(MClass::name))
                .map(cls -> new UmlClassRef(
                        cls.name(),
                        cls.isAbstract(),
                        cls.parents().stream()
                                .map(MClassifier::name)
                                .sorted()
                                .toList()))
                .toList();
    }

    public List<UmlAttributeRef> attributes(MModel model) {
        Objects.requireNonNull(model, "model");
        List<UmlAttributeRef> result = new ArrayList<>();
        for (MClass cls : sortedClasses(model)) {
            for (MAttribute attribute : cls.attributes()) {
                result.add(new UmlAttributeRef(
                        cls.name(),
                        attribute.name(),
                        attribute.type().toString(),
                        attribute.isDerived(),
                        attribute.getInitExpression().isPresent()
                                ? Optional.of(attribute.getInitExpression().get().toString())
                                : Optional.empty(),
                        Optional.ofNullable(attribute.getDeriveExpression()).map(Object::toString)));
            }
        }
        return result.stream()
                .sorted(Comparator.comparing(UmlAttributeRef::reference))
                .toList();
    }

    public List<UmlAssociationRef> associations(MModel model) {
        Objects.requireNonNull(model, "model");
        return model.associations().stream()
                .sorted(Comparator.comparing(MAssociation::name))
                .map(association -> new UmlAssociationRef(
                        association.name(),
                        association.isDerived(),
                        association.isUnion(),
                        association.associationEnds().stream()
                                .map(end -> new UmlAssociationEndRef(
                                        association.name(),
                                        end.cls().name(),
                                        end.nameAsRolename(),
                                        end.multiplicity().toString(),
                                        MAggregationKind.name(end.aggregationKind()),
                                        end.isOrdered(),
                                        end.isNavigable(),
                                        end.isExplicitNavigable(),
                                        end.isDerived(),
                                        end.isUnion()))
                                .toList()))
                .toList();
    }

    public List<UmlOperationRef> operations(MModel model) {
        Objects.requireNonNull(model, "model");
        List<UmlOperationRef> result = new ArrayList<>();
        for (MClass cls : sortedClasses(model)) {
            for (MOperation operation : cls.operations()) {
                List<UmlParameterRef> parameters = new ArrayList<>();
                for (VarDecl parameter : operation.paramList()) {
                    parameters.add(new UmlParameterRef(parameter.name(), parameter.type().toString()));
                }
                result.add(new UmlOperationRef(
                        cls.name(),
                        operation.name(),
                        parameters,
                        operation.hasResultType()
                                ? Optional.of(operation.resultType().toString())
                                : Optional.empty(),
                        operation.preConditions().stream()
                                .map(this::prePostCondition)
                                .toList(),
                        operation.postConditions().stream()
                                .map(this::prePostCondition)
                                .toList(),
                        operation.hasExpression(),
                        operation.hasStatement()));
            }
        }
        return result.stream()
                .sorted(Comparator.comparing(UmlOperationRef::reference))
                .toList();
    }

    public List<UmlConstraintRef> classInvariants(MModel model) {
        Objects.requireNonNull(model, "model");
        return model.classInvariants().stream()
                .sorted(Comparator.comparing(MClassInvariant::qualifiedName))
                .map(invariant -> new UmlConstraintRef(
                        invariant.cls().name(),
                        Optional.empty(),
                        "invariant",
                        invariant.name(),
                        invariant.bodyExpression().toString()))
                .toList();
    }

    public List<UmlObjectRef> objects(MSystemState state) {
        Objects.requireNonNull(state, "state");
        return state.allObjects().stream()
                .sorted(Comparator.comparing(MObject::name))
                .map(object -> objectRef(object, state))
                .toList();
    }

    public List<UmlLinkRef> links(MSystemState state) {
        Objects.requireNonNull(state, "state");
        return state.allLinks().stream()
                .map(link -> new UmlLinkRef(
                        link.association().name(),
                        link.linkedObjects().stream().map(MObject::name).toList(),
                        link.isVirtual()))
                .sorted(Comparator.comparing(this::linkKey))
                .toList();
    }

    public OclEvaluationResult evaluateOcl(MSystem system, String expression) {
        return oclEvaluator.evaluate(system, expression);
    }

    private List<MClass> sortedClasses(MModel model) {
        return model.classes().stream()
                .sorted(Comparator.comparing(MClass::name))
                .toList();
    }

    private UmlObjectRef objectRef(MObject object, MSystemState state) {
        MObjectState objectState = object.state(state);
        Map<String, String> values = new LinkedHashMap<>();
        if (objectState != null) {
            objectState.attributeValueMap().entrySet().stream()
                    .sorted(Map.Entry.comparingByKey(Comparator.comparing(MAttribute::qualifiedName)))
                    .forEach(entry -> values.put(entry.getKey().qualifiedName(), entry.getValue().toString()));
        }
        return new UmlObjectRef(object.name(), object.cls().name(), object.exists(state), values);
    }

    private UmlConstraintRef prePostCondition(MPrePostCondition condition) {
        return new UmlConstraintRef(
                condition.cls().name(),
                Optional.of(condition.operation().signature()),
                condition.isPre() ? "precondition" : "postcondition",
                condition.name(),
                condition.expression().toString());
    }

    private String linkKey(UmlLinkRef link) {
        return link.associationName() + "|" + String.join("|", link.objectNames());
    }
}
