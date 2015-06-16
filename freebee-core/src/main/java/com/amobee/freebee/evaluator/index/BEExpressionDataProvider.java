package com.amobee.freebee.evaluator.index;

/**
 A simple interface for retrieving expression data by expression id,
 *  used internally in the evaluator and index.
 *
 * @param <T> the type of the Data associated with each expression
 *
 * @author Kevin Doran
 */
public interface BEExpressionDataProvider<T>
{
    T get(int expressionId);
}
