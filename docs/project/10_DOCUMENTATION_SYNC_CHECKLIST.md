# Documentation Synchronization Checklist

Status: mandatory project documentation process
Verification: source-backed; see Git history and DocumentationContractTest

## 1. Principle

A feature is complete only when source, tests, runtime behavior, canonical
documentation, checklist state, evidence, and known limitations describe the
same verified system. Documentation is updated from inspected behavior, not
optimistically from a request or class name.

Local AI-agent prompts/instructions under `docs/agent/` are intentionally
untracked and ignored. Repository-wide collaboration rules therefore belong in
canonical project documentation, tests, commit history, and accepted ADRs.

## 2. Canonical ownership matrix

Numbers refer to files in `docs/project/`.

| Change type | Documents that must be checked |
| --- | --- |
| Problem, goals, scope, release status | `00`, `01`, `09`, `16` |
| Requirement, business rule, acceptance behavior | `01`, `03`, `12`, relevant guide/checklist |
| Module, dependency, lifecycle, threading | `02`, `05`, `06`, `09`, ADR log |
| AgentSpeak subset/IR/source span | `01`, `03`, `05`, `06`, limitations, traceability |
| Mapping kind/identity/suggestion | `01`, `03`, `04`, `05`, `09`, traceability |
| JSON schema or persistence | `01`, `04`, `05`, `07`, `08`, `09` |
| Rule ID/phase/semantics | `01`, `03`, rule catalog, `09`, `12`, evidence |
| OCL/state/effect behavior | `01`, `02`, `03`, `05`, `09`, `11`, ADR log |
| Swing menu/view/workflow | `01`, `02`, `03`, user/install guides, screenshot evidence |
| Report field/export composition | `01`, `04`, `05`, `07`, `09`, evidence |
| Fixture/oracle/mutant/metric | `01`, `03`, `07`, `09`, `12`, evidence, checklist |
| Build/JDK/Maven/package/dependency | `00`, `02`, `06`, `07`, `08`, notices, guides |
| Release/backup/tag | `00`, `01`, `07`, `08`, `09`, checklist, release evidence |
| Bug fix with no intended contract change | Regression test, affected current-state docs, checklist/evidence note |

## 3. Required metadata

Each canonical `00` through `12` document maintains:

- purpose/status;
- the stable source-backed verification marker;
- explicit Implemented/Partial/Planned/Optional distinctions;
- current limitations and source-of-truth links;
- a synchronization checklist where useful.

Verification date and commit provenance come from each file's Git history and
the CI result for that commit. Do not duplicate volatile dates, branch names,
or commit hashes across canonical files; that creates synchronization tax and
becomes stale immediately after a documentation commit.

## 4. Pre-change checklist

- [ ] Read the project index, context, requirements, architecture, risks,
  completion checklist, and relevant ADRs.
- [ ] Inspect branch, `git status`, recent commits, effective Java/Maven, and
  relevant current tests.
- [ ] Identify affected requirement IDs, contracts, risks/debt, and evidence.
- [ ] Confirm which files are executable truth: POM, descriptor, codec, source,
  fixture, or script.
- [ ] Record existing code-document mismatch before changing behavior.
- [ ] Preserve unrelated and user-owned worktree changes.
- [ ] Define focused and proportional validation commands.

## 5. During-change checklist

- [ ] Add/update tests with the behavior.
- [ ] Keep unsupported/unknown/failure paths explicit.
- [ ] Do not broaden USE core or parser boundaries without accepted ADR.
- [ ] Do not mark persistence/export helper code as a complete GUI workflow.
- [ ] Update schema/version/migration policy in the same change.
- [ ] Update rule catalog/config when rule IDs change.
- [ ] Keep evaluation claims scoped to the actual corpus/oracle.
- [ ] Add risk/debt when a limitation remains.

## 6. Post-change checklist

### Implementation and tests

- [ ] Focused tests pass.
- [ ] Module/reactor tests pass at the required scope.
- [ ] Package/smoke/clean-clone gates run when affected.
- [ ] Manual GUI acceptance runs when visible behavior changes.
- [ ] `git diff --check` passes.

### Documentation

- [ ] Status and acceptance wording match verified behavior.
- [ ] Context, requirements, architecture, domain flow, data/API, codebase,
  testing, operations, risks, safety, and traceability were reviewed as needed.
- [ ] Specialized guides and evidence remain consistent.
- [ ] Historical evidence is annotated/superseded rather than rewritten.
- [ ] Local Markdown links resolve.
- [ ] No tracked project document depends on `docs/agent/`.

### Git/release

- [ ] Only coherent task files are staged.
- [ ] User/unrelated changes remain unstaged.
- [ ] Commit message describes behavior or documentation contract.
- [ ] No force push or direct upstream-main merge occurred.
- [ ] Release tags are created only after all release gates.

## 7. Status transition rules

- Planned -> Partial only when a useful tested subset exists.
- Planned/Partial -> Implemented only when the full documented acceptance path
  exists and relevant tests pass.
- Implemented -> Partial when a verified regression or missing end-to-end
  composition invalidates the prior claim.
- Any -> Deprecated requires replacement/migration guidance.
- Optional does not become release-blocking without an explicit scope decision.

## 8. Documentation drift test policy

Automated documentation tests should verify stable, high-value facts:

- canonical file inventory and non-empty content;
- local Markdown link resolution;
- USE/plugin/Jason/schema versions;
- menu labels and script markers;
- rule catalog/source registry alignment;
- absence of tracked `docs/agent` references;
- guide/release/notice markers.

Tests should not freeze volatile prose, timestamps, branch names, commit hashes,
line counts, benchmark durations, or every heading. A test is a guardrail, not
a replacement for human review.

## 9. Drift handling

When a mismatch is found:

1. Determine executable truth with source and tests.
2. Decide whether code or documentation is defective.
3. Fix both sides in one coherent change where feasible.
4. Add regression/drift evidence.
5. Update status, risk/debt, checklist, and ADR when affected.
6. Do not hide the mismatch by deleting historical evidence.

## 10. Release documentation gate

Before `v1.0.0-thesis-rc`:

- all Must requirements are Implemented or explicitly waived by accepted ADR;
- root verify and distribution/package smoke pass;
- clean-clone reproduction passes from the intended tag commit;
- user, developer, install, operations, notices, risks, and limitations are
  current;
- canonical docs tests pass;
- complete source/data/report/slides backup exists and its manifest is reviewed;
- tag/release notes cite the exact commit and evidence.

## 11. Current synchronization record

The 2026-08-09 documentation audit established the canonical `00` through `12`
set from source-backed facts. It explicitly corrected overbroad interpretation
of GUI configuration auto-load, live report export, mapping unknown-field
validation, source-path portability, and release completion. Existing detailed
architecture, ADR, rule, guide, and evidence documents remain specialized
records linked by the canonical index.

The later sync-tax repair replaced duplicated date/hash/branch metadata with a
stable verification marker. Per-file Git history identifies the exact commit
and date, while `DocumentationContractTest` protects inventory, links, stable
facts, and the absence of volatile canonical metadata.
