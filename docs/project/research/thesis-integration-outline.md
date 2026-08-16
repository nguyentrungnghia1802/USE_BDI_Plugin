# Thesis Integration Outline

Status: thesis-ready integration map for the bounded research contribution

This outline connects the frozen research questions and contributions to the
canonical product contracts, executable implementation, evaluated evidence,
and explicit limitations. It is an authoring map, not a substitute for the
source-backed artifacts it links.

## 1. Introduction And Problem

- Motivate the problem of comparing static AgentSpeak/JaCaMo artifacts with a
  separate USE UML/OCL model and snapshot.
- State RQ1–RQ4 exactly as frozen in
  [research positioning](research-positioning.md).
- Frame the contribution as a source-first, evidence-preserving consistency
  analysis path for the supported static subset—not a new general JaCaMo
  language, graphical editor, or runtime verifier.
- Summarize C1–C6 and identify the reviewed Auction corpus as the evaluation
  boundary.

## 2. Background

- Explain USE as the UML/OCL and snapshot authority.
- Explain Jason/AgentSpeak, `.jcm` composition, CArtAgO declarations, and Moise
  organization specifications with their pinned implementation versions.
- Distinguish abstract syntax, concrete syntax, static semantics, execution
  semantics, correspondence, traceability, and derived graphical viewpoints.
- Use the primary-source metadata and verified evidence locations in the
  [literature/reuse audit](literature-reuse-audit.md); do not cite a search
  result or invent a page/figure reference.

## 3. Modeling-Language Engineering Framework

- Introduce Combemale et al.'s decomposition as an organizing framework, not
  an obligation to adopt an EMF/Sirius implementation stack.
- Use the mapping below to show how the thesis moves from domain knowledge to
  bounded tooling and evaluation.
- Explain why Ecore is a specification artifact while immutable Java IR and
  official parsers remain runtime truth.

## Combemale Language-Engineering Mapping

| Language engineering concern | Thesis artifact |
| --- | --- |
| Domain knowledge | BDI/MAS plus UML/OCL integration problem |
| Abstract syntax | Versioned JaCaMo Consistency Analysis Profile |
| Textual concrete syntax | Existing AgentSpeak/JaCaMo sources, owned by official parsers |
| Static semantics | Rule taxonomy, prerequisites, constraints, and PASS/FAIL/UNKNOWN policy |
| Cross-language relations | Explicit correspondence model with candidate/confirmed, provenance, and staleness semantics |
| Graphical representation | Read-only USE Diagram viewpoints derived from analysis and trace evidence |
| Semantics/execution authority | Jason and USE remain authorities; the plugin performs bounded static/snapshot analysis |
| Tooling | USE plugin, GUI/headless entry points, reports, and scripts |
| Evaluation | Auction baseline/mutants plus four canonical demos and performance evidence |

The table answers the “DSML → metamodel → syntax → graphical plugin” direction
without changing the implemented source-first workflow: source syntax is
imported, normalized into the bounded profile realization, related explicitly
to UML/OCL, checked, traced, and then presented graphically.

## 4. Related Work

Present the prior-work lineage before stating the scoped gap. The comparison is
limited to claims verified from primary sources in the reuse audit; “not
reported in the inspected source” is not a universal absence claim.

## Related-Work Comparison

