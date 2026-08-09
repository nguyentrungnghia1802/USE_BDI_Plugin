auction_status(draft).
registered_bidder(bidder2).
!run_auction.

@run_auction +!run_auction : auction_status(draft)
    <- open;
       +auction_status(open);
       -auction_status(draft);
       !receive_bid.

@receive_bid +!receive_bid : auction_status(open) & registered_bidder(bidder2)
    <- placeBid(bidder2, 120);
       !finish_auction.

@finish_auction +!finish_auction : auction_status(open)
    <- close.
