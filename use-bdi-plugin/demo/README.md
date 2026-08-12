# USE BDI Plugin Demos

These are the canonical, presentation-ready examples for the plugin. Each
subdirectory is self-contained and contains the UML/OCL model, AgentSpeak
source, optional JaCaMo resources, and a short walkthrough.

| Demo | Main point | Start with |
| --- | --- | --- |
| `auction` | AgentSpeak-to-UML/OCL mapping, static `.jcm`, and OCL mutation | `Auction.use` and `auction.jcm` |
| `smart-queue` | BDI decision-making beside a USE queue snapshot | `SmartQueue.use`, then `SmartQueue.cmd` |

The source-backed test fixtures under `src/test/resources` remain in place for
automated tests. They are not another user-facing demo location.
