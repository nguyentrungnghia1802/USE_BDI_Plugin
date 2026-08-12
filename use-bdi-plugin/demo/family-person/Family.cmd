-- Deterministic family snapshot for the Person-in-Family GUI walkthrough.
!create family1 : Family
!set family1.name := 'Nguyen family'

!create alice : Person
!set alice.name := 'Alice'
!set alice.age := 28
!insert (family1, alice) into FamilyMembers

!create ben : Person
!set ben.name := 'Ben'
!set ben.age := 8
!insert (family1, ben) into FamilyMembers

check
