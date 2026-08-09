budget(150).
auction(auction1).
!submit_bid.

@submit_bid +!submit_bid : budget(Budget) & auction(auction1)
    <- submitBid(auction1, 120);
       .print("bid submitted").
