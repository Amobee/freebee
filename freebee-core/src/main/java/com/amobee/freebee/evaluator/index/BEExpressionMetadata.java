package com.amobee.freebee.evaluator.index;

/**
 * An interface for an object that provides metadata about a boolean expression relating,
 * used in the control flow at evaluation time in the evaluator and index.
 *
 * @author Kevin Doran
 */
public interface BEExpressionMetadata
{

    int getExpressionId();

    int getMaxIntervalLength();

    boolean canUseBitSetMatching();

    boolean isPartial();

    String getPartialExpressionName();

}
