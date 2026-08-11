# Static JaCaMo Project Entry-Point Evidence

Status: T12 evidence for the static `.jcm` GUI and headless slice
Verification: source-backed; see Git history and focused tests

## Scope

This slice exposes the T11 `MasProjectAnalysisService` through two entry
points. It does not start JaCaMo, CArtAgO, Moise, or a runtime workspace.

## GUI path

1. Start the packaged USE distribution with its extracted home in `-H`.
2. Open `Plugins > AgentSpeak > Import JaCaMo Project...`, or open the BDI
   Explorer and click `Import .jcm...`.
3. Select `use-bdi-plugin/src/test/resources/fixtures/casestudy/auction/auction.jcm`.
4. The worker resolves three agent instances from two AgentSpeak files,
   displays project diagnostics in the Explorer tree/status, and enables
   `Export Current Analysis...` for the held snapshot.

The action uses a single-select `*.jcm` chooser. Analysis runs in
`BdiProjectImportWorker`; stale or cancelled generations cannot replace the
current view result.

## Headless path

```powershell
java -cp "$useHome\lib\plugins\use-bdi-plugin-7.1.1.jar;$useHome\lib\use-gui.jar" `
  org.tzi.use.plugins.bdi.cli.BdiQualityGateMain `
  --use .\use-bdi-plugin\src\test\resources\fixtures\casestudy\auction\Auction.use `
  --jcm .\use-bdi-plugin\src\test\resources\fixtures\casestudy\auction\auction.jcm `
  --json .\auction-jcm.json --html .\auction-jcm.html `
  --timestamp 2026-08-11T00:00:00Z
```

The CLI prints `JCM-005` for retained unsupported static resources and writes
the same snapshot-backed consistency report. `--asl` and `--jcm` are mutually
exclusive. Missing projects and wrong extensions return exit `3` without
creating a report; exits `0` through `4` retain the existing quality-gate
meaning.

## Automated evidence

- `BdiQualityGateMainTest`: `.jcm` success, deterministic report markers,
  missing/wrong-extension input, conflict, and no-output-on-input-error.
- `BdiExplorerViewTest`: worker-backed project import, resolved agents,
  project diagnostics, status, tree, and export availability.
- `BdiImportWorkerTest`: project composition completes through SwingWorker.
- `ImportBdiActionTest`: single-select `.jcm` chooser and descriptor config.
- `PluginGuiSmoke`: packaged `Import JaCaMo Project...` menu marker.

The project analysis service continues to be the only place where project
composition meets the shared immutable current-analysis snapshot.
