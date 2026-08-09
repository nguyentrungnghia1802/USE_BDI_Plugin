package org.tzi.use.plugins.bdi.model.environment;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public record ArtifactModel(
        String workspace,
        String instanceName,
        String typeName,
        List<EnvironmentOperation> operations,
        List<ObservablePropertyModel> observableProperties) {
    public ArtifactModel {
        EnvironmentOperation.requireText(workspace, "workspace");
        EnvironmentOperation.requireText(instanceName, "instanceName");
        EnvironmentOperation.requireText(typeName, "typeName");
        operations = List.copyOf(Objects.requireNonNull(operations, "operations")).stream()
                .sorted(Comparator.comparing(EnvironmentOperation::signature)).toList();
        observableProperties = List.copyOf(Objects.requireNonNull(observableProperties, "observableProperties"))
                .stream().sorted(Comparator.comparing(ObservablePropertyModel::signature)).toList();
        if (operations.stream().map(EnvironmentOperation::signature).distinct().count() != operations.size()) {
            throw new IllegalArgumentException("Duplicate artifact operation signature");
        }
        if (observableProperties.stream().map(ObservablePropertyModel::signature).distinct().count()
                != observableProperties.size()) {
            throw new IllegalArgumentException("Duplicate observable property signature");
        }
    }

    public String reference() {
        return workspace + "/" + instanceName;
    }
}
