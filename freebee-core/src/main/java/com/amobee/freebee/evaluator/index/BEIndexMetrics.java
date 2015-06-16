package com.amobee.freebee.evaluator.index;

import java.io.Serializable;

/**
 * Summary metrics that describe a BEIndex
 */
public class BEIndexMetrics implements Serializable
{
    private static final long serialVersionUID = -5061085943155484773L;

    private int intervalCount;
    private int expressionCount;
    private int fullExpressionCount;
    private int partialExpressionCount;
    private int expressionCountWithBitSetEvaluation;
    private int expressionCountWithIntervalEvaluation;

    public BEIndexMetrics() {}

    public int getIntervalCount()
    {
        return this.intervalCount;
    }

    public void setIntervalCount(final int intervalCount)
    {
        this.intervalCount = intervalCount;
    }

    public int getExpressionCount()
    {
        return this.expressionCount;
    }

    public void setExpressionCount(final int expressionCount)
    {
        this.expressionCount = expressionCount;
    }

    public int getFullExpressionCount()
    {
        return this.fullExpressionCount;
    }

    public void setFullExpressionCount(final int fullExpressionCount)
    {
        this.fullExpressionCount = fullExpressionCount;
    }

    public int getPartialExpressionCount()
    {
        return this.partialExpressionCount;
    }

    public void setPartialExpressionCount(final int partialExpressionCount)
    {
        this.partialExpressionCount = partialExpressionCount;
    }

    public int getExpressionCountWithBitSetEvaluation()
    {
        return this.expressionCountWithBitSetEvaluation;
    }

    public void setExpressionCountWithBitSetEvaluation(final int expressionCountWithBitSetEvaluation)
    {
        this.expressionCountWithBitSetEvaluation = expressionCountWithBitSetEvaluation;
    }

    public int getExpressionCountWithIntervalEvaluation()
    {
        return this.expressionCountWithIntervalEvaluation;
    }

    public void setExpressionCountWithIntervalEvaluation(final int expressionCountWithIntervalEvaluation)
    {
        this.expressionCountWithIntervalEvaluation = expressionCountWithIntervalEvaluation;
    }

    @Override
    public String toString()
    {
        return "BEIndexMetrics{" +
                "intervalCount=" + this.intervalCount +
                ", expressionCount=" + this.expressionCount +
                ", fullExpressionCount=" + this.fullExpressionCount +
                ", partialExpressionCount=" + this.partialExpressionCount +
                ", expressionCountWithBitSetEvaluation=" + this.expressionCountWithBitSetEvaluation +
                ", expressionCountWithIntervalEvaluation=" + this.expressionCountWithIntervalEvaluation +
                '}';
    }
}
