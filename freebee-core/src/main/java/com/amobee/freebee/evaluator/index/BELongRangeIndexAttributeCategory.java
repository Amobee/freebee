package com.amobee.freebee.evaluator.index;

import javax.annotation.Nonnull;

/**
 * @author Michael Bond
 */
public class BELongRangeIndexAttributeCategory extends BEAbstractRangeIndexAttributeCategory<Long>
{
    private static final long serialVersionUID = 935839294882742L;

    @Override
    protected Long valueOf(final byte value)
    {
        return Long.valueOf(value);
    }

    @Override
    protected Long valueOf(final int value)
    {
        return Long.valueOf(value);
    }

    @Override
    protected Long valueOf(final long value)
    {
        return Long.valueOf(value);
    }

    @Override
    protected Long valueOf(@Nonnull final String value)
    {
        return Long.valueOf(value);
    }
}
