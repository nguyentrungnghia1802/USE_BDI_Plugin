package org.tzi.use.plugins.bdi.validation;

import java.util.List;

import org.tzi.use.plugins.bdi.model.ir.TermModel;
import org.tzi.use.plugins.bdi.use.UmlOperationRef;

/** USE-independent boundary for snapshot OCL and bounded-effect checks. */
public interface SnapshotOclEvaluator {
    List<OclSnapshotResult> evaluatePreconditions(
            UmlOperationRef operation,
            String receiverObject,
            List<TermModel> arguments);

    OclSnapshotResult evaluateExpression(String expression, String subject);

    BoundedEffectResult simulateSoilEffect(String source);
}
