package com.amobee.freebee.bench.random;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

class Utils
{
    private static final Random DEFAULT_RANDOM = new Random(4358371950700L);

    private Utils() {}

    static void shuffleList(final List list)
    {
        shuffleList(list, list.size());
    }

    static void shuffleList(final List list, final int limit)
    {
        shuffleList(list, limit, DEFAULT_RANDOM);
    }

    static void shuffleList(final List list, final int limit, final Random random)
    {
        final int inputSize = list.size();
        for (int i = 0; i < limit; i++)
        {
            final int indexToSwap = i + random.nextInt(inputSize - i);
            final Object temp = list.get(i);
            list.set(i, list.get(indexToSwap));
            list.set(indexToSwap, temp);
        }
    }

    static Set<String> randomSubset(
            final Set<String> superset,
            final int sizeLowerBoundInclusive,
            final int sizeUpperBoundExclusive)
    {
        return randomSubset(superset, sizeLowerBoundInclusive, sizeUpperBoundExclusive, DEFAULT_RANDOM);
    }

    static Set<String> randomSubset(
            final Set<String> superset,
            final int sizeLowerBoundInclusive,
            final int sizeUpperBoundExclusive,
            final Random random)
    {
        final int subsetSize = randomIntBetween(sizeLowerBoundInclusive, sizeUpperBoundExclusive, random);
        final List<String> input = new ArrayList<>(superset);  // Copy input to new collection that we can modify
        Utils.shuffleList(input, subsetSize, random);
        return new HashSet<>(input.subList(0, subsetSize));
    }

    static int randomIntBetween(final int lowerBoundInclusive, final int upperBoundExclusive, final Random random)
    {
        return random.nextInt(upperBoundExclusive - lowerBoundInclusive) + lowerBoundInclusive;
    }

}
