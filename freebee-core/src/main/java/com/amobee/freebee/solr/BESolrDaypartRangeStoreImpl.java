package com.amobee.freebee.solr;

import java.util.List;
import java.util.Map;

import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;

/**
 * Default definition of Daypart names and ranges
 *
 * Daypart names are case insensitive. They are taken from the Nielsen TV set of daypart names and include:
 *
 * Weekday Morning: 6:00 AM - 10:00 AM
 * Weekday Daytime: 10:00 AM - 6:00 PM
 * Primetime: 8:00 PM - 11:00 PM
 * Early Fringe: 6:00 PM - 8:00 PM
 * Late Fringe: 11:00 PM - 6:00 AM
 * Weekend Daytime: 6:00 AM - 6:00 PM
 *
 * @author Ryan Ambrose
 */
public class BESolrDaypartRangeStoreImpl implements BESolrDaypartRangeStore
{
    public static final List<Range<Integer>> MORNING_RANGE = Lists.<Range<Integer>>newArrayList(
            Range.closedOpen(Integer.valueOf(6), Integer.valueOf(10)).canonical(DiscreteDomain.integers()));

    public static final List<Range<Integer>> DAYTIME_RANGE = Lists.<Range<Integer>>newArrayList(
            Range.closedOpen(Integer.valueOf(10), Integer.valueOf(18)).canonical(DiscreteDomain.integers()));

    public static final List<Range<Integer>> PRIME_RANGE = Lists.<Range<Integer>>newArrayList(
            Range.closedOpen(Integer.valueOf(20), Integer.valueOf(23)).canonical(DiscreteDomain.integers()));

    public static final List<Range<Integer>> EARLY_FRINGE_RANGE = Lists.<Range<Integer>>newArrayList(
            Range.closedOpen(Integer.valueOf(18), Integer.valueOf(20)).canonical(DiscreteDomain.integers()));

    public static final List<Range<Integer>> LATE_FRINGE_RANGE = Lists.<Range<Integer>>newArrayList(
            Range.closedOpen(Integer.valueOf(23), Integer.valueOf(24)).canonical(DiscreteDomain.integers()),
            Range.closedOpen(Integer.valueOf(0), Integer.valueOf(6)).canonical(DiscreteDomain.integers()));

    public static final List<Range<Integer>> WEEKEND_DAYTIME_RANGE = Lists.<Range<Integer>>newArrayList(
            Range.closedOpen(Integer.valueOf(6), Integer.valueOf(18)).canonical(DiscreteDomain.integers()));

    private final Map<String, List<Range<Integer>>> dayparts = ImmutableMap.<String, List<Range<Integer>>>builder()
            .put("wkm", MORNING_RANGE)
            .put("wkd", DAYTIME_RANGE)
            .put("pri", PRIME_RANGE)
            .put("ef", EARLY_FRINGE_RANGE)
            .put("lf", LATE_FRINGE_RANGE)
            .put("wed", WEEKEND_DAYTIME_RANGE)
            .build();

    @Override
    public List<Range<Integer>> getDaypartRange(final String daypart)
    {
        return this.dayparts.get(daypart.toLowerCase());
    }
}
