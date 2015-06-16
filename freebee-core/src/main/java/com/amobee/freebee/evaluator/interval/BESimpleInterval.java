package com.amobee.freebee.evaluator.interval;

import java.io.Serializable;
import java.util.Objects;

/**
 * A simple implementation of the {@link Interval} interface.
 *
 * This implementation has certain guarantees, including:
 *  - immutability
 *  - a final class (it cannot be extended)
 *  - equals and hashcode are based solely on the start and end of the interval
 *
 * These guarantees should not be broken.
 * For this reason, this class is intentionally final and restricted to package-level visibility.
 * It is purpose-built as a helper class for other algorithms in this package.
 *
 * @author Kevin Doran
 */
final class BESimpleInterval implements Interval, Serializable
{
    private static final long serialVersionUID = 4693989150058074099L;

    private final short start;
    private final short end;

    BESimpleInterval(final short start, final short end)
    {
        if (!(start < end))
        {
            throw new IllegalArgumentException("Start must be less than end. Note, end is exclusive.");
        }
        this.start = start;
        this.end = end;
    }

    BESimpleInterval(final Interval other)
    {
        this(other.getStart(), other.getEnd());
    }

    @Override
    public short getStart()
    {
        return this.start;
    }

    @Override
    public short getEnd()
    {
        return this.end;
    }

    @Override
    public String toString()
    {
        return "BESimpleInterval[" + start + "," + end + ')';
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        final BESimpleInterval that = (BESimpleInterval) o;
        return this.start == that.start &&
                this.end == that.end;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(this.start, this.end);
    }
}
