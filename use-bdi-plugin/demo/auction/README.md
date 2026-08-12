# Auction: BDI to UML/OCL

## What this demonstrates

- A UML/OCL auction model in `Auction.use`.
- Two Jason AgentSpeak sources: `auctioneer.asl` and `bidder.asl`, including a
  bidder-to-auctioneer notification after bid submission.
- Static JaCaMo composition through `auction.jcm`.
- Static Moise organization context in `auction-organization.xml`.
- A portable baseline mapping in `Auction.bdimap.json`.
- A deterministic object snapshot in `Auction.cmd` for OCL and mapping views.
- A deliberately mutated OCL precondition in `mutants/ocl-open-closed.use`.

This is a static analysis demo. It imports and normalizes source; it does not
start a JaCaMo, CArtAgO, or Moise runtime.

## Recommended GUI flow

1. Start USE with `Auction.use` and `Auction.cmd` as the specification and
   command-file arguments, or open `Auction.use` and execute `Auction.cmd`.
2. Show `View > Create View > Class diagram`.
3. Choose `View > Create View > Object diagram` to show the populated auction
   state, then choose `Plugins > AgentSpeak > Import JaCaMo Project...` and select
   `auction.jcm`. The project imports both `.asl` files and retains static
   workspace/institution diagnostics.
4. In BDI Explorer, expand the agents and load `Auction.bdimap.json` in
   `Mapping`.
5. Open `Diagram`, select `MAS Overview`, and click `Fit`. Show the auctioneer,
   two bidder instances, organization roles/missions, workspace/institution,
   UML/OCL evidence, and issue nodes. Use the layer checkboxes to separate BDI,
   Organization, Environment, UML/OCL, and Issues; `Reset` restores all layers.
6. Open `Problems` and select an issue to focus its evidence path in Diagram,
   then export the current analysis as
   JSON or HTML.
7. For a visible negative case, open `mutants/ocl-open-closed.use`, import the
   two `.asl` files again, refresh the USE snapshot, and show `OCL-001`.

## Reviewed mutant evidence

The complete reviewed corpus is declared by
[`auction-evaluation-manifest.json`](../../../docs/project/evidence/auction-evaluation-manifest.json).
It covers the clean scoped baseline and four visual paths: structural stale
targets (`MAP-003`), operation arity (`SIG-001`), absent `bidder2` references
(`REF-001`), and the false open precondition (`OCL-001`). After running a case,
select its row in `Problems`; `Diagram` highlights only the incoming evidence
chain for that rule instead of unrelated sibling issues. The manifest's
evidence tokens define the reviewed scope, so unrelated baseline `REF-001`
limitations are not presented as mutant detections.

## Headless check

From the repository root, use the command in `docs/guide/guide.md` with
`--use demo/auction/Auction.use --jcm demo/auction/auction.jcm`. Exit `1` can
mean confirmed consistency findings; inspect the generated report rather than
treating every non-zero result as a parser failure.
