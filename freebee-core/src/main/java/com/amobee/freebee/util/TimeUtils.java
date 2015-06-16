package com.amobee.freebee.util;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;

import org.eclipse.collections.api.map.primitive.MutableObjectIntMap;
import org.eclipse.collections.impl.map.mutable.primitive.ObjectIntHashMap;
import org.joda.time.DateTimeFieldType;
import org.joda.time.Interval;
import org.joda.time.MutableDateTime;
import org.joda.time.ReadableInstant;

/**
 * @author Michael Bond
 */
public class TimeUtils
{
    public static final char TIME_SEPARATOR = ':';
    public static final int SECONDS_IN_HOUR = 3600;
    public static final int SECONDS_IN_MINUTE = 60;

    private static final MutableObjectIntMap<String> TIMEZONE_OFFSET_MAP = new ObjectIntHashMap<String>().asSynchronized();

    private static final Map<String, ZoneId> AVAILABLE_ZONE_ID_MAP = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    static
    {
        ZoneId.getAvailableZoneIds().forEach(zone -> AVAILABLE_ZONE_ID_MAP.put(zone, ZoneId.of(zone)));
    }

    /**
     * Converts a string in time escape format to time in seconds.
     *
     * @param s
     *         Time in format "hh:mm[:ss]"
     * @return Time in seconds
     * @throws IllegalArgumentException
     *         if the string is not in the expected format
     */
    public static long parseTime(@Nonnull final String s)
    {
        final int lastChar = s.length() - 1;
        final int firstColon = s.indexOf(TIME_SEPARATOR);
        if (firstColon > 0 && firstColon < lastChar)
        {
            final int hour = Integer.parseInt(s.substring(0, firstColon));
            final int secondColon = s.indexOf(TIME_SEPARATOR, firstColon + 1);

            if (secondColon > 0)
            {
                if (secondColon < lastChar)
                {
                    final int minute = Integer.parseInt(s.substring(firstColon + 1, secondColon));
                    final int second = Integer.parseInt(s.substring(secondColon + 1));
                    return TimeUnit.HOURS.toSeconds(hour) + TimeUnit.MINUTES.toSeconds(minute) + second;
                }
            }
            else
            {
                final int minute = Integer.parseInt(s.substring(firstColon + 1));
                return TimeUnit.HOURS.toSeconds(hour) + TimeUnit.MINUTES.toSeconds(minute);
            }
        }

        throw new IllegalArgumentException();
    }

    /**
     * Converts time in seconds to standard time format "hh:mm:ss"
     *
     * @param seconds
     *         Time in seconds
     * @return Time formatted as string
     */
    public static String printTime(final long seconds)
    {
        return String.format(
                "%02d:%02d:%02d",
                Long.valueOf(seconds / SECONDS_IN_HOUR),
                Long.valueOf(seconds % SECONDS_IN_HOUR / SECONDS_IN_MINUTE),
                Long.valueOf(seconds % SECONDS_IN_MINUTE));
    }

    /**
     * Get a mask with bits set for each day of the week represented within the provided interval. Intervals are
     * expected to be in the form [..).
     *
     * @param interval
     *         Interval to get day of the week mask for
     * @return Day of the week mask.
     */
    public static int getIntervalDayOfWeekMask(@Nonnull final Interval interval)
    {
        final MutableDateTime periodCurrent = new MutableDateTime(interval.getStart());
        final ReadableInstant periodEnd = interval.getEnd();
        int mask = 0;

        while (periodCurrent.compareTo(periodEnd) < 0)
        {
            mask |= 1 << periodCurrent.getDayOfWeek();
            periodCurrent.addDays(1);
        }

        return mask;
    }

    /**
     * Get the total number of days in a list of intervals accounting for desired days of week.
     *
     * NOTE - This assumes that avails will be spread evenly across all eligible days.
     *
     * @param weeks
     *         Week intervals
     * @param dayOfWeekMask
     *         Day of week bitmask using day of week values found in {@link org.joda.time.DateTimeConstants}.
     * @return Total number of days that should get avails.
     */
    public static int getTotalDays(
            @Nonnull final List<Interval> weeks,
            final int dayOfWeekMask)
    {
        // calculate the total number of days that have been chosen each week based on number of non-zero bits
        final int totalDaysPerWeek = Integer.bitCount(dayOfWeekMask);

        // calculate number of days in the first week
        int totalDays = getTotalDays(weeks.get(0), dayOfWeekMask);
        final int numWeeks = weeks.size();

        // if more than 2 weeks, calculate the total days for all middle weeks, as they are guaranteed to be full weeks
        if (numWeeks > 2)
        {
            totalDays += (numWeeks - 2) * totalDaysPerWeek;
        }

        // if more than one week, add the days for the final week
        if (numWeeks > 1)
        {
            totalDays += getTotalDays(weeks.get(numWeeks - 1), dayOfWeekMask);
        }

        return totalDays;
    }

    /**
     * Get the total number of days in an interval accounting for desired days of week.
     *
     * NOTE - This assumes that avails will be spread evenly across all eligible days.
     *
     * @param week
     *         Week interval
     * @param dayOfWeekMask
     *         Day of week bitmask using day of week values found in {@link org.joda.time.DateTimeConstants}.
     * @return Total number of days that should get avails.
     */
    public static int getTotalDays(
            @Nonnull final Interval week,
            final int dayOfWeekMask)
    {
        return Integer.bitCount(dayOfWeekMask & getIntervalDayOfWeekMask(week));
    }

