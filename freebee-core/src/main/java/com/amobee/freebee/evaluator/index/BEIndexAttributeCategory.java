package com.amobee.freebee.evaluator.index;

import com.amobee.freebee.evaluator.BEInterval;
import lombok.Getter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class represents a single attribute category (age, gender, etc) within the index. It contains a map of all
 * indexed attribute values for the attribute category and intervals for any expressions that referenced that attribute
 * category in a predicate node.
 *
 * NOTE: There is only one method for adding intervals but one method per type for getting intervals. It was implemented
 * this way because boxing and parsing is not so important when building the index but is highly important during
 * runtime.
 *
 * @author Michael Bond
 */
@Getter
public abstract class BEIndexAttributeCategory implements Serializable
{
    private static final long serialVersionUID = -2797903862045133742L;

    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    @Nonnull
    private final List<BEInterval> negativeIntervals = new ArrayList<>();

    /* package */ BEIndexAttributeCategory()
    {
    }

    public abstract void addInterval(@Nonnull Object attributeValue, @Nonnull BEInterval interval);

    /**
     * Get intervals matching the specified attribute value.
     *
     * @param attributeValue
     *         Attribute value to get intervals for.
     * @param consumer
     *         Consumer to call with matched intervals.
     */
    public void getIntervals(final byte attributeValue, @Nonnull final Consumer<List<BEInterval>> consumer)
    {
        throw new UnsupportedOperationException("Cowardly refusal to risk loosing precision");
    }

    /**
     * Get intervals matching the specified attribute value.
     *
     * @param attributeValue
     *         Attribute value to get intervals for.
     * @param consumer
     *         Consumer to call with matched intervals.
     */
    public void getIntervals(final double attributeValue, @Nonnull final Consumer<List<BEInterval>> consumer)
    {
        throw new UnsupportedOperationException("Cowardly refusal to risk loosing precision");
    }

    /**
     * Get intervals matching the specified attribute value.
     *
     * @param attributeValue
     *         Attribute value to get intervals for.
     * @param consumer
     *         Consumer to call with matched intervals.
     */
    public void getIntervals(final int attributeValue, @Nonnull final Consumer<List<BEInterval>> consumer)
    {
        throw new UnsupportedOperationException("Cowardly refusal to risk loosing precision");
    }

    /**
     * Get intervals matching the specified attribute value.
     *
     * @param attributeValue
     *         Attribute value to get intervals for.
     * @param consumer
     *         Consumer to call with matched intervals.
     */
    public void getIntervals(final long attributeValue, @Nonnull final Consumer<List<BEInterval>> consumer)
    {
        throw new UnsupportedOperationException("Cowardly refusal to risk loosing precision");
    }

    /**
     * Get intervals matching the specified attribute value.
     *
     * @param attributeValue
     *         Attribute value to get intervals for.
     * @param consumer
     *         Consumer to call with matching intervals.
     */
    public abstract void getIntervals(@Nonnull String attributeValue, @Nonnull Consumer<List<BEInterval>> consumer);

    /**
     * Minimize the amount of memory used by the index
     */
    protected abstract void compact();

    protected void addNegativeInterval(@Nonnull final BEInterval interval)
    {
        this.negativeIntervals.add(new BEInterval(interval, false));
    }

    protected void callConsumer(
            @Nullable final List<BEInterval> intervals,
            @Nonnull final Consumer<List<BEInterval>> consumer)
    {
        if (null != intervals)
        {
            consumer.accept(intervals);
        }
    }
}
