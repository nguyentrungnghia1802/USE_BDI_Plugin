# Research Positioning and Scope Contract

Status: **FROZEN FOR ROADMAP IMPLEMENTATION**

Freeze date: 2026-08-17

Working title: **Metamodel-Guided Consistency Analysis and Traceable Visualization between JaCaMo BDI/MAS Models and UML/OCL in USE**

Inputs: [literature and reuse audit](literature-reuse-audit.md), [metamodel source matrix](metamodel-source-matrix.md), and [verified baseline](../evidence/baseline-reconciliation.md).

## Problem statement

Existing JaCaMo systems distribute static design information across AgentSpeak/Jason agents, `.jcm` project composition, CArtAgO artifact declarations, and Moise organization specifications. A USE model expresses a separate UML/OCL view of classes, objects, attributes, operations, associations, links, invariants, and snapshot state. Without explicit correspondences, disagreements across these artifacts are difficult to detect, qualify, reproduce, and explain.

For the supported static subset, the research problem is to represent imported JaCaMo evidence conservatively, connect it explicitly to immutable USE projections, and report deterministic structural, signature, reference, and state-dependent consistency findings with source-to-evidence traceability. Analysis must not start a JaCaMo runtime, invent missing facts, mutate USE core state, or introduce a second parser or validator.

## Research gap

Prior work already provides Jason and JaCaMo metamodels, graphical DSMLs, editors, and model-to-code generation. It must not be presented as absent. The scoped gap addressed here is the reverse, analysis-oriented path:

```text
Prior work:
designed model -> graphical DSML -> generated Jason/CArtAgO/Moise artifacts

This thesis:
existing AgentSpeak/JaCaMo artifacts -> normalized static analysis model
-> explicit UML/OCL correspondences -> deterministic consistency findings
-> evidence/trace graph -> read-only graphical explanation inside USE
```

The novelty candidate is therefore not a new general JaCaMo metamodel or graphical editor. It is an evidence-preserving USE integration profile and analysis method for a bounded static subset.

## Frozen research questions

### RQ1 — Representation

How can static AgentSpeak and JaCaMo artifacts be represented in a parser-independent, metamodel-aligned analysis profile suitable for USE integration while preserving source provenance, unsupported syntax, and uncertainty?

### RQ2 — Cross-model consistency

How can explicit, confirmed correspondences between supported BDI/MAS concepts and UML/OCL elements be used to detect structural, signature, reference, cardinality, and state-dependent inconsistencies when their required evidence is available?

### RQ3 — Traceable explanation

How can each consistency outcome be traced and visually explained from AgentSpeak/JaCaMo source through mappings and rules to UML/OCL evidence in USE without introducing a second parser, validator, or editable semantic model?

### RQ4 — Scoped evaluation

To what extent does the implemented approach deterministically detect and explain the controlled inconsistencies in the reviewed Auction corpus while preserving input immutability and USE state safety?

RQ4 remains separate. Detection/explanation effectiveness and reproducibility are empirical concerns distinct from the construction questions in RQ1–RQ3. The wording deliberately does not generalize beyond the reviewed corpus.

## Frozen contributions

| ID | Contribution | Existing implementation anchor | Test anchor | Evidence anchor |
|---|---|---|---|---|
| C1 | A bounded **JaCaMo Consistency Analysis Profile** adapting established BDI/JaCaMo concepts for conservative USE analysis | `model.ir`, `model.mas`, `model.environment`, `model.organization`; adapter normalization | Jason/JaCaMo/CArtAgO/Moise importer, golden, diagnostic, relocation, and boundary tests | baseline reconciliation; prior-work audit; Task 04 profile specification |
| C2 | An explicit BDI/MAS↔UML/OCL correspondence model with candidate/confirmed semantics, provenance, and staleness | `model.mapping`, environment/organization mapping records and persistence | mapping suggestion, codec/repository, confirmation, relocation, and stale-target tests | mapping and environment/organization evidence; requirement traceability |
| C3 | Deterministic cross-model consistency analysis over normalized source models and immutable USE projections | `validation`, `index`, `use`, `application` | rule-catalog, orchestrator, Auction baseline/mutant, snapshot/OCL, bounded-effect tests | rule catalog; Auction evaluation outputs; baseline reconciliation |
| C4 | Evidence-rich outcomes preserving source spans, UML/OCL references, severity, status, certainty, and PASS/FAIL/UNKNOWN behavior | `ConsistencyIssue`, OCL/bounded-effect results, reports, CLI | issue, report, headless, uncertainty, state-fingerprint, suppression tests | deterministic JSON/HTML/CSV; headless quality-gate evidence |
| C5 | Traceability-derived, read-only graphical viewpoints integrated into USE | `trace`, `diagram`, `ui`, `problems` | trace graph, diagram boundary/projection/layout/navigation/highlight/export/Explorer tests | canonical demos, Auction mutant paths, SVG/export evidence |
| C6 | A reproducible scoped evaluation protocol over reviewed baselines/mutants and deterministic reports | `evaluation`, manifest runner, headless service, scripts | manifest codec/runner, timeout/tool-error, repeated-output and packaged smoke tests | five-case Auction manifest and results; `AUCTION_EVALUATION_OK` |

