# CArtAgO Environment Mapping Persistence Evidence

Status: implemented static persistence slice; no runtime workspace startup.

## Scope

The plugin now persists static CArtAgO operation and observable-property
bindings in a separate `.cartago-map.json` document. The existing `.bdimap.json`
schema `0.2.0` remains unchanged for the six BDI mapping kinds.

## Contract

- Document schema: `0.1.0`.
- Records are plugin-owned typed operation/property values with artifact type,
  instance, UML target, arity/type evidence, confirmation, source provenance,
  staleness state, and non-empty evidence.
- Source provenance uses portable `ProjectSourceId` v2 and is resolved only under
  an explicit existing project root.
- JSON field order is deterministic and UTF-8 output is byte-stable after
  load/save and across equivalent checkout roots.
- Unknown versions, unknown fields, duplicate object keys, duplicate mapping
  identities, wrong roots, invalid arity/type fields, and malformed records are
  rejected before a destination is rewritten.

## Validation evidence

`EnvironmentMappingValidationService` revalidates confirmed mappings against the
current plugin-owned `EnvironmentModel` and read-only `UseModelSnapshot`.
Candidates are not evaluated. Confirmed current records reuse the existing
`EnvironmentConsistencyValidator`; confirmed stale/unknown records produce
`ENV-004` with source, target, and reason evidence.

The Auction fixture verifies that the persisted baseline preserves the existing
`ENV-003` dynamic-value `UNKNOWN` result and that a removed UML target produces
`ENV-004` rather than silently becoming valid.

## Tests

- `EnvironmentMappingFileRepositoryTest`: 4 tests.
- `AuctionEnvironmentMappingPersistenceTest`: 2 tests.
- `AuctionEnvironmentConsistencyTest`: 3 existing static pilot tests remain
  green.
- `RuleCatalogCompletenessTest`: includes `ENV-004` while the standard BDI
  catalog remains the existing 22 rules.

## Limitations

This slice does not start CArtAgO, capture live workspace state, infer property
types from runtime metadata, or automatically confirm candidates. Those remain
separate future work under ADR-0031.
