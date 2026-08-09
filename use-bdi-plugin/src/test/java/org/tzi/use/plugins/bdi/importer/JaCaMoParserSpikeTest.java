package org.tzi.use.plugins.bdi.importer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;

import jacamo.project.JaCaMoProject;
import jacamo.project.parser.JaCaMoProjectParser;
import jason.mas2j.AgentParameters;

class JaCaMoParserSpikeTest {
    private static final Path AUCTION = Path.of(
            "src/test/resources/fixtures/casestudy/auction/auction.jcm");

    @Test
    void officialParserAcceptsAuctionProjectAndResolvesNamedInstances() throws Exception {
        Path projectFile = AUCTION.toAbsolutePath().normalize();
        try (Reader source = Files.newBufferedReader(projectFile, StandardCharsets.UTF_8)) {
            JaCaMoProject project = new JaCaMoProjectParser(source)
                    .parse(projectFile.getParent().toString());
            List<AgentParameters> agents = project.getAgents();

            assertEquals("auction", project.getSocName());
            assertEquals(List.of("auctioneer", "bidder1", "bidder2"),
                    agents.stream().map(AgentParameters::getAgName).toList());
            assertEquals("file:auctioneer.asl", agents.get(0).getSource().toString());
            assertEquals("file:bidder.asl", agents.get(1).getSource().toString());
            assertEquals(projectFile.getParent().toString(), project.getDirectory());
        }
    }
}
