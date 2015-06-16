package com.amobee.freebee.solr;

import javax.annotation.Nonnull;

import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.amobee.freebee.expression.BEAttributeValue;

/**
 * Convert a integer range string into a {@link Range} as integer values. The range string must be in the form "{start}"
 * or "{start}-{end}" where start and end must be valid double values.
 *
 * @author Michael Bond
 */
public class BESolrIntConverter extends BESolrRangeConverter<Integer>
{
    public BESolrIntConverter()
    {
    }

    @Override
    public void convertRange(@Nonnull final String start, @Nonnull final String end, @Nonnull final BEAttributeValue value, @Nonnull final RangeSet<Integer> ranges)
    {
        if (!end.isEmpty())
        {
            // convert range to [..)
            ranges.add(Range.closed(Integer.valueOf(start), Integer.valueOf(end)).canonical(DiscreteDomain.integers()));
        }
        else
        {
            // convert range to atLeast
            ranges.add(Range.atLeast(Integer.valueOf(start)));
        }
    }

    @Override
    public Integer getStart(@Nonnull final Range<Integer> range)
    {
        return range.lowerEndpoint();
    }

    @Override
    public Integer getEnd(@Nonnull final Range<Integer> range)
    {
        // convert range back to [..]
        if (range.hasUpperBound())
        {
            return Integer.valueOf(range.upperEndpoint().intValue() - 1);
        }
        else
        {
            return null;
        }
    }
}
