# Claim–Evidence Matrix

Status: **FROZEN PRE-COMMITMENT**

Freeze date: 2026-08-17

Scope and wording are governed by [research positioning](research-positioning.md) and [terminology](terminology.md). Implementation/test entries identify current evidence anchors; later tasks must update a row if they materially change its claim or evidence path.

| Claim / contribution | Implementation | Test | Evidence artifact | Evaluation case | Limitation |
|---|---|---|---|---|---|
| **C1 / RQ1:** The supported static AgentSpeak subset is normalized into parser-independent typed IR while retaining spans and unsupported evidence. | `JasonAslParserAdapter`, `JasonAstToIrNormalizer`, `model.ir` | parser/importer/normalizer golden, malformed, unsupported, relocation tests; package boundary scans | [baseline reconciliation](../evidence/baseline-reconciliation.md); Task 04 profile | Canonical AgentSpeak demos and Auction agents | Not complete AgentSpeak operational semantics; rules/messages are not first-class where absent in IR. |
| **C1 / RQ1:** Static `.jcm` composition is normalized without starting JaCaMo. | `JaCaMoProjectParserAdapter`, `MasProjectImportService`, `MasProjectModel` | project parser/import/analysis, missing/partial/duplicate/relocation, GUI/CLI entry tests | JaCaMo parser spike and baseline reconciliation | Auction `.jcm` project | Static declarations only; no lifecycle, scheduling, or runtime trace. |
| **C1 / RQ1:** Static artifact metadata can be represented conservatively. | `CArtAgOArtifactAdapter`, `model.environment` | adapter, signature/property, unsupported/boundary, project-composition tests | CArtAgO spike and environment-pilot evidence | Auction environment cases | Only official retained operation metadata and explicit property descriptors; no live workspace/values. |
| **C1 / RQ1:** A bounded static Moise subset preserves organization identities, cardinalities, provenance, and unsupported features. | `MoiseOrganizationParserAdapter`, `OrganizationModel` | golden organization, diagnostic, duplicate, unsupported, relocation, adapter boundary tests | Moise parser spike and organization-pilot evidence | Auction organization cases | No enactment, dynamic membership, norm fulfilment, or full Moise coverage. |
| **C2 / RQ2:** Explicit confirmed BDI↔UML correspondences can be persisted and distinguished from candidates. | `MappingDocument`, `MappingBinding`, `MappingSuggestionService`, repository/codec | deterministic round-trip, validation, confirmation/candidate, relocation tests | mapping fixtures; requirement traceability | Auction action/belief/agent mappings | Confirmation is reviewer intent, not proof of semantic equivalence; mapping may become stale. |
| **C2 / RQ2:** Environment/organization correspondences preserve source identity and uncertainty. | environment/organization mapping records, repositories, staleness services | persisted environment mapping, candidate/unknown, stale-target, organization mapping tests | environment-mapping persistence and organization evidence | Auction ENV/ORG baseline and mutants | Persisted organization mapping is not claimed if implementation remains in-memory; runtime evidence remains unavailable. |
| **C3 / RQ2:** Deterministic rule phases detect declared structural/signature/reference/OCL inconsistencies using immutable indexes/snapshots. | `BdiIndex`, `StandardConsistencyRules`, `ValidationOrchestrator`, `UseModelSnapshot` | exact rule-catalog, orchestrator ordering, mapping/signature/reference/OCL baseline and mutant tests | [rule catalog](../08_CONSISTENCY_RULE_CATALOG.md); deterministic reports | Auction baseline plus mapping/signature/reference/OCL mutants | Bounded catalog only; absence of findings does not establish whole-system correctness. |
| **C3 / RQ2:** Static environment and organization checks distinguish violations from unavailable runtime evidence. | `EnvironmentConsistencyValidator`, `OrganizationConsistencyValidator`, separate catalogs | ENV/ORG baseline, mutant, candidate, unavailable-evidence, dedup tests | environment and organization pilot evidence | Auction artifact/property and role/mission/cardinality cases | Matching static bounds/properties remain UNKNOWN where live/enactment evidence is required. |
| **C3/C4 / RQ2:** Snapshot-dependent OCL checks and bounded effects preserve USE state. | `UseSnapshotOclEvaluator`, bounded-effect/status records, snapshot services | evaluation-result status, before/after fingerprint, restoration, failure/unknown tests | headless quality gate; baseline reconciliation | Auction OCL mutant and state fixtures | One reviewed snapshot is not continuous runtime monitoring; effects are bounded/disposable. |
| **C4 / RQ2–RQ3:** Diagnostics retain stable rule identity, status, severity, certainty, source/UML references, and evidence. | `ConsistencyIssue`, `IssueCertainty`, report/CLI models | issue fingerprint, suppression, report determinism, UNKNOWN branches, CLI exit tests | JSON/HTML reports; headless quality-gate evidence | All reviewed Auction cases | Evidence is bounded by available adapters/mappings/snapshot; UNKNOWN is not PASS. |
| **C5 / RQ3:** Trace graphs reconstruct source→mapping→UML/OCL→rule→issue/evidence paths. | `TraceabilityGraphBuilder`, environment/organization contributors, trace records | complete-chain, explicit-gap, certainty, deduplication, portability tests | trace JSON and requirement traceability | Auction baseline/mutants | Trace explains emitted evidence; it does not prove completeness of all possible causal relations. |
| **C5 / RQ3:** USE-integrated diagrams are deterministic read-only projections and do not reparse/revalidate. | `BdiDiagramBuilder`, `MasOverviewDiagramBuilder`, projectors, Swing panel/canvas, SVG exporter | package/boundary, projection/order, mode/layer/focus, navigation/highlight, state, export, Explorer tests | canonical demo walkthroughs; SVG and mutant-path evidence | Family, Smart Queue, Smart Home, Auction | Not an editable semantic model or full graphical editor; direct cross-tab source/mapping navigation remains residual. |
| **C6 / RQ4:** The reviewed Auction corpus executes reproducibly with declared expected outcomes. | `EvaluationManifestCodec`, `EvaluationRunner`, report writer, packaged script | manifest validation, real analysis, isolation, immutability, timeout/tool-error, repeated deterministic output | [Auction evaluation](../evidence/auction-evaluation.md) and versioned JSON/CSV/HTML | One valid baseline + four controlled mutants | Corpus-scoped result only; no general precision/recall or correctness claim. |
| **C6 / RQ4:** Analysis preserves inputs and reports operational failures rather than fabricating findings. | isolated workspaces, hashing, status/error model, state fixture boundary | input hash, path traversal, missing input, timeout, execution error, before/after state tests | evaluation manifest/result hashes; headless gate | Reviewed Auction manifest | Does not cover every OS/toolchain/runtime failure mode. |

