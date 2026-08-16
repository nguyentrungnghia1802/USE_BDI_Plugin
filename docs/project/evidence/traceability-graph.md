# Unified Traceability Graph Evidence

Status: source-backed immutable evidence and graphical-projection boundary

The trace slice derives an immutable graph from the Auction current-analysis
snapshot. `AuctionTraceabilityGraphTest` verifies:

- a complete AgentSpeak source to BDI element to confirmed mapping to UML/OCL
  to issue chain;
- preservation of `OPEN` status and `UNKNOWN` certainty for `OCL-004`;
- an explicit `GAP` and `MISSING_MAPPING` edge for an unmapped action;
- semantic deduplication and deterministic ordering;
- deterministic debug JSON without checkout-absolute paths;
- environment and organization builders extend the same trace vocabulary with
  static artifacts, operations, observable properties, roles, missions,
  reviewed cardinality targets, and their explicit gaps/findings.

`TraceabilityDiagramContributor` maps that evidence into the existing
`DiagramModel`: source/element/mapping/target nodes remain typed, portable
labels are enforced, certainty/status attributes survive, and trace relations
are converted only through the closed edge mapping documented in
[`GRAPHICAL_CONCRETE_SYNTAX.md`](../metamodel/GRAPHICAL_CONCRETE_SYNTAX.md).
Issue focus walks those existing evidence edges; it does not infer another
semantic link.

The graph and diagram are explanatory views only. They are not persisted, do
not rerun validation, and do not claim CArtAgO live state, Moise enactment,
JaCaMo runtime trace, editable graph semantics, or bidirectional model updates.
Static MAS diagrams display that boundary as text and node metadata.

Task 10 additionally evaluates the four reviewed Auction mutants through
`AuctionMutantDiagramTest`. `MAP-003` and `SIG-001` retain source, element,
mapping, target, and issue nodes; `REF-001` retains an explicit gap instead of
inventing a target; `OCL-001` also retains the OCL constraint. Each bounded
highlight contains only its expected issue rule and preserves the manifest's
certainty and evidence token. See
[`correspondence-coverage.md`](correspondence-coverage.md) for the closed
11-type correspondence inventory and its limits.
