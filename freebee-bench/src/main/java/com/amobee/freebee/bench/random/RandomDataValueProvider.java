package com.amobee.freebee.bench.random;

import com.amobee.freebee.bench.DataTypeConfigurer;
import com.amobee.freebee.bench.DataValueProvider;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RandomDataValueProvider implements DataValueProvider
{

    private final DataTypeConfigurer dataTypeConfigurer;

    private final Map<String, RandomValueSelector> valueSelectors;

    public RandomDataValueProvider(final DataTypeConfigurer dataTypeConfigurer)
    {
        this.dataTypeConfigurer = dataTypeConfigurer;
        final List<String> dataTypeNames = this.dataTypeConfigurer.getDataTypes();
        this.valueSelectors = new HashMap<>(dataTypeNames.size());
    }

    @Override
    public DataTypeConfigurer getDataTypeConfigurer()
    {
        return this.dataTypeConfigurer;
    }

    public void addValueSelector(final String dataType, final RandomValueSelector valueSelector)
    {
        if (this.valueSelectors.containsKey(dataType))
        {
            throw new IllegalArgumentException(dataType);
        }
        this.valueSelectors.put(dataType, valueSelector);
    }

    public Set<String> getDataTypes()
    {
        return this.valueSelectors.keySet();
    }

    @Override
    public int getMaxNumberOfValues(final String dataType)
    {
        return this.valueSelectors.get(dataType).getMaxUniqueValues();
    }

    @Override
    public RandomValueSelector getRandomValueSelector(final String dataType)
    {
        return this.valueSelectors.get(dataType);
    }

}
