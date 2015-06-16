package com.amobee.freebee.solr;

import java.time.DateTimeException;
import java.time.zone.ZoneRulesException;
import java.util.List;
import javax.annotation.Nonnull;

import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.amobee.freebee.expression.BEAttributeValue;
import com.amobee.freebee.util.TimeUtils;

/**
 * Convert a daypart range string to a {@link Range}. The range string can either be a single daypart, a single hour,
 * or a range of hours in the form "{start}-{end}".
 *
 * @author Michael Bond
 */
@SuppressWarnings("unchecked")
public class BESolrDaypartConverter extends BESolrIntConverter
{
    private final BESolrDaypartRangeStore daypartRangeStore;

    public BESolrDaypartConverter(final BESolrDaypartRangeStore daypartRangeStore)
    {
        this.daypartRangeStore = daypartRangeStore;
    }

    @Override
    public void convertRange(
            @Nonnull final String start,
            @Nonnull final String end,
            @Nonnull final BEAttributeValue value,
            @Nonnull final RangeSet<Integer> ranges)
    {
        final List<Range<Integer>> daypartRanges = this.daypartRangeStore.getDaypartRange(start.toLowerCase());

        // determine UTC hour range from given Timezone
        String timezone = "";
        if (value.hasProperty("tz"))
        {
            timezone = String.valueOf(value.getProperty("tz"));
        }
        else
        {
            throw new IllegalArgumentException("Daypart Timezone not defined");
        }

        try
        {
            // determine timezone hour offset from UTC
            final int hourOffset = TimeUtils.calculateTimezoneOffset(timezone);

            // if start is not a known daypart, assume it is a range of hours
            if (daypartRanges == null)
            {
                super.convertRange(
                        String.valueOf(TimeUtils.findHourFromOffset(Integer.valueOf(start), hourOffset)),
                        String.valueOf(TimeUtils.findHourFromOffset(Integer.valueOf(end), hourOffset)),
                        value, ranges);
            }
            // don't allow end to have been specified for dayparts
            else //noinspection StringEquality
                if (end != start)
            {
                throw new IllegalArgumentException("Dayparts cannot be used in a range");
            }
            else
            {
                // convert ranges to UTC based hours
                daypartRanges.forEach(range -> calculateRangeOffset(ranges, range, hourOffset));
            }
        } catch (ZoneRulesException e)
        {
            throw new IllegalArgumentException("Daypart Timezone is invalid");
        } catch (DateTimeException e)
        {
            throw new IllegalArgumentException("Daypart Timezone is invalid");
        }
    }

    public void calculateRangeOffset(final RangeSet<Integer> ranges, final Range<Integer> range, final int hourOffset)
    {
        final int lower = TimeUtils.findHourFromOffset(range.lowerEndpoint(), hourOffset);
        final int upper = TimeUtils.findHourFromOffset(range.upperEndpoint(), hourOffset);

        if (lower > upper)
        {
            ranges.add(Range.closedOpen(lower, 24).canonical(
                            DiscreteDomain.integers()));
            ranges.add(Range.closedOpen(0, upper).canonical(
                            DiscreteDomain.integers()));
        } else
        {
            ranges.add(
                    Range.closedOpen(lower, upper).canonical(
                            DiscreteDomain.integers()));
        }
    }
}
