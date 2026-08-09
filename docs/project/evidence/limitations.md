# Current Limitations

- Jason support is an explicit supported subset. Valid but unsupported syntax
  is retained as `ASL-002`; it is not silently normalized as a complete model.
- `REF-001` can still classify some literal-like terms too broadly, producing
  potential false positives. This is a known BDI index limitation.
- `ProjectSourceId` v2 now defines portable project-relative source evidence,
  but mapping and suppression schema `0.1.0` still contains absolute-path-based
  identities. Moving a checkout keeps legacy entries stale until migration.
- The mapping decoder validates required fields, kinds, duplicate mapping keys,
  and syntax, but does not reject every unknown JSON field.
- GUI configuration discovery is intentionally limited to `.bdi-plugin`
  beside the active file-backed `.use` model; unnamed/in-memory models use
  defaults and there is no broader workspace search.
- The BDI Explorer captures a USE model/snapshot projection and does not
  subscribe to later host-state changes.
- OCL checks are read-only snapshot checks. Bounded simulation is available
  only for the supported `soil:` effect form; missing or unknown effects yield
  `OCL-004`/`UNKNOWN` rather than an optimistic PASS.
- The GUI has no live report export action. `ReportMain` creates demonstration
  metadata output; the Auction pipeline composes real analysis reports.
- The Auction precision/recall/F1 result covers four targeted mutant
  instances, with no TN estimate and no claim for the whole rule catalog.
- The performance result is a seven-iteration local baseline on the Smart Queue
  fixture, not an Auction-scale or memory benchmark.
- Root `mvn clean verify` currently passes after ADR-0019. The release tag and
  complete external data/report/slides backup remain open.
