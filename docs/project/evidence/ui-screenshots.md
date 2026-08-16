# UI Screenshot Evidence

## Scope

These tracked screenshots document the demonstrable USE GUI path for the
plugin. They are presentation evidence, while the menu labels and plugin
loading are verified independently by `PluginGuiSmoke`.

## Evidence inventory

| Step | UI surface | Screenshot |
|---|---|---|
| 1 | USE class diagram loaded before the BDI analysis | [demo_uml_class_diagram.png](../../report/images/demo_uml_class_diagram.png) |
| 2 | BDI view with the `Import .asl...` entry point | [demo_import_button.png](../../report/images/demo_import_button.png) |
| 3 | Imported AgentSpeak tree with agents, beliefs, goals, and plans | [demo_bdi_explorer.png](../../report/images/demo_bdi_explorer.png) |
| 4 | Selected BDI element and source/detail panel | [demo_bdi_explorer_detail.png](../../report/images/demo_bdi_explorer_detail.png) |
| 5 | `Problems` tab with consistency findings | [demo_bdi_problems.png](../../report/images/demo_bdi_problems.png) |
| 6 | Explorer toolbar after a real current-analysis JSON export | [release_current_analysis_export.png](../../report/images/release_current_analysis_export.png) |
| 7 | Focused Auction BDI Plan viewpoint | [release_diagram_bdi_plan.png](../../report/images/release_diagram_bdi_plan.png) |
| 8 | Focused confirmed Auction Mapping viewpoint | [release_diagram_mapping_evidence.png](../../report/images/release_diagram_mapping_evidence.png) |
| 9 | Static Auction MAS overview with runtime boundary | [release_diagram_static_mas.png](../../report/images/release_diagram_static_mas.png) |
| 10 | Reviewed `SIG-001` source→element→mapping→target→issue highlight | [release_issue_highlight_sig001.png](../../report/images/release_issue_highlight_sig001.png) |
| 11 | Static MAS SVG export result in the Diagram workflow | [release_svg_export.png](../../report/images/release_svg_export.png) |

## Task 12 refreshed raster set

All refreshed files are 1280×820 PNG captures of production Swing components,
rendered from checked-in source through the opt-in
`ReleaseScreenshotCaptureTest`. They are not generated illustrations or mock
dialogs.

| Filename | Demo and inputs | Exact view state / expected evidence | Static-only legend |
| --- | --- | --- | --- |
| [release_use_class_diagram.png](../../report/images/release_use_class_diagram.png) | Auction; `Auction.use` plus tracked `Auction_default.clt` copied to an isolated capture workspace | Production USE Class Diagram; `Auctioneer`, `Auction`, `Bidder`, `Bid`, `AuctionStatus`, and associations visible | Not applicable |
| [release_use_object_diagram.png](../../report/images/release_use_object_diagram.png) | Auction; isolated `Auction.use`; populated `auctioneer1`, `auction1`, `bidder1` and two links | Production USE Object Diagram over the captured state | Not applicable |
| [release_bdi_explorer.png](../../report/images/release_bdi_explorer.png) | Auction; `Auction.use`, `auction.jcm`, both `.asl`, organization XML, project configuration | `Explorer` tab; portable file-level tree, enabled export action, project diagnostics, and IR-version detail | Not applicable |
| [release_problems.png](../../report/images/release_problems.png) | Same Auction project snapshot and confirmed mapping document | `Problems` tab; rule, severity, certainty, source/evidence columns and current count | Not applicable |
| [release_mapping.png](../../report/images/release_mapping.png) | Same Auction project snapshot; `Auction.bdimap.json` | `Mapping` tab; confirmed bindings and candidate review controls | Not applicable |
| [release_current_analysis_export.png](../../report/images/release_current_analysis_export.png) | Same Auction project snapshot | Real `Export Current Analysis...` helper writes `target/release-evidence/current-analysis.json`; status is normalized to that portable path before capture | Not applicable |
| [release_diagram_bdi_plan.png](../../report/images/release_diagram_bdi_plan.png) | Same Auction project snapshot | `Diagram` → `BDI Plan` → select `submit_bid` plan → `Focus Goal/Plan` → `Fit`; ordered plan path remains visible | Not applicable |
| [release_diagram_mapping_evidence.png](../../report/images/release_diagram_mapping_evidence.png) | Same Auction project snapshot | `Diagram` → select `Bidder` agent → `Focus Agent` → `Mapping` → `Fit`; confirmed agent/class/object path is readable | Not applicable |
| [release_diagram_static_mas.png](../../report/images/release_diagram_static_mas.png) | Same `auction.jcm` normalized project plus current immutable analysis | Production `MasOverviewDiagramBuilder` → `All` → `Fit`; agents, project, organization, roles/missions, and unsupported static resources visible | Yes: explicit `STATIC ONLY` status and legend node |
| [release_issue_highlight_sig001.png](../../report/images/release_issue_highlight_sig001.png) | Reviewed `SIG-001-open-arity` manifest inputs; signature-mutant USE model, two `.asl`, signature mapping, `auction-populated` state fixture | Real headless snapshot → trace contributor → bounded five-node `SIG-001` highlight → `Fit`; confirmed issue and qualified UML operation visible | Not applicable; mutant is static/snapshot analysis |
| [release_svg_export.png](../../report/images/release_svg_export.png) | Static Auction MAS overview above | Real `Export SVG...` helper writes `target/release-evidence/auction-current-view.svg`; portable status plus runtime boundary visible | Yes |

