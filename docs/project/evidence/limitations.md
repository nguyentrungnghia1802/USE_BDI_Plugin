# Current Limitations

- Jason support is an explicit supported subset. Valid but unsupported syntax
  is retained as `ASL-002`; it is not silently normalized as a complete model.
- JaCaMo support is static `.jcm` project/agent import only. Named instances and
  relative `.asl` links are normalized; declaration-template identity is not
  recoverable after the official parser expands named instances.
- Workspaces, organizations, and institutions are retained as unsupported
  references. CArtAgO Java semantics, Moise semantics, runtime lifecycle,
  and execution traces are absent. GUI and CLI `.jcm` selection is static only.
- `REF-001` can still classify some literal-like terms too broadly, producing
  potential false positives. This is a known BDI index limitation.
- Schema `0.2.0` makes new mapping and suppression identities portable. Legacy
  `0.1.0` suppression hashes are intentionally retained as legacy-only because
  their original paths cannot be recovered; they do not match after relocation.
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
- `ReportMain` remains serializer-demo output. Live users should use Explorer's
  snapshot-backed export; CI users should use `BdiQualityGateMain`. Its `.jcm`
  option composes static project/agent evidence but does not imply runtime
  JaCaMo support.
- The Auction precision/recall/F1 result covers four targeted mutant
  instances, with no TN estimate and no claim for the whole rule catalog.
- The performance result is a seven-iteration local baseline on the Smart Queue
  fixture, not an Auction-scale or memory benchmark.
- Root `mvn clean verify` currently passes after ADR-0019. The release tag and
  complete external data/report/slides backup remain open.
