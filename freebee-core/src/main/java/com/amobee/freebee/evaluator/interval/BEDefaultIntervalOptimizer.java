package com.amobee.freebee.evaluator.interval;

import java.util.BitSet;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

/**
 * A simple implementation of {@link BEIntervalOptimizer} that
 * checks intervals for intersection (aka overlap) in order to
 * determine the optimizations for which they qualify.
 *
 * @author kdoran
 */
public class BEDefaultIntervalOptimizer implements BEIntervalOptimizer
{

    @Override
    public boolean canUseBitSetMatching(final int intervalLength, @Nonnull final Collection<? extends Interval> intervals)
    {
        if (intervals == null || intervals.isEmpty())
        {
            throw new IllegalArgumentException("intervals cannot be empty");
        }

        // In the future we may want to detect additional sets of intervals that qualify for bit set matching.
        // for now, just take the conservative approach and only allow bit set matching for collections of
        // intervals in which *distinct* intervals *do not overlap*.

        return distinctIntervalsDoNoOverlap(intervalLength, intervals);
    }

    private boolean distinctIntervalsDoNoOverlap(final int intervalLength, @Nonnull final Collection<? extends Interval> intervals)
    {

        final Set<BitSet> distinctBitSets = intervals.stream()
                .map(this::toSimpleInterval)
                .distinct()
                .map(this::toBitSet)
                .collect(Collectors.toSet());

        final BitSet aggregator = new BitSet(intervalLength);
        for (final BitSet current : distinctBitSets)
        {
            if (current.intersects(aggregator))
            {
                // overlap detected, stop looking
                return false;
            }
            aggregator.xor(current);
        }
        return true;

    }

    private BESimpleInterval toSimpleInterval(final Interval interval)
    {
        return interval instanceof BESimpleInterval ? (BESimpleInterval) interval : new BESimpleInterval(interval);
    }

    private BitSet toBitSet(final Interval interval)
    {
        if (interval.getStart() < 0)
        {
            throw new IllegalArgumentException("Interval start cannot be negative: " + interval.toString());
        }
        if (interval.getEnd() < 1)
        {
            throw new IllegalArgumentException("All interval ends must be >= 1: " + interval.toString());
        }

        final BitSet bitset = new BitSet(interval.getEnd());
        bitset.set(interval.getStart(), interval.getEnd() - 1, true);
        return bitset;
    }

}
