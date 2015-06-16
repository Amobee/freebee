package com.amobee.freebee.evaluator.index;

import com.amobee.freebee.evaluator.BEInterval;
import com.amobee.freebee.evaluator.interval.BEIntervalComparator;
import com.amobee.freebee.evaluator.interval.BENodeInterval;
import com.amobee.freebee.evaluator.interval.Interval;

import java.util.BitSet;
import java.util.Map;
import java.util.TreeMap;

/**
 * An internal helper class for converting {@link BENodeInterval}s
 * to the {@link BEInterval}s needed by a {@link BEIndex}.
 *
 * @author Kevin Doran
 */
class BEIndexIntervalMaker
{

    // Used to remove duplicate interval bit sets which is a significant memory reduction
    private final Map<Interval, BitSet> uniqueIntervalBitSets =
            new TreeMap<>(new BEIntervalComparator());

    BEInterval make(final int expressionId, final int intervalId, final boolean canUseBitSetMatching, final BENodeInterval interval)
    {
        final boolean isNegative = interval.getNode() != null && interval.getNode().isNegative();
        return new BEInterval(
                expressionId,
                intervalId,
                canUseBitSetMatching,
                isNegative,
                getIntervalBitSet(interval)
        );
    }


    /**
     * Gets the bit set that represents an interval. The resulting bit set is de-duplicated to reduce memory.
     *
     * @return Bit set representing the specified interval.
     */
    private BitSet getIntervalBitSet(final Interval interval)
    {

        return this.uniqueIntervalBitSets.computeIfAbsent(interval, intervalKey -> {
            final short start = intervalKey.getStart();
            final short end = intervalKey.getEnd();
            final BitSet bits = new BitSet(end);
            bits.set(start, end);
            return bits;
        });

    }


}
