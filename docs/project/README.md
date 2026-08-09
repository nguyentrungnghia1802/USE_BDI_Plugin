# USE BDI Plugin Project Documentation

Status: canonical documentation index
Last verified: 2026-08-09
Code baseline: `c1b11b41` on `thesis/snapshot-ocl-slice`

This directory is the source of truth for the thesis extension that imports,
maps, and checks consistency between AgentSpeak BDI models and USE UML/OCL
models. Runtime claims must be backed by source, tests, or a dated evidence
record. A design proposal is not an implementation claim.

## Canonical specification set

Read these documents in order:

1. [Project context](00_PROJECT_CONTEXT.md)
2. [Product requirements](01_PRODUCT_REQUIREMENTS.md)
3. [System architecture](02_SYSTEM_ARCHITECTURE.md)
4. [Domain and flows](03_DOMAIN_AND_FLOWS.md)
5. [Data and persistence](04_DATABASE.md)
6. [Plugin and internal API contracts](05_API.md)
7. [Codebase guide](06_CODEBASE_GUIDE.md)
8. [Development and testing](07_DEVELOPMENT_AND_TESTING.md)
9. [Deployment and operations](08_DEPLOYMENT_AND_OPERATIONS.md)
10. [Decisions and risks](09_DECISIONS_AND_RISKS.md)
11. [Documentation synchronization checklist](10_DOCUMENTATION_SYNC_CHECKLIST.md)
12. [State isolation and audit guarantees](11_TENANT_ISOLATION_AND_AUDIT.md)
13. [Requirement traceability matrix](12_REQUIREMENT_TRACEABILITY.md)

## Specialized design and execution records

- [Detailed architecture notes](04_SYSTEM_ARCHITECTURE.md)
- [Consistency rule catalog](08_CONSISTENCY_RULE_CATALOG.md)
- [Plugin technical design](10_PLUGIN_TECHNICAL_DESIGN.md)
- [Roadmap](14_ROADMAP_TO_DECEMBER_2026.md)
- [Completion checklist](16_PROJECT_COMPLETION_CHECKLIST.md)
- [Decision log](DECISION_LOG.md)
- [User guide](USER_GUIDE.md)
- [Developer guide](DEVELOPER_GUIDE.md)
- [Install guide](PLUGIN_INSTALL_GUIDE.md)
- [Third-party notices](THIRD_PARTY_NOTICES.md)

Evidence artifacts live under [evidence](evidence/). They record a particular
experiment or verification run and do not override current source behavior.

## Source-of-truth precedence

When documents disagree, resolve the conflict in this order:

1. Executable source, Maven configuration, plugin descriptor, and versioned
   JSON schema validators.
2. Passing automated tests and reproducible scripts.
3. Accepted ADRs in `DECISION_LOG.md`.
4. Canonical documents `00` through `12`.
5. Specialized guides, roadmap notes, historical evidence, and screenshots.

The mismatch must then be fixed in the same change. Historical evidence should
be annotated or superseded, not silently rewritten.

## Current release boundary

The implemented MVP includes the USE plugin shell, Jason 3.3.0 import,
normalized BDI IR, indexing, Explorer/Problems/Mapping UI, versioned mapping
persistence, 22 consistency rules, snapshot OCL checks, bounded SOIL effect
simulation, JSON/HTML exporters, and the Auction mutation evidence bundle.

The release is not final while the thesis release tag and complete external
source/data/report/slides backup remain open. House Building is optional. The
GUI does not yet auto-discover rule/suppression configuration or export the
current live analysis in one click; those limitations are explicit in the
requirements and risk register.
