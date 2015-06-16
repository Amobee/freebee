package com.amobee.freebee.evaluator.evaluator;

import com.amobee.freebee.evaluator.index.BEIndexMetrics;

import java.io.Serializable;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * An interface for a free-form boolean expression evaluator.
 *
 * Users of the library should get an instance of a BEEvaluator using a BEEvaluatorBuilder.
 *
 * @param <T> the type of data associated with each boolean expression
 * @see BEEvaluatorBuilder
 */
public interface BEEvaluator<T> extends Serializable
{

    /**
     * Return the set of expressions that match the given input.
     *
     * @param input The input record / request to evaluate against
     * @return the set of expressions that were matched, represented as the T data associated with each expression.
     */
    @Nonnull
    Set<T> evaluate(@Nonnull BEInput input);

    /**
     * Get summary metrics for the BEIndex that backs the evaluator.
     *
     * @return a BEIndex instance with the metrics from the index used by this evaluator,
     *          or null if metrics are not available.
     */
    @Nullable
    BEIndexMetrics getMetrics();

    /**
     * Determine if two BEEvaluators are equal.
     *
     * Implementations should make a best effort to reflect logical equivalence
     * of the evaluators. That is, if two evaluator instances would match the same
     * expressions for the same input, they should be considered equal.
     *
     * @param o the other evaluator to compare to this one.
     * @return true if the evaluators are semantically equal, false otherwise
     */
    @Override
    boolean equals(Object o);

    /**
     * Compute a hashCode for the evaluator.
     *
     * Implementations should make a best effort to reflect logical equivalence
     * of the evaluator. That is, if two evaluator instances would match the same
     * expressions for the same input, they should produce the same hashCode.
     */
    @Override
    int hashCode();

}
