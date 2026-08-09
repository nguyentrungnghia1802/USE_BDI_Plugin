# Development Ideas For USE BDI And JaCaMo

Status: prioritized research backlog, not implemented requirements

## Current JaCaMo Assessment

The project has **not** integrated full JaCaMo. Verified source evidence shows:

- `use-bdi-plugin/pom.xml` depends on `jason-interpreter:3.3.0` only;
- the repository contains `.asl` fixtures but no JaCaMo `.jcm` application;
- there is no CArtAgO artifact/workspace model or Moise organization model;
- the plugin parses and analyzes AgentSpeak statically; it does not start or
  control a JaCaMo multi-agent runtime;
- no runtime event/trace protocol connects executing agents to USE snapshots.

Jason is one JaCaMo layer, so the current work is a strong foundation, but
“Jason-compatible AgentSpeak import” and “full JaCaMo integration” are different
claims. The safest research path is staged interoperability through new adapters
and plugin-owned IR, not embedding all JaCaMo runtime classes into existing
rules.

## Priority Summary

| Rank | Idea | Research value | Effort | Recommended phase |
| --- | --- | --- | --- | --- |
| 1 | Portable project identity and incremental synchronization | High, fixes current validity gap | Medium | next |
| 2 | JaCaMo `.jcm` project importer | Very high, establishes full-project boundary | Medium | next JaCaMo slice |
| 3 | CArtAgO artifact-to-UML/OCL consistency | Very high, connects environment with USE | High | after `.jcm` |
| 4 | Moise organization-to-UML/OCL consistency | Very high, adds social/organizational semantics | High | after `.jcm` |
| 5 | Unified BDI-organization-environment traceability graph | High, thesis explainability contribution | Medium | after static adapters |
| 6 | External JaCaMo runtime trace conformance | Very high, bridges design-time and runtime | High | advanced |
| 7 | Live GUI report and CI quality gate | High practical/reproducibility value | Medium | parallel hardening |
| 8 | Bounded scenario generation and mutation testing | High evaluation value | High | evaluation extension |

## Idea 1 - Portable Project Identity And Incremental Synchronization

**Problem:** mappings and suppressions currently include absolute source paths;
moving a checkout causes false staleness. Explorer also captures one USE
snapshot without a host-state subscription.

**Small vertical slice:** define a project-root-relative `SourceId` v2, migrate
one mapping/suppression fixture from v1, and add a manual Refresh action that
reprojects the current `MSystem` before validation.

**Research question:** how can cross-language trace links remain stable while
still detecting meaningful source/model changes?

**Evaluation:** relocation test, migration round trip, stale/non-stale mutants,
and snapshot fingerprint before/after refresh. This resolves OD-001 and narrows
OD-005 before broader JaCaMo sources introduce more file identities.

## Idea 2 - JaCaMo `.jcm` Project Importer

**Problem:** users currently select isolated `.asl` files manually, so agent
membership, environment configuration, organization files, and launch topology
are absent.

**Small vertical slice:** parse one checked JaCaMo `.jcm` Auction fixture into a
plugin-owned `MasProjectModel` containing agents, referenced `.asl` files, and
declared environment/organization resources. Reuse the current AgentSpeak
importer for each resolved agent source.

**Candidate diagnostics:** missing source, duplicate agent instance, unresolved
resource, and a `.jcm` declaration whose `.asl` import failed.

**Boundary:** use the official JaCaMo parser/API after a technical spike; do not
write a regex replacement and do not expose JaCaMo AST types to rules.

## Idea 3 - CArtAgO Artifact To UML/OCL Consistency

**Problem:** BDI actions often operate on environment artifacts, while current
mappings jump directly from AgentSpeak actions to UML operations and cannot
represent workspaces, artifacts, operations, or observable properties.

**Small vertical slice:** normalize one Auction CArtAgO artifact into
`EnvironmentModel`, map artifact type to UML class, artifact instance to USE
object, operation to UML operation, and observable property to attribute.

**Candidate rules:** action targets an existing artifact operation; operation
arity/types agree; observable property and UML attribute agree; artifact
instance exists in the selected workspace/snapshot.

**Evaluation:** valid Auction environment plus missing-operation, wrong-arity,
and wrong-property mutants.

