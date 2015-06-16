package com.amobee.freebee.evaluator;

import lombok.Data;

import java.io.Serializable;
import java.util.BitSet;
import javax.annotation.Nonnull;

/**
 * Expression metadata used by the expression indexer and evaluator.
 *
 * Applications should never create instances of this class directly, use
 * {@link com.amobee.freebee.evaluator.evaluator.BEEvaluatorBuilder} instead.
 *
 * @author Michael Bond
 * @see com.amobee.freebee.evaluator.evaluator.BEEvaluatorBuilder
 * @see <a href="https://videologygroup.atlassian.net/wiki/pages/viewpage.action?pageId=24119554">Design: Boolean Expressions & Evaluator</a>
 */
@Deprecated
@Data
public class BEInfo<T> implements Serializable
{
    private static final long serialVersionUID = 3357276960480889394L;

    @Nonnull
    private final T data;
    private final int expressionId;
    private final boolean partial;
    @Nonnull
    private final BitSet bits;

    public BEInfo(@Nonnull final T data, final int expressionId, final boolean partial, final int intervalLength)
    {
        this.data = data;
        this.expressionId = expressionId;
        this.partial = partial;
        this.bits = new BitSet();
        this.bits.set(0, intervalLength);
    }
}
