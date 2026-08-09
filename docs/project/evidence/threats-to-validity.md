# Threats To Validity

## Construct validity

The experiment uses rule-triggered mutant detection as the operational proxy
for consistency checking. This measures whether the expected rule responds to
the selected mutation; it does not measure developer usefulness, repair cost,
or semantic equivalence of a generated suggestion.

## Internal validity

The Auction suite isolates each mutation in a separate fixture and checks a
zero targeted baseline before comparing mutant output. The remaining risks are
the conservative reference index, hand-confirmed mappings, and the bounded
OCL/SOIL effect model. A fixture or mapping mistake could look like a rule
failure, so reports retain source spans and evidence instead of only counts.

## External validity

One small Auction model and one Smart Queue import benchmark cannot represent
all Jason syntax, JaCaMo applications, UML/OCL models, or USE states. The
results should not be generalized to large multi-agent systems or other Jason
versions without a new corpus and rerun.

## Conclusion validity

Four detected mutants produce a wide uncertainty boundary. The reported 1.000
precision/recall/F1 values are therefore scoped descriptive results, not a
statistical estimate of the complete rule catalog. The benchmark has seven
measurements and no confidence interval; it is suitable for a local comparison
baseline only.

## Mitigations

Use separate mutant resources, deterministic report exports, a checked-in
ground-truth manifest, immutable USE snapshots, and focused reruns on the
same Java/Jason versions. Expand the labeled corpus before making comparative
claims.
