package com.amobee.freebee.evaluator.index;

import javax.annotation.Nonnull;

/**
 * @author Michael Bond
 */
public class BEIntRangeIndexAttributeCategory extends BEAbstractRangeIndexAttributeCategory<Integer>
{
    private static final long serialVersionUID = -322799933882277442L;

    @Override
    protected Integer valueOf(final byte value)
    {
        return Integer.valueOf(value);
    }

    @Override
    protected Integer valueOf(final int value)
    {
        return Integer.valueOf(value);
    }

    @Override
    protected Integer valueOf(@Nonnull final String value)
    {
        return Integer.valueOf(value);
    }
}