| Work | Metamodel | Graphical modeling | Code generation | Existing-source import | UML/OCL consistency | Trace/evidence | This thesis difference |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Jason Metamodel 2016 ([S2](https://doi.org/10.4230/OASIcs.SLATE.2016.8)) | Jason-specific agent/plan/context/body concepts and Ecore conformance | Graphical syntax/editor feasibility is reported | Not reported as the paper's contribution | Not evaluated as the source-first analysis workflow | No cross-model USE UML/OCL consistency framework reported | Model conformance is discussed; this thesis's issue/trace chain is not reported | Adapts the vocabulary to conservative imported evidence and explicit USE targets |
| DSML4BDI ([S3](https://doi.org/10.1049/iet-sen.2017.0094)) | Ecore abstract syntax | Sirius MAS/Agent/Plan/Logical Expression perspectives | Acceleo generates Jason | Model-first authoring/generation, not the evaluated reverse import path | Environment constraints are reported, not the bounded USE cross-model contract | Evaluation concerns generation/development time and a small questionnaire | Imports existing sources and evaluates evidence-bounded cross-model rules rather than generating AgentSpeak |
| DSML4JaCaMo ([S4](https://doi.org/10.15439/2024F6157)) | Ecore partitions for Jason, CArtAgO, and Moise | Sirius MAS/agent/organization viewpoints | Acceleo generates ASL, artifact Java, and organization XML | Existing-source analysis is not the reported central workflow | Inspected Sections III–VI report static analysis as future M2M work, not a USE UML/OCL framework | Two case studies and generated/final LOC; no comparable issue/evidence graph reported | Uses a bounded source-first profile, explicit UML/OCL correspondences, deterministic findings, and trace-derived USE views |
| FAML/platform-independent MAS work ([S5](https://doi.org/10.1109/TSE.2009.34)) | Generic MAS abstractions and platform separation | Not established by the abstract-level evidence used here | Not established by the evidence used here | Not established by the evidence used here | Not established by the evidence used here | Not established by the evidence used here | Supplies generic conceptual context; this thesis targets pinned JaCaMo/USE adapters and executable evidence |
| This thesis | Versioned specification-only profile aligned with tested Java IR | Read-only derived views inside USE; not an editor | Out of scope | Imports existing `.asl`/`.jcm` plus bounded declaration evidence | Deterministic structural/signature/reference/cardinality/snapshot rules for declared prerequisites | Portable source→mapping→target→rule→issue/evidence chains and deterministic reports | Combines source-first static import, explicit USE correspondences, bounded consistency semantics, and reproducible scoped evaluation |

## 5. USE–JaCaMo Analysis Metamodel

- Define the profile identity, packages, multiplicities, provenance, unsupported
  evidence, and scope from the [profile specification](../metamodel/USE_JACAMO_ANALYSIS_METAMODEL.md).
- Explain the normative adaptations recorded in the
  [metamodel source matrix](metamodel-source-matrix.md), including optional
  Jason plan context and declaration-only environment evidence.
- Show Ecore validation and the
  [Java-alignment contract](../metamodel/METAMODEL_TO_JAVA_ALIGNMENT.md).
- Report structural coverage using the classified matrix; do not restate it as
  semantic completeness.

## 6. Correspondence And Consistency Semantics

- Define correspondence kinds, lifecycle, provenance, confidence, staleness,
  and supported target categories from the
  [cross-model correspondence model](../metamodel/CROSS_MODEL_CORRESPONDENCE.md).
- Present static semantics, rule prerequisites, certainty, and
  PASS/FAIL/UNKNOWN from the [rule catalog](../08_CONSISTENCY_RULE_CATALOG.md)
  and [formal semantics](../metamodel/STATIC_SEMANTICS.md).
- Separate persisted BDI/environment mappings from current in-memory
  organization correspondence evidence.
- Treat absent runtime facts as UNKNOWN, never as an invented PASS.

## 7. Plugin Architecture And Implementation

- Present adapter boundaries and normalized Java realizations.
- Follow the immutable current-analysis snapshot through indexes, mappings,
  USE projection, validation, trace, reports, and UI/headless consumers.
- Explain independent profile, Java IR, parser, plugin, and USE version fields.
- Preserve source-of-truth boundaries: Jason parses AgentSpeak; JaCaMo/CArtAgO/
  Moise adapters supply bounded static evidence; USE supplies UML/OCL/state;
  the Java rule engine supplies executable validation behavior.

## 8. Traceability And Graphical Viewpoints

- Define the portable trace vocabulary and explicit gap semantics.
- Explain graphical concrete syntax, view modes, layers, focus, issue paths,
  navigation limits, and deterministic current-view SVG export.
- State repeatedly that these views explain a frozen result and neither edit a
  semantic model nor reparse/revalidate source.
- Use the Task 12 source-backed raster set with its recorded inputs/view state;
  retain deterministic SVG/tests as the executable graphical boundary evidence.

## Thesis Figure Catalog

| Figure source | Draft caption | Narrative use |
| --- | --- | --- |
| [BDI/profile metamodel](../evidence/bdi-metamodel-diagram.mmd) | **Figure: Bounded JaCaMo Consistency Analysis Profile.** Source/evidence, BDI, static MAS, declaration-level environment, and static-normalized organization concepts are shown with explicit runtime exclusions. | Chapter 5: introduce profile structure and multiplicities, then point to unsupported/unknown evidence and explain why UML/OCL, mappings, issues, and UI are outside the abstract syntax. |
| [Java IR realization](../evidence/ir-class-diagram.mmd) | **Figure: Adapter-to-IR realization of the analysis profile.** Official parser/API types stop at adapter boundaries and produce plugin-owned immutable values. | Chapters 5 and 7: connect profile concepts to executable Java types, highlighting optional context, portable source identity, and environment/organization limits. |
| [Auction architecture](../evidence/auction-architecture.mmd) | **Figure: Auction analysis and evidence pipeline.** Existing AgentSpeak/JaCaMo and USE inputs converge in one immutable current-analysis snapshot whose reports retain profile, IR, and parser provenance. | Chapter 7: narrate source authority, snapshot composition, validation, trace, diagram, report, and state-safety boundaries. |
| [Traceability diagram](../evidence/traceability-diagram.mmd) | **Figure: Evidence-preserving source-to-diagnostic trace.** Confirmed mappings lead to qualified UML/OCL targets while absent/stale mappings remain explicit gaps; reports and views consume the same evidence. | Chapter 8: explain how a reviewed issue is reconstructed without presentation-layer inference and why runtime evidence remains out of scope. |

## 9. Evaluation

- Describe the versioned five-case Auction manifest, isolated runner, external
  oracle, deterministic output, state/input immutability, and process outcomes.
- Report metamodel and correspondence trace coverage with their declared
  classification denominators and limitations.
- Present the four-demo diagram benchmark method and structural determinism;
  timings are local observations, not universal thresholds.
- Separate detection/explanation evidence from usability, productivity, and
  statistical claims, none of which are evaluated.

## 10. Threats And Limitations

Use the checked-in [threats](../evidence/threats-to-validity.md),
[limitations](../evidence/limitations.md), and
[future work](../evidence/future-work.md) directly. Preserve construct,
internal, external, conclusion, profile-coverage, runtime, persistence,
navigation, screenshot, and release boundaries. Do not remove UNKNOWN results
or residual requirements to simplify the narrative.

## 11. Conclusion And Future Work

- Answer each RQ only within the supported subset and reviewed corpus.
- Restate C1–C6 using the evidence-qualified wording below.
- Prioritize an independent corpus, richer term/reference classification,
  organization mapping persistence, larger performance study, and a versioned
  runtime-trace sidecar before any runtime claim.
- Keep release tag and external artifact backup separate from technical/research
  completion because they require release-owner inputs.

## Contribution-To-Evidence Trace

| Contribution | Source implementation | Tests | Evidence file | Evaluation case | Limitation | Chapter section |
| --- | --- | --- | --- | --- | --- | --- |
| C1 bounded analysis profile | `model.ir`, `model.mas`, `model.environment`, `model.organization`, official adapters, `AnalysisMetamodelDescriptor` | importer/normalizer golden, diagnostic, relocation, package-boundary, profile artifact, and Java-alignment tests | [profile validation](../evidence/metamodel-profile-validation.md), [Java alignment](../evidence/metamodel-java-alignment-validation.md), [coverage](../evidence/metamodel-coverage.md) | Four canonical imports and Auction `.asl`/`.jcm` | Static supported subset; no complete AgentSpeak/JaCaMo semantics or EMF runtime | Chapters 5 and 7 |
| C2 explicit correspondence model | `model.mapping`, environment/organization mapping records, repositories, staleness services | candidate/confirmation, codec/repository, relocation, stale-target, correspondence-contract tests | [cross-model validation](../evidence/cross-model-correspondence-validation.md), [coverage](../evidence/correspondence-coverage.md) | Auction BDI/environment/organization mappings and mutants | Organization correspondence persistence is absent; confirmation is reviewer intent, not equivalence proof | Chapter 6 |
| C3 deterministic consistency analysis | `validation`, `rules`, `index`, `use`, `application` | catalog/phase/orchestrator, baseline/mutant, OCL/effect, ENV/ORG UNKNOWN, fingerprint tests | [static-semantics validation](../evidence/static-semantics-validation.md), [Auction evaluation](../evidence/auction-evaluation.md) | One Auction PASS baseline and four detected controlled mutants plus ENV/ORG cases | Bounded rule catalog and evidence prerequisites; no whole-system correctness result | Chapters 6 and 9 |
| C4 evidence-rich outcomes | `ConsistencyIssue`, certainty/status models, current snapshot, JSON/HTML/CSV writers, CLI | issue fingerprint, report determinism/escaping, suppression, CLI exits, descriptor propagation, state safety | [pipeline integration](../evidence/metamodel-pipeline-integration-validation.md), [baseline/headless reconciliation](../evidence/baseline-reconciliation.md) | All reviewed Auction cases and repeated reports | Evidence completeness is bounded by adapters, mappings, and captured snapshot; UNKNOWN is not PASS | Chapters 7 and 9 |
| C5 trace-derived graphical viewpoints | `trace`, `diagram`, `ui`, `problems`, SVG exporter | trace chain/gap/dedup, diagram boundary/projection/layout/mode/layer/focus/navigation/highlight/export/Explorer tests | [graphical validation](../evidence/graphical-viewpoint-validation.md), [trace evidence](../evidence/traceability-graph.md), [performance](../evidence/diagram-performance.md), [raster evidence](../evidence/ui-screenshots.md) | Family Person, Smart Queue, Smart Home, Auction, and four Auction mutant paths | Read-only presentation, not an editor/runtime view; direct cross-tab navigation remains residual | Chapter 8 |
| C6 reproducible scoped evaluation | `evaluation`, manifest codec/runner/writer, headless service, scripts | validation, isolation, input/state immutability, timeout/tool-error, repeat-output, packaged smoke tests | [research evaluation validation](../evidence/research-evaluation-validation.md), [Auction evidence](../evidence/auction-evaluation.md) | Versioned one-baseline/four-mutant Auction manifest | One small reviewed corpus; no statistical/general precision, recall, correctness, usability, or productivity claim | Chapter 9 |

## Writing And Citation Discipline

- Use the exact source metadata and inspected sections/pages in the literature
  audit. Do not copy page/figure claims from unverified secondary references.
- Cite project behavior to source, tests, ADRs, or evidence—not to a generated
  deep report alone.
- Qualify conclusions with “for the supported static subset,” “for the reviewed
  Auction corpus,” “using explicit confirmed mappings,” and “when required USE
  snapshot evidence is available.”
- Never use “proof,” “guarantee,” “full JaCaMo integration,” “runtime
  verification,” “automatic mapping,” or “synchronization” as unqualified
  descriptions of the implemented contribution.

## Integration Gate

```text
Canonical docs: synchronized
Combemale mapping: explicit
Related work: primary-source bounded comparison
Contribution/evidence trace: C1-C6 complete with limitations and chapters
Figures: four source-backed captions and narratives
Requirements: FR-META traced; FR-DIA-007/008 and release boundaries explicit
Threats/limitations: preserved
```