Capture environment: Windows 11, Java 21 Maven test JVM, USE 7.1.1,
`use-bdi-plugin` 0.1.0, JaCaMo Consistency Analysis Profile 1.0.0. Capture
date: 2026-08-17 (Asia/Bangkok). Release identity: Task 12 pre-tag candidate;
the final committed source/archive identity is recorded by the release evidence
manifest. No screenshot contains a personal username. The file-level Explorer
view deliberately avoids expanded plan labels whose local navigation metadata
contains checkout-absolute paths.

Reproduce the refreshed set from the repository root:

```powershell
mvn --batch-mode --no-transfer-progress -pl use-bdi-plugin `
  "-Dtest=ReleaseScreenshotCaptureTest" `
  "-Dsurefire.failIfNoSpecifiedTests=false" `
  "-Dbdi.releaseScreenshots=docs/report/images" test
```

Expected marker: `RELEASE_SCREENSHOTS_OK` with 11 files. Without the opt-in
property, the capture writer is skipped and normal test runs do not rewrite
tracked evidence.

## Live demo path

1. Start the extracted USE distribution with `-nr` and `-H=<use-home>`.
2. Click `Plugins > AgentSpeak > Hello BDI Plugin` to verify plugin loading.
3. Click `Plugins > AgentSpeak > Import AgentSpeak...`.
4. Optionally click `Plugins > AgentSpeak > Import JaCaMo Project...` and
   select one `.jcm` file to show static project diagnostics.
5. Select one or more `.asl` files and confirm the chooser when demonstrating
   direct source import.
6. In the BDI view, show the tree first, then select a plan or action to show
   the source/detail panel.
6. Click the `Problems` tab and show rule ID, severity, certainty, and source
   evidence. Keep `UNKNOWN` findings visible; do not present them as PASS.
7. Open `Diagram`; show `All`, `BDI Plan`, `Agent Overview`, or `Mapping`, keep
   status badges readable, exercise one layer/focus control, and click `Fit`.
   For Static MAS evidence keep the “No JaCaMo runtime / No Moise enactment /
   No live CArtAgO state” legend visible.
8. Click `Export Current Analysis...`, select JSON or HTML, and show the saved
   path in the status line. Capture pending Steps 6–7 only from an assembled
   USE runtime; do not substitute a mock dialog or generated image.

## Automated trace

The GUI smoke entry point checks all three menu/actions in the real USE runtime:

```powershell
powershell -ExecutionPolicy Bypass -File .\use-bdi-plugin\scripts\smoke.ps1
```

The expected marker from the GUI probe is:
`GUI_SMOKE_OK: Plugins > AgentSpeak > Hello BDI Plugin + Import AgentSpeak... + Import JaCaMo Project...`.

The screenshots are intentionally kept under `docs/report/images` because
they are also referenced by the existing thesis report. This document adds a
stable checklist and click path without claiming that a screenshot itself is a
runtime test.

The original Steps 1–5 remain historical presentation evidence. The Task 12
set is the current raster evidence for the finalized graphical-viewpoint
contract; deterministic SVG and automated diagram tests remain the executable
boundary evidence.
