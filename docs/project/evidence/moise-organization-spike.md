# Moise Organization Technical Spike

Date: 2026-08-11  
Status: blocked by verified API/package evidence; no Moise IR or runtime claim

## Evidence inspected

- The repository pins `org.jacamo:jacamo:1.3.0` and excludes its transitive
  runtime dependencies in `use-bdi-plugin/pom.xml`.
- The local upstream POM declares `org.jacamo:moise:1.1`, but
  `mvn --batch-mode --no-transfer-progress -pl use-bdi-plugin dependency:tree
  -Dverbose -Dincludes=org.jacamo,ora4mas,moise` resolves only direct
  `org.jacamo:jacamo:1.3.0` and `org.jacamo:cartago:3.1` for this plugin.
- The JaCaMo `1.3.0` JAR contains the verified `.jcm` parser and project
  parameter classes, including `JaCaMoOrgParameters`, groups, and schemes. It
  also contains `jacamo.platform.Moise`, which `javap` identifies as a runtime
  platform class with `init/start/createGroup/createScheme`, not a parser for a
  Moise organization file.
- The resolved local repository has no separate `org.jacamo:moise:1.1` JAR. The
  shaded plugin has no `ora4mas/nopl/ORA4MASConstants.class` marker.
- The Auction `.jcm` parser accepts `organisation auction_organization :
  auction-organization.xml {}` and returns the reference. The referenced XML is
  not parsed into roles, groups, missions, goals, permissions, or cardinality.

## Selected fallback

Per ADR-0032, the plugin keeps the organization as a plugin-owned
`MasResourceReference` with `UNSUPPORTED` status and emits `JCM-005` stating
that no verified Moise parser/API is packaged and the referenced file is not
parsed. Existing parse, missing-source, duplicate-agent, and outside-root
diagnostics remain unchanged.

`MoiseOrganizationFallbackTest` covers the explicit fallback and scans the
plugin boundary for guessed `moise`, `ora4mas`, or `org.jacamo.moise` imports.

## Unblock conditions

Do not implement organization IR until a reviewed change supplies the official
parser/API entry point, exact version/checksum, source/license evidence,
dependency/package decision, and a valid Auction organization fixture accepted
by that API. No runtime launcher or live organization state is implied.
