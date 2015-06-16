package com.amobee.freebee.evaluator.index;

import com.amobee.freebee.evaluator.BEInterval;

import java.util.BitSet;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import static org.junit.Assert.*;

public class BEAbstractRangeIndexAttributeCategoryTest
{

    @Test
    public void testByteRangeIndex()
    {
        final BEByteRangeIndexAttributeCategory indexAttributeCategory = new BEByteRangeIndexAttributeCategory();
        indexAttributeCategory.addInterval("[45,49]", interval(1));
        indexAttributeCategory.addInterval("[23,35]", interval(2));
        indexAttributeCategory.addInterval("[40,50]", interval(3));
        indexAttributeCategory.addInterval("[24,30]", interval(4));
        indexAttributeCategory.addInterval("[40,45]", interval(5));
        indexAttributeCategory.addInterval("[30,)", interval(6));
        indexAttributeCategory.addInterval("[19,25]", interval(7));
        indexAttributeCategory.addInterval("[26,48]", interval(8));
        indexAttributeCategory.addInterval("[22,52]", interval(9));
        indexAttributeCategory.addInterval("[2,]", interval(10));
        indexAttributeCategory.addInterval("[30,40]", interval(11));
        indexAttributeCategory.addInterval("[25,40]", interval(12));
        indexAttributeCategory.addInterval("[46,50]", interval(13));
        indexAttributeCategory.addInterval("[46,48)", interval(14));

        final Set<Integer> matchedIntervals = new HashSet<>();
        indexAttributeCategory.getIntervals((byte) 48, intervals -> intervals.forEach(i -> matchedIntervals.add(i.getIntervalId())));

        assertEquals(7, matchedIntervals.size());
        assertTrue(matchedIntervals.contains(1));
        assertTrue(matchedIntervals.contains(3));
        assertTrue(matchedIntervals.contains(6));
        assertTrue(matchedIntervals.contains(8));
        assertTrue(matchedIntervals.contains(9));
        assertTrue(matchedIntervals.contains(10));
        assertTrue(matchedIntervals.contains(13));

    }

    private BEInterval interval(final int id)
    {
        return new BEInterval(id, id, false, false, bitSet(id));
    }

    private BitSet bitSet(final int bitIndex)
    {
        final BitSet bitSet = new BitSet();
        bitSet.set(bitIndex);
        return bitSet;
    }
}