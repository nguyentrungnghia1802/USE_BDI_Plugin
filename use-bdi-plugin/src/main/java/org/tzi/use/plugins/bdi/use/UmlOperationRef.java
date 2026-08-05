package org.tzi.use.plugins.bdi.use;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record UmlOperationRef(
        String ownerName,
        String name,
        List<UmlParameterRef> parameters,
        Optional<String> resultType,
        List<UmlConstraintRef> preconditions,
        List<UmlConstraintRef> postconditions,
        boolean expressionBody,
        boolean statementBody) {
    public UmlOperationRef {
        requireText(ownerName, "ownerName");
        requireText(name, "name");
        parameters = List.copyOf(Objects.requireNonNull(parameters, "parameters"));
        resultType = Objects.requireNonNull(resultType, "resultType");
        preconditions = List.copyOf(Objects.requireNonNull(preconditions, "preconditions"));
        postconditions = List.copyOf(Objects.requireNonNull(postconditions, "postconditions"));
    }

    public String signature() {
        String parameterText = parameters.stream()
                .map(parameter -> parameter.name() + ":" + parameter.type())
                .reduce((left, right) -> left + "," + right)
                .orElse("");
        return name + "(" + parameterText + ")" + resultType.map(type -> ":" + type).orElse("");
    }

    public String reference() {
        return ownerName + "::" + signature();
    }

    private static void requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
    }
}
