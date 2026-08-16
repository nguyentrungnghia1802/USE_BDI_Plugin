# Threats To Validity

## Construct validity

The experiment uses rule-triggered mutant detection as the operational proxy
for consistency checking. This measures whether the expected rule responds to
the selected mutation; it does not measure developer usefulness, repair cost,
or semantic equivalence of a generated suggestion.
Metamodel coverage counts structural concepts and trace anchors, not semantic
completeness. A readable screenshot or bounded highlight path is not evidence
of usability or user comprehension.

## Internal validity

The Auction suite isolates each mutation in a separate fixture and checks a
zero targeted baseline before comparing mutant output. The remaining risks are
the conservative reference index, hand-confirmed mappings, and the bounded
OCL/SOIL effect model. A fixture or mapping mistake could look like a rule
failure, so reports retain source spans and evidence instead of only counts.
Fixed analysis timestamps, deterministic ordering, external manifest oracles,
isolated case workspaces, repeated structural hashes, and before/after state
fingerprints reduce—but do not eliminate—tool and fixture bias.

## External validity

One small Auction model and one Smart Queue import benchmark cannot represent
all Jason syntax, JaCaMo applications, UML/OCL models, or USE states. The
results should not be generalized to large multi-agent systems or other Jason
versions without a new corpus and rerun.
The wider evidence still relies mainly on Auction plus three small canonical
demos, covers only static JaCaMo declarations, and has no optional second
research corpus or runtime lifecycle evidence.

## Conclusion validity

Four detected mutants produce a wide uncertainty boundary. The reported 1.000
precision/recall/F1 values are therefore scoped descriptive results, not a
statistical estimate of the complete rule catalog. The benchmark has seven
measurements and no confidence interval; it is suitable for a local comparison
baseline only.
The diagram benchmark adds 15 repetitions per four canonical cases, still too
small and environment-dependent for statistical inference, cross-machine
comparison, or a universal latency threshold.

## Mitigations

Use separate mutant resources, deterministic report exports, a checked-in
ground-truth manifest, immutable USE snapshots, and focused reruns on the
same Java/Jason versions. Expand the labeled corpus before making comparative
claims.
