package com.amobee.freebee.evaluator.index;

import com.amobee.freebee.evaluator.BEInterval;

import java.util.Collection;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.amobee.freebee.evaluator.evaluator.BEInputAttributeCategory;
import org.eclipse.collections.api.map.primitive.MutableIntObjectMap;
import org.eclipse.collections.impl.factory.primitive.IntObjectMaps;

/**
 * A class representing the results of querying a {@link BEIndex} for a given
 * {@link com.amobee.freebee.evaluator.evaluator.BEInput}.
 *
 * Designed for internal use only by a
 * @link com.amobee.freebee.evaluator.evaluator.BEEvaluator}.
 *
 * @author Kevin Doran.
 */
public class BEIndexResults
{
    private final MutableIntObjectMap<BEIndexExpressionResult> indexExpressionResults;
    private final BEExpressionMetadataProvider expressionMetadataProvider;

    BEIndexResults(@Nonnull final BEExpressionMetadataProvider expressionMetadataProvider)
    {
        this.expressionMetadataProvider = expressionMetadataProvider;
        this.indexExpressionResults = IntObjectMaps.mutable.withInitialCapacity(expressionMetadataProvider.getAll().size());
    }

    public Collection<BEIndexExpressionResult> getExpressionResults()
    {
        return this.indexExpressionResults.values();
    }

    public void addInterval(@Nonnull final BEInterval interval)
    {
        addInterval(interval, null, null);
    }

    public void addInterval(
            @Nonnull final BEInterval interval,
            @Nullable final BEInputAttributeCategory matchedInputValue,
            @Nullable final BEIndexExpressionResult partialExpressionResult)
    {
        final int exprId = interval.getExpressionId();
        this.indexExpressionResults
                .getIfAbsentPut(exprId, createExpressionIndexResultBuilder(exprId))
                .addInterval(interval, matchedInputValue, partialExpressionResult);
    }

    private BEIndexExpressionResult createExpressionIndexResultBuilder(final int expressionId)
    {
        return new BEIndexExpressionResult(this.expressionMetadataProvider.get(expressionId));
    }



}
