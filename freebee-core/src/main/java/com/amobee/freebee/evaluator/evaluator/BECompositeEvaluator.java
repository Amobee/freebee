package com.amobee.freebee.evaluator.evaluator;

import com.amobee.freebee.evaluator.index.BEIndexMetrics;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * An example implementation of the BEEvaluator interface that is capable of
 * decomposing a large set of expressions into many evaluators and calling
 * all in parallel in order to match expressions faster that using a single
 * evaluator with all the expressions.
 *
 * Note: This is untested and should not be used in production.
 * It is merely a reference example of how divide and conquer could be used
 * to quickly evaluate a very large number of expressions.
 *
 * Also note that the manner in which expressions were divided among the
 * collection of backing Evaluators would be important. In particular, one
 * would want to try avoid creating Evaluators that had BEIndexes with
 * BEIntervals duplicated across more than one evaluator.
 *
 * @author Kevin Doran
 */
public class BECompositeEvaluator<T> implements BEEvaluator<T>
{

    private static final long serialVersionUID = -2518650722474953889L;

    private final Collection<BEEvaluator<T>> evaluators;

    BECompositeEvaluator(@Nonnull final Collection<BEEvaluator<T>> evaluators)
    {
        this.evaluators = evaluators;
    }

    @Nullable
    @Override
    public BEIndexMetrics getMetrics()
    {
        final BEIndexMetrics aggregate = new BEIndexMetrics();
        aggregate.setIntervalCount(0);
        aggregate.setExpressionCount(0);
        aggregate.setFullExpressionCount(0);
        aggregate.setPartialExpressionCount(0);
        aggregate.setExpressionCountWithIntervalEvaluation(0);
        aggregate.setExpressionCountWithBitSetEvaluation(0);

        this.evaluators.stream()
                .map(BEEvaluator::getMetrics)
                .filter(Objects::nonNull)
                .forEach(metrics -> {
                    aggregate.setIntervalCount(aggregate.getIntervalCount() + metrics.getIntervalCount());
                    aggregate.setExpressionCount(aggregate.getExpressionCount() + metrics.getExpressionCount());
                    aggregate.setPartialExpressionCount(aggregate.getFullExpressionCount() + metrics.getFullExpressionCount());
                    aggregate.setPartialExpressionCount(aggregate.getPartialExpressionCount() + metrics.getPartialExpressionCount());
                    aggregate.setExpressionCountWithBitSetEvaluation(
                            aggregate.getExpressionCountWithBitSetEvaluation()
                                    + metrics.getExpressionCountWithBitSetEvaluation());
                    aggregate.setExpressionCountWithIntervalEvaluation(
                            aggregate.getExpressionCountWithIntervalEvaluation()
                                    + metrics.getExpressionCountWithIntervalEvaluation());
                });

        return aggregate;
    }

    @Nonnull
    @Override
    public Set<T> evaluate(@Nonnull final BEInput input)
    {
        final Set<T> matchedResults = this.evaluators.parallelStream()
                .map(evaluator -> evaluator.evaluate(input))
                .flatMap(Set::stream)
                .collect(Collectors.toSet());

        return matchedResults;
    }

    @Nonnull
    @Override
    public BEEvaluatorResult<T> evaluateAndTrack(@Nonnull final BEInput input)
    {
        throw new UnsupportedOperationException(
                "This feature is not yet supported by the BECompositeEvaluator.");
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        final BECompositeEvaluator<?> that = (BECompositeEvaluator<?>) o;
        return Objects.equals(this.evaluators, that.evaluators);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(this.evaluators);
    }
}
