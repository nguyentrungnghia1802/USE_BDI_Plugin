package org.tzi.use.plugins.bdi.diagram;

import java.util.Objects;
import java.util.regex.Pattern;

import org.tzi.use.plugins.bdi.model.source.ProjectSourceId;

/** Portable semantic identity used for diagram selection and navigation. */
public record DiagramSelectionRef(String namespace, String reference) {
    private static final Pattern NAMESPACE = Pattern.compile("[a-z][a-z0-9.-]*");
    private static final Pattern WINDOWS_ABSOLUTE_PATH = Pattern.compile("^[A-Za-z]:[\\\\/].*");

    public DiagramSelectionRef {
        DiagramValues.requireText(namespace, "namespace");
        DiagramValues.requireText(reference, "reference");
        if (!NAMESPACE.matcher(namespace).matches()) {
            throw new IllegalArgumentException("namespace must be a stable lowercase identifier: " + namespace);
        }
        if (reference.startsWith("/") || reference.startsWith("\\")
                || WINDOWS_ABSOLUTE_PATH.matcher(reference).matches()
                || reference.regionMatches(true, 0, "file:", 0, 5)) {
            throw new IllegalArgumentException("reference must not contain an absolute path: " + reference);
        }
    }

    public static DiagramSelectionRef source(ProjectSourceId source) {
        return new DiagramSelectionRef("source", Objects.requireNonNull(source, "source").canonical());
    }

    public static DiagramSelectionRef of(String namespace, String reference) {
        return new DiagramSelectionRef(namespace, reference);
    }

    public String canonical() {
        return DiagramIdentity.frame("diagram-ref-v1", namespace, reference);
    }
}
