# Static Organization Consistency Pilot

## Scope

The pilot evaluates the normalized Auction `OrganizationModel` against a
read-only USE UML/OCL snapshot. It never starts Moise or JaCaMo and never imports
Moise concrete types outside `MoiseOrganizationParserAdapter`.

## Rule contract

| Rule | Confirmed failure | Unknown outcome | Required evidence |
| --- | --- | --- | --- |
| `ORG-001` | mapped role or UML class is absent | mapping remains a candidate | role qualified ID, target class, confirmation, source identity |
| `ORG-002` | mapped mission or UML operation is absent | mapping remains a candidate | mission qualified ID, operation reference, confirmation, source identity |
| `ORG-003` | role cardinality or invariant is absent, or reviewed bounds differ | candidate, unavailable reviewed bounds, or matching static bounds without enactment | group/role IDs, invariant reference, reviewed bounds, confirmation, source identity |

Only `CONFIRMED` records are bindings. The validator does not infer mappings
from names and does not parse OCL expression strings. For `ORG-003`, the reviewer
records normalized minimum/maximum bounds as mapping evidence after selecting
the invariant. This keeps unsupported semantic interpretation visible.

## Auction evidence

`AuctionOrganizationConsistencyTest` loads the official-parser organization IR
through `auction.jcm` and compiles `auction-organization.use` through USE. The
valid role and mission mappings produce no confirmed error. Matching static
cardinalities produce `INFO / UNKNOWN` because dynamic membership is absent.

Controlled mappings cover one confirmed mutant per rule:

- missing UML class -> `ORG-001 / ERROR / CONFIRMED`;
- missing UML operation -> `ORG-002 / ERROR / CONFIRMED`;
- reviewed OCL bound mismatch -> `ORG-003 / ERROR / CONFIRMED`.

The same suite covers candidate and unavailable-bound `UNKNOWN`, duplicate
mapping rejection, deterministic graph ordering/deduplication, portable JSON
serialization, source evidence, and the adapter-only Moise import boundary.

## Claim boundary

These results show deterministic behavior for the declared Auction mappings and
mutants. They do not prove arbitrary OCL equivalence, runtime role cardinality,
organization enactment, obligation fulfillment, or general correctness.
