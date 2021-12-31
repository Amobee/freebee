package com.amobee.freebee.evaluator.evaluator;

import com.amobee.freebee.evaluator.BEInterval;
import com.amobee.freebee.evaluator.BEMatchedInterval;
import com.amobee.freebee.evaluator.index.BEIndex;
import com.amobee.freebee.evaluator.index.BEIndexExpressionResult;
import com.amobee.freebee.evaluator.index.BEIndexMetrics;
import com.amobee.freebee.evaluator.index.BEIndexResults;
import com.amobee.freebee.evaluator.interval.Interval;

import javax.annotation.Nonnull;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * An implementation of {@link BEEvaluator} that uses a hybrid matching algorithm
 * based on the characteristics of each expression.
 *
 * Expressions that do not contain overlapping predicate node intervals (e.g., CNF expressions)
 * are matched using a bitset matching algorithm that is faster than the standard, sorted-interval
 * matching algorithm presented in the Efficient Evaluation of Boolean Expressions whitepaper.
 *
 * All other expressions (free-form) are matched using the standard matching algorithm.
 *
 * Note: Users of the FreeBEE library should not create instances of this class directly,
 * rather, {@link BEEvaluatorBuilder} should be used.
 *
 * @param <T> the type of data associated with each boolean expression.
 *
 * @author Kevin Doran
 * @see BEEvaluatorBuilder
 */
public class BEHybridEvaluator<T> implements BEEvaluator<T>
{
    private static final long serialVersionUID = 2025241730308385602L;

    private final BEIndex<T> index;

    BEHybridEvaluator(@Nonnull final BEIndex<T> index)
    {
        this.index = index;
    }

    public BEIndex<T> getIndex()
    {
        return this.index;
    }

    @Override
    public BEIndexMetrics getMetrics()
    {
        return this.index.getIndexMetrics();
    }

    @Nonnull
    @Override
    public Set<T> evaluate(@Nonnull final BEInput input)
    {
        final BEEvaluatorResult<T> evaluatorResult = evaluateAndTrack(input);
        return evaluatorResult.getMatchedExpressions();
    }

    @Nonnull
    @Override
    public BEEvaluatorResult evaluateAndTrack(@Nonnull final BEInput input)
    {

        final BEEvaluatorResult<T> evaluatorResult = new BEEvaluatorResult<>();

        final BEIndexResults expressionIntervals = queryIndexForMatchingExpressionIntervals(input);

        expressionIntervals.getExpressionResults().forEach(indexResult ->
        {
            final int expressionId = indexResult.getExpressionId();
            final T expressionData = this.index.getExpressionsData(expressionId);
            if (!indexResult.isPartial() && expressionData != null)
            {
                if (match(indexResult))
                {
                    evaluatorResult.add(indexResult, expressionData);
                }
            }
        });

        return evaluatorResult;
    }

    private BEIndexResults queryIndexForMatchingExpressionIntervals(@Nonnull final BEInput input)
    {
        // Find all intervals for each expression that match the input.
        final BEIndexResults indexResults = this.index.findMatchingExpressionIntervals(input);

        // Using the matched expression intervals, evaluate partial expressions that were fully matched by the input
        final Set<String> matchedPartialExpressionNames = new HashSet<>();
        indexResults.getExpressionResults()
                .stream()
                .filter(BEIndexExpressionResult::isPartial)
                .filter(this::match)
                .map(BEIndexExpressionResult::getPartialExpressionName)
                .forEach(matchedPartialExpressionNames::add);

        // If any partial expressions were matched, we must now add the referenced partial expression ids to the input
        // and rerun the index query to find more intervals (for full expressions) that may now match.
        this.index.addRefIntervalsForMatchedPartialExpressions(matchedPartialExpressionNames, indexResults);
        return indexResults;
    }

    private boolean match(final BEIndexExpressionResult expressionIndexResult)
    {
        return expressionIndexResult.canUseBitSetMatching()
                ? matchUsingBitSet(expressionIndexResult)
                : matchUsingIntervals(expressionIndexResult);
    }

    private boolean matchUsingBitSet(final BEIndexExpressionResult expressionIndexResult)
    {
        // TODO there may be an even faster way to do this comparison check than .getMatchedBits().nextClearBit(...)
        return expressionIndexResult.getMatchedBits().nextClearBit(0) >= expressionIndexResult.getMaxIntervalLength();
    }

    private boolean matchUsingIntervals(final BEIndexExpressionResult expressionIndexResult)
    {
        // get the intervals to match, **important** they must be sorted by start index
        final List<BEMatchedInterval> intervals = expressionIndexResult.getMatchedIntervals();
        intervals.sort(Comparator.comparingInt(Interval::getStart));

        // create a matched array that spans all intervals across the entire expression
        final boolean[] matched = new boolean[expressionIndexResult.getMaxIntervalLength() + 1];

        // initialize index zero of matched to true so that matched[start] for the first interval will pass
        matched[0] = true;

        for (final BEInterval interval : intervals)
        {
            if (matched[interval.getStart()])
            {
                matched[interval.getEnd()] = true;
            }
        }

        // if the entire interval was matched for the expression, then mark the expression as matched
        return matched[matched.length - 1];
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
        final BEHybridEvaluator<?> that = (BEHybridEvaluator<?>) o;
        return Objects.equals(this.index.hashCode(), that.index.hashCode());
    }

    @Override
    public int hashCode()
    {
        return this.index.hashCode();
    }
}
