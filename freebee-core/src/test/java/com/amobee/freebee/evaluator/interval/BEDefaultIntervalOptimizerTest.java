package com.amobee.freebee.evaluator.interval;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.Test;

import static org.junit.Assert.*;

public class BEDefaultIntervalOptimizerTest
{

    private static final int WIDTH = 4;


    @Test(expected = IllegalArgumentException.class)
    public void canUseBitSetMatchingDoesNotAcceptNull()
    {
        new BEDefaultIntervalOptimizer().canUseBitSetMatching(WIDTH, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void canUseBitSetMatchingDoesNotAcceptEmpty()
    {
        new BEDefaultIntervalOptimizer().canUseBitSetMatching(WIDTH, intervals());
    }

    @Test(expected = IllegalArgumentException.class)
    public void canUseBitSetMatchingDoesNotAcceptNegativeStart()
    {
        new BEDefaultIntervalOptimizer().canUseBitSetMatching(WIDTH, intervals(-1, 4));
    }

    @Test(expected = IllegalArgumentException.class)
    public void canUseBitSetMatchingDoesNotAcceptZeroEnd()
    {
        new BEDefaultIntervalOptimizer().canUseBitSetMatching(WIDTH, intervals(0, 0));
    }

    @Test
    public void canUseBitSetMatchingTest()
    {
        // Arrange
        final BEIntervalOptimizer intervalOptimizer = new BEDefaultIntervalOptimizer();

        // Act & Assert
        assertTrue(intervalOptimizer.canUseBitSetMatching(WIDTH, intervals(0,4)));
        assertTrue(intervalOptimizer.canUseBitSetMatching(WIDTH, intervals(0,1,  1,4)));
        assertTrue(intervalOptimizer.canUseBitSetMatching(WIDTH, intervals(0,2,  2,4)));
        assertTrue(intervalOptimizer.canUseBitSetMatching(WIDTH, intervals(0,3,  3,4)));
        assertTrue(intervalOptimizer.canUseBitSetMatching(WIDTH, intervals(0,1,  1,2,  2,4)));
        assertTrue(intervalOptimizer.canUseBitSetMatching(WIDTH, intervals(0,1,  1,2,  2,4)));
        assertTrue(intervalOptimizer.canUseBitSetMatching(WIDTH, intervals(0,1,  1,2,  2,4)));
        assertTrue(intervalOptimizer.canUseBitSetMatching(WIDTH, intervals(0,1,  1,2,  2,3,  3,4)));
        assertTrue(intervalOptimizer.canUseBitSetMatching(WIDTH, intervals(0,4,  0,4)));
        assertTrue(intervalOptimizer.canUseBitSetMatching(WIDTH, intervals(0,2,  0,2,  2,4,  2,4)));

        assertFalse(intervalOptimizer.canUseBitSetMatching(WIDTH, intervals(0,4,  0,1,  1,4)));
        assertFalse(intervalOptimizer.canUseBitSetMatching(WIDTH, intervals(0,1,  1,4,  0,2,  2,4)));
        assertFalse(intervalOptimizer.canUseBitSetMatching(WIDTH, intervals(0,3,  3,4,  1,3)));
    }


    // --- Helper methods ---

    private static Collection<Interval> intervals(int... intervals) {
        if (intervals.length % 2 != 0)
        {
            throw new IllegalArgumentException("must pass pairs of numbers in the form [start1, end1, start2, end2, ...]");
        }

        final Collection<Interval> intervalCollection = new ArrayList<>();
        for (int i = 0; i < intervals.length; i += 2) {
            intervalCollection.add(interval(intervals[i], intervals[i+1]));
        }
        return intervalCollection;
    }

    private static Interval interval(int start, int end)
    {
        return new BESimpleInterval((short) start, (short) end);
    }

}