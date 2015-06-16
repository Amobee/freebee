package com.amobee.freebee.evaluator.index;

import javax.annotation.Nonnull;

/**
 * @author Michael Bond
 */
public class BEByteRangeIndexAttributeCategory extends BEAbstractRangeIndexAttributeCategory<Byte>
{
    private static final long serialVersionUID = -832532207721392172L;

    @Override
    protected Byte valueOf(final byte value)
    {
        return Byte.valueOf(value);
    }

    @Override
    protected Byte valueOf(@Nonnull final String value)
    {
        return Byte.valueOf(value);
    }
}