    /**
     * Split the provided date range into seven day intervals in the interval form [..) for each week. The weeks will be
     * split on the start date's day of the week, so that every interval will be 7 days with the exception of the last
     * interval, which may be a partial week.
     *
     * @param startDate
     *         First day in the date range.
     * @param endDate
     *         Last day in the date range.
     * @return List of one week intervals, the last week may not consist of a full week.
     * @throws IllegalArgumentException
     *         if the start date is on or after the end date
     */
    public static List<Interval> getWeeks(
            @Nonnull final ReadableInstant startDate,
            @Nonnull final ReadableInstant endDate)
    {
        return getWeeks(startDate, endDate, startDate.get(DateTimeFieldType.dayOfWeek()));
    }

    /**
     * Split the provided date range into intervals in the interval form [..) for each week. The week intervals will
     * start and end on the specified start day of the week, which references the day of week constants found in
     * {@link org.joda.time.DateTimeConstants}.
     *
     * @param startDate
     *         First day in the date range.
     * @param endDate
     *         Last day in the date range.
     * @param startDayOfWeek
     *         The day of the week to split the weeks on.
     * @return List of one week intervals, the last week may not consist of a full week.
     * @throws IllegalArgumentException
     *         if the start date is on or after the end date
     */
    public static List<Interval> getWeeks(
            @Nonnull final ReadableInstant startDate,
            @Nonnull final ReadableInstant endDate,
            final int startDayOfWeek)
    {

        // make sure start date is not on or after end date
        if (!endDate.isAfter(startDate))
        {
            throw new IllegalArgumentException("Start date (" + startDate + ") must precede end date (" + endDate + ")");
        }

        final List<Interval> weekPeriods = new ArrayList<>();
        final MutableDateTime periodStartDate = new MutableDateTime(startDate);
        final MutableDateTime periodEndDate = new MutableDateTime(startDate);

        // if start date occurs before the specified start day of the week, set the first period's end date to this week's start/end boundary day
        if (periodStartDate.getDayOfWeek() < startDayOfWeek)
        {
            // interval is exclusive of end date, and thus, the day is equal to the specified start day in the the current week
            periodEndDate.setDayOfWeek(startDayOfWeek);
        }

        // otherwise, the start date occurs after the specified start day of the week, so we need to set the end date to next week's start/end boundary day
        else
        {
            periodEndDate.addDays(7);
            // interval is exclusive of end date, and thus, the day is equal to the specified start day in the next week
            periodEndDate.setDayOfWeek(startDayOfWeek);
        }

        // every interval after that (with the possible exception of the last interval) will be a full 7 day period
        while (periodEndDate.compareTo(endDate) <= 0)
        {
            weekPeriods.add(new Interval(periodStartDate, periodEndDate));
            periodStartDate.setDate(periodEndDate);
            periodEndDate.addDays(7);
        }

        // if the end date is not on an even week boundary add the remaining days to a separate period
        if (!periodStartDate.isEqual(endDate) && periodEndDate.compareTo(endDate) > 0)
        {
            weekPeriods.add(new Interval(periodStartDate, endDate));
        }

        return weekPeriods;
    }

    /**
     * Find the offset in hours from UTC for the given timezone
     *
     * @param timezone
     *         Timezone to calculate offset for.
     * @return Offset in hours from UTC for the given timezone.
     */
    public static int calculateTimezoneOffset(final String timezone)
    {
        final int offset;
        if (TIMEZONE_OFFSET_MAP.containsKey(timezone))
        {
            offset = TIMEZONE_OFFSET_MAP.get(timezone);
        }
        else
        {
            final ZoneId zone = ZoneId.of(timezone);
            final ZonedDateTime now = ZonedDateTime.now(zone);
            final ZoneOffset zoneOffset = now.getOffset();
            offset = (int) TimeUnit.SECONDS.toHours(zoneOffset.getTotalSeconds());
            TIMEZONE_OFFSET_MAP.put(timezone, offset);
        }

        return offset;
    }

    /**
     * Determine the ZoneId for a given case insensitive timezone string.
     * @param timezone String value of Timezone
     * @return {@link ZoneId} if given string is valid timezone else null.
     */
    public static ZoneId getZoneId(@Nonnull final String timezone)
    {
        return AVAILABLE_ZONE_ID_MAP.get(timezone);
    }

    /**
     * Determine the new hour on a 0 to 23 hour clock when applying an offset to a given hour
     *
     * @param hour an hour between 0 and 23
     * @param offset positive or negative offset to apply to hour
     * @return new hour between 0 and 23 based on offset, wraps when offset takes hour outside of 0 to 23 range
     */
    public static int findHourFromOffset(final int hour, final int offset)
    {
        int result = hour - offset;
        if (result < 0)
        {
            result = 24 + result;
        } else if (result > 23)
        {
            result = result - 24;
        }
        return result;
    }

    public static long getCurrentTimeMillisSince(final long baseTime)
    {
        return System.currentTimeMillis() - baseTime;
    }

    public static long getCurrentTimeSecondsSince(final long baseTime)
    {
        return TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - baseTime);
    }

    public static short getCurrentUTCOffsetInMinutes()
    {
        return (short) TimeUnit.SECONDS.toMinutes(ZonedDateTime.now().getOffset().getTotalSeconds());
    }

    private TimeUtils()
    {
    }
}
