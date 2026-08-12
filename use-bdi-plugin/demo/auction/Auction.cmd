-- Deterministic Auction snapshot for GUI OCL and mapping inspection.
!create auctioneer1 : Auctioneer
!set auctioneer1.name := 'auctioneer'

!create auction1 : Auction
!set auction1.title := 'Demo auction'
!set auction1.status := #open
!set auction1.reservePrice := 100.0
!insert (auctioneer1, auction1) into AuctioneerAuctions

!create bidder1 : Bidder
!set bidder1.name := 'bidder1'
!set bidder1.budget := 150.0
!insert (auction1, bidder1) into AuctionBidders

!create bid1 : Bid
!set bid1.amount := 120.0
!insert (auction1, bid1) into AuctionBids
!insert (bidder1, bid1) into BidderBids

check
