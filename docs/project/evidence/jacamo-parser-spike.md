# JaCaMo `.jcm` Parser Technical Spike

Date: 2026-08-10  
Scope: static project parsing only; no runtime launch

## Verified Upstream

- Official repository: `https://github.com/jacamo-lang/jacamo.git`.
- Release tag: `v.1.3.0b`, commit
  `b9ae1319d8c03366e5e3cadbc64106df6b8deac2`.
- Maven coordinate: `org.jacamo:jacamo:1.3.0`, served by the official JaCaMo
  Maven repository at `https://raw.githubusercontent.com/jacamo-lang/mvn-repo/master`.
- JAR SHA-256: `c1de5c0d9b39af4059a0b260be7c2399a9fd65db42fd92921e673c0715196304`.
- JaCaMo license: GNU LGPLv3. The packaged notice names the exact artifact.

The release source `src/main/javacc/JaCaMoProjectParser.jj` defines `mas`,
`agent`, `instances`, `workspace`, `organisation`, and `institution`; the
Auction fixture uses only the first three verified productions. Named
instances are expanded by the official parser into ordinary Jason
`AgentParameters` entries. Agent source URIs remain relative in the parsed
model; `MAS2JProject.getDirectory()` retains the base directory, so the plugin
adapter, not the domain model, is responsible for safe path resolution.

## Verified Binary API

`javap` against the downloaded 1.3.0 JAR establishes these signatures:

```text
JaCaMoProjectParser(Reader)
JaCaMoProject parse(String directory) throws ParseException
JaCaMoProject extends MAS2JProject
Collection<JaCaMoWorkspaceParameters> getWorkspaces()
Collection<JaCaMoOrgParameters> getOrgs()
Collection<JaCaMoInstParameters> getInstitutions()
AgentParameters.getAgName()
AgentParameters.getSourceAsFile()
AgentParameters.getNbInstances()
```

The published JaCaMo 1.3.0 POM requests
`io.github.jason-lang:jason-interpreter:3.3.0`, exactly matching the plugin's
pinned parser. Maven resolves the artifact successfully; the upstream raw
repository omits checksum sidecars, so Maven emits a checksum warning. The
SHA-256 above was therefore recorded independently from the downloaded JAR.

## Dependency And Packaging Decision

The upstream POM also declares CArtAgO, JaCa, Moise, NPL, SAI, REST, Gradle
Tooling, Vert.x, Jackson, Graphviz, and logging/runtime libraries. They are not
needed to parse the Auction agent declarations and would imply unsupported
runtime capabilities. The plugin excludes all JaCaMo transitives and keeps its
existing explicit Jason 3.3.0 dependency.

Package smoke requires both
`jacamo/project/parser/JaCaMoProjectParser.class` and
`jason/asSemantics/Agent.class`, and rejects representative CArtAgO/Moise
runtime classes. `JaCaMoParserSpikeTest` parses `auction.jcm` with the official
parser and checks deterministic instance/source expansion.

## Safe Fallback

If a future `.jcm` construct triggers a missing runtime class, the importer
must return an explicit unsupported/infrastructure diagnostic. It must not add
a regex parser or silently package the full runtime. A dedicated dependency
and adapter ADR is required before CArtAgO, Moise, or runtime launch support.
