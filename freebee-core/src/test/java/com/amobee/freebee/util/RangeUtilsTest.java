package com.amobee.freebee.util;

import org.junit.Test;

import com.google.common.collect.Range;

import static org.junit.Assert.*;

/**
 * @author Michael Bond
 */
public class RangeUtilsTest
{
    @Test
    public void testCreateRange() throws Exception
    {
        assertEquals(
                Range.closed(Integer.valueOf(12), Integer.valueOf(16)),
                RangeUtils.createRange("[12,16]", Integer::valueOf));
        assertEquals(
                Range.closedOpen(Integer.valueOf(12), Integer.valueOf(16)),
                RangeUtils.createRange("[12,16)", Integer::valueOf));
        assertEquals(
                Range.open(Integer.valueOf(12), Integer.valueOf(16)),
                RangeUtils.createRange("(12,16)", Integer::valueOf));
        assertEquals(
                Range.openClosed(Integer.valueOf(12), Integer.valueOf(16)),
                RangeUtils.createRange("(12,16]", Integer::valueOf));
        assertEquals(
                Range.atLeast(Integer.valueOf(12)),
                RangeUtils.createRange("[12,]", Integer::valueOf));
        assertEquals(
                Range.greaterThan(Integer.valueOf(12)),
                RangeUtils.createRange("(12,]", Integer::valueOf));
        assertEquals(
                Range.atMost(Integer.valueOf(16)),
                RangeUtils.createRange("[,16]", Integer::valueOf));
        assertEquals(
                Range.lessThan(Integer.valueOf(16)),
                RangeUtils.createRange("[,16)", Integer::valueOf));
        assertEquals(
                Range.all(),
                RangeUtils.createRange("[,)", Integer::valueOf));
        assertEquals(
                Range.closed(Integer.valueOf(12), Integer.valueOf(12)),
                RangeUtils.createRange("12", Integer::valueOf));

    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateRangeMissingStartEndpoint() throws Exception
    {
        RangeUtils.createRange("12,16]", Integer::valueOf);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateRangeMissingEndEndpoint() throws Exception
    {
        RangeUtils.createRange("[12,16", Integer::valueOf);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateRangeMissingEndpoints() throws Exception
    {
        RangeUtils.createRange("12,16", Integer::valueOf);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateRangeInvalidStartEndpoint() throws Exception
    {
        RangeUtils.createRange("[12", Integer::valueOf);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateRangeInvalidEndEndpoint() throws Exception
    {
        RangeUtils.createRange("12]", Integer::valueOf);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateRangeInvalidEndpoints() throws Exception
    {
        RangeUtils.createRange("[12]", Integer::valueOf);
    }

    @Test
    public void testCreateClosedRange()
    {
        final String inputRange = "12-14";
        final char delim = '-';

        final Range<Integer> range = RangeUtils.createClosedRange(inputRange, delim);
        assertNotNull(range);
        assertEquals(new Integer(12), range.lowerEndpoint());
        assertEquals(new Integer(14), range.upperEndpoint());
    }

    @Test
    public void testCreateClosedRange_invalid()
    {
        final Range<Integer> range = RangeUtils.createClosedRange(null, '-');
        assertNull(range);
    }
}