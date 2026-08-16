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
| 6 | Explorer toolbar with `Export Current Analysis...` and JSON/HTML chooser | Pending capture after the next assembled-GUI evidence run |
| 7 | `Diagram` tab showing view selector, text status badges, layer/focus controls, static-only legend where applicable, and `Export SVG...` | Pending final Task 12 assembled-GUI capture; do not reuse pre-viewpoint rasters |

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

The existing Steps 1–5 predate the finalized graphical-viewpoint contract and
remain evidence for their listed surfaces only. Task 12 owns the final raster
refresh for Step 7 after all UI-affecting roadmap work is frozen; deterministic
SVG and automated diagram tests are the current Task 08 evidence.
