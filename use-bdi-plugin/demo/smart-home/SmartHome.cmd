-- Deterministic smart-home snapshot for the Resident decision walkthrough.
!create home1 : Home
!set home1.address := '12 Demo Street'

!create resident1 : Resident
!set resident1.name := 'Minh'
!set resident1.evening := true
!set resident1.lightsAvailable := true
!insert (home1, resident1) into HomeResidents

!create livingRoomLight : Light
!set livingRoomLight.room := 'living room'
!set livingRoomLight.on := false
!insert (home1, livingRoomLight) into HomeLights

check
