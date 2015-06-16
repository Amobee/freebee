package com.amobee.freebee.evaluator.index;

import com.amobee.freebee.evaluator.BEInterval;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

/**
 * A class representing the result of an expression and its intervals when searching a BEIndex.
 *
 * @author Kevin Doran
 */
@SuppressWarnings("unused")
public class BEIndexExpressionResult implements BEExpressionMetadata, Serializable
{
    private static final long serialVersionUID = -5593940785078733262L;

    private final BEExpressionMetadata expressionMetadata;
    private final boolean useBitSetMatching;
    private final List<BEInterval> matchedIntervals;
    private final BitSet matchedBits;

    BEIndexExpressionResult(final BEExpressionMetadata metadata)
    {
        this.expressionMetadata = metadata;
        this.useBitSetMatching = metadata.canUseBitSetMatching();
        this.matchedIntervals = this.useBitSetMatching ? null : new ArrayList<>();
        this.matchedBits = this.useBitSetMatching ? new BitSet() : null;
    }

    @Override
    public int getExpressionId()
    {
        return this.expressionMetadata.getExpressionId();
    }

    @Override
    public int getMaxIntervalLength()
    {
        return this.expressionMetadata.getMaxIntervalLength();
    }

    @Override
    public boolean canUseBitSetMatching()
    {
        return this.expressionMetadata.canUseBitSetMatching();
    }

    @Override
    public boolean isPartial()
    {
        return this.expressionMetadata.isPartial();
    }

    @Override
    public String getPartialExpressionName()
    {
        return this.expressionMetadata.getPartialExpressionName();
    }

    public List<BEInterval> getMatchedIntervals()
    {
        return this.matchedIntervals;
    }

    public BitSet getMatchedBits()
    {
        return this.matchedBits;
    }

    void addInterval(final BEInterval interval)
    {
        if (this.useBitSetMatching)
        {
            this.matchedBits.or(interval.getBits());
        }
        else
        {
            this.matchedIntervals.add(interval);
        }
    }

}
