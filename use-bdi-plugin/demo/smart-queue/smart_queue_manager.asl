queue_length(queue1, 6).
counter_status(counter1, busy).
counter_status(counter2, free).

waiting_customer(queue1, customer1).
waiting_customer(queue1, customer2).
waiting_customer(queue1, customer3).
waiting_customer(queue1, customer4).
waiting_customer(queue1, customer5).
waiting_customer(queue1, customer6).

!reduce_waiting_time(queue1).

@reduce_waiting_time +!reduce_waiting_time(Q)
    : queue_length(Q, N) & N > 5 & counter_status(Counter, free)
<-
    .print("Queue is crowded; assigning a free counter: ", Counter);
    !assign_customer(Q, Customer, Counter).

@reduce_waiting_time_no_counter +!reduce_waiting_time(Q)
    : queue_length(Q, N) & N > 5 & not counter_status(_, free)
<-
    .print("Queue is crowded; requesting another counter");
    !request_open_counter(Q).

@reduce_waiting_time_ok +!reduce_waiting_time(Q)
    : queue_length(Q, N) & N <= 5
<-
    .print("Queue is within the target size: ", N).

@assign_customer +!assign_customer(Q, Customer, Counter)
    : waiting_customer(Q, Customer)
<-
    .print("Assigning ", Customer, " to ", Counter);
    assignCustomer(Q, Customer, Counter);
    -waiting_customer(Q, Customer);
    -counter_status(Counter, free);
    +counter_status(Counter, busy);
    ?queue_length(Q, OldLength);
    -queue_length(Q, OldLength);
    +queue_length(Q, OldLength - 1).

@request_open_counter +!request_open_counter(Q)
<-
    .print("Requesting staff to open a counter for ", Q).