Detailed claim-to-evidence pre-commitments are frozen in the [claim-evidence matrix](claim-evidence-matrix.md).

## Non-contributions and non-goals

- No claim of the first Jason metamodel, first JaCaMo metamodel, first BDI Ecore model, or first graphical JaCaMo DSML.
- No new general-purpose JaCaMo authoring language and no semantic-source graphical editor.
- No model-to-code generation contribution; Acceleo-style generation is prior work and out of scope.
- No complete AgentSpeak/Jason, CArtAgO, Moise, or JaCaMo semantics.
- No JaCaMo launch/lifecycle, live CArtAgO workspace/value capture, Moise enactment, dynamic membership, norm-fulfilment monitoring, or runtime trace.
- No formal proof of semantic equivalence or guarantee that a system is consistent/correct.
- No mutation-based claim of general precision, recall, or correctness beyond the declared Auction cases.
- No usability, learnability, or productivity claim without a separate empirical user study.
- No mutation of USE core APIs or persistent USE state as an analysis technique.
- No Ecore/EMF, Sirius, EVL, Acceleo, EuGENia, or GMF runtime adoption unless a later approved ADR changes the frozen boundary.

## Supported subset freeze

### AgentSpeak/BDI

- Production import uses the pinned Jason parser through the adapter boundary.
- The normalized IR supports beliefs, goals, plans, triggers, optional contexts, ordered steps, typed terms, source spans, and explicit unsupported features according to current code.
- Internal actions, external/basic actions, belief updates, tests, constraints, and achievement subgoals are distinguished where the importer has authoritative evidence.
- Rules, messages, mental notes, and other constructs are not claimed as first-class coverage where the current IR does not materialize them.
- No claim is made for complete AgentSpeak operational semantics.

### Static JaCaMo project

- `.jcm` parsing/composition is static and adapter-backed.
- Agent instances and resource references retain project-relative identity, resolution status, diagnostics, and provenance.
- Analysis does not launch or schedule a JaCaMo runtime.

### Static CArtAgO environment

- The subset covers statically inspectable artifact type/instance/workspace identity, official runtime-retained operation metadata, and explicit static observable-property descriptors.
- Operation/property correspondences can target USE operations/attributes.
- Missing live property values produce `UNKNOWN`; no live workspace or value claim is made.

### Static Moise organization

- The bounded IR covers organizations, roles, group hierarchy, role cardinalities, schemes, missions, organizational goals, mission cardinalities, permission/obligation norms, source spans, and explicit unsupported features as implemented.
- Confirmed mappings can target UML classes/operations and reviewed OCL cardinality bounds.
- Static agreement does not prove enactment; dynamic membership and norm fulfilment remain unavailable/`UNKNOWN`.

### USE

- USE UML/OCL input is represented by an immutable projection of classes, attributes, associations, operations, constraints, objects, and links plus a fingerprint.
- Snapshot OCL evaluation is read-only. Bounded counterfactual effects use disposable/restored state and must preserve the before/after fingerprint.
- The plugin does not mutate USE core or treat current state as universally representative of runtime behavior.

### Visualization

- Diagrams are immutable, derived, read-only graphical concrete representations/viewpoints of the analysis and trace model.
- A diagram is not a parser, validator, editable semantic model, persisted source of truth, or runtime view.
- Selection, focus, layers, issue paths, mapping state, and export explain existing evidence; they do not create semantic facts.

## Expected evidence by research question

| RQ | Required evidence | Sufficient scoped answer | Explicitly insufficient |
|---|---|---|---|
| RQ1 | Profile/source matrix; adapter and IR boundary tests; golden/unsupported/relocation cases | Demonstrate loss-aware normalization for declared constructs and explicit handling of unsupported/unknown input | Merely drawing an Ecore-like diagram or parsing only a happy path |
| RQ2 | Mapping schemas; immutable USE snapshots; rule catalog; baseline/mutant/UNKNOWN cases; state fingerprints | Show deterministic findings for declared prerequisites and rule families using confirmed mappings | Claiming full semantic verification or treating absent runtime evidence as PASS |
| RQ3 | Trace-node/edge schema; issue evidence; GUI/diagram boundary and navigation tests; portable reports/SVG | Reconstruct reviewed source→mapping→target→rule→issue/evidence paths without semantic recomputation in presentation | A screenshot without trace identity/evidence, or a renderer that reparses/revalidates |
| RQ4 | Versioned manifest/corpus hashes; repeated runner output; expected/actual oracle comparison; input/state immutability | Report corpus-scoped detection/explanation outcomes and determinism, including missed/unexpected/unknown/tool-error counts | General precision/recall or usability claims from four mutants |

