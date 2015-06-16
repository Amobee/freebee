package com.amobee.freebee.evaluator.index;

import com.amobee.freebee.config.BEDataTypeConfig;
import com.amobee.freebee.evaluator.BEInterval;
import lombok.Getter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.annotation.Nonnull;

import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.impl.factory.Maps;

/**
 * @author Michael Bond
 */
@Getter
public class BEStringIndexAttributeCategory extends BEAbstractStringIndexAttributeCategory implements Serializable
{
    private static final long serialVersionUID = 4185476996223857599L;

    @Nonnull
    private final MutableMap<String, List<BEInterval>> values = Maps.mutable.of();

    public static BEIndexAttributeCategory newInstance(@Nonnull final BEDataTypeConfig dataTypeConfig)
    {
        if (dataTypeConfig.isRange())
        {
            throw new IllegalArgumentException("Ranges currently not supported for string datatype");
        }
        return !dataTypeConfig.isPartial() ?
                new BEStringIndexAttributeCategory(dataTypeConfig.isIgnoreCase()) :
                new BEPartialStringIndexAttributeCategory(dataTypeConfig.isIgnoreCase(), dataTypeConfig.isReverse());
    }

    public BEStringIndexAttributeCategory(final boolean ignoreCase)
    {
        super(ignoreCase);
    }

    @Override
    public void addInterval(@Nonnull final Object attributeValue, @Nonnull final BEInterval interval)
    {
        this.values.getIfAbsentPut(getValue(attributeValue.toString()), ArrayList::new).add(interval);

        // if interval is negative, add a "wildcard" interval
        if (interval.isNegative())
        {
            addNegativeInterval(interval);
        }
    }

    @Override
    public void getIntervals(@Nonnull final String attributeValue, @Nonnull final Consumer<List<BEInterval>> consumer)
    {
        callConsumer(this.values.get(getValue(attributeValue)), consumer);
    }

    @Override
    protected void compact()
    {
        this.values.values().forEach(value -> ((ArrayList<BEInterval>) value).trimToSize());
    }
}
