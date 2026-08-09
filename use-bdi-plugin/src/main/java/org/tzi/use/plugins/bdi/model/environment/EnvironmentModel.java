package org.tzi.use.plugins.bdi.model.environment;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record EnvironmentModel(List<ArtifactModel> artifacts) {
    public EnvironmentModel {
        artifacts = List.copyOf(Objects.requireNonNull(artifacts, "artifacts")).stream()
                .sorted(Comparator.comparing(ArtifactModel::reference)).toList();
        if (artifacts.stream().map(ArtifactModel::reference).distinct().count() != artifacts.size()) {
            throw new IllegalArgumentException("Duplicate artifact reference");
        }
    }

    public Optional<ArtifactModel> artifact(String workspace, String instanceName) {
        String reference = workspace + "/" + instanceName;
        return artifacts.stream().filter(value -> value.reference().equals(reference)).findFirst();
    }
}
