package com.amobee.freebee.evaluator.index;

import javax.annotation.Nonnull;

/**
 * @author Michael Bond
 */
public class BEDoubleRangeIndexAttributeCategory extends BEAbstractRangeIndexAttributeCategory<Double>
{
    private static final long serialVersionUID = 832342271243719992L;

    @Override
    protected Double valueOf(final byte value)
    {
        return Double.valueOf(value);
    }

    @Override
    protected Double valueOf(final double value)
    {
        return Double.valueOf(value);
    }

    @Override
    protected Double valueOf(final int value)
    {
        return Double.valueOf(value);
    }

    @Override
    protected Double valueOf(final long value)
    {
        return Double.valueOf(value);
    }

    @Override
    protected Double valueOf(@Nonnull final String value)
    {
        return Double.valueOf(value);
    }
}