## RQ closure criteria

| RQ | Evidence bundle required before answering | Planned answer form |
|---|---|---|
| RQ1 | Task 04 profile + source matrix + importer/IR/golden/unsupported/boundary tests | Construct-by-construct coverage and deviation table for the supported static subset |
| RQ2 | Mapping contract + rule prerequisites/catalog + baseline/mutant/UNKNOWN + USE-state safety | Rule-family results and representative evidence chains, qualified by required mappings/snapshot evidence |
| RQ3 | Trace schema + portable paths + visualization boundary/navigation/export tests | Trace reconstruction examples and integration evidence without semantic recomputation |
| RQ4 | Frozen manifest/hashes + repeated deterministic outputs + expected/actual/miss/unexpected/error counts | Descriptive corpus-scoped table; no inferential/general accuracy claim |

## Prohibited claim-to-evidence substitutions

- A passing baseline cannot establish completeness or general correctness.
- Four detected mutants cannot establish general precision/recall.
- A static Moise cardinality match cannot establish enactment correctness.
- A missing live CArtAgO value cannot be converted to PASS.
- A diagram screenshot cannot replace stable trace/evidence identities.
- Parser-library presence cannot be described as runtime integration.
- Existing implementation volume cannot establish novelty; novelty is comparative and bounded by the prior-work audit.
