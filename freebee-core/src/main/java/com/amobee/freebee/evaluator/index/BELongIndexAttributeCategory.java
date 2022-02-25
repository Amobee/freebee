package com.amobee.freebee.evaluator.index;

import com.amobee.freebee.config.BEDataTypeConfig;
import com.amobee.freebee.evaluator.BEInterval;
import com.amobee.freebee.evaluator.evaluator.BEInputAttributeCategory;
import lombok.Getter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.eclipse.collections.api.map.primitive.MutableLongObjectMap;
import org.eclipse.collections.impl.factory.primitive.LongObjectMaps;

/**
 * @author Michael Bond
 */
@Getter
public class BELongIndexAttributeCategory extends BEIndexAttributeCategory implements Serializable
{
    private static final long serialVersionUID = 4776835776584972751L;

    @Nonnull
    private final MutableLongObjectMap<List<BEInterval>> values = LongObjectMaps.mutable.empty();

    @SuppressWarnings("UnusedParameters")
    public static BEIndexAttributeCategory newInstance(@Nonnull final BEDataTypeConfig dataTypeConfig)
    {
        return dataTypeConfig.isRange() ? new BELongRangeIndexAttributeCategory() : new BELongIndexAttributeCategory();
    }

    @Override
    public void addInterval(@Nonnull final Object attributeValue, @Nonnull final BEInterval interval)
    {
        this.values.getIfAbsentPut(getKey(attributeValue), ArrayList::new).add(interval);

        // if interval is negative, add a "wildcard" interval
        if (interval.isNegative())
        {
            addNegativeInterval(interval);
        }
    }

    @Override
    public void getIntervals(final byte attributeValue, @Nonnull final Consumer<List<BEInterval>> consumer)
    {
        callConsumer(this.values.get(attributeValue), consumer);
    }

    @Override
    public void getIntervals(final byte attributeValue,
                             @Nullable final BEInputAttributeCategory matchedInput,
                             @Nonnull final BEAttributeCategoryMatchedIntervalConsumer consumer)
    {
        callConsumer(this.values.get(attributeValue), matchedInput, consumer);
    }

    @Override
    public void getIntervals(final int attributeValue, @Nonnull final Consumer<List<BEInterval>> consumer)
    {
        callConsumer(this.values.get(attributeValue), consumer);
    }

    @Override
    public void getIntervals(final int attributeValue,
                             @Nullable final BEInputAttributeCategory matchedInput,
                             @Nonnull final BEAttributeCategoryMatchedIntervalConsumer consumer)
    {
        callConsumer(this.values.get(attributeValue), matchedInput, consumer);
    }

    @Override
    public void getIntervals(final long attributeValue, @Nonnull final Consumer<List<BEInterval>> consumer)
    {
        callConsumer(this.values.get(attributeValue), consumer);
    }

    @Override
    public void getIntervals(final long attributeValue,
                             @Nullable final BEInputAttributeCategory matchedInput,
                             @Nonnull final BEAttributeCategoryMatchedIntervalConsumer consumer)
    {
        callConsumer(this.values.get(attributeValue), matchedInput, consumer);
    }

    @Override
    public void getIntervals(@Nonnull final String attributeValue, @Nonnull final Consumer<List<BEInterval>> consumer)
    {
        callConsumer(this.values.get(Long.parseLong(attributeValue)), consumer);
    }

    @Override
    public void getIntervals(
            @Nonnull final String attributeValue,
            @Nullable final BEInputAttributeCategory matchedInput,
            @Nonnull final BEAttributeCategoryMatchedIntervalConsumer consumer)
    {
        callConsumer(this.values.get(Long.parseLong(attributeValue)), matchedInput, consumer);
    }

    @Override
    protected void compact()
    {
        this.values.values().forEach(value -> ((ArrayList<BEInterval>) value).trimToSize());
    }

    private long getKey(@Nonnull final Object attributeValue)
    {
        return attributeValue instanceof Number ?
                ((Number) attributeValue).longValue() :
                Long.parseLong(attributeValue.toString());
    }
}
