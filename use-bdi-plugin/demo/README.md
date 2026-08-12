# USE BDI Plugin Demos

These are the canonical, presentation-ready examples for the plugin. Each
subdirectory is self-contained and contains the UML/OCL model, AgentSpeak
source, optional JaCaMo resources, and a short walkthrough.

| Demo | Main point | Start with |
| --- | --- | --- |
| `auction` | AgentSpeak-to-UML/OCL mapping, static `.jcm`, and OCL mutation | `Auction.use` and `auction.jcm` |
| `smart-queue` | BDI decision-making beside a USE queue snapshot | `SmartQueue.use`, then `SmartQueue.cmd` |
| `family-person` | Person membership in a Family with a small BDI mapping | `Family.use` and `family-person.jcm` |
| `smart-home` | Resident decision-making over a Home and Light snapshot | `SmartHome.use` and `smart-home.jcm` |

The source-backed test fixtures under `src/test/resources` remain in place for
automated tests. They are not another user-facing demo location.

`family-person` and `smart-home` are clean teaching baselines. Their local rule
configuration omits only the informational `OCL-004` bounded-effect check because
these two examples do not declare a `soil:` effect; they contain no mutant or
intentional parser/OCL failure.
