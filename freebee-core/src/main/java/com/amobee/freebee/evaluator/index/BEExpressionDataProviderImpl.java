package com.amobee.freebee.evaluator.index;

import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nonnull;

import org.eclipse.collections.api.map.primitive.MutableIntObjectMap;
import org.eclipse.collections.impl.factory.primitive.IntObjectMaps;

/**
 * A IntObjectMap-backed implementation of {@link BEExpressionDataProvider}
 * that also supports adding expression metadata by expression id.
 *
 * @param <T> the type of data associated with each expression
 *
 * @author Kevin Doran
 */
class BEExpressionDataProviderImpl<T> implements BEExpressionDataProvider<T>, Serializable
{
    private static final long serialVersionUID = 2362070283028637260L;

    private final MutableIntObjectMap<T> expressionData;

    BEExpressionDataProviderImpl()
    {
        this.expressionData = IntObjectMaps.mutable.empty();
    }

    @Override
    public T get(final int expressionId)
    {
        return this.expressionData.get(expressionId);
    }

    void put(final int expressionId, @Nonnull final T data)
    {
        this.expressionData.put(expressionId, data);
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        final BEExpressionDataProviderImpl<?> that = (BEExpressionDataProviderImpl<?>) o;
        return Objects.equals(this.expressionData, that.expressionData);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(this.expressionData);
    }
}
