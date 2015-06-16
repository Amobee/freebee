package com.amobee.freebee.evaluator.index;

import lombok.Getter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.annotation.Nonnull;

import org.eclipse.collections.api.map.primitive.MutableDoubleObjectMap;
import org.eclipse.collections.impl.factory.primitive.DoubleObjectMaps;

import com.amobee.freebee.config.BEDataTypeConfig;
import com.amobee.freebee.evaluator.BEInterval;

/**
 * @author Michael Bond
 */
@Getter
public class BEDoubleIndexAttributeCategory extends BEIndexAttributeCategory implements Serializable
{
    private static final long serialVersionUID = -6635679620120437041L;

    @Nonnull
    private final MutableDoubleObjectMap<List<BEInterval>> values = DoubleObjectMaps.mutable.empty();

    @SuppressWarnings("UnusedParameters")
    public static BEIndexAttributeCategory newInstance(@Nonnull final BEDataTypeConfig dataTypeConfig)
    {
        return dataTypeConfig.isRange() ?
                new BEDoubleRangeIndexAttributeCategory() :
                new BEDoubleIndexAttributeCategory();
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
    public void getIntervals(final int attributeValue, @Nonnull final Consumer<List<BEInterval>> consumer)
    {
        callConsumer(this.values.get(attributeValue), consumer);
    }

    @Override
    public void getIntervals(final long attributeValue, @Nonnull final Consumer<List<BEInterval>> consumer)
    {
        callConsumer(this.values.get(attributeValue), consumer);
    }

    @Override
    public void getIntervals(@Nonnull final String attributeValue, @Nonnull final Consumer<List<BEInterval>> consumer)
    {
        callConsumer(this.values.get(Long.parseLong(attributeValue)), consumer);
    }

    @Override
    protected void compact()
    {
        this.values.values().forEach(value -> ((ArrayList<BEInterval>) value).trimToSize());
    }

    private double getKey(@Nonnull final Object attributeValue)
    {
        return attributeValue instanceof Number ?
                ((Number) attributeValue).doubleValue() :
                Double.parseDouble(attributeValue.toString());
    }
}
