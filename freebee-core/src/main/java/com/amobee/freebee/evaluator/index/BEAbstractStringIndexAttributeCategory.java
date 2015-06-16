package com.amobee.freebee.evaluator.index;

import java.util.List;
import java.util.function.Consumer;
import javax.annotation.Nonnull;

import com.amobee.freebee.evaluator.BEInterval;

/**
 * @author Michael Bond
 */
public abstract class BEAbstractStringIndexAttributeCategory extends BEIndexAttributeCategory
{
    private final boolean ignoreCase;

    public BEAbstractStringIndexAttributeCategory(final boolean ignoreCase)
    {
        this.ignoreCase = ignoreCase;
    }

    @Override
    public void getIntervals(final byte attributeValue, @Nonnull final Consumer<List<BEInterval>> consumer)
    {
        getIntervals(String.valueOf(attributeValue), consumer);
    }

    @Override
    public void getIntervals(final double attributeValue, @Nonnull final Consumer<List<BEInterval>> consumer)
    {
        getIntervals(String.valueOf(attributeValue), consumer);
    }

    @Override
    public void getIntervals(final int attributeValue, @Nonnull final Consumer<List<BEInterval>> consumer)
    {
        getIntervals(String.valueOf(attributeValue), consumer);
    }

    @Override
    public void getIntervals(final long attributeValue, @Nonnull final Consumer<List<BEInterval>> consumer)
    {
        getIntervals(String.valueOf(attributeValue), consumer);
    }

    protected String getValue(@Nonnull final String value)
    {
        return this.ignoreCase ? value.toLowerCase() : value;
    }
}
