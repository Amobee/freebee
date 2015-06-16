package com.amobee.freebee.bench.reference;

import com.amobee.freebee.config.BEDataTypeConfig;
import com.amobee.freebee.evaluator.evaluator.BEInput;
import com.amobee.freebee.evaluator.evaluator.BEInputAttributeCategory;
import com.amobee.freebee.evaluator.evaluator.BEIntInputAttributeCategory;
import com.amobee.freebee.evaluator.evaluator.BEStringInputAttributeCategory;
import com.amobee.freebee.expression.BEConjunctionNode;
import com.amobee.freebee.expression.BENode;
import com.amobee.freebee.expression.BEPredicateNode;
import com.amobee.freebee.expression.BEReferenceNode;
import com.amobee.freebee.bench.range.Range;
import com.amobee.freebee.bench.shim.Evaluator;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;

public class ReferenceEvaluator implements Evaluator
{

    private final Map<String, BEDataTypeConfig> dataTypeConfigs;
    private final Map<String, BENode> expressions;
    private final Map<String, BENode> partialExpressions;

    public ReferenceEvaluator(
            @Nonnull final Map<String, BEDataTypeConfig> dataTypeConfigs,
            @Nonnull final Map<String, BENode> expressions,
            @Nonnull final Map<String, BENode> partialExpressions)
    {
        this.dataTypeConfigs = dataTypeConfigs;
        this.expressions = expressions;
        this.partialExpressions = partialExpressions;
    }

    @Override
    public Set<String> evaluate(@Nonnull final BEInput input)
    {
        final Set<String> matchedExpressions = new HashSet<>();
        this.expressions.forEach((id, expression) -> {
            if (evaluate(expression, input))
            {
                matchedExpressions.add(id);
            }
        });
        return matchedExpressions;
    }

    private boolean evaluate(final BENode expression, final BEInput input)
    {

        final boolean nodeResult;

        if (expression instanceof BEReferenceNode)
        {
            final BEReferenceNode referenceNode = (BEReferenceNode) expression;

            nodeResult = referenceNode.getValues().stream()
                    .map(referenceValue -> this.partialExpressions.get(referenceValue.getId()))
                    .anyMatch(beNode -> evaluate(beNode, input));

        }
        else if (expression instanceof BEConjunctionNode)
        {
            final BEConjunctionNode conjunctionNode = (BEConjunctionNode) expression;
            final String conjunctionType = conjunctionNode.getType();

            if ("or".equalsIgnoreCase(conjunctionType))
            {
                nodeResult = conjunctionNode.getValues().stream().anyMatch(beNode -> evaluate(beNode, input));

            }
            else if ("and".equalsIgnoreCase(conjunctionType))
            {
                nodeResult = conjunctionNode.getValues().stream().allMatch(beNode -> evaluate(beNode, input));

            }
            else
            {
                throw new IllegalArgumentException("Unknown BEConjunctionNode type '" + conjunctionType + "'");

            }


        }
        else if (expression instanceof BEPredicateNode)
        {
            final BEPredicateNode predicateNode = (BEPredicateNode) expression;
            return evaluate(predicateNode, input);

        }
        else
        {
            throw new IllegalArgumentException("Unknown BENode type '" + expression.getClass().getSimpleName() + "'");
        }

        return expression.isNegative() ? !nodeResult : nodeResult;
    }

    private boolean evaluate(final BEPredicateNode predicateNode, final BEInput input)
    {

        final String attributeCategoryName = predicateNode.getType();
        final BEInputAttributeCategory inputAttributeCategory = input.getCategory(attributeCategoryName);
        final BEDataTypeConfig dataTypeConfig = this.dataTypeConfigs.get(attributeCategoryName);

        if (dataTypeConfig == null)
        {
            throw new IllegalStateException(
                    "Cannot evaluate predicate node '" + predicateNode + "' " +
                            "because there is no data type config for '" + attributeCategoryName + "'");
        }

        if (inputAttributeCategory == null)
        {
            // return false if predicateNode is positively targeting a category that is not part of the input attributes
            // return true if predicateNode is negatively targeting a category that is not part of the input attributes
            return predicateNode.isNegative();
        }

        final boolean positiveTargetingResult = predicateNode.getValues().stream().anyMatch(
                targetedValue -> evaluate(targetedValue.getId(), inputAttributeCategory, dataTypeConfig));

        return predicateNode.isNegative() ? !positiveTargetingResult : positiveTargetingResult;

    }

