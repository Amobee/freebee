package com.amobee.freebee.bench.random;

import com.amobee.freebee.bench.DataTypeConfigurer;
import com.amobee.freebee.bench.DataValueProvider;
import com.amobee.freebee.bench.InputGenerator;
import com.amobee.freebee.config.BEDataTypeConfig;
import com.amobee.freebee.evaluator.evaluator.BEByteInputAttributeCategory;
import com.amobee.freebee.evaluator.evaluator.BEDoubleInputAttributeCategory;
import com.amobee.freebee.evaluator.evaluator.BEInput;
import com.amobee.freebee.evaluator.evaluator.BEIntInputAttributeCategory;
import com.amobee.freebee.evaluator.evaluator.BELongInputAttributeCategory;
import com.amobee.freebee.evaluator.evaluator.BEStringInputAttributeCategory;
import com.amobee.freebee.bench.range.Range;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

public class RandomInputGenerator implements InputGenerator
{
    private static final int DEFAULT_MAX_CATEGORIES = 0;  // unbound

    private final DataValueProvider dataValueProvider;
    private final DataTypeConfigurer dataTypeConfigurer;
    private final Random random;
    private final int maxCategories;
    private final int maxValues;
    private final boolean inputTrackingEnabled;

    public RandomInputGenerator(
            final RandomBenchmarkConfigurationProperties properties,
            final DataValueProvider dataValueProvider)
    {
        this.dataValueProvider = dataValueProvider;
        this.dataTypeConfigurer = dataValueProvider.getDataTypeConfigurer();

        this.random = properties.getRandomSeed() != null ? new Random(properties.getRandomSeed()) : new Random();
        this.inputTrackingEnabled = properties.isInputTrackingEnabled();
        this.maxValues = properties.getMaxInputValues();
        // TODO allow these values to be configured externally
        this.maxCategories = DEFAULT_MAX_CATEGORIES;

    }

    @Override
    public BEInput generate()
    {
        final Collection<String> categories;
        if (this.maxCategories > 0 && this.maxCategories < this.dataTypeConfigurer.getDataTypes().size())
        {
            final Set<String> allCategories = new HashSet<>(this.dataTypeConfigurer.getDataTypes());
            categories = Utils.randomSubset(allCategories, 1, this.maxCategories + 1, this.random);
        }
        else
        {
            categories = this.dataTypeConfigurer.getDataTypes();
        }

        int remainingValues = this.maxValues;
        final BEInput input = new BEInput();
        for (final String category : categories)
        {

            final DataValueProvider.RandomValueSelector valueSelector = this.dataValueProvider.getRandomValueSelector(category);
            final int maxNumberOfValuesUpperBound = this.random.nextInt(Math.max(remainingValues, this.dataValueProvider.getMaxNumberOfValues(category)) + 1);
            List<String> values = valueSelector.getValueList(maxNumberOfValuesUpperBound);
            remainingValues -= values.size();

            final BEDataTypeConfig dataTypeConfig = this.dataTypeConfigurer.getDataType(category)
                    .orElseThrow(() -> new IllegalStateException("No data type config found for data type " + category));

            if (dataTypeConfig.isRange())
            {
                // Overwrite values in the form [x,y) with random values between x and y
                values = values
                        .stream()
                        .map(Range::fromString)
                        .map(range -> Utils.randomIntBetween(
                                (int) range.getLowerBoundInclusive(),
                                (int) range.getUpperBoundExclusive(),
                                this.random))
                        .map(String::valueOf)
                        .collect(Collectors.toList());
            }

            switch (dataTypeConfig.getDataType())
            {
                case BYTE:
                    // TODO decide on string encoding for bytes and decode strings provided from file here
                    final BEByteInputAttributeCategory byteInputCategory = input.getOrCreateByteCategory(category);
                    byteInputCategory.setTrackingEnabled(this.inputTrackingEnabled);
                    throw new IllegalStateException("Byte data types are not yet supported");
                case DOUBLE:
                    final BEDoubleInputAttributeCategory doubleInputCategory = input.getOrCreateDoubleCategory(category);
                    doubleInputCategory.setTrackingEnabled(this.inputTrackingEnabled);
                    values.forEach(value -> doubleInputCategory.add(Double.valueOf(value)));
                    break;
                case INT:
                    final BEIntInputAttributeCategory intInputCategory = input.getOrCreateIntCategory(category);
                    intInputCategory.setTrackingEnabled(this.inputTrackingEnabled);
                    values.forEach(value -> intInputCategory.add(Integer.valueOf(value)));
                    break;
                case LONG:
                    final BELongInputAttributeCategory longInputCategory = input.getOrCreateLongCategory(category);
                    longInputCategory.setTrackingEnabled(this.inputTrackingEnabled);
                    values.forEach(value -> longInputCategory.add(Long.valueOf(value)));
                    break;
                case STRING:
                    final BEStringInputAttributeCategory stringInputCategory = input.getOrCreateStringCategory(category);
                    stringInputCategory.setTrackingEnabled(this.inputTrackingEnabled);
                    values.forEach(stringInputCategory::add);
                    break;
                default:
                    throw new IllegalStateException("Unknown BEDataType '" + dataTypeConfig.getDataType() + "'");
            }
        }

        return input;


    }
}
