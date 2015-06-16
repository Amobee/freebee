package com.amobee.freebee.evaluator.index;

import java.io.Serializable;
import java.util.Collection;
import javax.annotation.Nonnull;

import org.eclipse.collections.api.map.primitive.MutableIntObjectMap;
import org.eclipse.collections.impl.factory.primitive.IntObjectMaps;

/**
 * A IntObjectMap-backed implementation of {@link BEExpressionMetadataProvider}
 * that also supports adding expression metadata by expression id.
 *
 * @author Kevin Doran
 */
public class BEExpressionMetadataProviderImpl implements BEExpressionMetadataProvider, Serializable
{
    private static final long serialVersionUID = 3782022675751928889L;

    private final MutableIntObjectMap<BEExpressionMetadata> expressionMetadataMap = IntObjectMaps.mutable.empty();

    @Override
    public BEExpressionMetadata get(final int expressionId)
    {
        return this.expressionMetadataMap.get(expressionId);
    }

    @Override
    public Collection<BEExpressionMetadata> getAll()
    {
        return this.expressionMetadataMap.values();
    }

    void put(final int expressionId, @Nonnull final BEExpressionMetadata metadata)
    {
        this.expressionMetadataMap.put(expressionId, metadata);
    }

}
