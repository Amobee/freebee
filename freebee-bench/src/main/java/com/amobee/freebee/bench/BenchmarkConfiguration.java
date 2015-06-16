package com.amobee.freebee.bench;

public interface BenchmarkConfiguration
{

    DataTypeConfigurer dataTypeConfigurer();

    ExpressionGenerator expressionGenerator();

    InputGenerator inputGenerator();

}