## Novelty comparison

| Dimension | Established prior work | This project's bounded contribution | Novelty-safe wording |
|---|---|---|---|
| Abstract syntax | Jason MM, DSML4BDI, DSML4JaCaMo, generic MAS metamodels | Adapted analysis profile aligned with current Java IR and USE target concepts | “adaptation/profile for USE consistency analysis” |
| Direction | Model-first authoring and generation | Existing-source import and analysis | “source-first static analysis path” |
| Heterogeneity | Platform mappings mainly support generation | Explicit BDI/MAS↔UML/OCL correspondences and staleness | “explicit cross-model correspondence model” |
| Validation | Metamodel conformance/static controls; future M2M analysis in DSML4JaCaMo | Deterministic phased cross-model checks with unknown/certainty evidence | “bounded deterministic consistency analysis” |
| Explanation | Graphical modeling viewpoints | Trace-derived diagnostic viewpoints inside USE | “read-only graphical explanation” |
| Evaluation | Code generation, development time, and qualitative DSML dimensions | Reviewed mutation corpus, evidence paths, determinism, and state safety | “reproducible scoped evaluation” |

## Decision register

| Decision | Frozen outcome | Consequence |
|---|---|---|
| DP-03-01 metamodel name | **JaCaMo Consistency Analysis Profile** | Avoids implying a new general JaCaMo metamodel. “USE–JaCaMo analysis metamodel” may appear only as an explanatory synonym, not the novelty claim. |
| DP-03-02 Ecore role | **Specification vocabulary only** | Task 04 documents an Ecore-compatible conceptual structure if useful; no EMF runtime/dependency without a new ADR and demonstrated benefit. |
| DP-03-03 graphical wording | **Read-only graphical concrete representation / graphical viewpoints derived from metamodel and traceability** | Do not call the current plugin a full graphical editor. |
| DP-03-04 FR-DIA-007 | **RESIDUAL** | Direct cross-tab source/mapping navigation is future work; implemented Problems-to-Diagram, detail, and bounded focus remain valid. |
| DP-03-05 OD-004 / OD-005 | **RESIDUAL** | Strict unknown-field policy and automatic USE state subscription remain future work; documented validation/manual refresh are the current contract. |
| DP-03-06 House Building | **OPTIONAL** | Auction is the required corpus; absence of House Building does not block research or release gates. |

## Threats and limitations pre-commitment

| Threat | Pre-committed limitation / mitigation |
|---|---|
| Construct validity | “Consistency” means satisfaction of declared, evidence-bounded rules—not whole-system correctness or semantic equivalence. Rule prerequisites and UNKNOWN outcomes remain visible. |
| Internal validity | Mutants are reviewed, single-fault controlled cases. Runner isolation, input hashes, before/after fingerprints, and deterministic ordering reduce execution confounds. |
| External validity | The five Auction cases do not establish performance across arbitrary JaCaMo systems. House Building is optional and any future corpus must be reported separately. |
| Coverage bias | The profile mirrors the implemented static subset. Unsupported syntax and absent runtime evidence are reported rather than generalized away. |
| Oracle bias | Each mutant has a declared expected rule/evidence oracle; unexpected findings and misses are retained in output. The corpus does not prove absence of other defects. |
| Tool coupling | Jason/JaCaMo/CArtAgO/Moise and USE APIs are behind adapters/projections, with package-boundary tests. Version pins limit claims to the verified toolchain. |
| State dependence | USE snapshot findings depend on the captured state. Bounded effects must restore state, and missing dynamic evidence yields UNKNOWN. |
| Researcher bias | Claims are pre-mapped to executable tests and artifacts; unsupported novelty and usability/productivity wording is forbidden. |

## Claim discipline

Research conclusions must use qualifiers where applicable: “for the supported static subset,” “for the reviewed Auction corpus,” “without starting the JaCaMo runtime,” “using explicit confirmed mappings,” and “when required USE snapshot evidence is available.” The phrases “the tool guarantees consistency,” “the tool verifies JaCaMo systems” without qualification, “all BDI semantics,” and “runtime organization consistency” are prohibited.

## Final gate

```text
RQs: PASS — four frozen questions; RQ4 remains separate and corpus-scoped
Contributions: PASS — C1–C6 each map to implementation, tests, and evidence
Scope: PASS — AgentSpeak, .jcm, CArtAgO, Moise, USE, visualization, and runtime boundaries frozen
Terminology: PASS — companion terminology contract linked
Novelty: PASS — prior-work ownership and novelty-safe comparison explicit
Claim-evidence matrix: PASS — companion matrix linked
Optional/residual decisions: PASS — FR-DIA-007/OD-004/OD-005 residual; House Building optional
Doc tests: PASS — DocumentationContractTest 1/1
git diff --check: PASS
Open failures: none in research contract
Result: PASS
```
