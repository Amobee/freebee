package com.amobee.freebee.evaluator.index;

import com.amobee.freebee.config.BEDataTypeConfig;
import com.amobee.freebee.expression.BEAttributeValue;
import com.amobee.freebee.expression.BEConjunctionNode;
import com.amobee.freebee.expression.BEConstants;
import com.amobee.freebee.expression.BENode;
import com.amobee.freebee.expression.BEPredicateNode;
import com.amobee.freebee.expression.BEReferenceNode;
import com.amobee.freebee.expression.BEReferenceValue;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@SuppressWarnings("checkstyle:ReturnCount")
public class BEDefaultExpressionHashProvider<T> implements BEExpressionHashProvider<T>
{

    @Override
    public int computeHash(final BEExpressionInfo<T> expression, final BEDataTypeConfigSupplier dataTypeConfigSupplier)
    {
        return computeHash(Collections.singletonList(expression), Collections.emptyMap(), dataTypeConfigSupplier);
    }

    @Override
    public int computeHash(
            final BEExpressionInfo<T> expression,
            final Collection<BENode> partialExpressions,
            final BEDataTypeConfigSupplier dataTypeConfigSupplier)
    {
        return computeHash(Collections.singletonList(expression), partialExpressions, dataTypeConfigSupplier);
    }

    @Override
    public int computeHash(
            final Collection<BEExpressionInfo<T>> expressions,
            final BEDataTypeConfigSupplier dataTypeConfigSupplier)
    {
        return computeHash(expressions, Collections.emptyMap(), dataTypeConfigSupplier);
    }

    @Override
    public int computeHash(
            final Collection<BEExpressionInfo<T>> expressions,
            final Collection<BENode> partialExpressions,
            final BEDataTypeConfigSupplier dataTypeConfigSupplier)
    {

        final Map<String, BENode> partialExpressionMap;
        if (partialExpressions != null)
        {
            partialExpressionMap = partialExpressions.stream().collect(Collectors.toMap(BENode::getId, Function.identity()));
        }
        else
        {
            partialExpressionMap = null;
        }
        return computeHash(expressions, partialExpressionMap, dataTypeConfigSupplier);

    }

    @Override
    public int computeHash(
            final Collection<BEExpressionInfo<T>> expressions,
            final Map<String, BENode> partialExpressions,
            final BEDataTypeConfigSupplier dataTypeConfigSupplier)
    {

        final HashContext partialExpressionContext = new HashContext(dataTypeConfigSupplier);
        final Map<String, Integer> partialExpressionHashCodes = new HashMap<>();
        if (partialExpressions != null)
        {
            partialExpressions.forEach((expressionId, expressionNode) ->
                    partialExpressionHashCodes.put(expressionId, hashNode(expressionNode, partialExpressionContext)));
        }

        final HashContext fullExpressionContext = new HashContext(dataTypeConfigSupplier, partialExpressionHashCodes);
        return expressions.stream()
                .map(exprInfo -> {
                    final int dataHashCode = exprInfo.getData().hashCode();
                    final int expressionHashCode = hashNode(exprInfo.getExpression(), fullExpressionContext);
                    return Arrays.hashCode(new int[]{dataHashCode, expressionHashCode});
                })
                .reduce(0, Integer::sum);  // By adding the hashes of each expression, we get order independence
    }

    private int hashNode(final BENode node, final HashContext context)
    {
        switch (node.getType().toUpperCase())
        {
            case BEConstants.NODE_TYPE_AND:
            case BEConstants.NODE_TYPE_OR:
                return hashConjunctionNode((BEConjunctionNode) node, context);
            case BEConstants.NODE_TYPE_REFERENCE:
                if (!context.allowReferenceNodes())
                {
                    throw new IllegalArgumentException("Nested partial expressions are not allowed.");
                }
                return hashReferenceNode((BEReferenceNode) node, context);
            default:
                return hashPredicateNode((BEPredicateNode) node, context);
        }
    }

