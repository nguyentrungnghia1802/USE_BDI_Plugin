# Current Limitations

- The repository does not contain the authoritative `00_PROJECT_CONTEXT.md`,
  so automatic project-context discovery is not claimed.
- Jason support is an explicit supported subset. Valid but unsupported syntax
  is retained as `ASL-002`; it is not silently normalized as a complete model.
- `REF-001` can still classify some literal-like terms too broadly, producing
  potential false positives. This is a known BDI index limitation.
- Mapping source IDs currently contain normalized absolute paths. Portable
  checked-in case-study mapping files are therefore avoided; temporary mapping
  documents are used in tests.
- OCL checks are read-only snapshot checks. Bounded simulation is available
  only for the supported `soil:` effect form; missing or unknown effects yield
  `OCL-004`/`UNKNOWN` rather than an optimistic PASS.
- The Auction precision/recall/F1 result covers four targeted mutant
  instances, with no TN estimate and no claim for the whole rule catalog.
- The performance result is a seven-iteration local baseline on the Smart Queue
  fixture, not an Auction-scale or memory benchmark.
- The root `mvn clean verify` gate may remain blocked by the existing USE GUI
  Failsafe handshake behavior even when the plugin module and distribution
  package gates pass.
