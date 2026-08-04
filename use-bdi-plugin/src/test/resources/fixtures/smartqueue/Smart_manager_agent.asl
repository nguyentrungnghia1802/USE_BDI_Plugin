// manager_agent.asl
// Smart Queue decision-making agent for Jason/JaCaMo
//
// SmartQueue.use describes structure and constraints.
// This file describes the ManagerAgent's decision making.
//
// This is a concept demo. A future USE plugin or JaCaMo artifact
// would be needed to synchronize these actions with USE's MSystem.

// =========================
// INITIAL BELIEFS
// =========================

queue_length(queue1, 6).

counter_status(counter1, busy).
counter_status(counter2, free).

waiting_customer(queue1, customer1).
waiting_customer(queue1, customer2).
waiting_customer(queue1, customer3).
waiting_customer(queue1, customer4).
waiting_customer(queue1, customer5).
waiting_customer(queue1, customer6).

// =========================
// INITIAL GOAL
// =========================

!reduce_waiting_time(queue1).

// =========================
// DECISION-MAKING PLANS
// =========================

// Queue is crowded and a free counter exists.
+!reduce_waiting_time(Q)
    : queue_length(Q, N)
      & N > 5
      & counter_status(Counter, free)
      & waiting_customer(Q, Customer)
<-
    .print("[ManagerAgent] Queue ", Q, " has ", N, " customers.");
    .print("[ManagerAgent] Free counter: ", Counter);
    !assign_customer(Q, Customer, Counter).

// Queue is crowded but all counters are busy.
+!reduce_waiting_time(Q)
    : queue_length(Q, N)
      & N > 5
      & not counter_status(_, free)
<-
    .print("[ManagerAgent] Queue ", Q, " is crowded.");
    .print("[ManagerAgent] No free counter is available.");
    !request_open_counter(Q).

// Queue is not crowded.
+!reduce_waiting_time(Q)
    : queue_length(Q, N)
      & N <= 5
<-
    .print("[ManagerAgent] Queue ", Q, " is acceptable: ", N);
    .print("[ManagerAgent] No action is required.").

// =========================
// ACTION PLANS
// =========================

+!assign_customer(Q, Customer, Counter)
<-
    .print("[ManagerAgent] Assigning ", Customer, " to ", Counter);

    // In a multi-agent system, request CounterAgent to serve the customer.
    .send(counter_agent, achieve, serve(Customer));

    // Update ManagerAgent's beliefs.
    -waiting_customer(Q, Customer);
    -counter_status(Counter, free);
    +counter_status(Counter, busy);

    ?queue_length(Q, OldLength);
    -queue_length(Q, OldLength);
    +queue_length(Q, OldLength - 1);

    .print("[ManagerAgent] Decision completed.").

+!request_open_counter(Q)
<-
    .print("[ManagerAgent] Requesting an additional counter for ", Q);

    // In a complete system, StaffAgent would handle this request.
    .send(staff_agent, achieve, open_counter(Q)).
