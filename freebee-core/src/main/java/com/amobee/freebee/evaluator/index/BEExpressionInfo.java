package com.amobee.freebee.evaluator.index;

import com.amobee.freebee.expression.BENode;

import javax.annotation.Nonnull;

public class BEExpressionInfo<T>
{

    private final T data;
    private final BENode expression;

    public BEExpressionInfo(@Nonnull final T data, @Nonnull final BENode expression)
    {
        this.data = data;
        this.expression = expression;
    }

    @Nonnull public T getData()
    {
        return this.data;
    }

    @Nonnull public BENode getExpression()
    {
        return this.expression;
    }

}
