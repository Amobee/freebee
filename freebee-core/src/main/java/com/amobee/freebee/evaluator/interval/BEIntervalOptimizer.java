package com.amobee.freebee.evaluator.interval;

import java.util.Collection;

/**
 * An internal interface for assisting in determining if an
 * expression's intervals qualify for optimized evaluation
 * and matching algorithms.
 *
 * @author Kevin Doran
 */
public interface BEIntervalOptimizer
{
    boolean canUseBitSetMatching(int intervalLength, Collection<? extends Interval> intervals);
}
