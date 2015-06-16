package com.amobee.freebee.evaluator.index;

import com.amobee.freebee.expression.BENode;

import java.util.Collection;
import java.util.Map;

public interface BEExpressionHashProvider<T>
{

    /**
     * Compute the hash of a single expression,
     * taking the data type matching evaluation rules into account.
     *
     * @param expression the expression to hash
     * @param dataTypeConfigSupplier the data type configuration supplier for the given expressions
     * @return the hashCode of the expression
     */
    int computeHash(BEExpressionInfo<T> expression, BEDataTypeConfigSupplier dataTypeConfigSupplier);

    /**
     *
     * Compute the hash of a single expression,
     * taking any referenced partial expressions and
     * the data type matching evaluation rules into account.
     *
     * @param expression the expression to hash
     * @param partialExpressions and partial expressions referenced from the expression
     * @param dataTypeConfigSupplier the data type configuration supplier for the given expressions
     * @return the hashCode of the expression
     */
    int computeHash(
            BEExpressionInfo<T> expression,
            Collection<BENode> partialExpressions,
            BEDataTypeConfigSupplier dataTypeConfigSupplier);

    /**
     * Compute the hash of a collection of expression,
     * taking the data type matching evaluation rules into account.
     *
     * @param expressions the expression to hash
     * @param dataTypeConfigSupplier the data type configuration supplier for the given expressions
     * @return the hashCode of the expressions
     */
    int computeHash(
            Collection<BEExpressionInfo<T>> expressions,
            BEDataTypeConfigSupplier dataTypeConfigSupplier);

    /**
     *
     * Compute the hash of a collection of expressions,
     * taking any referenced partial expressions and
     * the data type matching evaluation rules into account.
     *
     * @param expressions the expressions to hash
     * @param partialExpressions and partial expressions referenced from the expression
     * @param dataTypeConfigSupplier the data type configuration supplier for the given expressions
     * @return the hashCode of the expressions
     */
    int computeHash(
            Collection<BEExpressionInfo<T>> expressions,
            Collection<BENode> partialExpressions,
            BEDataTypeConfigSupplier dataTypeConfigSupplier);

    /**
     *
     * Compute the hash of a collection of expressions,
     * taking any referenced partial expressions and
     * the data type matching evaluation rules into account.
     *
     * @param expressions the expressions to hash
     * @param partialExpressions and partial expressions referenced from the expression
     * @param dataTypeConfigSupplier the data type configuration supplier for the given expressions
     * @return the hashCode of the expressions
     */
    int computeHash(
            Collection<BEExpressionInfo<T>> expressions,
            Map<String, BENode> partialExpressions,
            BEDataTypeConfigSupplier dataTypeConfigSupplier);

}
