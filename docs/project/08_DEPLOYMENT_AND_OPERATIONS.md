# Deployment and Operations

Status: canonical local distribution and release operations guide
Last verified: 2026-08-09
Code baseline: `c1b11b41`

## 1. Environment model

The plugin runs inside a local USE desktop process. There are no development,
staging, and production servers. Operational environments are:

| Environment | Purpose |
| --- | --- |
| Source checkout | Development, tests, package generation |
| Extracted USE assembly | Supported local runtime/demo |
| Clean temporary clone | Reproducibility validation |
| Existing USE 7.1.1 home | Optional manual plugin installation |

## 2. Configuration

No credentials or secrets are required. Configuration artifacts are local:

- `rules.json` selects enabled rules when explicitly loaded by an application
  composition;
- `suppressions.json` records reviewed suppressions when explicitly loaded;
- `.bdimap.json` contains user-confirmed mapping bindings;
- `-H=<use-home>` tells USE where to load libraries/plugins.

The current GUI does not auto-discover the two `.bdi-plugin` project files.
Users choose mapping files through the Mapping tab.

## 3. Build and package sequence

```powershell
mvn --batch-mode --no-transfer-progress clean verify
mvn --batch-mode --no-transfer-progress -pl use-assembly -am package
```

The assembly ZIP must contain:

- `lib/use-gui.jar`;
- `lib/plugins/use-bdi-plugin-7.1.1.jar`;
- USE runtime/configuration resources;
- licensing material.

The plugin JAR must contain `useplugin.xml`, plugin classes, Jason runtime
classes, and `META-INF/THIRD-PARTY-NOTICES.txt`.

## 4. Launch sequence

```powershell
$useExtractRoot = Join-Path $PWD 'use-assembly\target\bdi-dist'
Expand-Archive .\use-assembly\target\use-7.1.1.zip $useExtractRoot -Force
$useRuntimeHome = (Get-ChildItem $useExtractRoot -Directory |
  Where-Object { Test-Path (Join-Path $_.FullName 'lib\use-gui.jar') } |
  Select-Object -First 1).FullName
java -jar (Join-Path $useRuntimeHome 'lib\use-gui.jar') '-nr' "-H=$useRuntimeHome"
```

The `java` executable must be JDK 21. The `-H` path must be the same extracted
home that contains `lib/plugins`.

## 5. Install into an existing USE home

Only use a matching USE 7.1.1 distribution. Build the shaded plugin, copy
`use-bdi-plugin-7.1.1.jar` into `<use-home>/lib/plugins/`, restart USE, then run
the Hello/import menu checks. Preserve a copy of the previous plugin JAR for
rollback.

Do not mix host classes into the plugin directory and do not deploy the test
JAR.

## 6. Operational checks

| Gate | Command | Required outcome |
| --- | --- | --- |
| Plugin tests | `mvn ... -pl use-bdi-plugin -am test` | All plugin tests pass |
| Full reactor | `mvn ... clean verify` | Core, GUI integration, plugin, assembly pass |
| Package/UI smoke | `scripts/smoke.ps1` | `GUI_SMOKE_OK` |
| Auction evidence | `scripts/auction-evidence.ps1` | `AUCTION_EVIDENCE_OK` |
| Clean committed tree | `scripts/clean-clone.ps1` | `CLEAN_CLONE_REPRODUCIBILITY_OK` |
| Backup | `scripts/backup-thesis-artifacts.ps1` | Manifest reviewed, not marker alone |

## 7. Logs and diagnostics

USE writes startup/plugin/compiler output to its normal GUI/console logging.
The plugin presents import/consistency diagnostics in Problems and status text.
Reports preserve rule/source/evidence fields.

There is no metrics backend, distributed tracing, health endpoint, or remote
log collector. The benchmark JSON and script markers are reproducibility
artifacts, not production observability.

## 8. Backup and recovery

The backup script archives committed source/docs with `git archive`, copies
present `data`, `slides`, or `presentation` directories, records uncommitted
paths, and writes a manifest of missing material.

Recovery procedure:

1. Verify the archive hash and manifest.
2. Extract into a new path.
3. Restore external data/report/slides from their separately recorded source.
4. Run plugin tests and assembly package.
5. Run clean-clone/package smoke if the restored repository will be released.

The current repository does not contain all external thesis artifacts, so the
complete backup release gate remains open.

## 9. Rollback

For a plugin-only runtime regression:

1. Exit USE.
2. Replace the plugin JAR with the previously verified shaded JAR.
3. Keep mapping/config/suppression files; migrate only with an explicit schema
   decision.
4. Restart with the same `-H`.
5. Run Hello/import and affected fixture checks.

For a source change, use normal Git revert on the feature branch. Do not reset
or overwrite unrelated working-tree changes.

## 10. Incident runbooks

### Plugin not discovered

Check `-H`, JAR location, descriptor presence, USE version, and startup log.
Re-run package smoke.

### Import freezes or stale result appears

Capture source set and UI status. Reproduce with importer tests. Verify worker
cancellation/generation behavior and avoid blocking the Swing event thread.

### Analysis changes current USE state

Stop using the affected OCL/effect path, preserve the model/commands, record
before/after fingerprints, and treat as release-blocking. Add a regression test
before re-enabling it.

### Reports disagree with Problems

Confirm both were built from the same import snapshot, mapping, rule config,
suppression set, and USE fingerprint. `ReportMain` alone is not the live result.

### Mapping/suppression breaks after relocation

Treat it as current absolute-path identity behavior. Regenerate/review the
artifact; do not bypass stale checks.

### GUI smoke unavailable

Run non-GUI package/parser/report tests and record the missing desktop session
as a separate gate. Do not claim GUI acceptance from headless tests.

## 11. Release readiness

A release candidate requires:

- clean root verification;
- package, smoke, Auction evidence, and clean-clone gates;
- synchronized canonical docs and third-party notices;
- reviewed source/data/report/slides backup manifest;
- no open release-blocking state-corruption or packaging defect;
- annotated tag `v1.0.0-thesis-rc` only after the preceding gates.

## 12. Operations synchronization checklist

- [x] Runtime topology reflects a local desktop plugin.
- [x] Build, launch, smoke, rollback, and incident paths are documented.
- [x] No cloud/server/secret capability is implied.
- [x] Marker limitations and missing external backup artifacts are explicit.
- [x] Release tag remains unclaimed.