    private int hashConjunctionNode(final BEConjunctionNode node, final HashContext context)
    {
        // Compute hash consisting of the fields: type, negative, values
        final int typeHash = caseIndependentHashCode(node.getType());
        final int negationHash = booleanHashCode(node.isNegative());
        final int valuesHash = node.getValues().stream()
                .map(n -> hashNode(n, context))
                .reduce(0, Integer::sum);  // By adding the hashes of each value, we get order independence
        final int[] intermediateHashes = new int[]{typeHash, negationHash, valuesHash};
        return Arrays.hashCode(intermediateHashes);
    }

    private int hashPredicateNode(final BEPredicateNode node, final HashContext context)
    {
        final String type = node.getType();
        final BEDataTypeConfig dataTypeConfig = context.getDataTypeConfig(type);
        // Compute hash consisting of the fields: type, dataTypeConfig, negative, values
        final int typeHash = caseIndependentHashCode(node.getType());
        final int dataTypeConfigHash = dataTypeConfig.hashCode();
        final int negationHash = booleanHashCode(node.isNegative());
        final int valuesHash = node.getValues().stream()
                .map(BEAttributeValue::getId)
                .map(value -> toLogicalValue(value, dataTypeConfig))
                .map(String::hashCode)
                .reduce(0, Integer::sum); // By adding the hashes of each value, we get order independence
        final int[] intermediateHashes = new int[]{typeHash, dataTypeConfigHash, negationHash, valuesHash};
        return Arrays.hashCode(intermediateHashes);
    }

    private int hashReferenceNode(final BEReferenceNode node, final HashContext context)
    {
        // Compute hash consisting of the fields: type, negative, values
        final int typeHash = caseIndependentHashCode(node.getType());
        final int negationHash = booleanHashCode(node.isNegative());
        final int valuesHash = node.getValues().stream()
                .map(BEReferenceValue::getId)
                .map(context::getPartialExpressionHashCode)
                .reduce(0, Integer::sum); // By adding the hashes of each value, we get order independence
        final int[] intermediateHashes = new int[]{typeHash, negationHash, valuesHash};
        return Arrays.hashCode(intermediateHashes);
    }

    private String toLogicalValue(final String attributeValue, final BEDataTypeConfig config)
    {
        return config.isIgnoreCase() ? attributeValue.toUpperCase() : attributeValue;
    }

    private static int caseIndependentHashCode(final String string)
    {
        return string == null ? 0 : string.toUpperCase().hashCode();
    }

    private static int booleanHashCode(final boolean bool)
    {
        return bool ? 1231 : 1237;
    }

    private static class HashContext
    {
        private final boolean allowReferenceNodes;
        private final BEDataTypeConfigSupplier dataTypeConfigSupplier;
        private final Map<String, Integer> partialExpressionHashCodes;

        private HashContext(final BEDataTypeConfigSupplier dataTypeConfigSupplier)
        {
            this(dataTypeConfigSupplier, false, null);
        }

        private HashContext(final BEDataTypeConfigSupplier dataTypeConfigSupplier, final Map<String, Integer> partialExpressionHashCodes)
        {
            this(dataTypeConfigSupplier, true, partialExpressionHashCodes);
        }

        HashContext(
                final BEDataTypeConfigSupplier dataTypeConfigSupplier,
                final boolean allowReferenceNodes,
                final Map<String, Integer> partialExpressionHashCodes)
        {
            this.allowReferenceNodes = allowReferenceNodes;
            this.partialExpressionHashCodes = partialExpressionHashCodes;
            this.dataTypeConfigSupplier = dataTypeConfigSupplier;
        }

        public boolean allowReferenceNodes()
        {
            return this.allowReferenceNodes;
        }

        BEDataTypeConfig getDataTypeConfig(final String nodeCategoryType)
        {
            return this.dataTypeConfigSupplier.get(nodeCategoryType);
        }

        int getPartialExpressionHashCode(final String partialExpressionRefId)
        {
            if (!this.allowReferenceNodes)
            {
                throw new IllegalArgumentException("Nested partial expressions are not allowed.");
            }
            final Integer partialExpressionHashCode = this.partialExpressionHashCodes.get(partialExpressionRefId);
            if (partialExpressionHashCode == null)
            {
                throw new IllegalArgumentException("Reference to undefined partial expression " + partialExpressionRefId);
            }
            return partialExpressionHashCode;
        }
    }
}
