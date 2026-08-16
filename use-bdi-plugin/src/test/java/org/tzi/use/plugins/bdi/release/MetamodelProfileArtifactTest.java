package org.tzi.use.plugins.bdi.release;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.jupiter.api.Test;
import org.tzi.use.plugins.bdi.application.AnalysisMetamodelDescriptor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

class MetamodelProfileArtifactTest {
    private static final String ANALYSIS_NAMESPACE =
            "https://useocl.github.io/bdi/metamodel/analysis/1.0";
    private static final String XSI_NAMESPACE = "http://www.w3.org/2001/XMLSchema-instance";

    @Test
    void keepsVersionedProfileArtifactsWellFormedAndTraceable() throws Exception {
        Path project = repositoryRoot().resolve("docs/project");
        Path metamodel = project.resolve("metamodel");
        Path ecore = metamodel.resolve("use-jacamo-analysis.ecore");
        Path specification = metamodel.resolve("USE_JACAMO_ANALYSIS_METAMODEL.md");
        Path coverage = metamodel.resolve("METAMODEL_COVERAGE.md");
        Path diagram = project.resolve("evidence/bdi-metamodel-diagram.mmd");

        for (Path artifact : Set.of(ecore, specification, coverage, diagram)) {
            assertTrue(Files.isRegularFile(artifact), () -> "Missing profile artifact: " + artifact);
            assertTrue(Files.size(artifact) > 0, () -> "Empty profile artifact: " + artifact);
        }

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        Document document = factory.newDocumentBuilder().parse(ecore.toFile());
        Element root = document.getDocumentElement();
        assertEquals("useJacamoAnalysis", root.getAttribute("name"));
        assertEquals(ANALYSIS_NAMESPACE, root.getAttribute("nsURI"));
        assertEquals(AnalysisMetamodelDescriptor.CURRENT_ID, root.getAttribute("nsURI"));
        assertEquals("ujap", root.getAttribute("nsPrefix"));

        NodeList subpackages = document.getElementsByTagName("eSubpackages");
        assertEquals(5, subpackages.getLength());
        Set<String> packageNames = new HashSet<>();
        Set<String> namespaceUris = new HashSet<>();
        for (int index = 0; index < subpackages.getLength(); index++) {
            Element subpackage = (Element) subpackages.item(index);
            assertTrue(packageNames.add(subpackage.getAttribute("name")), "Duplicate package name");
            assertTrue(namespaceUris.add(subpackage.getAttribute("nsURI")), "Duplicate package namespace");
            assertTrue(subpackage.getAttribute("nsURI").startsWith(ANALYSIS_NAMESPACE + "/"));
            assertClassifierNamesAreUnique(subpackage);
        }

        NodeList classifiers = document.getElementsByTagName("eClassifiers");
        int classes = 0;
        int enums = 0;
        for (int index = 0; index < classifiers.getLength(); index++) {
            Element classifier = (Element) classifiers.item(index);
            String type = classifier.getAttributeNS(XSI_NAMESPACE, "type");
            classes += "ecore:EClass".equals(type) ? 1 : 0;
            enums += "ecore:EEnum".equals(type) ? 1 : 0;
        }
        assertEquals(48, classes);
        assertEquals(8, enums);

        assertEquals("1.0.0", AnalysisMetamodelDescriptor.CURRENT_VERSION);
        assertEquals("JaCaMo Consistency Analysis Profile",
                AnalysisMetamodelDescriptor.CURRENT_PROFILE_NAME);

        String specificationText = Files.readString(specification);
        assertTrue(specificationText.contains("RUNTIME_NOT_AVAILABLE"));
        assertTrue(specificationText.contains("MM-012"));
        assertTrue(Files.readString(coverage).contains("UNSUPPORTED_EXPLICIT"));
        String diagramText = Files.readString(diagram);
        assertTrue(diagramText.startsWith("classDiagram"));
        assertTrue(diagramText.contains("No UML, OCL, mapping, issue, trace, diagram, Swing, report, or runtime classes"));
    }

    private static void assertClassifierNamesAreUnique(Element subpackage) {
        Set<String> names = new HashSet<>();
        NodeList children = subpackage.getChildNodes();
        for (int index = 0; index < children.getLength(); index++) {
            Node child = children.item(index);
            if (child instanceof Element element && "eClassifiers".equals(element.getTagName())) {
                assertTrue(names.add(element.getAttribute("name")),
                        () -> "Duplicate classifier in " + subpackage.getAttribute("name"));
            }
        }
    }

    private static Path repositoryRoot() {
        Path current = Path.of("").toAbsolutePath().normalize();
        while (current != null) {
            if (Files.isRegularFile(current.resolve("docs/project/16_PROJECT_COMPLETION_CHECKLIST.md"))) {
                return current;
            }
            current = current.getParent();
        }
        throw new IllegalStateException("Could not locate repository root from the test working directory");
    }
}
