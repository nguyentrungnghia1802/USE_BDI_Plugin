# Smart Queue: BDI Decision-Making

## What this demonstrates

- A USE UML/OCL model for a queue, customers, counters, and a manager.
- A Jason AgentSpeak manager that chooses between assigning a free counter
  and requesting another counter when the queue is crowded.
- A deterministic `SmartQueue.cmd` snapshot with six waiting customers and one
  free counter, so the OCL state can be inspected in the GUI.
- A confirmed mapping from the manager's `assignCustomer` external action to
  `Manager::assignCustomer(...)` in the USE model.
- The separation between the immutable BDI import and the mutable USE state.

The AgentSpeak source is an analysis input, not a live runtime. The plugin does
not execute the plans or synchronize a JaCaMo workspace in this demo.

## Recommended GUI flow

1. Open `SmartQueue.use` with `File > Open specification...`.
2. Run `SmartQueue.cmd` through the USE command view or the command-file
   action. The script creates one queue, six customers, and two counters.
3. Open `View > Create View > Class diagram`, then
   `View > Create View > Object diagram` to show the snapshot.
4. Choose `Plugins > AgentSpeak > Import AgentSpeak...` and select
   `smart_queue_manager.asl`.
5. In `Mapping`, click `Load...` and select `SmartQueue.bdimap.json`.
6. Open `Diagram`, select `BDI Plan`, click `Fit`, and show
   `reduce_waiting_time`, its crowded-queue context, `assign_customer`, and the
   mapped `Manager::assignCustomer(...)` operation. Select a plan and use
   `Focus Goal/Plan` to isolate the decision branch.
7. Click `Refresh USE Snapshot` before showing `Problems`.

## Expected observation

The manager has a crowded queue and a free counter, so the first decision path
is `assign_customer`. The model invariant `SizeMatchesCustomers` confirms that
the snapshot has six linked customers. Changing the queue or counter state and
refreshing the Explorer demonstrates why snapshot refresh is explicit.
