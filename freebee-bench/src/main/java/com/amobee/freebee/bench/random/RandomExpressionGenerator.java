package com.amobee.freebee.bench.random;

import com.amobee.freebee.bench.DataValueProvider;
import com.amobee.freebee.bench.ExpressionGenerator;
import com.amobee.freebee.expression.BEConjunctionNode;
import com.amobee.freebee.expression.BEConjunctionType;
import com.amobee.freebee.expression.BENode;
import com.amobee.freebee.expression.BEPredicateNode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class RandomExpressionGenerator implements ExpressionGenerator
{

    private final DataValueProvider dataValueProvider;
    private final Random random;
    private final int maxDepth;
    private final int maxPredicates;
    private final int maxValuesPerPredicate;

    public RandomExpressionGenerator(
            final RandomBenchmarkConfigurationProperties properties,
            final DataValueProvider dataValueProvider)
    {
        this.dataValueProvider = dataValueProvider;
        this.random = properties.getRandomSeed() != null ? new Random(properties.getRandomSeed()) : new Random();
        this.maxDepth = properties.getMaxDepth();
        this.maxPredicates = properties.getMaxPredicatesPerExpression();
        this.maxValuesPerPredicate = properties.getMaxValuesPerPredicate();
    }

    @Override
    public BENode generate()
    {
        return generate(
                this.maxDepth,
                this.maxPredicates,
                new HashSet<>(this.dataValueProvider.getDataTypes())
        );
    }

    @SuppressWarnings("checkstyle:ReturnCount")
    private BENode generate(
            final int maxDepth,
            final int maxPredicates,
            final Set<String> availableDataTypes)
    {

        if (maxDepth == 1 || maxPredicates == 1 || availableDataTypes.size() == 1)
        {
            return generatePredicateNode(availableDataTypes.toArray(new String[0]));
        }
        else
        {
            final int randomChoice = this.random.nextInt(3);

            if (randomChoice == 0)
            {
                // Return predicate node(s)
                return generatePredicateNode(availableDataTypes.toArray(new String[0]));

            }
            else if (randomChoice == 1)
            {
                // Return OR conjunction node with subtrees generated via recursion
                // NOTE: OR passes same available data types to all subtrees

                final List<SubtreeMetadata> subtreeShapes = randomizeShapeOfConjunctionSubtrees(
                        BEConjunctionType.OR,
                        maxPredicates,
                        availableDataTypes);

                final List<BENode> subtrees = new ArrayList<>();
                for (final SubtreeMetadata metadata : subtreeShapes)
                {
                    subtrees.add(generate(maxDepth - 1, metadata.maxPredicates, metadata.availableDataTypes));
                }
                return new BEConjunctionNode(BEConjunctionType.OR, subtrees);

            }
            else /* randomChoice == 2 */
            {
                // Return AND conjunction node with subtrees generated via recursion
                // NOTE: AND splits available data types between all available subtrees to avoid (x == 1 AND x ==2)

                final List<SubtreeMetadata> subtreeShapes = randomizeShapeOfConjunctionSubtrees(
                        BEConjunctionType.AND,
                        maxPredicates,
                        availableDataTypes);

                final List<BENode> subtrees = new ArrayList<>();
                for (final SubtreeMetadata metadata : subtreeShapes)
                {
                    subtrees.add(generate(maxDepth - 1, metadata.maxPredicates, metadata.availableDataTypes));
                }
                return new BEConjunctionNode(BEConjunctionType.AND, subtrees);
            }
        }
    }

    private BEPredicateNode generatePredicateNode(final String[] availableDataTypes)
    {

        if (availableDataTypes == null || availableDataTypes.length < 1)
        {
            throw new IllegalArgumentException("Invalid arguments for generating random predicate node");
        }

        final int dataTypeChoice = this.random.nextInt(availableDataTypes.length);
        final String dataTypeName = availableDataTypes[dataTypeChoice];

        return generatePredicateNode(dataTypeName);

    }

    private BEPredicateNode generatePredicateNode(final String dataTypeName)
    {
        if (dataTypeName == null)
        {
            throw new IllegalArgumentException("Invalid arguments for generating random predicate node");
        }

        final boolean isNegatedChoice = this.random.nextBoolean();

        final DataValueProvider.RandomValueSelector valueSelector = this.dataValueProvider.getRandomValueSelector(dataTypeName);

        final int maxValues = Math.min(this.maxValuesPerPredicate, valueSelector.getMaxUniqueValues());
        final int numValuesChoice = this.random.nextInt(maxValues) + 1;

        final String[] values = valueSelector.getValueArray(numValuesChoice);

        return new BEPredicateNode(dataTypeName, isNegatedChoice, values);

    }

    private List<SubtreeMetadata> randomizeShapeOfConjunctionSubtrees(
            final BEConjunctionType conjunctionType,
            final int maxPredicates,
            final Set<String> availableDataTypes)
    {
        final int maxChildNodes = Math.min(maxPredicates, availableDataTypes.size());
        if (maxChildNodes < 2)
        {
            throw new IllegalStateException("Cannot create a conjunction node with one child node");
        }
        final int numberOfChildNodes = randomIntBetween(2, maxChildNodes + 1);

        if (BEConjunctionType.OR.equals(conjunctionType))
        {
            // NOTE: OR passes same available data types to all subtrees
            final List<SubtreeMetadata> subtreeShapes = new ArrayList<>();
            int remainingMaxPredicates = maxPredicates;
            int remainingChildNodes = numberOfChildNodes;
            while (remainingChildNodes > 1 /* stop with one child node left */)
            {
                final int adjustedMaxPredicates = remainingMaxPredicates - remainingChildNodes + 1;  // each node needs at least 1 predicate.

                final int subtreeMaxPredicates = randomIntBetween(1, adjustedMaxPredicates + 1);
                remainingMaxPredicates = remainingMaxPredicates - subtreeMaxPredicates;

                subtreeShapes.add(new SubtreeMetadata(subtreeMaxPredicates, availableDataTypes));
                remainingChildNodes--;
            }
            subtreeShapes.add(new SubtreeMetadata(remainingMaxPredicates, availableDataTypes));  // add the last child node
            return subtreeShapes;

        }
        else if (BEConjunctionType.AND.equals(conjunctionType))
        {
            // NOTE: AND splits available data types between all available subtrees to avoid (x == 1 AND x == 2)

            final List<SubtreeMetadata> subtreeShapes = new ArrayList<>();
            final Set<String> remainingDataTypes = new HashSet<>(availableDataTypes);
            int remainingMaxPredicates = maxPredicates;
            int remainingChildNodes = numberOfChildNodes;
            while (remainingChildNodes > 1 /* stop with one child node left */)
            {
                final int adjustedMaxPredicates = remainingMaxPredicates - remainingChildNodes + 1;  // each node needs at least 1 predicate.
                final int adjustedMaxDataTypes = remainingDataTypes.size() - remainingChildNodes + 1;

                final int subtreeMaxPredicates = randomIntBetween(1, adjustedMaxPredicates + 1);
                remainingMaxPredicates = remainingMaxPredicates - subtreeMaxPredicates;

                final Set<String> subtreeDataTypes =
                        Utils.randomSubset(remainingDataTypes, 1, adjustedMaxDataTypes + 1, this.random);
                remainingDataTypes.removeAll(subtreeDataTypes);

                subtreeShapes.add(new SubtreeMetadata(subtreeMaxPredicates, subtreeDataTypes));
                remainingChildNodes--;
            }
            subtreeShapes.add(new SubtreeMetadata(remainingMaxPredicates, remainingDataTypes));  // add the last child node
            return subtreeShapes;

        }
        else
        {
            throw new IllegalArgumentException("Expected BEConjunctionType to be AND or OR");
        }

    }

    private int randomIntBetween(final int lowerBoundInclusive, final int upperBoundExclusive)
    {
        return this.random.nextInt(upperBoundExclusive - lowerBoundInclusive) + lowerBoundInclusive;
    }


    private static class SubtreeMetadata
    {
        private final int maxPredicates;
        private final Set<String> availableDataTypes;

        SubtreeMetadata(final int maxPredicates, final Set<String> availableDataTypes)
        {
            this.maxPredicates = maxPredicates;
            this.availableDataTypes = availableDataTypes;
        }
    }

}
