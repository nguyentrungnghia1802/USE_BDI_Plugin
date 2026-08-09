# Future Work

## Near term

1. Reduce `REF-001` false positives by separating object references from
   enum-like literals, context values, variables, and other term categories.
2. Add dedicated positive and negative fixtures for the rules that currently
   rely on the aggregate validation test.
3. Add a larger labeled mutant corpus covering communication, goal support,
   contexts, types, ownership, and bounded OCL effects.

## Integration

1. Make project-root and source identity handling portable so confirmed mapping
   documents can be checked in and replayed on a clean machine.
2. Add a user guide for mapping confirmation, suppression review, and the
   `UNKNOWN` certainty policy.
3. Integrate the report/evaluation commands into a CI job that records Java,
   Maven, USE, Jason, and host metadata.

## Evaluation

1. Repeat the protocol on Smart Queue and House Building only after their
   model and AgentSpeak contracts are verified.
2. Measure larger workloads, memory use, and repeated-run confidence intervals.
3. Compare the plugin with a manually labeled oracle using issue-level metrics,
   not only mutant-level detection.

## Release quality

Resolve the root USE Failsafe handshake limitation through a separately
reviewed ADR before claiming a full `mvn clean verify` release gate. Then test
the assembled ZIP on a clean Java 21 profile and create the thesis release tag.
