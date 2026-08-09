# Auction Mapping Examples

The examples below use portable display names. Runtime mapping source IDs keep
the normalized source path as required by the current `MappingSourceId`
contract.

## Confirmed baseline

| Mapping kind | AgentSpeak source | USE target |
| --- | --- | --- |
| `AGENT_CLASS` | `auctioneer.asl` | `Auctioneer` |
| `AGENT_OBJECT` | `auctioneer.asl` | `auctioneer1` |
| `AGENT_CLASS` | `bidder.asl` | `Bidder` |
| `AGENT_OBJECT` | `bidder.asl` | `bidder1` |
| `ACTION_OPERATION` | `run_auction / step 1` | `Auction::open()` |
| `ACTION_OPERATION` | `receive_bid / step 1` | `Auction::placeBid(b:Bidder,amount:Real)` |
| `ACTION_OPERATION` | `finish_auction / step 1` | `Auction::close()` |
| `ACTION_OPERATION` | `submit_bid / step 1` | `Bidder::submitBid(a:Auction,amount:Real)` |
| `BELIEF_ATTRIBUTE` | `auction_status/1` | `Auction::status` |
| `BELIEF_ATTRIBUTE` | `budget/1` | `Bidder::budget` |

The baseline test confirms 14 bindings, including four positional parameter
bindings, and persists them through `MappingFileRepository`.

## Mutant examples

- Structural mutant `STR-001` removes `Bidder`; the existing `Bidder` class,
  object, operation, parameter, and budget mappings become stale and produce
  nine `MAP-003` issues.
- Signature mutant `SIG-001` changes `Auction::open()` to
  `Auction::open(flag:String)`; `open/0` remains in AgentSpeak and produces
  one `SIG-001` arity issue.
- Reference mutant `REF-001` changes `bidder1` to `bidder2`; the current USE
  state has no `bidder2`, producing four targeted `REF-001` findings.
- OCL mutant `OCL-001` changes the `Auction::open()` precondition from
  `status = #draft` to `status = #closed`; with `auction1.status = #draft`,
  snapshot evaluation produces one confirmed `OCL-001` issue.
