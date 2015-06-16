package com.amobee.freebee.bench.range;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Range
{
    private static final String RANGE_REGEX = "[\\[\\(](\\d+),(\\d+)[\\]\\)]";
    private static final Pattern RANGE_REGEX_PATTERN = Pattern.compile(RANGE_REGEX);

    private long lowerBoundInclusive;
    private long upperBoundExclusive;

    public Range() {}

    public Range(final long lowerBoundInclusive, final long upperBoundExclusive)
    {
        this.lowerBoundInclusive = lowerBoundInclusive;
        this.upperBoundExclusive = upperBoundExclusive;
    }

    public long getLowerBoundInclusive()
    {
        return this.lowerBoundInclusive;
    }

    public Range setLowerBoundInclusive(final long lowerBoundInclusive)
    {
        this.lowerBoundInclusive = lowerBoundInclusive;
        return this;
    }

    public long getUpperBoundExclusive()
    {
        return this.upperBoundExclusive;
    }

    public Range setUpperBoundExclusive(final long upperBoundExclusive)
    {
        this.upperBoundExclusive = upperBoundExclusive;
        return this;
    }

    public boolean includes(final long value)
    {
        return this.lowerBoundInclusive <= value && value < this.upperBoundExclusive;
    }

    @Override
    public String toString()
    {
        return "[" + this.lowerBoundInclusive + "," + this.upperBoundExclusive + ")";
    }

    public static Range fromString(final String rangeString)
    {
        final Matcher m = RANGE_REGEX_PATTERN.matcher(rangeString);
        final boolean matched = m.find();
        if (!matched)
        {
            throw new IllegalArgumentException("Unrecognized range format '" + rangeString + "'");
        }
        final String rangeLowerBound = m.group(1);
        final String rangeUpperBound = m.group(2);
        final int rangeLowerBoundInclusive = Integer.valueOf(rangeLowerBound);
        final int rangeUpperBoundExclusive = Integer.valueOf(rangeUpperBound);

        return new Range(rangeLowerBoundInclusive, rangeUpperBoundExclusive);
    }
}
