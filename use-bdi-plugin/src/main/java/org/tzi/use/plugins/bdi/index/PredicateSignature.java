package org.tzi.use.plugins.bdi.index;

/** Stable lookup key for a predicate, action, or goal term. */
public record PredicateSignature(String functor, int arity) {
    public PredicateSignature {
        if (functor == null || functor.isBlank()) {
            throw new IllegalArgumentException("functor must not be blank");
        }
        if (arity < 0) {
            throw new IllegalArgumentException("arity must not be negative");
        }
    }

    @Override
    public String toString() {
        return functor + "/" + arity;
    }
}
