package org.tzi.use.plugins.bdi.importer;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import org.tzi.use.plugins.bdi.model.environment.ArtifactModel;
import org.tzi.use.plugins.bdi.model.environment.EnvironmentOperation;
import org.tzi.use.plugins.bdi.model.environment.ObservablePropertyModel;

import cartago.Artifact;
import cartago.OPERATION;

/** Boundary adapter for static CArtAgO operation metadata; it never starts a workspace. */
public final class CArtAgOArtifactAdapter {
    public ArtifactModel normalize(
            String workspace,
            String instanceName,
            Class<?> artifactType,
            List<ObservablePropertyModel> declaredProperties) {
        Objects.requireNonNull(artifactType, "artifactType");
        if (!Artifact.class.isAssignableFrom(artifactType)) {
            throw new IllegalArgumentException("Not a CArtAgO Artifact type: " + artifactType.getName());
        }
        List<EnvironmentOperation> operations = new ArrayList<>();
        for (Class<?> current = artifactType; current != null && current != Artifact.class;
                current = current.getSuperclass()) {
            Arrays.stream(current.getDeclaredMethods())
                    .filter(method -> method.isAnnotationPresent(OPERATION.class))
                    .map(CArtAgOArtifactAdapter::operation)
                    .forEach(operations::add);
        }
        operations.sort(Comparator.comparing(EnvironmentOperation::signature));
        return new ArtifactModel(workspace, instanceName, artifactType.getName(), operations, declaredProperties);
    }

    private static EnvironmentOperation operation(Method method) {
        OPERATION annotation = method.getAnnotation(OPERATION.class);
        List<String> parameterTypes = Arrays.stream(method.getParameterTypes())
                .map(Class::getTypeName).toList();
        return new EnvironmentOperation(
                method.getName(), method.getParameterCount(), parameterTypes, annotation.guard());
    }
}
