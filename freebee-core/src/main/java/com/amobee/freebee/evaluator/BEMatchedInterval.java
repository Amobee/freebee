package com.amobee.freebee.evaluator;

import com.amobee.freebee.evaluator.evaluator.BEInputAttributeCategory;
import com.amobee.freebee.evaluator.index.BEIndexExpressionResult;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.BitSet;

/**
 * Expression interval capable of tracking the input values that resulted in matching an interval.
 *
 * Applications should never create instances of this class directly, it is used internally be a BEEvaluator.
 *
 * @author Kevin Doran
 */
@Getter
@ToString
@EqualsAndHashCode(callSuper = true)
public class BEMatchedInterval extends BEInterval
{
    private static final long serialVersionUID = -2315253087374471504L;

    private BEInputAttributeCategory matchedInputValues;
    private final boolean partialExpressionReferenceInterval;
    private BEIndexExpressionResult partialExpressionIndexResult;

    public BEMatchedInterval(
            final int expressionId,
            final int intervalId,
            final boolean isPartialExpressionReferenceInterval,
            final boolean canUseBitSetMatching,
            final boolean negative,
            @Nonnull final BitSet bits)
    {
        super(expressionId, intervalId, canUseBitSetMatching, negative, bits);
        this.partialExpressionReferenceInterval = isPartialExpressionReferenceInterval;
    }

    public BEMatchedInterval(@Nonnull final BEInterval other)
    {
        this(other, false);
    }

    public BEMatchedInterval(@Nonnull final BEInterval other, final boolean isPartialExpressionReferenceInterval)
    {
        this(
                other.getExpressionId(),
                other.getIntervalId(),
                isPartialExpressionReferenceInterval,
                other.isCanUseBitSetMatching(),
                other.isNegative(),
                other.getBits()
        );
    }

    public void addMatchedInputValues(@Nullable final BEInputAttributeCategory matchedInputAttributeCategoryValues)
    {
        if (matchedInputAttributeCategoryValues != null)
        {
            if (this.matchedInputValues == null)
            {
                this.matchedInputValues = matchedInputAttributeCategoryValues.clone();
            } else
            {
                // Assert that incoming attribute category type / name matches the existing one for this interval
                // and add incoming value(s) to the existing values for this interval.
                if (!matchedInputAttributeCategoryValues.getClass().isAssignableFrom(this.matchedInputValues.getClass())
                        || !matchedInputAttributeCategoryValues.getName().equals(this.matchedInputValues.getName()))
                {
                    throw new IllegalStateException(
                            "Unexpected multiple attribute categories for a single expression interval. "
                                    + "Was expecting " + this.matchedInputValues.getClass().getSimpleName()
                                    + " named '" + this.matchedInputValues.getName() + "', but got"
                                    + matchedInputAttributeCategoryValues.getClass().getSimpleName()
                                    + " named '" + matchedInputAttributeCategoryValues.getName() + "'");
                }
                this.matchedInputValues.addAll(matchedInputAttributeCategoryValues);
            }
        }
    }

    public void setPartialExpressionIndexResult(final BEIndexExpressionResult partialExpressionIndexResult)
    {
        this.partialExpressionIndexResult = partialExpressionIndexResult;
    }

};
