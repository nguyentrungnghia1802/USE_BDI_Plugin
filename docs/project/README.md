# USE BDI Plugin Documentation

Status: canonical documentation index
Verification: source-backed; see Git history and DocumentationContractTest

This compact set is the source of truth for the thesis extension. It avoids
duplicating code-level facts across many generic project templates.

## Read First

1. [Project context](00_PROJECT_CONTEXT.md): research question, scope, baseline,
   and current status.
2. [Product requirements](01_PRODUCT_REQUIREMENTS.md): requirement IDs and
   acceptance criteria.
3. [System architecture](04_SYSTEM_ARCHITECTURE.md): layers, runtime flow,
   state safety, packaging, and JaCaMo boundary.
4. [Plugin technical design](10_PLUGIN_TECHNICAL_DESIGN.md): verified USE/Jason
   APIs, internal contracts, persistence, and test commands.
5. [Completion checklist](16_PROJECT_COMPLETION_CHECKLIST.md): current gates and
   next incomplete work only.
6. [Decision log](DECISION_LOG.md): accepted ADR invariants and open decisions.

An agent should not read every evidence file by default. Open specialized files
only when the selected task needs them.

## Specialized References

- [Consistency rule catalog](08_CONSISTENCY_RULE_CATALOG.md)
- [Requirement traceability](12_REQUIREMENT_TRACEABILITY.md)
- [User guide](USER_GUIDE.md)
- [Developer guide](DEVELOPER_GUIDE.md)
- [Plugin install guide](PLUGIN_INSTALL_GUIDE.md)
- [Demo guide](../guide/guide.md)
- [Third-party notices](THIRD_PARTY_NOTICES.md)
- [Thesis evidence](evidence/)
- [Research positioning and scope contract](research/research-positioning.md)
- [Frozen research terminology](research/terminology.md)
- [Claim–evidence matrix](research/claim-evidence-matrix.md)
- [Literature and reuse audit](research/literature-reuse-audit.md)
- [Metamodel source matrix](research/metamodel-source-matrix.md)
- [JaCaMo Consistency Analysis Profile](metamodel/USE_JACAMO_ANALYSIS_METAMODEL.md)
- [Analysis-profile coverage matrix](metamodel/METAMODEL_COVERAGE.md)
- [Metamodel-to-Java IR alignment](metamodel/METAMODEL_TO_JAVA_ALIGNMENT.md)
- [Cross-model correspondence model](metamodel/CROSS_MODEL_CORRESPONDENCE.md)
- [Correspondence-to-rule matrix](metamodel/CORRESPONDENCE_RULE_MATRIX.md)
- [Formalized static semantics](metamodel/STATIC_SEMANTICS.md)
- [Rule-to-metamodel matrix](metamodel/RULE_TO_METAMODEL_MATRIX.md)
- [Development ideas and JaCaMo roadmap](../idea/idea.md)

## Source-Of-Truth Order

When documents disagree, use this order:

1. executable source, descriptors, fixtures, and tests;
2. accepted ADR invariants in `DECISION_LOG.md`;
3. requirements and architecture;
4. checklist and guides;
5. historical evidence.

Git history and CI own volatile commit/date provenance. Canonical files do not
repeat branch names, commit hashes, or last-verified dates.

## Release Boundary

The plugin imports AgentSpeak through Jason, builds a plugin-owned BDI IR,
maps it to a read-only USE UML/OCL projection, evaluates 22 standard rules plus
separate static CArtAgO and organization pilot catalogs, and provides
reproducible, corpus-scoped Auction evaluation evidence through a packaged
manifest runner.
It is not a full JaCaMo integration: `.jcm`, CArtAgO artifact declarations,
and a bounded Moise organization subset can be inspected statically, but there
is no organization enactment or JaCaMo runtime lifecycle support.

The release remains open until the thesis release tag and complete external
source/data/report/slides backup are created. House Building remains optional.
