package com.amobee.freebee.bench.simple;

import com.amobee.freebee.bench.BenchmarkConfiguration;
import com.amobee.freebee.bench.DataTypeConfigurer;
import com.amobee.freebee.bench.DefaultDataTypeConfigurer;
import com.amobee.freebee.bench.ExpressionGenerator;
import com.amobee.freebee.bench.InputGenerator;

public class SimpleBenchmarkConfiguration implements BenchmarkConfiguration
{

    private DataTypeConfigurer dataTypeConfigurer;
    private ExpressionGenerator expressionGenerator;
    private InputGenerator inputGenerator;

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
            this.expressionGenerator = new SimpleExpressionGenerator();
        }
        return this.expressionGenerator;
    }

    @Override
    public InputGenerator inputGenerator()
    {
        if (this.inputGenerator == null)
        {
            this.inputGenerator = new SimpleInputGenerator();
        }
        return this.inputGenerator;
    }
}
