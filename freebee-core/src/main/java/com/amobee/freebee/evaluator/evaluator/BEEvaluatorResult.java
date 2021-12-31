package com.amobee.freebee.evaluator.evaluator;

import com.amobee.freebee.evaluator.BEMatchedInterval;
import com.amobee.freebee.evaluator.index.BEIndexExpressionResult;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Represents the result of running a {@link BEEvaluator} against a given {@link BEInput}.
 *
 * Contains matched expressions and metadata about which input values contributed to
 * satisfying each matched expressions.
 *
 * @param <T> The type of expression data for the given evaluator that generated this result object.
 */
public class BEEvaluatorResult<T>
{

    private final Set<T> matchedExpressionData = new HashSet<>();
    private final Map<T, BEIndexExpressionResult> matchedExpressionIntervals = new HashMap<>();

    public void add(final BEIndexExpressionResult matchedExpression, final T expressionData)
    {
        this.matchedExpressionData.add(expressionData);
        this.matchedExpressionIntervals.put(expressionData, matchedExpression);
    }

    /**
     * Returns the set of expression data for all matched expressions.
     *
     * @return a set of expression data (e.g., ID strings) corresponding to matched expressions for a given input.
     */
    public Set<T> getMatchedExpressions()
    {
        return this.matchedExpressionData;
    }

    /**
     * Returns the possible combinations of input values that could satisfy a given expression.
     *
     * Input values are returned as 2D list, ie a list of lists.
     * Each row in the outer list represents one possible way to satisfy the overall expression.
     * Each inner list represents the input attribute category values required to fulfil the expression.
     * For each attribute category entry in the inner list, one value must be used for the overall expression to be satisfied.
     *
     * Put another way: For the outer list, choose any row/entry. For the inner list for that selection,
     * chose one value from every BEInputAttributeCategory in the list.
     *
     * Note: Input attribute categories and values are only returned if tracking is enabled when creating the input
     * attribute category. Untracked categories will not appear at all in the return values of this method.
     */
    public List<List<? extends BEInputAttributeCategory>> getPossibleInputValuesThatSatisfy(final T expressionData)
    {
        if (expressionData == null)
        {
            return null;
        }

        final BEIndexExpressionResult indexExpressionResult = this.matchedExpressionIntervals.get(expressionData);

        if (indexExpressionResult == null)
        {
            return null;
        }

        final List<List<BEMatchedInterval>> possibleCompleteIntervalPaths =
                findAllPossibleIntervalPathsThatSatisfyTheExpression(indexExpressionResult);

        final List<List<? extends BEInputAttributeCategory>> matchedInputValues = new ArrayList<>(possibleCompleteIntervalPaths.size());
        for (final List<BEMatchedInterval> intervalPath : possibleCompleteIntervalPaths)
        {
            // Each path will result in a List<? extends BEInputAttributeCategory> where each element in
            // the list is one set of possible values that would complete the leaf node in the expression
            final List<? extends BEInputAttributeCategory> possibleValuesToSatisfyThisPath =
                    intervalPath.stream()
                            .map(BEMatchedInterval::getMatchedInputValues)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList());
            if (!possibleValuesToSatisfyThisPath.isEmpty())
            {
                matchedInputValues.add(possibleValuesToSatisfyThisPath);
            }
        }

        return matchedInputValues;
    }

    @Nonnull
    private List<List<BEMatchedInterval>> findAllPossibleIntervalPathsThatSatisfyTheExpression(@Nonnull final BEIndexExpressionResult indexExpressionResult)
    {
        final Map<Integer, List<BEMatchedInterval>> intervalsByStartingPosition = new HashMap<>();
        indexExpressionResult.getMatchedIntervals().forEach(interval -> {
            final int start = interval.getStart();
            intervalsByStartingPosition.putIfAbsent(start, new ArrayList<>());
            intervalsByStartingPosition.get(start).add(interval);
        });

        final int currentPosition = 0;
        final int terminalPosition = indexExpressionResult.getMaxIntervalLength() + 1;
        final List<BEMatchedInterval> currentPath = new ArrayList<>();
        final List<List<BEMatchedInterval>> completePaths = new ArrayList<>();

        findAllCompleteIntervalPaths(currentPosition, terminalPosition, currentPath, intervalsByStartingPosition, completePaths);

        return completePaths;
    }

    private void findAllCompleteIntervalPaths(
            final int currentPosition,
            final int terminalPosition,
            final List<BEMatchedInterval> currentPath,
            final Map<Integer, List<BEMatchedInterval>> intervalsByStartingPosition,
            final List<List<BEMatchedInterval>> completePaths)
    {
        if (currentPosition + 1 == terminalPosition)
        {
            completePaths.add(new ArrayList<>(currentPath));
            return;
        }

        // Recurse for all the intervals "touching" the end of the current interval
        final List<BEMatchedInterval> intervalsStartingAtCurrentPosition = intervalsByStartingPosition.get(currentPosition);
        if (intervalsStartingAtCurrentPosition != null)
        {
            for (final BEMatchedInterval interval : intervalsByStartingPosition.get(currentPosition))
            {
                currentPath.add(interval);
                final int nextIntervalEndPosition = interval.getEnd();
                findAllCompleteIntervalPaths(nextIntervalEndPosition, terminalPosition, currentPath, intervalsByStartingPosition, completePaths);
                currentPath.remove(currentPath.size() - 1);
            }
        }
    }

}
