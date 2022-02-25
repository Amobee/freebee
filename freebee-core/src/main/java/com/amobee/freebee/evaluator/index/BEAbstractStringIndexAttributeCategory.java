package com.amobee.freebee.evaluator.index;

import com.amobee.freebee.evaluator.BEInterval;
import com.amobee.freebee.evaluator.evaluator.BEInputAttributeCategory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

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
    public void getIntervals(final byte attributeValue,
                             @Nullable final BEInputAttributeCategory matchedInput,
                             @Nonnull final BEAttributeCategoryMatchedIntervalConsumer consumer)
    {
        getIntervals(String.valueOf(attributeValue), matchedInput, consumer);
    }

    @Override
    public void getIntervals(final double attributeValue, @Nonnull final Consumer<List<BEInterval>> consumer)
    {
        getIntervals(String.valueOf(attributeValue), consumer);
    }

    @Override
    public void getIntervals(final double attributeValue,
                             @Nullable final BEInputAttributeCategory matchedInput,
                             @Nonnull final BEAttributeCategoryMatchedIntervalConsumer consumer)
    {
        getIntervals(String.valueOf(attributeValue), matchedInput, consumer);
    }

    @Override
    public void getIntervals(final int attributeValue, @Nonnull final Consumer<List<BEInterval>> consumer)
    {
        getIntervals(String.valueOf(attributeValue), consumer);
    }

    @Override
    public void getIntervals(final int attributeValue,
                             @Nullable final BEInputAttributeCategory matchedInput,
                             @Nonnull final BEAttributeCategoryMatchedIntervalConsumer consumer)
    {
        getIntervals(String.valueOf(attributeValue), matchedInput, consumer);
    }

    @Override
    public void getIntervals(final long attributeValue, @Nonnull final Consumer<List<BEInterval>> consumer)
    {
        getIntervals(String.valueOf(attributeValue), consumer);
    }

    @Override
    public void getIntervals(final long attributeValue,
                             @Nullable final BEInputAttributeCategory matchedInput,
                             @Nonnull final BEAttributeCategoryMatchedIntervalConsumer consumer)
    {
        getIntervals(String.valueOf(attributeValue), matchedInput, consumer);
    }

    protected String getValue(@Nonnull final String value)
    {
        return this.ignoreCase ? value.toLowerCase() : value;
    }
}
