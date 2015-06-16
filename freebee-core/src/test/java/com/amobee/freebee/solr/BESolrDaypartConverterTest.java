package com.amobee.freebee.solr;

import org.junit.Test;

import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;

import static org.junit.Assert.*;

public class BESolrDaypartConverterTest
{
    @Test
    public void testCalculateRangeOffsetNoOffset()
    {
        final BESolrDaypartConverter converter = new BESolrDaypartConverter(new BESolrDaypartRangeStoreImpl());

        // test no offset
        final RangeSet<Integer> ranges = TreeRangeSet.create();
        BESolrDaypartRangeStoreImpl.MORNING_RANGE.forEach(range -> converter.calculateRangeOffset(ranges, range, 0));
        assertEquals(1, ranges.asRanges().size());
        assertTrue(
                ranges.asRanges().contains(
                        Range.closedOpen(Integer.valueOf(6), Integer.valueOf(10)).canonical(
                                DiscreteDomain.integers())));
    }

    @Test
    public void testCalculateRangeOffsetNoWrap()
    {
        final BESolrDaypartConverter converter = new BESolrDaypartConverter(new BESolrDaypartRangeStoreImpl());

        // test bounds no wrapping
        final RangeSet<Integer> ranges = TreeRangeSet.create();
        BESolrDaypartRangeStoreImpl.MORNING_RANGE.forEach(range -> converter.calculateRangeOffset(ranges, range, -3));
        assertEquals(1, ranges.asRanges().size());
        assertTrue(
                ranges.asRanges().contains(
                        Range.closedOpen(Integer.valueOf(9), Integer.valueOf(13)).canonical(
                                DiscreteDomain.integers())));
    }

    @Test
    public void testCalculateRangeOffsetUpperWrapDownLowerWrapDown()
    {
        final BESolrDaypartConverter converter = new BESolrDaypartConverter(new BESolrDaypartRangeStoreImpl());

        // test lower bounds wraps down, upper bounds wraps down
        final RangeSet<Integer> ranges = TreeRangeSet.create();
        BESolrDaypartRangeStoreImpl.MORNING_RANGE.forEach(range -> converter.calculateRangeOffset(ranges, range, 11));
        assertEquals(1, ranges.asRanges().size());
        assertTrue(
                ranges.asRanges().contains(
                        Range.closedOpen(Integer.valueOf(19), Integer.valueOf(23)).canonical(
                                DiscreteDomain.integers())));
    }

    @Test
    public void testCalculateRangeOffsetUpperWrapUpLowerWrapUp()
    {
        final BESolrDaypartConverter converter = new BESolrDaypartConverter(new BESolrDaypartRangeStoreImpl());

        // test upper bounds wraps up, lower bounds wraps up
        final RangeSet<Integer> ranges = TreeRangeSet.create();
        BESolrDaypartRangeStoreImpl.PRIME_RANGE.forEach(range -> converter.calculateRangeOffset(ranges, range, -4));
        assertEquals(1, ranges.asRanges().size());
        assertTrue(
                ranges.asRanges().contains(
                        Range.closedOpen(Integer.valueOf(0), Integer.valueOf(3)).canonical(
                                DiscreteDomain.integers())));
    }

    @Test
    public void testCalculateRangeOffsetUpperNoWrapLowerWrapDown()
    {
        final BESolrDaypartConverter converter = new BESolrDaypartConverter(new BESolrDaypartRangeStoreImpl());

        // test lower bounds wraps down, upper bounds does not
        final RangeSet<Integer> ranges = TreeRangeSet.create();
        BESolrDaypartRangeStoreImpl.MORNING_RANGE.forEach(range -> converter.calculateRangeOffset(ranges, range, 8));
        assertEquals(2, ranges.asRanges().size());
        assertTrue(
                ranges.asRanges().contains(
                        Range.closedOpen(Integer.valueOf(22), Integer.valueOf(24)).canonical(
                                DiscreteDomain.integers())));
        assertTrue(
                ranges.asRanges().contains(
                        Range.closedOpen(Integer.valueOf(0), Integer.valueOf(2)).canonical(
                                DiscreteDomain.integers())));
    }

    @Test
    public void testCalculateRangeOffsetUpperWrapUpLowerNoWrap()
    {
        final BESolrDaypartConverter converter = new BESolrDaypartConverter(new BESolrDaypartRangeStoreImpl());

        // test upper bounds wraps up, lower bounds does not
        final RangeSet<Integer> ranges = TreeRangeSet.create();
        BESolrDaypartRangeStoreImpl.PRIME_RANGE.forEach(range -> converter.calculateRangeOffset(ranges, range, -2));
        assertEquals(2, ranges.asRanges().size());
        assertTrue(
                ranges.asRanges().contains(
                        Range.closedOpen(Integer.valueOf(22), Integer.valueOf(24)).canonical(
                                DiscreteDomain.integers())));
        assertTrue(
                ranges.asRanges().contains(
                        Range.closedOpen(Integer.valueOf(0), Integer.valueOf(1)).canonical(
                                DiscreteDomain.integers())));
    }

    @Test
    public void testCalculateRangeOffUpperIsMidnight()
    {
        final BESolrDaypartConverter converter = new BESolrDaypartConverter(new BESolrDaypartRangeStoreImpl());

        // test upper bounds wraps up, lower bounds does not
        final RangeSet<Integer> ranges = TreeRangeSet.create();
        BESolrDaypartRangeStoreImpl.PRIME_RANGE.forEach(range -> converter.calculateRangeOffset(ranges, range, -1));
        assertEquals(1, ranges.asRanges().size());
        assertTrue(
                ranges.asRanges().contains(
                        Range.closedOpen(Integer.valueOf(21), Integer.valueOf(24)).canonical(
                                DiscreteDomain.integers())));
    }

    @Test
    public void testCalculateRangeOffsetLowerIsElevenPM()
    {
        final BESolrDaypartConverter converter = new BESolrDaypartConverter(new BESolrDaypartRangeStoreImpl());

        // test lower bounds wraps down, upper bounds does not
        final RangeSet<Integer> ranges = TreeRangeSet.create();
        BESolrDaypartRangeStoreImpl.MORNING_RANGE.forEach(range -> converter.calculateRangeOffset(ranges, range, 7));
        assertEquals(2, ranges.asRanges().size());
        assertTrue(
                ranges.asRanges().contains(
                        Range.closedOpen(Integer.valueOf(23), Integer.valueOf(24)).canonical(
                                DiscreteDomain.integers())));
        assertTrue(
                ranges.asRanges().contains(
                        Range.closedOpen(Integer.valueOf(0), Integer.valueOf(3)).canonical(
                                DiscreteDomain.integers())));
    }
}
