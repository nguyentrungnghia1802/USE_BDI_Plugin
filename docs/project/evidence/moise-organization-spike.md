# Moise Organization Technical Spike

Date: 2026-08-11  
Status: implemented static normalization; runtime and consistency rules excluded

## Verified authority

- Official source: [moise-lang/moise](https://github.com/moise-lang/moise),
  tag `v1.1`, commit `c68d4b7068c56b7a42e657bdc160f55f2d366ea8`.
- Maven coordinate: `org.jacamo:moise:1.1`, served by the already configured
  official JaCaMo raw Maven repository.
- Local JAR SHA-256:
  `2B7D0415C5EC0BCF060A430C7D12753AA02BDF769172922D10FF2909870161A8`.
  The raw repository publishes no checksum sidecars, so Maven warns and this
  evidence records the independently calculated digest.
- License: GNU LGPL version 3 from the tagged repository `LICENSE`.
- `javap` and the tagged source verify
  `moise.os.OS.loadOSFromURI(String)`. The parser reads XML, validates it
  against the bundled `xml/os.xsd`, builds `SS`/`FS`/`NS`, and returns
  `null` after reporting rejected input.
- The published artifact has no source classifier. API/source verification
  therefore uses the official tagged repository rather than a guessed class.

## Selected package boundary

ADR-0034 supersedes the earlier ADR-0032 blocker because the official artifact,
source, API, license, and Auction fixture are now reproducible. The plugin pins
Moise 1.1 directly and excludes all Maven transitives; Jason 3.3.0 is already a
separate pinned dependency. The Moise JAR is monolithic, so the shaded artifact
contains its classes, but the plugin invokes only the static OS parser and does
not create organization entities, boards, workspaces, or runtime launchers.

`MoiseOrganizationParserAdapter` is the sole production boundary importing
`moise.*`. It immediately converts parser objects to immutable
`OrganizationModel`: roles, groups and role cardinalities, schemes, goals,
missions and mission cardinalities, and permission/obligation norms. Stable
qualified IDs and `ProjectSourceId` make the result deterministic and
checkout-portable.

Moise 1.1 does not expose XML line/column positions on OS elements. Every IR
node therefore retains the portable source file with an explicit unknown
position; no location is invented. Supported parser constructs outside the
pilot are retained as `UnsupportedFeature` and `JCM-010`.

## Auction fixture and diagnostics

The fixture `fixtures/casestudy/auction/auction-organization.xml` is a minimal
schema-valid subset of the official Moise Auction example. It covers the
auctioneer/participant roles, group role cardinalities, one scheme, one goal,
two missions, permission, and obligation.

`MasProjectImportService` maps successful organization references to
`NORMALIZED`. Missing, invalid, duplicate, and unsupported inputs use
`JCM-008`, `JCM-007`, `JCM-009`, and `JCM-010`, respectively. Workspace
and institution declarations retain the existing `JCM-005` fallback.

## Validation evidence

- `MoiseOrganizationParserAdapterTest`: four tests cover deterministic
  Auction normalization, cardinalities/norms, missing/invalid/unsupported
  evidence, duplicate source rejection, and the adapter-only import boundary.
- `MasProjectImportServiceTest`: five tests cover the normalized golden MAS
  project, partial failure, direct AgentSpeak compatibility, and relocation.
- Package smoke requires `moise/os/OS.class`, bundled `xml/os.xsd`, and the
  third-party notice.

No result in this slice claims organization enactment, dynamic role membership,
norm fulfillment, Moise-to-UML/OCL consistency, or a live JaCaMo runtime.
