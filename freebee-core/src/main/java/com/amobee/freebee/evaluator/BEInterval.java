package com.amobee.freebee.evaluator;

import com.amobee.freebee.evaluator.interval.Interval;

import lombok.Data;

import java.io.Serializable;
import java.util.BitSet;
import javax.annotation.Nonnull;

/**
 * Expression interval representing a single conjunction node in a boolean expression. Expression intervals are stored
 * in an expression index and evaluated in the expression evaluator.
 *
 * Applications should never create instances of this class directly, use
 * {@link com.amobee.freebee.evaluator.evaluator.BEEvaluatorBuilder} instead.
 *
 * @author Michael Bond
 * @see com.amobee.freebee.evaluator.evaluator.BEEvaluatorBuilder
 * @see <a href="https://videologygroup.atlassian.net/wiki/pages/viewpage.action?pageId=24119554">Design: Boolean Expressions & Evaluator</a>
 */
@Data
public class BEInterval implements Interval, Serializable
{
    private static final long serialVersionUID = -2315253087374471503L;

    private final int expressionId;
    private final int intervalId;
    private final boolean canUseBitSetMatching;
    private final boolean negative;
    private final BitSet bits;
    private short start;
    private short end;

    public BEInterval(
            final int expressionId,
            final int intervalId,
            final boolean canUseBitSetMatching,
            final boolean negative,
            @Nonnull final BitSet bits)
    {
        this.expressionId = expressionId;
        this.intervalId = intervalId;
        this.canUseBitSetMatching = canUseBitSetMatching;
        this.negative = negative;
        this.bits = bits;
        this.start = (short) bits.nextSetBit(0);
        this.end = (short) bits.nextClearBit(this.start);
    }

    public BEInterval(@Nonnull final BEInterval other, final boolean negative)
    {
        this(other.expressionId, other.intervalId, other.canUseBitSetMatching, negative, other.bits);
    }
}
