package com.amobee.freebee.bench.random;

import com.amobee.freebee.bench.BenchmarkConfiguration;
import com.amobee.freebee.bench.DataTypeConfigurer;
import com.amobee.freebee.bench.DataValueProvider;
import com.amobee.freebee.bench.DefaultDataTypeConfigurer;
import com.amobee.freebee.bench.ExpressionGenerator;
import com.amobee.freebee.bench.InputGenerator;
import com.amobee.freebee.config.BEDataTypeConfig;

public class RandomBenchmarkConfiguration implements BenchmarkConfiguration
{

    private final RandomBenchmarkConfigurationProperties randomBenchmarkConfigurationProperties;

    private DataTypeConfigurer dataTypeConfigurer;
    private DataValueProvider dataValueProvider;
    private ExpressionGenerator expressionGenerator;
    private InputGenerator inputGenerator;

    public RandomBenchmarkConfiguration()
    {
        this(new RandomBenchmarkConfigurationProperties());
    }

    public RandomBenchmarkConfiguration(final RandomBenchmarkConfigurationProperties properties)
    {
        this.randomBenchmarkConfigurationProperties = properties;
    }

    @Override
    public DataTypeConfigurer dataTypeConfigurer()
    {
        if (this.dataTypeConfigurer == null)
        {
            this.dataTypeConfigurer = new DefaultDataTypeConfigurer();
        }
        return this.dataTypeConfigurer;
    }

    @Override
    public ExpressionGenerator expressionGenerator()
    {
        if (this.expressionGenerator == null)
        {
            this.expressionGenerator = new RandomExpressionGenerator(this.randomBenchmarkConfigurationProperties, dataValueProvider());
        }
        return this.expressionGenerator;
    }

    @Override
    public InputGenerator inputGenerator()
    {
        if (this.inputGenerator == null)
        {
            this.inputGenerator = new RandomInputGenerator(this.randomBenchmarkConfigurationProperties, dataValueProvider());
        }
        return this.inputGenerator;
    }

    private DataValueProvider dataValueProvider()
    {
        if (this.dataValueProvider == null)
        {
            final DataTypeConfigurer dtc = dataTypeConfigurer();
            final RandomDataValueProvider randomDataValueProvider = new RandomDataValueProvider(dtc);
            dtc.getDataTypeConfigs().stream()
                    .map(BEDataTypeConfig::getType)
                    .forEach(typeName -> randomDataValueProvider.addValueSelector(typeName, new FileValueSelector(typeName)));
            this.dataValueProvider = randomDataValueProvider;
        }
        return this.dataValueProvider;
    }

}
