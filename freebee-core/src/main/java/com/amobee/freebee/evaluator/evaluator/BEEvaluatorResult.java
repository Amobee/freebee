package com.amobee.freebee.evaluator.evaluator;

import com.amobee.freebee.evaluator.BEMatchedInterval;
import com.amobee.freebee.evaluator.index.BEIndexExpressionResult;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Stack;
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
    public Set<List<BEInputAttributeCategory>> getPossibleInputValuesThatSatisfy(final T expressionData)
    {
        if (expressionData == null)
        {
            return null;
        }

        final BEIndexExpressionResult indexExpressionResult = this.matchedExpressionIntervals.get(expressionData);
        return getPossibleInputValuesThatSatisfy(indexExpressionResult);
    }

    private Set<List<BEInputAttributeCategory>> getPossibleInputValuesThatSatisfy(final BEIndexExpressionResult indexExpressionResult)
    {
        if (indexExpressionResult == null)
        {
            return null;
        }

        final List<List<BEMatchedInterval>> possibleCompleteIntervalPathsWithPartialExpressionRefs =
                findAllPossibleIntervalPathsThatSatisfyTheExpression(indexExpressionResult);

        // Now, we need to expand partial expressions into possible paths that complete them, and
        // incorporate those results into the full expressions.
        final List<List<BEMatchedInterval>> possibleCompleteIntervalPathsFlattened = new ArrayList<>();
        for (final List<BEMatchedInterval> pathOfIntervals : possibleCompleteIntervalPathsWithPartialExpressionRefs)
        {
            possibleCompleteIntervalPathsFlattened.addAll(replacePartialExpressionRefsWithIntervalPaths(pathOfIntervals));
        }

        final Set<List<BEInputAttributeCategory>> matchedInputValues = new LinkedHashSet<>(possibleCompleteIntervalPathsFlattened.size());
        for (final List<BEMatchedInterval> pathOfIntervals : possibleCompleteIntervalPathsFlattened)
        {
            // Each path will result in a List<? extends BEInputAttributeCategory> where each element in
            // the list is one set of possible values that would complete the leaf node in the expression
            final List<BEInputAttributeCategory> possibleValuesToSatisfyThisPath =
                    pathOfIntervals.stream()
                            .map(BEMatchedInterval::getMatchedInputValues)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList());
            // Add the list of possible values that satisfy the current path to the
            // set representing all possible paths. We use a set so that duplicate lists
            // (e.g., an empty list) only occur once in the overall result.
            matchedInputValues.add(possibleValuesToSatisfyThisPath);
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

                // This is a depth first search, with the recursive call above this line.
                // By the time we have reached this next line, we have completed one possible path.
                // The next iteration of the for loop represents a new possible path in our DFX,
                // so we need to remove the interval that was added previously before iterating
                // on the loop to resume searching on a new path that takes a different "fork"
                currentPath.remove(currentPath.size() - 1);
            }
        }
    }

    private List<List<BEMatchedInterval>> replacePartialExpressionRefsWithIntervalPaths(
            final List<BEMatchedInterval> possibleCompleteIntervalPathsWithPartialExpressionRefs)
    {
        final List<List<BEMatchedInterval>> possibleCompleteIntervalPathsFlattened = new ArrayList<>();
        possibleCompleteIntervalPathsFlattened.add(new ArrayList<>());

        final Stack<BEMatchedInterval> remainingIntervalsOnPath = new Stack<>();
        for (int i = possibleCompleteIntervalPathsWithPartialExpressionRefs.size() - 1; i >= 0; i--)
        {
            remainingIntervalsOnPath.add(possibleCompleteIntervalPathsWithPartialExpressionRefs.get(i));
        }

        return expandIntervalsRepresentingPartialExpressReferencesToPossibleCompletePaths(
                possibleCompleteIntervalPathsFlattened,
                remainingIntervalsOnPath);

    }

    private List<List<BEMatchedInterval>> expandIntervalsRepresentingPartialExpressReferencesToPossibleCompletePaths(
            final List<List<BEMatchedInterval>> allFlattenedPaths,
            final Stack<BEMatchedInterval> remainingIntervalsOnPath)
    {
        List<List<BEMatchedInterval>> updatedFlattenedPaths = allFlattenedPaths;
        while (!remainingIntervalsOnPath.isEmpty())
        {
            final BEMatchedInterval nextInterval = remainingIntervalsOnPath.pop();
            if (nextInterval.isPartialExpressionReferenceInterval())
            {
                final List<List<BEMatchedInterval>> possibleCompleteIntervalPathsThatSatisfyThePartialExpression =
                        findAllPossibleIntervalPathsThatSatisfyTheExpression(nextInterval.getPartialExpressionIndexResult());

                // multiply our current 2D list representing all paths for all expressions
                // by the new 2D list representing all paths for the partial expression
                updatedFlattenedPaths = new ArrayList<>();
                for (final List<BEMatchedInterval> path : allFlattenedPaths)
                {
                    for (final List<BEMatchedInterval> partialExpressionPath : possibleCompleteIntervalPathsThatSatisfyThePartialExpression)
                    {
                        final List<BEMatchedInterval> flattenedPath = new ArrayList<>(path);
                        flattenedPath.addAll(partialExpressionPath);
                        updatedFlattenedPaths.add(flattenedPath);
                    }
                }
            }
            else
            {
                updatedFlattenedPaths.forEach(path -> path.add(nextInterval));
            }
            updatedFlattenedPaths = expandIntervalsRepresentingPartialExpressReferencesToPossibleCompletePaths(updatedFlattenedPaths, remainingIntervalsOnPath);
        }
        return updatedFlattenedPaths;
    }

}
