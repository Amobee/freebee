package com.amobee.freebee.evaluator.interval;

/**
 * A representation of an interval as a start (inclusive) and end (exclusive) index.
 *
 * @author Kevin Doran
 */
public interface Interval
{
    /**
     * @return The start of the interval (inclusive).
     */
    short getStart();

    /**
     * @return The start of the interval (exclusive).
     */
    short getEnd();

}
