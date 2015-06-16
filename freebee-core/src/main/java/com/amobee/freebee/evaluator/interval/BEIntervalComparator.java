package com.amobee.freebee.evaluator.interval;

import java.util.Comparator;

/**
 * Compares two {@link Interval}s based on their start and end indices.
 *
 * @author Kevin Doran
 */
public class BEIntervalComparator implements Comparator<Interval>
{
    @Override
    @SuppressWarnings("checkstyle:ReturnCount")
    public int compare(final Interval interval1, final Interval interval2)
    {
        if (interval1.getStart() != interval2.getStart())
        {
            return interval1.getStart() - interval2.getStart();
        }

        if (interval1.getEnd() != interval2.getEnd())
        {
            return interval1.getEnd() - interval2.getEnd();
        }

        return 0;  // start and end match, intervals are equal
    }
}
