# Auction Classification Metrics

## Scope

These metrics are for the four labeled Auction mutants in
`auction-ground-truth.json`. The unit of classification is a mutant instance,
and a positive prediction means that the expected primary rule appears with a
positive mutant delta while its baseline targeted count is zero.

## Counts and formulas

| Quantity | Value |
|---|---:|
| Labeled mutant instances | 4 |
| True positives (TP) | 4 |
| False positives (FP) | 0 |
| False negatives (FN) | 0 |
| True negatives (TN) | Not defined by this corpus |

The scoped values are calculated as:

```text
precision = TP / (TP + FP)
recall    = TP / (TP + FN)
F1        = 2 * precision * recall / (precision + recall)
```

| Metric | Value |
|---|---:|
| Precision | 1.000 |
| Recall | 1.000 |
| F1 | 1.000 |

`AuctionMetricsEvidenceTest` recomputes these values from the tracked CSV and
checks that the four expected rule IDs are present. The underlying detections
are produced by `AuctionStructuralMutantTest` and `AuctionFaultInjectionTest`.

## Interpretation

The result is a targeted mutation-detection score, not a claim that the rules
have perfect precision or recall on arbitrary AgentSpeak/Jason programs. The
corpus contains no labeled clean non-mutant negatives for a TN estimate and
does not cover every rule. In particular, the current `REF-001` policy is
conservative and the baseline intentionally retains potential/unknown issues.
The thesis must report the corpus size and this boundary beside the values.

The Task 10 rerun on 2026-08-17 reproduced the same four true positives and
zero false negatives/process errors across two byte-deterministic packaged
executions. This repetition checks tooling determinism; it does not add new
independent mutant observations or narrow the statistical uncertainty.
