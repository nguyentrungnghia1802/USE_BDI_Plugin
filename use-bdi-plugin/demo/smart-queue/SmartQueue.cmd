-- Deterministic snapshot for the Smart Queue presentation.
!create queue1 : Queue
!set queue1.size := 6

!create customer1 : Customer
!set customer1.name := 'customer1'
!create customer2 : Customer
!set customer2.name := 'customer2'
!create customer3 : Customer
!set customer3.name := 'customer3'
!create customer4 : Customer
!set customer4.name := 'customer4'
!create customer5 : Customer
!set customer5.name := 'customer5'
!create customer6 : Customer
!set customer6.name := 'customer6'

!insert (queue1, customer1) into QueueCustomers
!insert (queue1, customer2) into QueueCustomers
!insert (queue1, customer3) into QueueCustomers
!insert (queue1, customer4) into QueueCustomers
!insert (queue1, customer5) into QueueCustomers
!insert (queue1, customer6) into QueueCustomers

!create counter1 : Counter
!set counter1.name := 'counter1'
!set counter1.status := #busy
!create counter2 : Counter
!set counter2.name := 'counter2'
!set counter2.status := #free

!insert (queue1, counter1) into QueueCounters
!insert (queue1, counter2) into QueueCounters

!create manager1 : Manager
!set manager1.name := 'manager1'
!insert (manager1, queue1) into ManagerQueues

check
