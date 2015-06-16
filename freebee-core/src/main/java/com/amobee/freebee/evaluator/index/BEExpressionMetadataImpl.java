package com.amobee.freebee.evaluator.index;

import java.io.Serializable;

/**
 * A simple implementation of {@link BEExpressionMetadata} for use in a {@link BEIndex}.
 *
 * @author kdoran
 */
class BEExpressionMetadataImpl implements BEExpressionMetadata, Serializable
{

    private final int expressionId;
    private final int maxIntervalLength;
    private final boolean useBitSetMatching;
    private final boolean partial;
    private final String partialExpressionName;

    BEExpressionMetadataImpl(final int expressionId, final int maxIntervalLength, final boolean useBitSetMatching)
    {
        this(expressionId, maxIntervalLength, useBitSetMatching, false, null);
    }

    BEExpressionMetadataImpl(final int expressionId, final int maxIntervalLength, final boolean useBitSetMatching, final boolean partial, final String partialExpressionName)
    {
        this.expressionId = expressionId;
        this.maxIntervalLength = maxIntervalLength;
        this.useBitSetMatching = useBitSetMatching;
        this.partial = partial;
        this.partialExpressionName = partialExpressionName;
    }

    @Override
    public int getExpressionId()
    {
        return this.expressionId;
    }

    @Override
    public int getMaxIntervalLength()
    {
        return this.maxIntervalLength;
    }

    @Override
    public boolean canUseBitSetMatching()
    {
        return this.useBitSetMatching;
    }

    @Override
    public boolean isPartial()
    {
        return this.partial;
    }

    @Override
    public String getPartialExpressionName()
    {
        return this.partialExpressionName;
    }
}