    private boolean evaluate(
            final String targetedValue,
            final BEInputAttributeCategory attributeCategory,
            final BEDataTypeConfig dataTypeConfig)
    {
        if (attributeCategory instanceof BEStringInputAttributeCategory)
        {
            return evaluateStringAttribute(targetedValue, (BEStringInputAttributeCategory) attributeCategory, dataTypeConfig);
        }
        else if (attributeCategory instanceof BEIntInputAttributeCategory)
        {
            return evaluateIntAttribute(targetedValue, (BEIntInputAttributeCategory) attributeCategory, dataTypeConfig);
        }
        else
        {
            throw new UnsupportedOperationException("Reference Evaluator implementation for '"
                    + attributeCategory.getClass().getSimpleName() + "' attribute types is not yet implemented");
        }
    }

    @SuppressWarnings("checkstyle:ReturnCount")
    private boolean evaluateStringAttribute(
            final String targetedValue,
            final BEStringInputAttributeCategory attributeCategory,
            final BEDataTypeConfig dataTypeConfig)
    {

        return attributeCategory.getValues().stream().anyMatch(inputValue -> {
            if (!dataTypeConfig.isReverse() && !dataTypeConfig.isPartial() && !dataTypeConfig.isIgnoreCase())
            {
                return inputValue.equals(targetedValue);

            }
            else if (!dataTypeConfig.isReverse() && !dataTypeConfig.isPartial() && dataTypeConfig.isIgnoreCase())
            {
                return inputValue.equalsIgnoreCase(targetedValue);

            }
            else if (!dataTypeConfig.isReverse() && dataTypeConfig.isPartial() && !dataTypeConfig.isIgnoreCase())
            {
                return inputValue.startsWith(targetedValue);

            }
            else if (!dataTypeConfig.isReverse() && dataTypeConfig.isPartial() && dataTypeConfig.isIgnoreCase())
            {
                return inputValue.toLowerCase().startsWith(targetedValue.toLowerCase());

            }
            else if (dataTypeConfig.isReverse() && !dataTypeConfig.isPartial() && !dataTypeConfig.isIgnoreCase())
            {
                return inputValue.equals(targetedValue);

            }
            else if (dataTypeConfig.isReverse() && !dataTypeConfig.isPartial() && dataTypeConfig.isIgnoreCase())
            {
                return inputValue.equalsIgnoreCase(targetedValue);

            }
            else if (dataTypeConfig.isReverse() && dataTypeConfig.isPartial() && !dataTypeConfig.isIgnoreCase())
            {
                return inputValue.endsWith(targetedValue);

            }
            else if (dataTypeConfig.isReverse() && dataTypeConfig.isPartial() && dataTypeConfig.isIgnoreCase())
            {
                return inputValue.toLowerCase().endsWith(targetedValue.toLowerCase());

            }

            throw new IllegalStateException("Should not get here");
        });

    }

    private boolean evaluateIntAttribute(
            final String targetedValue,
            final BEIntInputAttributeCategory attributeCategory,
            final BEDataTypeConfig dataTypeConfig)
    {
        if (dataTypeConfig.isRange())
        {
            return attributeCategory
                    .getValues()
                    .anySatisfy(inputValue -> Range.fromString(targetedValue).includes(inputValue));
        }
        else
        {
            final int targetedIntValue = Integer.valueOf(targetedValue);
            return attributeCategory.getValues().anySatisfy(inputValue -> targetedIntValue == inputValue);
        }
    }

}
