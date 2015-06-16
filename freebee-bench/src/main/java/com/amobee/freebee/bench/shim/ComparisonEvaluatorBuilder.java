package com.amobee.freebee.bench.shim;

import com.amobee.freebee.bench.reference.ReferenceEvaluator;
import com.amobee.freebee.config.BEDataTypeConfig;
import com.amobee.freebee.evaluator.evaluator.BEEvaluatorBuilder;
import com.amobee.freebee.expression.BENode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;

public class ComparisonEvaluatorBuilder
{

    private final Map<String, BEDataTypeConfig> dataTypeConfigs;
    private final Map<String, BENode> expressions;
    private final Map<String, BENode> partialExpressions;

    public ComparisonEvaluatorBuilder()
    {
        this.dataTypeConfigs = new HashMap<>();
        this.expressions = new HashMap<>();
        this.partialExpressions = new HashMap<>();

    }

    public ComparisonEvaluatorBuilder addDataTypeConfigs(@Nonnull final List<BEDataTypeConfig> dataTypeConfigs)
    {
        dataTypeConfigs.forEach(dtc -> this.dataTypeConfigs.put(dtc.getType(), dtc));
        return this;
    }

    public ComparisonEvaluatorBuilder addExpression(@Nonnull final String expressionId, @Nonnull final BENode expression)
    {
        if (this.expressions.containsKey(expressionId))
        {
            throw new IllegalStateException(
                    "While building evaluator, multiple expressions were passed with the same id '" + expressionId + "'");
        }
        this.expressions.put(expressionId, expression);
        return this;
    }

    public Evaluator buildFastEvaluator()
    {
        final BEEvaluatorBuilder<String> fastEvaluatorBuilder = new BEEvaluatorBuilder<>();
        fastEvaluatorBuilder.addDataTypeConfigs(new ArrayList<>(this.dataTypeConfigs.values()));
        this.expressions.forEach(fastEvaluatorBuilder::addExpression);
        return new FreebeeEvaluator(fastEvaluatorBuilder.build());
    }

    public Evaluator buildReferenceEvaluator()
    {
        return new ReferenceEvaluator(this.dataTypeConfigs, this.expressions, this.partialExpressions);
    }

}
