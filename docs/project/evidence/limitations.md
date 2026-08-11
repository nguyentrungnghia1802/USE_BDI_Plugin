# Current Limitations

- Jason support is an explicit supported subset. Valid but unsupported syntax
  is retained as `ASL-002`; it is not silently normalized as a complete model.
- JaCaMo support is static `.jcm` project/agent import only. Named instances and
  relative `.asl` links are normalized; declaration-template identity is not
  recoverable after the official parser expands named instances.
- Workspaces and institutions are retained as unsupported references. Moise
  organization XML is parsed statically into a bounded role/group/scheme/goal/
  mission/norm/cardinality IR. Source line/column positions are unavailable
  from Moise 1.1 and remain explicit unknowns; links, preferences, plans,
  conditions, and other out-of-scope details produce `JCM-010`.
- Static organization-to-UML/OCL rules cover only explicit confirmed role/class,
  mission/operation, and cardinality/invariant mappings. Cardinality bounds are
  reviewer-normalized evidence; arbitrary OCL text is not reinterpreted.
  Matching static bounds remain `UNKNOWN` without enactment/runtime membership.
  Mapping persistence, runtime lifecycle, dynamic norm fulfillment, and execution
  traces are absent. GUI and CLI `.jcm` selection remains static only.
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
