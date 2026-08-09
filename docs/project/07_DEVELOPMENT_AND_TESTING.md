# Development and Testing

Status: canonical build, test, and definition-of-done guide
Verification: source-backed; see Git history and DocumentationContractTest

## 1. Prerequisites

- Windows PowerShell for the checked-in automation scripts.
- JDK 21 for Maven and direct GUI execution.
- Maven 3.9 or newer recommended.
- A desktop session for GUI smoke; headless checks remain separate.

Verify the effective tools, not only environment variables:

```powershell
mvn -version
java -version
git status --short --branch
```

If Maven reports Java 21 but `java -version` reports Java 8, direct launcher
commands can fail even though Maven builds pass. Set `JAVA_HOME` and put its
`bin` first for the shell used to launch USE.

## 2. Build commands

Run from repository root.

```powershell
# Plugin and required modules, all unit tests
mvn --batch-mode --no-transfer-progress -pl use-bdi-plugin -am test

# Shaded plugin and assembled USE distribution
mvn --batch-mode --no-transfer-progress -pl use-assembly -am package

# Complete reactor including integration tests
mvn --batch-mode --no-transfer-progress clean verify
```

Expected primary artifacts:

- `use-bdi-plugin/target/use-bdi-plugin-7.1.1.jar`;
- `use-assembly/target/use-7.1.1.zip`.

## 3. Test layers

| Layer | Scope | Examples |
| --- | --- | --- |
| Domain unit | Immutable values, validation, fingerprints | mapping/IR/config/suppression tests |
| Adapter | Jason parsing and USE projection/OCL | importer and `use` package tests |
| Application/UI | import orchestration and Swing models | worker, explorer, mapping, problems tests |
| Contract/evidence | docs, catalogs, fixtures, metrics | release and case-study artifact tests |
| Host integration | USE compiler/shell lifecycle | `use-core` and `use-gui` integration tests |
| Package smoke | shaded JAR and extracted UI | `smoke.ps1` |
| Reproducibility | exact committed tree | `clean-clone.ps1` |

## 4. Fixture policy

```text
src/test/resources/fixtures/
  asl/valid/
  asl/invalid/
  asl/unsupported/
  expected/
  smartqueue/
  use/
  casestudy/auction/
    mutants/
```

- Valid fixtures must parse with Jason and test supported output.
- Invalid fixtures test syntax/import diagnostics.
- Unsupported fixtures are valid Jason syntax that the IR deliberately cannot
  fully represent; they must remain visible.
- Golden fixtures test deterministic normalized output.
- Case-study mutants are independent files and never modify the baseline at
  test runtime.
- Fixtures must not appear at repository root.

## 5. Focused test commands

PowerShell requires quoting dotted Maven properties:

```powershell
mvn --batch-mode --no-transfer-progress -pl use-bdi-plugin -am `
  '-Dtest=ReleaseArtifactTest' `
  '-Dsurefire.failIfNoSpecifiedTests=false' test
```

For several plugin-only tests after dependencies are built:

```powershell
mvn --batch-mode --no-transfer-progress -pl use-bdi-plugin `
  '-Dtest=RuleCatalogCompletenessTest,AuctionEvidenceArtifactTest' test
```

An unquoted `-D...` containing dots may be split by PowerShell and reported by
Maven as an unknown lifecycle phase.

## 6. Reproducible scripts

```powershell
powershell -ExecutionPolicy Bypass -File .\use-bdi-plugin\scripts\smoke.ps1
powershell -ExecutionPolicy Bypass -File .\use-bdi-plugin\scripts\auction-evidence.ps1
powershell -ExecutionPolicy Bypass -File .\use-bdi-plugin\scripts\performance.ps1
powershell -ExecutionPolicy Bypass -File .\use-bdi-plugin\scripts\clean-clone.ps1
powershell -ExecutionPolicy Bypass -File .\use-bdi-plugin\scripts\backup-thesis-artifacts.ps1
```

The expected markers are documented in [the API contracts](05_API.md). A
script marker proves only its bounded checks.

## 7. Manual GUI acceptance

1. Package and extract `use-7.1.1.zip`.
2. Launch `lib/use-gui.jar` with `-H` pointing to that extracted USE home.
3. Confirm both actions under `Plugins > AgentSpeak`.
4. Load `Auction.use` and import both Auction `.asl` files.
5. Inspect Explorer, Problems, Mapping, source details, and standard USE class
   and object diagrams.
6. Save/load a mapping to a temporary path.
7. Confirm a mapping edit refreshes Problems.
8. Place a restricted `.bdi-plugin/rules.json` beside the loaded `.use` model,
   reopen the import action, and verify the status identifies project rules.
9. Corrupt the configuration in a disposable copy and verify the Explorer is
   not opened and the configuration error is shown.
10. Restart USE after changing the plugin JAR.

Use [the user guide](USER_GUIDE.md) for exact clicks.

## 8. Acceptance matrix

| Capability | Minimum automated evidence |
| --- | --- |
| Plugin/menu | action tests plus `PluginGuiSmoke` |
| Parser/diagnostics | valid, invalid, unsupported fixture tests |
| IR/index | hierarchy, golden serialization, index tests |
| Mapping | suggestion, model, persistence, staleness tests |
| Rules | orchestrator tests and catalog completeness |
| USE/OCL | facade and snapshot evaluator tests |
| GUI | worker/explorer/mapping/problems tests |
| Reports | JSON/HTML escaping, hash, suppression tests |
| Auction | model/import/mapping/baseline/mutant/evidence tests |
| Documentation/release | documentation and release artifact tests |

## 9. Test data and determinism

- Tests do not require Internet, remote services, a database, or credentials.
- Time-dependent reports use fixed timestamps when byte reproducibility is
  asserted.
- File paths are normalized where fixture portability is required.
- Performance durations are recorded but are not hard pass thresholds.
- UI tests avoid claiming full visual/manual acceptance.

## 10. Definition of done

A behavior change is complete only when:

- acceptance criteria and affected requirement IDs are identified;
- implementation respects plugin/domain/state boundaries;
- focused tests cover success, failure, and unknown/unsupported paths;
- module tests pass;
- root verification is run when host/module integration changes;
- relevant canonical docs, guides, checklist, and evidence are synchronized;
- architecture changes have an accepted/superseding ADR;
- `git diff --check` passes;
- unrelated user work remains unstaged/unmodified;
- the coherent change is committed on a feature/thesis branch.

## 11. Common failures

### Plugin menu missing

Use the extracted assembly, confirm the shaded plugin under `lib/plugins`, pass
the same directory as `-H`, and restart USE.

### Jason class missing

The unshaded/test JAR was likely copied. Inspect the runtime JAR for
`jason/asSemantics/Agent.class`.

### Shell integration fork exits early

ADR-0019 allows invalid specification fixtures to return only in explicit
integration-test mode. Do not suppress integration tests or change normal CLI
exit semantics.

### Mapping/suppression becomes stale after moving checkout

Current source identity includes absolute paths. Regenerate/review persisted
artifacts; do not manually claim the old fingerprint is equivalent.

### Report contains zero findings

`ReportMain` is a serializer demo. Use the Auction pipeline or compose
`ReportData` from a real validation result.

## 12. Development synchronization checklist

- [x] Commands match current Maven module names and scripts.
- [x] Root verification status matches current evidence.
- [x] PowerShell/JDK pitfalls are documented.
- [x] Test layers cover domain, adapters, UI, host, package, and docs.
- [x] Definition of done includes docs and state safety.
