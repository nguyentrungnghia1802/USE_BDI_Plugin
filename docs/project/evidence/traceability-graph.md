# Unified Traceability Graph Evidence

Status: source-backed MVP evidence

The T09 slice derives an immutable graph from the Auction current-analysis
snapshot. `AuctionTraceabilityGraphTest` verifies:

- a complete AgentSpeak source to BDI element to confirmed mapping to UML/OCL
  to issue chain;
- preservation of `OPEN` status and `UNKNOWN` certainty for `OCL-004`;
- an explicit `GAP` and `MISSING_MAPPING` edge for an unmapped action;
- semantic deduplication and deterministic ordering;
- deterministic debug JSON without checkout-absolute paths.

The graph is an explanatory view only. It is not persisted, does not rerun
validation, and does not claim CArtAgO, Moise, runtime trace, or graph UI
support.
