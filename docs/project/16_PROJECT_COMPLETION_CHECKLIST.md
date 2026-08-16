# Project Completion Checklist

Status: current gates only; historical execution detail belongs to Git history
and `evidence/`
Verification: source-backed; see Git history and DocumentationContractTest

Use this file for current completion and release gates. Do not append task
diaries, commit hashes, dates, or volatile test counts.

## 1. Research MVP

- [x] AgentSpeak is parsed through pinned Jason and normalized into immutable,
  source-located, parser-independent IR with explicit unsupported evidence.
- [x] Static `.jcm`, CArtAgO declaration, and bounded Moise organization inputs
  are normalized behind adapter boundaries without launching a runtime.
- [x] USE UML/OCL and snapshot data are projected read-only; bounded effects
  restore the original state.
- [x] Candidate and confirmed correspondences are distinguished, persisted
  where supported, portable, and checked for stale sources/targets.
- [x] The standard 22-rule catalog and separate environment/organization
  catalogs have deterministic executable tests and explicit UNKNOWN behavior.
- [x] Problems, Mapping, Explorer, headless analysis, and current-analysis
  JSON/HTML export consume one immutable analysis snapshot.
- [x] Trace graphs and BDI/MAS diagrams are derived, portable, read-only
  explanations with explicit gaps, issue paths, focus/layers, and SVG export.
- [x] The Auction baseline and four reviewed mutants execute through the real
  isolated headless service with deterministic scoped outcomes.
- [x] Four canonical demos have source-backed walkthroughs and deterministic
  diagram projections.

## 2. Metamodel And Research Integration

- [x] Analysis profile and Java-alignment contracts classify every profile
  concept and preserve the Java IR and official parsers as executable truth.
- [x] The versioned profile descriptor is propagated through direct `.asl` and
  `.jcm` snapshots and JSON/HTML reports independently of IR/parser versions.
- [x] Static semantics, correspondence types, graphical notation/viewpoints,
  and compatibility rules trace to executable implementation and tests.
- [x] Metamodel and correspondence coverage are measured as structural trace
  coverage, not semantic completeness.
- [x] The thesis outline includes the language-engineering mapping, prior-work
  comparison, figure narratives, C1–C6 evidence trace, and limitations.
- [x] Canonical requirements, architecture, design, rule catalog, traceability,
  decision log, user/developer guides, and documentation index are synchronized.

## 3. Quality Gates

- [x] Focused metamodel, documentation, evidence, and diagram benchmark tests.
- [x] Plugin reactor test.
- [x] Root `mvn --batch-mode --no-transfer-progress clean verify`.
- [x] Packaged parser/report/menu and headless quality-gate smoke checks.
- [x] Reviewed Auction evaluation and performance scripts.
- [x] Documentation links, stable facts, and evidence inventory contract tests.
- [x] Task 12 refreshed Auction raster set covers USE class/object, Explorer,
  Problems, Mapping, focused BDI/mapping/static-MAS views, reviewed `SIG-001`,
  current-analysis export, and SVG export.
- [x] `git diff --check`.

Each completed gate is backed by its task-specific file under `evidence/` and
Git history. Rerun the applicable gate after any source, test, fixture,
dependency, or canonical-document change; do not infer current success from an
old count.

## 4. Open Must Tasks

- [ ] Reviewed Git tag `v1.0.0-thesis-rc` after branch integration and release
  owner approval.
- [ ] Complete, verified backup of source, data, generated report, and thesis
  slides using owner-supplied external artifact locations.

FR-DIA-008 and its diagram-performance/screenshot gates are complete. Tag and
complete-backup entries are release-owner gates; do not create or claim them
without the release decision and external inputs.

## 5. Optional And Accepted Residual Work

- [ ] Optional: import House Building as an exploratory second corpus.

Accepted future work, not hidden completion claims:

- direct cross-tab source/mapping navigation (FR-DIA-007);
- automatic host model/snapshot subscription; manual refresh is supported;
- strict rejection of every unknown mapping JSON field;
- live CArtAgO state, Moise enactment/monitoring, persisted organization
  mappings, runtime traces, and complete JaCaMo semantics;
- larger independent corpora, statistical performance claims, and empirical
  usability/productivity evaluation.

## 6. Evidence Map

| Claim | Primary evidence |
| --- | --- |
| Profile structure and Java alignment | `metamodel-profile-validation.md`, `metamodel-java-alignment-validation.md`, coverage matrix/tests |
| Static semantics and correspondence | rule catalog, rule/correspondence matrices, alignment and coverage evidence |
| Snapshot/report profile metadata | `metamodel-pipeline-integration-validation.md` and descriptor/report tests |
| Import, mapping, validation, and state safety | adapter/IR/mapping/rule/OCL tests and baseline evidence |
| Static JaCaMo/CArtAgO/Moise boundaries | parser/pilot evidence, adapter boundary tests, and ADRs |
| Trace and visualization | trace graph, graphical viewpoint evidence, canonical demos, mutant paths, SVG tests |
| Diagram performance | `diagram-performance.md` and repeated structural fingerprint script |
| Scoped evaluation | versioned Auction manifest/results and `research-evaluation-validation.md` |
| Thesis/canonical integration | `thesis-integration-outline.md` and `thesis-integration-validation.md` |
| Limitations and future work | `threats-to-validity.md`, `limitations.md`, and `future-work.md` |
| Packaging and release | smoke/clean-clone/backup evidence and open owner gates above |

## 7. Per-Task Completion Rule

A task is complete only when:

1. requirements and affected boundaries are identified;
2. implementation and documentation match executable source truth;
3. success, failure, and unknown/unsupported behavior have proportionate tests;
4. focused, module, and required root gates pass;
5. evidence, traceability, limitations, and ADRs are synchronized;
6. `git diff --check` passes and unrelated work is preserved;
7. the coherent change is committed on the feature branch.
