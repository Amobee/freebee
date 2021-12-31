package com.amobee.freebee.evaluator.index;

import com.amobee.freebee.evaluator.BEInterval;
import com.amobee.freebee.evaluator.BEMatchedInterval;
import com.amobee.freebee.evaluator.evaluator.BEInputAttributeCategory;
import org.eclipse.collections.api.map.primitive.MutableIntObjectMap;
import org.eclipse.collections.impl.factory.primitive.IntObjectMaps;

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
    private final MutableIntObjectMap<BEMatchedInterval> matchedIntervals;
    private final BitSet matchedBits;

    BEIndexExpressionResult(final BEExpressionMetadata metadata)
    {
        this.expressionMetadata = metadata;
        this.useBitSetMatching = metadata.canUseBitSetMatching();
        this.matchedIntervals = IntObjectMaps.mutable.empty();
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

    public List<BEMatchedInterval> getMatchedIntervals()
    {
        return new ArrayList<>(this.matchedIntervals.values());
    }

    public BitSet getMatchedBits()
    {
        return this.matchedBits;
    }

    void addInterval(final BEInterval interval, final BEInputAttributeCategory matchedInputValues)
    {
        final int intervalId = interval.getIntervalId();
        final BEMatchedInterval matchedInterval = this.matchedIntervals.get(intervalId);
        if (matchedInterval == null)
        {
            // Create a matched interval from the raw interval and add it to the matched intervals for this expression
            final BEMatchedInterval newMatchedInterval = new BEMatchedInterval(interval);
            newMatchedInterval.addMatchedInputValues(matchedInputValues);
            this.matchedIntervals.put(intervalId, newMatchedInterval);
            if (this.useBitSetMatching)
            {
                this.matchedBits.or(interval.getBits());
            }
        } else
        {
            // Update matched interval with new input
            matchedInterval.addMatchedInputValues(matchedInputValues);
        }
    }

}
