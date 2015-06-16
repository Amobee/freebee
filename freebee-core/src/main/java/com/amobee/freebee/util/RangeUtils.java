package com.amobee.freebee.util;

import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.BoundType;
import com.google.common.collect.Range;
import org.apache.commons.lang3.StringUtils;

import static java.lang.Integer.parseInt;

/**
 * @author Michael Bond
 */
public class RangeUtils
{
    private static final char RANGE_DELIMITER = ',';

    /**
     * Converts a string containing either a discrete value or standard mathematical notation to a {@link Range}.
     *
     * Examples:
     * Range "[12,16]" returns Range.closed(12, 16)
     * Range "[24,16)" returns Range.closedOpen(16, 24)
     * Range "(12,16)" returns Range.open(12, 16)
     * Range "(12,16]" returns Range.openClosed(12, 16)
     * Range "[12,]" returns Range.atLeast(12)
     * Range "(12,]" returns Range.greaterThan(12)
     * Range "[,16]" returns Range.atMost(16)
     * Range "[,16)" returns Range.lessThan(16)
     * Range "[,]" returns Range.all() - True for any variation of endpoints
     * Range "12" returns Range.closed(12, 12)
     * Range "12,16" throws IllegalArgumentException - True if either or both interval endpoints are missing
     * Range "[12)" throws IllegalArgumentException - True for a discrete value with any interval endpoints
     * Range "" throws IllegalArgumentException
     *
     * @param range
     *         Range expressed by Delimited-string
     * @param function
     *         Function to convert String to desired range type, for instance Byte::valueOf
     * @return New {@link Range} that represents the input range.
     * @throws NumberFormatException
     *         if input is invalid
     * @see <a href="https://en.wikipedia.org/wiki/Interval_(mathematics)">https://en.wikipedia.org/wiki/Interval_(mathematics)</a>
     */
    @SuppressWarnings("EnumSwitchStatementWhichMissesCases")
    @Nonnull
    public static <T extends Comparable<T>> Range<T> createRange(
            @Nonnull final String range,
            @Nonnull final Function<String, T> function)
    throws IllegalArgumentException
    {
        final int length = range.length();

        if (length == 0)
        {
            throw new IllegalArgumentException("range must not be empty");
        }

        final BoundType startBoundType = getStartBoundType(range.charAt(0));
        final BoundType endBoundType = getEndBoundType(range.charAt(length - 1));
        final int delimIndex = range.indexOf(RANGE_DELIMITER);
        final Range<T> result;

        if (-1 == delimIndex)
        {
            if (null != startBoundType || null != endBoundType)
            {
                throw new IllegalArgumentException("Constant must not contain endpoints for range=" + range);
            }
            final T discrete = function.apply(range);
            result = Range.closed(discrete, discrete);
        }
        else if (null == startBoundType)
        {
            throw new IllegalArgumentException("Invalid or missing start endpoint for range=" + range);
        }
        else if (null == endBoundType)
        {
            throw new IllegalArgumentException("Invalid or missing end endpoint for range=" + range);
        }
        else
        {
            final String startString = range.substring(1, delimIndex);
            final String endString = range.substring(delimIndex + 1, length - 1);

            if (startString.isEmpty())
            {
                if (endString.isEmpty())
                {
                    result = Range.all();
                }
                else
                {
                    result = Range.upTo(function.apply(endString), endBoundType);
                }
            }
            else if (endString.isEmpty())
            {
                result = Range.downTo(function.apply(startString), startBoundType);
            }
            else
            {
                result = Range.range(
                        function.apply(startString),
                        startBoundType,
                        function.apply(endString),
                        endBoundType);
            }
        }

        return result;
    }

    /**
     * Returns an Integer range that contains all values strictly greater than or equal to
     * lower and less than or equal to upper value in the delimited string.
     *
     * For example:
     * Range "12-16" delim: '-') : Range.closed(12, 16)
     * Range "24-16" delim: '-') : Range.closed(16, 24)
     * Range "12.16" delim: '.') : Range.closed(12, 16)
     * Range "12.16" delim: '-') : null
     * Range "1216" delim: '-') : null
     *
     * @param range
     *         Range expressed by Delimited-string
     * @param delim
     *         Delimiter for the range
     * @return Integer Closed com.google.common.collect.Range bounded by input range or null if input is invalid.
     */
    @Nullable
    public static Range<Integer> createClosedRange(@Nullable final String range, final char delim)
    {
        try
        {
            final String[] rangeArr = StringUtils.split(range, delim);
            Range<Integer> result = null;
            if (rangeArr != null && rangeArr.length > 0)
            {
                int min = parseInt(rangeArr[0]);
                int max = parseInt(rangeArr[1]);
                // Need to ensure that min is first argument to Range::closed to avoid java.lang.IllegalArgumentException.
                if (max < min)
                {
                    final int tmp = max;
                    max = min;
                    min = tmp;
                }
                result = Range.closed(Integer.valueOf(min), Integer.valueOf(max));
            }
            return result;
        }
        catch (final NumberFormatException | NullPointerException ignored)
        {
            return null;
        }
    }

    @SuppressWarnings("checkstyle:ReturnCount")
    @Nullable
    private static BoundType getEndBoundType(final char endpoint)
    {
        switch (endpoint)
        {
        case ']':
            return BoundType.CLOSED;
        case ')':
            return BoundType.OPEN;
        default:
            return null;
        }
    }

    @SuppressWarnings("checkstyle:ReturnCount")
    @Nullable
    private static BoundType getStartBoundType(final char endpoint)
    {
        switch (endpoint)
        {
        case '[':
            return BoundType.CLOSED;
        case '(':
            return BoundType.OPEN;
        default:
            return null;
        }
    }

    private RangeUtils()
    {
    }
}
