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

import org.eclipse.collections.api.map.primitive.MutableByteObjectMap;
import org.eclipse.collections.impl.factory.primitive.ByteObjectMaps;

/**
 * @author Michael Bond
 */
@Getter
public class BEByteIndexAttributeCategory extends BEIndexAttributeCategory implements Serializable
{
    private static final long serialVersionUID = 8010569588337670801L;

    @Nonnull
    private final MutableByteObjectMap<List<BEInterval>> values = ByteObjectMaps.mutable.empty();

    @SuppressWarnings("UnusedParameters")
    public static BEIndexAttributeCategory newInstance(@Nonnull final BEDataTypeConfig dataTypeConfig)
    {
        return dataTypeConfig.isRange() ? new BEByteRangeIndexAttributeCategory() : new BEByteIndexAttributeCategory();
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
    public void getIntervals(@Nonnull final String attributeValue, @Nonnull final Consumer<List<BEInterval>> consumer)
    {
        callConsumer(this.values.get(Byte.parseByte(attributeValue)), consumer);
    }

    @Override
    public void getIntervals(
            @Nonnull final String attributeValue,
            @Nullable final BEInputAttributeCategory matchedInput,
            @Nonnull final BEAttributeCategoryMatchedIntervalConsumer consumer)
    {
        callConsumer(this.values.get(Byte.parseByte(attributeValue)), matchedInput, consumer);
    }

    @Override
    protected void compact()
    {
        this.values.values().forEach(value -> ((ArrayList<BEInterval>) value).trimToSize());
    }

    private byte getKey(@Nonnull final Object attributeValue)
    {
        return attributeValue instanceof Number ?
                ((Number) attributeValue).byteValue() :
                Byte.parseByte(attributeValue.toString());
    }
}
