package com.amobee.freebee.util;

import java.util.Arrays;
import java.util.List;

import org.joda.time.DateTimeConstants;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Michael Bond
 */
public class TimeUtilsTest
{
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormat.forPattern("yyyy-MM-dd");

    @Test
    public void testParseTime() throws Exception
    {
        assertEquals(32465, TimeUtils.parseTime("09:01:05"));
        assertEquals(32460, TimeUtils.parseTime("09:01"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseTime_EmptyString() throws Exception
    {
        TimeUtils.parseTime("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseTime_MissingColon() throws Exception
    {
        TimeUtils.parseTime("0901");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseTime_MissingMinutes() throws Exception
    {
        TimeUtils.parseTime("09:01:");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseTime_MissingSeconds() throws Exception
    {
        TimeUtils.parseTime("09:");
    }

    @Test
    public void testPrintTime() throws Exception
    {
        assertEquals("00:00:30", TimeUtils.printTime(30));
        assertEquals("00:01:15", TimeUtils.printTime(75));
        assertEquals("01:05:10", TimeUtils.printTime(3910));
    }

    @Test
    public void testGetIntervalDayOfWeekMask()
    {
        Interval interval = new Interval(
                DATE_FORMAT.parseDateTime("2015-02-09"),
                DATE_FORMAT.parseDateTime("2015-02-11"));
        assertEquals(
                1 << DateTimeConstants.MONDAY | 1 << DateTimeConstants.TUESDAY,
                TimeUtils.getIntervalDayOfWeekMask(interval));

        interval = new Interval(
                DATE_FORMAT.parseDateTime("2015-02-14"),
                DATE_FORMAT.parseDateTime("2015-02-16"));
        assertEquals(
                1 << DateTimeConstants.SATURDAY | 1 << DateTimeConstants.SUNDAY,
                TimeUtils.getIntervalDayOfWeekMask(interval));
    }

    @Test
    public void testGetTotalDays() throws Exception
    {
        final Interval interval1 = new Interval(
                DATE_FORMAT.parseDateTime("2015-02-09"),
                DATE_FORMAT.parseDateTime("2015-02-16"));
        final Interval interval2 = new Interval(
                DATE_FORMAT.parseDateTime("2015-02-16"),
                DATE_FORMAT.parseDateTime("2015-02-23"));

        assertEquals(
                4,
                TimeUtils.getTotalDays(
                        Arrays.asList(interval1, interval2),
                        1 << DateTimeConstants.MONDAY | 1 << DateTimeConstants.TUESDAY));
    }

    @Test
    public void testGetTotalDaysPartialFirstWeek() throws Exception
    {
        final Interval interval1 = new Interval(
                DATE_FORMAT.parseDateTime("2015-02-11"),
                DATE_FORMAT.parseDateTime("2015-02-16"));
        final Interval interval2 = new Interval(
                DATE_FORMAT.parseDateTime("2015-02-16"),
                DATE_FORMAT.parseDateTime("2015-02-22"));

        assertEquals(
                3,
                TimeUtils.getTotalDays(
                        Arrays.asList(interval1, interval2),
                        1 << DateTimeConstants.MONDAY | 1 << DateTimeConstants.WEDNESDAY));
    }

    @Test
    public void testGetTotalDaysPartialLastWeek() throws Exception
    {
        final Interval interval1 = new Interval(
                DATE_FORMAT.parseDateTime("2015-02-09"),
                DATE_FORMAT.parseDateTime("2015-02-16"));
        final Interval interval2 = new Interval(
                DATE_FORMAT.parseDateTime("2015-02-16"),
                DATE_FORMAT.parseDateTime("2015-02-17"));

        assertEquals(
                3,
                TimeUtils.getTotalDays(
                        Arrays.asList(interval1, interval2),
                        1 << DateTimeConstants.MONDAY | 1 << DateTimeConstants.WEDNESDAY));
    }

    @Test
    public void testGetTotalDaysPartialFirstAndLastWeek() throws Exception
    {
        final Interval interval1 = new Interval(
                DATE_FORMAT.parseDateTime("2015-02-11"),
                DATE_FORMAT.parseDateTime("2015-02-16"));
        final Interval interval2 = new Interval(
                DATE_FORMAT.parseDateTime("2015-02-16"),
                DATE_FORMAT.parseDateTime("2015-02-23"));
        final Interval interval3 = new Interval(
                DATE_FORMAT.parseDateTime("2015-02-23"),
                DATE_FORMAT.parseDateTime("2015-03-02"));
        final Interval interval4 = new Interval(
                DATE_FORMAT.parseDateTime("2015-03-02"),
                DATE_FORMAT.parseDateTime("2015-03-03"));

        assertEquals(
                6,
                TimeUtils.getTotalDays(
                        Arrays.asList(interval1, interval2, interval3, interval4),
                        1 << DateTimeConstants.MONDAY | 1 << DateTimeConstants.WEDNESDAY));
    }

    @Test
    public void testGetTotalDaysFullSingleWeek() throws Exception
    {
        final Interval interval1 = new Interval(
                DATE_FORMAT.parseDateTime("2015-02-09"),
                DATE_FORMAT.parseDateTime("2015-02-16"));

        assertEquals(
                2,
                TimeUtils.getTotalDays(
                        interval1,
                        1 << DateTimeConstants.MONDAY | 1 << DateTimeConstants.TUESDAY));
    }

    @Test
    public void testGetTotalDaysPartialSingleWeek() throws Exception
    {
        final Interval interval1 = new Interval(
                DATE_FORMAT.parseDateTime("2015-02-09"),
                DATE_FORMAT.parseDateTime("2015-02-11"));

        assertEquals(
                1,
                TimeUtils.getTotalDays(
                        interval1,
                        1 << DateTimeConstants.MONDAY | 1 << DateTimeConstants.SUNDAY));
    }

    @Test
    public void testGetTotalDaysStartingMidWeek() throws Exception
    {
        final Interval interval1 = new Interval(
                DATE_FORMAT.parseDateTime("2015-02-11"),
                DATE_FORMAT.parseDateTime("2015-02-16"));

        assertEquals(
                1,
                TimeUtils.getTotalDays(
                        interval1,
                        1 << DateTimeConstants.MONDAY | 1 << DateTimeConstants.SUNDAY));
    }

    @Test
    public void testGetPartialWeek() throws Exception
    {
        final List<Interval> weeks = TimeUtils.getWeeks(
                DATE_FORMAT.parseDateTime("2015-02-09"),
                DATE_FORMAT.parseDateTime("2015-02-10"));
        assertEquals(1, weeks.size());
    }

    @Test
    public void testGetFullWeeksStartingMidWeek() throws Exception
    {
        final List<Interval> weeks = TimeUtils.getWeeks(
                DATE_FORMAT.parseDateTime("2015-02-11"),
                DATE_FORMAT.parseDateTime("2015-02-25"));
        assertEquals(2, weeks.size());
        assertEquals(
                new Interval(DATE_FORMAT.parseDateTime("2015-02-11"), DATE_FORMAT.parseDateTime("2015-02-18")),
                weeks.get(0));
        assertEquals(
                new Interval(DATE_FORMAT.parseDateTime("2015-02-18"), DATE_FORMAT.parseDateTime("2015-02-25")),
                weeks.get(1));
    }

    @Test
    public void testGetFullAndPartialWeek() throws Exception
    {
        final List<Interval> weeks = TimeUtils.getWeeks(
                DATE_FORMAT.parseDateTime("2015-02-09"),
                DATE_FORMAT.parseDateTime("2015-02-20"));
        assertEquals(2, weeks.size());
        assertEquals(
                new Interval(DATE_FORMAT.parseDateTime("2015-02-09"), DATE_FORMAT.parseDateTime("2015-02-16")),
                weeks.get(0));
        assertEquals(
                new Interval(DATE_FORMAT.parseDateTime("2015-02-16"), DATE_FORMAT.parseDateTime("2015-02-20")),
                weeks.get(1));
    }

    @Test
    public void testGetMultipleFullWeeks() throws Exception
    {
        final List<Interval> weeks = TimeUtils.getWeeks(
                DATE_FORMAT.parseDateTime("2015-02-09"),
                DATE_FORMAT.parseDateTime("2015-02-23")); // date range is exclusive of end date
        assertEquals(2, weeks.size());
        assertEquals(
                new Interval(DATE_FORMAT.parseDateTime("2015-02-09"), DATE_FORMAT.parseDateTime("2015-02-16")),
                weeks.get(0));
        assertEquals(
                new Interval(DATE_FORMAT.parseDateTime("2015-02-16"), DATE_FORMAT.parseDateTime("2015-02-23")),
                weeks.get(1));

        // [2015-02-23, 2015-02-23) is not a valid range because it starts and ends on the same day, but the end of the
        // range is exclusive.
    }

    @Test
    public void testAlignToWeekBoundariesFullWeeksStartingOnMonday() throws Exception
    {
        final List<Interval> weeks = TimeUtils.getWeeks(
                DATE_FORMAT.parseDateTime("2015-02-09"),
                DATE_FORMAT.parseDateTime("2015-02-23"), // date range is exclusive of end date
                DateTimeConstants.MONDAY);
        assertEquals(2, weeks.size());
        assertEquals(
                new Interval(DATE_FORMAT.parseDateTime("2015-02-09"), DATE_FORMAT.parseDateTime("2015-02-16")),
                weeks.get(0));
        assertEquals(
                new Interval(DATE_FORMAT.parseDateTime("2015-02-16"), DATE_FORMAT.parseDateTime("2015-02-23")),
                weeks.get(1));

        // [2015-02-23, 2015-02-23) is not a valid range because it starts and ends on the same day, but the end of the
        // range is exclusive.
    }

    @Test
    public void testAlignToWeekBoundariesPartialFirstAndLastWeeksStartingBeforeStartDay() throws Exception
    {
        final List<Interval> weeks = TimeUtils.getWeeks(
                DATE_FORMAT.parseDateTime("2015-02-11"),
                DATE_FORMAT.parseDateTime("2015-02-25"), // date range is exclusive of end date
                DateTimeConstants.MONDAY);
        assertEquals(3, weeks.size());
        assertEquals(
                new Interval(DATE_FORMAT.parseDateTime("2015-02-11"), DATE_FORMAT.parseDateTime("2015-02-16")),
                weeks.get(0));
        assertEquals(
                new Interval(DATE_FORMAT.parseDateTime("2015-02-16"), DATE_FORMAT.parseDateTime("2015-02-23")),
                weeks.get(1));
        assertEquals(
                new Interval(DATE_FORMAT.parseDateTime("2015-02-23"), DATE_FORMAT.parseDateTime("2015-02-25")),
                weeks.get(2));
    }

    @Test
    public void testAlignToWeekBoundariesOnlyPartialWeekStartingBeforeStartDay() throws Exception
    {
        final List<Interval> weeks = TimeUtils.getWeeks(
                DATE_FORMAT.parseDateTime("2015-02-11"),
                DATE_FORMAT.parseDateTime("2015-02-13"), // date range is exclusive of end date
                DateTimeConstants.MONDAY);
        assertEquals(1, weeks.size());
        assertEquals(
                new Interval(DATE_FORMAT.parseDateTime("2015-02-11"), DATE_FORMAT.parseDateTime("2015-02-13")),
                weeks.get(0));
    }

    @Test
    public void testAlignToWeekBoundariesFullWeeksStartingOnThursday() throws Exception
    {
        final List<Interval> weeks = TimeUtils.getWeeks(
                DATE_FORMAT.parseDateTime("2015-02-12"),
                DATE_FORMAT.parseDateTime("2015-02-26"), // date range is exclusive of end date
                DateTimeConstants.THURSDAY);
        assertEquals(2, weeks.size());
        assertEquals(
                new Interval(DATE_FORMAT.parseDateTime("2015-02-12"), DATE_FORMAT.parseDateTime("2015-02-19")),
                weeks.get(0));
        assertEquals(
                new Interval(DATE_FORMAT.parseDateTime("2015-02-19"), DATE_FORMAT.parseDateTime("2015-02-26")),
                weeks.get(1));

        // [2015-02-23, 2015-02-23) is not a valid range because it starts and ends on the same day, but the end of the
        // range is exclusive.
    }

    @Test
    public void testAlignToWeekBoundariesPartialFirstAndLastWeeksStartingAfterStartDay() throws Exception
    {
        final List<Interval> weeks = TimeUtils.getWeeks(
                DATE_FORMAT.parseDateTime("2015-02-11"),
                DATE_FORMAT.parseDateTime("2015-02-25"), // date range is exclusive of end date
                DateTimeConstants.FRIDAY);
        assertEquals(3, weeks.size());
        assertEquals(
                new Interval(DATE_FORMAT.parseDateTime("2015-02-11"), DATE_FORMAT.parseDateTime("2015-02-13")),
                weeks.get(0));
        assertEquals(
                new Interval(DATE_FORMAT.parseDateTime("2015-02-13"), DATE_FORMAT.parseDateTime("2015-02-20")),
                weeks.get(1));
        assertEquals(
                new Interval(DATE_FORMAT.parseDateTime("2015-02-20"), DATE_FORMAT.parseDateTime("2015-02-25")),
                weeks.get(2));
    }

    @Test
    public void testAlignToWeekBoundariesOnlyPartialWeekStartingAfterStartDay() throws Exception
    {
        final List<Interval> weeks = TimeUtils.getWeeks(
                DATE_FORMAT.parseDateTime("2015-02-11"),
                DATE_FORMAT.parseDateTime("2015-02-13"), // date range is exclusive of end date
                DateTimeConstants.SATURDAY);
        assertEquals(1, weeks.size());
        assertEquals(
                new Interval(DATE_FORMAT.parseDateTime("2015-02-11"), DATE_FORMAT.parseDateTime("2015-02-13")),
                weeks.get(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidWeekStartDateAfterEndDate() throws Exception
    {
        TimeUtils.getWeeks(
                DATE_FORMAT.parseDateTime("2015-02-09"),
                DATE_FORMAT.parseDateTime("2015-02-08"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidWeekStartDateEqualsEndDate() throws Exception
    {
        TimeUtils.getWeeks(
                DATE_FORMAT.parseDateTime("2015-02-09"),
                DATE_FORMAT.parseDateTime("2015-02-09"));
    }

    @Test
    public void testInvalidZoneId()
    {
        assertEquals(null, TimeUtils.getZoneId("INVALID"));
    }

    @Test
    public void testZoneIdCaseInsensitive()
    {
        assertEquals("America/New_York", TimeUtils.getZoneId("aMeRiCa/nEw_yOrK").getId());
    }

    @Test
    public void testZoneIdCaseSensitive()
    {
        assertEquals("America/New_York", TimeUtils.getZoneId("America/New_York").getId());
    }

}
