# Smart Home: Resident Decision

## What this demonstrates

- A small UML/OCL home model with `Home`, `Resident`, and `Light` objects.
- One Jason AgentSpeak resident with a preparation goal and a mapped
  `turn_on_lights` decision action.
- A deterministic snapshot with one home, one resident, and one light.
- Static JaCaMo/Moise project context alongside the UML/OCL and BDI sources.
- A static `smart_home_environment` workspace reference that makes the agent
  and environment layers distinct without starting CArtAgO.
- `smart-home-organization.use` and `smart-home-organization.cmd` provide a
  valid standalone organization UML/OCL snapshot.

This is a valid baseline demo with no intentional error case. The plugin
imports and checks the models statically; it does not execute a JaCaMo runtime.

## Recommended GUI flow

1. Open `SmartHome.use`, then execute `SmartHome.cmd`.
2. Choose `View > Create View > Class diagram`, followed by
   `View > Create View > Object diagram`.
3. Choose `Plugins > AgentSpeak > Import JaCaMo Project...` and select
   `smart-home.jcm`.
4. In BDI Explorer, expand `resident.asl` to show the preparation goal, plan,
   and `turn_on_lights` action. Load `SmartHome.bdimap.json` in `Mapping`.
5. Open `Diagram`, select `MAS Overview`, and click `Fit`. Show the Resident
   under the BDI layer, `smart_home_environment` under Environment, and the
   static-analysis legend. Switch to `BDI Plan` to show
   `prepare_evening -> turn_on_lights -> Resident::turn_on_lights()`.
6. Click `Refresh USE Snapshot`, inspect `Problems`, and export the current
   analysis if a report is needed.

The repository-level command and UI guide is
[`docs/guide/guide.md`](../../../docs/guide/guide.md).