## Idea 4 - Moise Organization To UML/OCL Consistency

**Problem:** roles, groups, missions, goals, norms, and permissions are outside
the current AgentSpeak-only IR, so organizational constraints cannot be checked
against UML/OCL structures or current agent-role assignments.

**Small vertical slice:** import one Moise organizational specification into an
`OrganizationModel`; map role/group/mission concepts to explicitly chosen UML
classes, associations, operations, and invariants.

**Candidate rules:** agent role is declared; mission goals have BDI support;
role permission matches mapped operation ownership; cardinality and separation-
of-duty constraints agree with OCL.

**Evaluation:** baseline plus undeclared-role, unsupported-mission, permission,
and cardinality mutants. Unknown dynamic enactment must remain UNKNOWN.

## Idea 5 - Unified Traceability Graph

**Problem:** current indexes, mappings, UML references, issues, and future
JaCaMo layers are separate structures, making multi-hop explanations difficult.

**Small vertical slice:** build an immutable graph with typed nodes for source
span, plan/action, agent, role/mission, artifact operation, UML element, OCL
constraint, and issue; edges retain origin and confidence.

**User value:** selecting an issue can show the complete chain, for example
`plan step -> CArtAgO operation -> UML operation -> failed OCL precondition`.

**Evaluation:** graph completeness assertions on Auction and explanation-quality
review against a fixed question set. This graph should be derived, not a second
mutable source of truth.

## Idea 6 - External JaCaMo Runtime Trace Conformance

**Problem:** static consistency cannot confirm which plans/actions actually run
or whether runtime artifact/organization state follows the UML/OCL snapshot.

**Small vertical slice:** keep JaCaMo in a separate process, consume a versioned
JSON event trace for plan selection, action execution, artifact observation, and
role enactment, and correlate events with static source IDs/mappings.

**Candidate checks:** unmapped executed action; runtime argument violates mapped
UML type/precondition; observed state contradicts OCL invariant; trace event has
no static source; expected bounded transition never occurs.

**Safety:** the first slice is read-only/offline replay. USE must not control the
live MAS until protocol, failure, and cleanup semantics have a separate ADR.

## Idea 7 - Live GUI Report And CI Quality Gate

**Problem:** exporters are tested, but the current Explorer cannot export its
live import/mapping/config/snapshot result in one click. Automation also lacks a
single project-level exit contract.

**Small vertical slice:** add `Export Current Analysis...` to Explorer and a
headless command that accepts `.use`, `.asl` or `.jcm`, mappings, config, and an
output path. Define exit codes for confirmed errors versus unknown/potential
findings and optionally produce SARIF.

**Evaluation:** GUI/headless outputs are semantically identical, deterministic,
and include version/hash/config/suppression provenance. This resolves OD-003 and
makes future JaCaMo checks usable in CI.

## Idea 8 - Bounded Scenario Generation And Mutation Testing

**Problem:** current Auction mutants are hand-authored and cover a small static
corpus. Broader JaCaMo claims need systematic but bounded fault generation.

**Small vertical slice:** derive mutations from mappings and the traceability
graph: remove/rename UML or artifact operations, change arity/type, remove role
permission, alter mission-goal support, and negate one OCL precondition.

**Research question:** which inconsistency classes are detected by static BDI,
environment, organization, and snapshot checks, and with what certainty?

**Evaluation:** versioned mutant manifest, ground truth, TP/FP/FN per rule family,
runtime cost, and threats-to-validity statement. Generated mutations must use
disposable fixtures and never modify the baseline model in place.

## Recommended Sequence

1. Implement Idea 1 first to stabilize identity and refresh semantics.
2. Add Idea 2 as the minimum credible JaCaMo project boundary.
3. Implement Idea 3, then Idea 4 as independent adapters and IRs.
4. Add Idea 5 to unify explanations before adding dynamic evidence.
5. Deliver Idea 7 in parallel so every new layer is reproducible from GUI/CI.
6. Attempt Idea 6 only after stable cross-layer identities exist.
7. Use Idea 8 to evaluate the combined contribution rather than expanding
   syntax without a research question.

This sequence extends the thesis from “AgentSpeak vs UML/OCL consistency” to
“JaCaMo architecture vs UML/OCL consistency” while preserving a defensible,
incremental scope.
