package com.amobee.freebee.evaluator.index;

import com.amobee.freebee.evaluator.BEInterval;
import com.amobee.freebee.evaluator.evaluator.BEInputAttributeCategory;

import java.util.List;

/**
 * An internal interface called during matching an input against indexed results.
 */
@FunctionalInterface
public interface BEAttributeCategoryMatchedIntervalConsumer
{

    /**
     * Interface to call when a match between an input and index is detected.
     *
     * @param inputAttributeCategory The input attribute category value(s) that resulted in the match.
     *                               Can be null if input tracking is disabled.
     * @param matchedIntervals The matched intervals in the index for the given input vlaue(s).
     */
    void accept(BEInputAttributeCategory inputAttributeCategory, List<BEInterval> matchedIntervals);

}
