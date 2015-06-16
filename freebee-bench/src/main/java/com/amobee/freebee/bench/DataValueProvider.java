package com.amobee.freebee.bench;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface DataValueProvider
{

    DataTypeConfigurer getDataTypeConfigurer();

    Set<String> getDataTypes();

    default int getMaxNumberOfValues()
    {
        try
        {
            int totalMaxNumberOfValues = 0;
            for (final String dataType : getDataTypes())
            {
                totalMaxNumberOfValues = Math.addExact(totalMaxNumberOfValues, getMaxNumberOfValues(dataType));
            }
            return totalMaxNumberOfValues;
        }
        catch (final ArithmeticException e)
        {
            // overflow of long sum occurred
            return Integer.MAX_VALUE;
        }
    }

    int getMaxNumberOfValues(String dataType);

    RandomValueSelector getRandomValueSelector(String dataType);

    interface RandomValueSelector
    {

        default String[] getValueArray(final long count)
        {
            return getValueStream().limit(count).toArray(String[]::new);
        }

        default List<String> getValueList(final long count)
        {
            return getValueStream().limit(count).collect(Collectors.toList());
        }

        default Stream<String> getValueStream()
        {
            return Stream.generate(getSupplier());
        }

        default Supplier<String> getSupplier()
        {
            return this::getValue;
        }

        String getValue();

        int getMaxUniqueValues();

    }

}
