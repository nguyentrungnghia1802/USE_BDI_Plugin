# Literature, Prior-Work, and Reuse Audit

Status: **VERIFIED FOR ROADMAP USE**

Audit date: 2026-08-17

Scope: metamodel and tooling decisions for the USE BDI consistency plugin

Baseline: [Task 01 reconciliation](../evidence/baseline-reconciliation.md)

## Research method and evidence policy

The optional `deep-research-report_1.md` and `deep-research-report_2.md` files are not present in the repository. This audit therefore uses the mandated fallback: primary papers, publisher records, official project documentation, the current source tree, and executable project evidence. A search result or secondary bibliography was used only to locate a primary record; no thesis claim below depends only on a secondary summary.

`PRIMARY_SOURCE_UNVERIFIED` means that the primary record or the exact detail was not accessible. No item carrying that marker is a mandatory design foundation. Detailed claims identify a page, section, figure, or official-document section. Paraphrases are intentionally short.

## Verified source ledger

| ID | Complete metadata | Exact claim used and location | Provenance and verification |
|---|---|---|---|
| S1 | Benoit Combemale, Robert France, Jean-Marc Jézéquel, Bernhard Rumpe, James Steel, Didier Vojtisek. *Engineering Modeling Languages: Turning Domain Knowledge into Tools*. 1st ed., Chapman & Hall/CRC, copyright 2017, 402 pp. DOI [10.1201/b21841](https://doi.org/10.1201/b21841), print ISBN 978-1-4665-8373-3. | A modeling language is engineered through abstract syntax, concrete syntax, semantics, validation, and instrumentation; heterogeneous modeling and language integration are explicit concerns. Publisher description and table-of-contents sections “What's a Modeling Language,” “Metamodeling With MOF and ECORE,” “Metamodeling With OCL,” “Heterogeneous Modeling,” and “Software Language Engineering.” | Primary publisher record. The record says copyright 2017; some catalogues cite 2016. Use 2017 unless a consulted edition's title page says otherwise. Page-level text was not accessible, so no page-specific quotation is used. |
| S2 | Baris Tekin Tezel, Moharram Challenger, Geylani Kardas. “A Metamodel for Jason BDI Agents.” *5th Symposium on Languages, Applications and Technologies (SLATE 2016)*, OASIcs 51, article 8, pp. 8:1–8:9, Schloss Dagstuhl, 2016. DOI [10.4230/OASIcs.SLATE.2016.8](https://doi.org/10.4230/OASIcs.SLATE.2016.8), ISBN 978-3-95977-006-4. | Jason-specific agent structure, plan trigger/context/body, actions/subgoals, graphical syntax, and model conformance. Sections 3–4 and Figure 1, pp. 8:3–8:7. | Primary open-access paper and official Dagstuhl metadata; CC BY 3.0. Concepts may be referenced, but no implementation artifact is copied. |
| S3 | Geylani Kardas, Baris Tekin Tezel, Moharram Challenger. “Domain-specific modelling language for belief–desire–intention software agents.” *IET Software* 12(4), pp. 356–364, 2018. DOI [10.1049/iet-sen.2017.0094](https://doi.org/10.1049/iet-sen.2017.0094). | The abstract syntax is an Ecore metamodel; the concrete syntax is a Sirius IDE with MAS, Agent, Plan, and Logical Expression diagrams; constraints/static-semantics checks run in the environment; Acceleo M2T generates Jason. Sections 2–4 and Figures 1–3. Evaluation covers generation/development time and a small developer questionnaire; Sections 4 and 6. | Primary Wiley full-text HTML was available during the audit. The downloadable implementation bundle's current license/provenance was not independently verified, so it is not reused. |
| S4 | Burak Karaduman, Baris Tekin Tezel, Geylani Kardas, Moharram Challenger. “DSML4JaCaMo: A Modelling Tool for Multi-agent Programming with JaCaMo.” *Proceedings of the 19th Conference on Computer Science and Intelligence Systems (FedCSIS 2024)*, ACSIS 39, pp. 637–642, 2024. DOI [10.15439/2024F6157](https://doi.org/10.15439/2024F6157). | Ecore abstract syntax partitioned into Jason agent, CArtAgO artifact/environment, and Moise organization; Sirius viewpoint-based graphical syntax; Acceleo generation of ASL, artifact Java, and organization XML. Sections III–V, Figure 1, Listing 1, pp. 638–641. Evaluation is two case studies and generated/final lines of code; static analysis via M2M to CPN or Statecharts is future work. Sections V–VI, pp. 640–642. | Primary proceedings record and full paper. Figure 1 was rendered at 600 dpi and inspected in full because multiplicities are layout-sensitive. |
| S5 | Ghassan Beydoun, Graham Low, Brian Henderson-Sellers, Haralambos Mouratidis, Jorge J. Gomez-Sanz, Juan Pavón, Cesar Gonzalez-Perez. “FAML: A Generic Metamodel for MAS Development.” *IEEE Transactions on Software Engineering* 35(6), pp. 841–863, 2009. DOI [10.1109/TSE.2009.34](https://doi.org/10.1109/TSE.2009.34). | FAML seeks consensus/unification among heterogeneous MAS metamodels and supplies generic MAS abstractions. Abstract, p. 841. | Primary author manuscript and DOI metadata located; only the abstract-level claim is used. |
| S6 | Christian Hahn, Cristián Madrigal-Mora, Klaus Fischer. “A Platform-Independent Metamodel for Multiagent Systems.” *Autonomous Agents and Multi-Agent Systems* 18(2), pp. 239–266, Springer, 2009. DOI [10.1007/s10458-008-9042-0](https://doi.org/10.1007/s10458-008-9042-0). | A platform-independent MAS metamodel abstracts from methodologies, languages, and platforms and supports transformations to JACK and JADE. Abstract and paper overview. | Primary publisher DOI and official DFKI publication record. The DFKI display omits Madrigal-Mora, while the paper citation/DOI metadata includes all three authors; the three-author form is retained. |
| S7 | Ömer Faruk Alaca, Baris Tekin Tezel, Moharram Challenger, Miguel Goulão, Vasco Amaral, Geylani Kardas. “AgentDSM-Eval: A Framework for the Evaluation of Domain-Specific Modeling Languages for Multi-agent Systems.” *Computer Standards & Interfaces* 76, article 103513, 2021. DOI [10.1016/j.csi.2021.103513](https://doi.org/10.1016/j.csi.2021.103513). | Evaluation dimensions include ease of use, MAS-domain coverage, toolset richness/efficiency, generation productivity, development time/throughput, and qualitative quality characteristics. Section 2, pp. 5–13; Table 2.1. | Primary accepted manuscript plus publisher DOI metadata. |
| T1 | Eclipse Foundation. [Eclipse Modeling Framework](https://eclipse.dev/emf/), continuously maintained official documentation. | Ecore describes models; EMF adds runtime notification, XMI persistence, reflection, edit support, and code generation. “EMF Core” section. | Official project documentation, accessed 2026-08-17. |
| T2 | Eclipse Foundation. [Sirius Specifier Manual](https://eclipse.dev/sirius/doc/specifier/Sirius%20Specifier%20Manual.html), continuously maintained official documentation. | Sirius defines viewpoint-based representations over EMF domain models, including diagrams, tables, trees, and properties. “Viewpoints” and representation-description sections. | Official project documentation, accessed 2026-08-17. |
| T3 | Eclipse Foundation. [Epsilon Validation Language](https://eclipse.dev/epsilon/doc/evl/) and [Epsilon overview](https://eclipse.dev/epsilon/doc/). | EVL specifies constraints/critiques, messages, fixes, dependencies, and intra/inter-model validation. EVL “Abstract Syntax” and “Execution Semantics”; overview “Task-Specific Languages.” | Official project documentation, accessed 2026-08-17. |
| T4 | Eclipse Foundation. [Acceleo](https://eclipse.dev/acceleo/), continuously maintained official documentation. | Acceleo is template-based model-to-text generation over EMF models. Home-page “Generate anything from any EMF model” and “Easily create custom code generators.” | Official project documentation, accessed 2026-08-17. |

## 1. Modeling language engineering framework

For this project, the modeling-language layers are operationalized as follows:

| Language-engineering concern | Meaning here | Existing project implementation | Decision |
|---|---|---|---|
| Abstract syntax | Stable concepts, attributes, relationships, multiplicities, and invariants | Parser-independent Java IR in `model.ir`, `model.mas`, `model.environment`, and `model.organization` | Specify a normative UML-style profile and map it to, rather than replace, the Java IR. |
| Concrete syntax | Textual source and graphical projections | Existing Jason/MAS/Moise source import; Java2D/Swing explorer and diagrams | Preserve source-first analysis and existing renderer. Prior graphical DSML notations are references, not copied assets. |
| Static semantics | Well-formedness and cross-model consistency constraints | Deterministic Java validators, rule phases, USE snapshots/OCL evaluation, issue evidence | Extend the existing rule engine. Do not introduce a parallel EVL runtime. |
| Dynamic/translational semantics | Execution, interpretation, or code generation | Out of the plugin's analysis purpose; the plugin consumes existing sources | Reference prior work only. Code generation is not a thesis contribution. |
| Tool instrumentation | Importers, editor/viewer, validator, report/export, test harness | Implemented plugin UI, CLI, reports, trace graph, JUnit and smoke/evaluation scripts | Keep and strengthen. |
| Heterogeneous integration | Explicit relationships among languages/models | BDI↔UML mapping, environment/organization mapping, OCL evidence, staleness detection | Central thesis concern: make correspondence, uncertainty, and evidence explicit. |

S1 therefore supplies an engineering decomposition, not an implementation mandate. This project already has equivalents for most workbench components and must avoid replacing them merely to resemble an EMF stack.

## 2. BDI/Jason metamodel lineage

### Jason metamodel (2016)

S2 models a MAS with agents and shared sets, then models an agent using belief base, plans, goals/events, rules, and actions. Its plan structure includes one triggering event, a context/logical expression, and a body containing actions, subgoals, or mental notes (Sections 3–4, Figure 1). Internal and external actions are distinct. Communication is represented by message-related concepts in the graphical notation.

Reusable semantic shape:

- `Agent` owns collections of beliefs, goals, and plans.
- `Plan` has a mandatory trigger and body, with an applicability context that can be absent in textual Jason.
- Body elements are ordered; internal actions and external/basic actions must remain distinguishable.
- Trigger kinds distinguish additions/deletions and belief/achievement/test-style events.

Current naming differs intentionally: `PlanStepModel` is the ordered-body abstraction; `ContextExpr` is a typed expression tree; `ActionStepModel` and `InternalActionStepModel` distinguish external/basic and internal actions. `TriggerModel` makes operator and type explicit. The current IR has no first-class `RuleModel` or `MessageModel`; those are gaps, not permission to fabricate instances from incomplete source data.

S2's EuGENia/Ecore editor and graphical notation demonstrate feasibility and conformance, but they are not adopted. The paper is CC BY, yet no upstream implementation artifact or compatible code provenance is required for this roadmap.

### DSML4BDI (2018)

S3 extends the lineage into a full model-first DSML: Ecore defines abstract syntax, Sirius supplies four graphical perspectives, environment constraints perform static-semantic checks, and Acceleo generates Jason. The distinction from this project is material:

- DSML4BDI starts with a designed model and generates AgentSpeak.
- This plugin starts with existing Jason/JaCaMo and USE artifacts, imports conservative IR, and diagnoses cross-model consistency.
- DSML4BDI's evaluation targets generation productivity, development time, and a small user study; this roadmap targets deterministic detection, evidence completeness, and reproducibility.

Reuse is conceptual (`ADAPT_CONCEPT` for the BDI vocabulary, `REFERENCE_ONLY` for its workbench and evaluation), not an Ecore/runtime import.

## 3. JaCaMo DSML lineage

S4 is the highest-priority prior work because it already spans the three JaCaMo dimensions.

### EClass inventory and partitions

| Partition | EClasses visible in Section III/Figure 1 |
|---|---|
| Root | `MAS` |
| Jason agent | `Agent`, `Belief`, `Rule`, `Goal`, `Plan`, `Context`, `Body`, `BodyTerm`, `Action`, `InternalAction`, `ExternalAction`, `MentalNotes`, `Message`, `TriggeringEvent` |
| CArtAgO environment | `Workspace`, `Artifact`, `Port`, `ObsProperty`, `AbsOperation`, `Operation`, `GuardOperation`, `InternalOperation`, `LinkedOperation` |
| Moise organization | `Organisation`, `NormativeSpecification`, `StructuralSpecification`, `FunctionalSpecification`, `Norm`, `Group`, `Role`, `FormationConstraints`, `Link`, `Scheme`, `Mission`, `OGoal`, `OPlan` |

### Associations and multiplicities retained as prior-work evidence

The following values were read directly from Figure 1 at 600 dpi. They are evidence about S4, not automatic constraints on this project:

| Association | Figure 1 multiplicity/role |
|---|---|
| `MAS` → `Agent` | `agent [1..*]` |
| `MAS` → `Workspace` | `workspace [0..*]` |
| `MAS` → `Organisation` | `organisation [0..*]` |
| `Agent` → `Plan` | `plan [0..*]` |
| `Agent` → `Belief` | `belief [0..*]` |
| `Agent` → `Rule` | `rule [0..*]` |
| `Agent` → `Goal` | `hasGoal [0..*]` |
| `Plan` → `Body` | `hasBody [1..1]` |
| `Plan` → `Context` | `hasContext [1..1]` in the figure |
| `Plan` → `TriggeringEvent` | `triggersPlan [1..1]` |
| `Plan` → `Action` | `hasAction [0..*]`; `Body.firstAction [0..1]`; `Action.nextAction [0..1]` |
| `Workspace` → `Artifact` | `artifact [0..*]`; also `refArtifact [0..*]` |
| `Artifact` → `ObsProperty` | `obsproperty [0..*]` |
| `Artifact` → `AbsOperation` | `operation [0..*]` |
| `Artifact` → `Port` | `port [0..*]`; `Port.linkArtifacts [0..*]` |
| `Organisation` → specification partitions | `normativespecification [1..1]`, `structuralspecification [1..1]`, `functionalspecification [1..1]` |
| `NormativeSpecification` → `Norm` | `norm [0..*]` |
| `StructuralSpecification` → `Group` | `group [0..*]`; recursive subgroup support is shown |
| `FunctionalSpecification` → `Scheme` | `scheme [0..*]` |
| `Scheme` → `Mission` | `mission [1..*]` |
| `Scheme` → `OGoal` | `SchemeOgoal [1..*]` |
| `Mission` → `OGoal` | `ogoal [1..*]` |
| `Norm` → `Role` / `Mission` | `NRole [1..1]` and `NMission [1..1]` |

Two adaptations are mandatory. First, Jason textual plans may omit the context, and the current `PlanModel` correctly represents it as `Optional<ContextExpr>`; S4's `hasContext [1..1]` must not override observed source semantics. Second, the current plugin imports conservative static subsets; missing source details remain unsupported/unknown rather than being completed to satisfy S4 multiplicities.

### Viewpoints, generation, and evaluation

S4 uses Sirius viewpoints and demonstrates MAS, agent, and organizational/scheme views in its case studies (Sections III and V, Figures 2–4). Listing 1 generates one ASL file per agent, Java for artifact classes/operations/properties, and Moise organization XML. The evaluation compares generated versus final lines of code in two cases and reports 76% overall generation in the conclusion.

The paper explicitly puts static analysis after modeling into future M2M work: Jason/Moise could be transformed to Coloured Petri Nets, and deterministic organizational goal/plan structures could be represented with Statecharts (Section V, pp. 640–641). Inspection of Sections III–VI found no UML/OCL cross-model consistency framework, no issue/evidence/trace model comparable to this repository, and no import-existing-JaCaMo-to-USE analysis path. These are scoped absence findings about the paper, not universal claims about every related artifact.

## 4. Generic MAS metamodel references

FAML (S5) and Hahn/Madrigal-Mora/Fischer (S6) justify `MAS`, `Agent`, relationships, goals/tasks, interactions, roles/organizations, and platform separation at a generic level. They deliberately sit above JaCaMo platform details. They are `REFERENCE_ONLY`:

- they prevent a JaCaMo profile from confusing platform details with universal MAS concepts;
- they help explain why a root system, agents, roles, and interactions belong in an abstract syntax;
- they are not implementation targets and do not displace the Jason/CArtAgO/Moise-specific import model.

## 5. Tooling ecosystem

| Tool/technology | Role in prior work | Current project need | Decision | Reason |
|---|---|---|---|---|
| EMF/Ecore | Metamodel specification/runtime and basis of S2–S4 | Formal abstract-syntax specification | `REFERENCE_ONLY` / specification vocabulary | A new Ecore runtime would duplicate and destabilize the tested Java IR. Task 04 may use Ecore-like concepts in documentation without adding EMF dependencies. |
| Sirius | Viewpoint-based graphical modeling over Ecore in S3–S4 | Notation and viewpoints | `REFERENCE_ONLY` | USE already embeds a Java2D/Swing explorer, layered diagrams, navigation, issue overlays, and SVG export. |
| Epsilon/EVL | Constraints, critiques, fixes, intra/inter-model validation | Conceptual comparison for deterministic cross-model rules | `REFERENCE_ONLY` | The existing Java rule engine already has phases, configuration, evidence, certainty, suppression, CLI, and JUnit coverage. No rewrite by default. |
| Acceleo | M2T generation in S3–S4 | None central | `OUT_OF_SCOPE` | The project analyses existing source and does not claim model-to-code generation. |
| EuGENia/GMF | Historical Ecore-to-editor tooling used in S2 lineage | None | `DO_NOT_REUSE` | It adds an Eclipse editor stack without serving the USE-integrated analysis path. |
| Java2D/Swing | Current USE plugin visualization | Integrated graphical diagnostics | `REUSE_DIRECT` | It is an implemented, tested product asset. |
| JUnit 5 | Executable evidence | Unit/integration/contract gates | `REUSE_DIRECT` | It anchors deterministic regression evidence in the current Maven build. |

## 6. Gaps relevant to this thesis

Prior work already covers Jason and JaCaMo metamodels, graphical editors, and model-to-code generation. The defensible gaps addressed by this project are narrower:

1. A conservative, source-first JaCaMo analysis profile adapted to USE rather than another authoring DSML.
2. Explicit heterogeneous correspondences among BDI/MAS/environment/organization elements and USE UML classes, objects, attributes, operations, links, cardinalities, and OCL constraints.
3. Deterministic cross-model rules whose prerequisites, outcomes, certainty, and bounded evidence are reportable.
4. Staleness-aware persisted mappings and source provenance.
5. A trace graph linking source elements, mappings, UML targets, rules, issues, and evidence.
6. Integrated graphical diagnostics and reproducible headless/evaluation gates.

Current implementation already covers most of points 2–6. Task 04 should formalize the conceptual profile around the code rather than create a parallel model stack.

AgentDSM-Eval (S7) is useful for claim discipline. The roadmap may measure domain coverage, detection behavior, evidence completeness, reproducibility, and scoped tool integration. A user study is not required unless the thesis claims broad usability, learnability, productivity, or development-time improvement.

## 7. Reuse/adapt/reject decision register

Only the following decision enum values are valid: `REUSE_DIRECT`, `ADAPT_CONCEPT`, `REFERENCE_ONLY`, `DO_NOT_REUSE`, `OUT_OF_SCOPE`.

| ID | Item | Decision | Rationale / consequence |
|---|---|---|---|
| R01 | Current parser-independent Java IR | `REUSE_DIRECT` | Normative runtime representation; extend compatibly only where a roadmap task proves a gap. |
| R02 | Current MAS/environment/organization IR | `REUSE_DIRECT` | Already imports JaCaMo, CArtAgO, and Moise subsets with provenance and unsupported-feature handling. |
| R03 | Current mapping, validation, trace, diagram, report, CLI, and JUnit assets | `REUSE_DIRECT` | They implement the analysis pipeline and executable evidence. |
| R04 | S2 Jason Agent/Belief/Goal/Plan/Trigger/Context/Body/Action vocabulary | `ADAPT_CONCEPT` | Align names and relationships while preserving source semantics and current IR identities. |
| R05 | S3 DSML4BDI abstract-syntax partitions | `ADAPT_CONCEPT` | Useful lineage for BDI completeness; do not adopt its model-first workflow. |
| R06 | S4 agent/environment/organization partition | `ADAPT_CONCEPT` | Primary prior-work alignment for Task 04; record deviations explicitly. |
| R07 | S4 multiplicities | `ADAPT_CONCEPT` | Reuse only when compatible with actual JaCaMo/Jason source and conservative import behavior. |
| R08 | FAML and platform-independent MAS abstractions | `REFERENCE_ONLY` | Justify generic concepts without making them an implementation target. |
| R09 | Ecore | `REFERENCE_ONLY` | Use as metamodel-specification vocabulary; do not replace the Java IR/runtime. |
| R10 | Sirius viewpoints/notations | `REFERENCE_ONLY` | Inform view separation; do not add Sirius or copy notation assets. |
| R11 | EVL concepts | `REFERENCE_ONLY` | Compare rule organization and inter-model validation; retain Java validator. |
| R12 | Acceleo generation | `OUT_OF_SCOPE` | Prior work already covers generation; plugin analyses existing artifacts. |
| R13 | EuGENia/GMF editor stack | `DO_NOT_REUSE` | No current need; conflicts with integrated USE UI direction. |
| R14 | Upstream DSML4BDI/DSML4JaCaMo implementation code | `DO_NOT_REUSE` | No dependency is needed, and artifact license/provenance is not established for code reuse. |
| R15 | CPN/Statechart verification transformations | `OUT_OF_SCOPE` | Interesting future work in S4, but not required for the bounded consistency claims. |
| R16 | AgentDSM-Eval domain-coverage and qualitative dimensions | `REFERENCE_ONLY` | Select only dimensions matching actual evidence; do not imply a user study. |

## 8. Novelty risks

### Claims we must NOT make

- “first Jason metamodel” — forbidden by S2 and S3.
- “first JaCaMo metamodel” — forbidden by S4.
- “first graphical JaCaMo DSML” — forbidden by S4.
- “first Ecore model for BDI” — forbidden by S2/S3.
- “full JaCaMo integration” — forbidden because the importer deliberately supports bounded static subsets and records unsupported features.
- “formal proof of semantic equivalence” — forbidden; rules provide bounded checks/evidence, not a proof of whole-system semantics.
- “general correctness from Auction mutants” — forbidden; the mutation corpus demonstrates scoped detection behavior only.
- “complete coverage of Jason/CArtAgO/Moise” — forbidden unless future evidence removes the documented unsupported/unknown cases.
- “usability or productivity improvement” — forbidden without an appropriately designed empirical user study.

### Candidate contributions supported by the project

- An adaptation/profile of established Jason/JaCaMo metamodel concepts for conservative USE analysis.
- Explicit heterogeneous correspondences between source concepts and UML/OCL model/state elements.
- A deterministic cross-model rule framework with ordered phases and stable rule identifiers.
- Uncertainty-aware semantic checks that distinguish confirmed, potential, and unknown outcomes.
- Source/mapping/rule/issue trace links with bounded evidence.
- Integrated graphical diagnostics inside USE rather than a separate model-authoring IDE.
- Reproducible, scoped evaluation using tests, headless gates, and a versioned mutation corpus.

## 9. Open questions

| Question | Safe current position | Roadmap consumer |
|---|---|---|
| Should `Rule` and `Message` become first-class IR records? | They are established prior-work concepts but absent from current IR. Add only if a later requirement/rule needs them and source adapters can populate them without invention. | Tasks 04–07 |
| Should plan context be mandatory? | No. Preserve `Optional<ContextExpr>` because textual Jason permits context omission; document deviation from S4 Figure 1. | Task 04 |
| Should the normative profile be delivered as `.ecore`? | Not by default. A precise textual/UML profile mapped to Java is sufficient unless Task 04's own gate explicitly requires machine-readable Ecore. | Task 04 |
| How much of CArtAgO operation specialization is required? | Current IR retains operation signature/type-relevant data but does not mirror every S4 subclass. Treat absent distinctions as scoped, not silently equivalent. | Tasks 04, 06 |
| Are all Moise formation/link features supported? | No. Current `OrganizationModel` is a documented static subset and records unsupported features. | Tasks 04, 07 |
| Is a user study required? | Only for usability/productivity claims. Detection/evidence/reproducibility claims can use existing executable evidence. | Tasks 10–12 |

## Project alignment audit

| Current capability | Source evidence in repository | Prior-work relationship |
|---|---|---|
| Jason source-first import and typed IR | `importer`, `model.ir`, parser/normalizer tests | Adapts S2/S3 vocabulary; reverses the model-first generation direction. |
| JaCaMo project root and resources | `model.mas`, project importer | Adapts S4 root/partition while retaining source provenance/status. |
| Artifact/workspace/operation/property model | `model.environment` | Adapts S4 CArtAgO partition conservatively. |
| Role/group/scheme/mission/goal/norm/cardinality model | `model.organization` | Adapts S4 Moise partition and records unsupported details. |
| UML class/object/attribute/operation/association/OCL snapshot | `use` | Project-specific heterogeneous target absent from S2–S4's described workflows. |
| Persisted mapping and staleness | `model.mapping`, environment/organization mapping packages, `persistence` | Project-specific correspondence layer. |
| Deterministic issues, certainty, evidence, suppression | `validation`, `problems` | Project-specific static/cross-model analysis, conceptually comparable to EVL but already implemented. |
| Trace nodes/edges and graphical projections | `trace`, `diagram`, `ui` | Project-specific evidence navigation integrated into USE. |
| Reproducible tests/CLI/evaluation | JUnit, smoke/headless/evaluation scripts and evidence | Supports scoped claims; does not substitute for user-study claims. |

## Verification and final gate

- All mandatory primary sources are verified at the level used. No foundational claim is marked `PRIMARY_SOURCE_UNVERIFIED`.
- S4 EClasses, partitions, selected associations/multiplicities, viewpoints, code-generation scope, evaluation scope, and future static-analysis position are extracted.
- Tooling decisions explicitly preserve the Java IR, Java rule engine, Java2D/Swing UI, and JUnit evidence.
- The novelty firewall forbids all roadmap-mandated claims and records additional overclaim risks.
- [Metamodel source matrix](metamodel-source-matrix.md) supplies concept-level alignment for Task 04.

```text
Primary-source verification: PASS — S1–S7 and T1–T4 verified at the claim level used
Metamodel source matrix: PASS — mandatory concepts and current-IR decisions recorded
Reuse decision register: PASS — R01–R16 use only the required enum
Novelty firewall: PASS — forbidden and supportable claims separated
Project alignment: PASS — import, IR, UML/OCL, mapping, validation, trace, UI, report, and evidence covered
Doc tests: PASS — DocumentationContractTest 1/1
git diff --check: PASS
Open failures: none in audit content
Result: PASS
```
