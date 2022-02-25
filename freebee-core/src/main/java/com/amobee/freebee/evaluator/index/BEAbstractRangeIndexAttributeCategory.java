package com.amobee.freebee.evaluator.index;

import com.amobee.freebee.evaluator.BEInterval;
import com.amobee.freebee.evaluator.evaluator.BEInputAttributeCategory;
import com.amobee.freebee.util.RangeUtils;
import com.google.common.collect.Range;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author Michael Bond
 * @author Kevin Doran
 */
public abstract class BEAbstractRangeIndexAttributeCategory<T extends Comparable<T>> extends BEIndexAttributeCategory
{
    /**
     * Unique range -> list of intervals map is useful for troubleshooting or debugging
     */
    @Nonnull
    private final Map<Range<T>, List<BEInterval>> unique = new HashMap<>();

    private final RangeCollectionMap<T, BEInterval> rangeIndex = new RangeCollectionMapImpl<>(ArrayList::new);

    @Override
    public void addInterval(@Nonnull final Object attributeValue, @Nonnull final BEInterval interval)
    {
        final Range<T> range = RangeUtils.createRange(attributeValue.toString(), this::valueOf);

        // Keeping track of a map of unique ranges -> intervals is not necessary to function, but useful for debugging
        final List<BEInterval> existingIntervalsForRange = this.unique.computeIfAbsent(range, k -> new ArrayList<>());
        existingIntervalsForRange.add(interval);

        this.rangeIndex.putAdd(range, interval);

        // if interval is negative, add a "wildcard" interval
        if (interval.isNegative())
        {
            addNegativeInterval(interval);
        }
    }

    @Override
    public void getIntervals(final byte attributeValue, @Nonnull final Consumer<List<BEInterval>> consumer)
    {
        callConsumer((List<BEInterval>) this.rangeIndex.get(valueOf(attributeValue)), consumer);
    }

    @Override
    public void getIntervals(final byte attributeValue, @Nullable final BEInputAttributeCategory matchedInput, @Nonnull final BEAttributeCategoryMatchedIntervalConsumer consumer)
    {
        callConsumer((List<BEInterval>) this.rangeIndex.get(valueOf(attributeValue)), matchedInput, consumer);
    }

    @Override
    public void getIntervals(final double attributeValue, @Nonnull final Consumer<List<BEInterval>> consumer)
    {
        callConsumer((List<BEInterval>) this.rangeIndex.get(valueOf(attributeValue)), consumer);
    }

    @Override
    public void getIntervals(final double attributeValue, @Nullable final BEInputAttributeCategory matchedInput, @Nonnull final BEAttributeCategoryMatchedIntervalConsumer consumer)
    {
        callConsumer((List<BEInterval>) this.rangeIndex.get(valueOf(attributeValue)), matchedInput, consumer);
    }

    @Override
    public void getIntervals(final int attributeValue, @Nonnull final Consumer<List<BEInterval>> consumer)
    {
        callConsumer((List<BEInterval>) this.rangeIndex.get(valueOf(attributeValue)), consumer);
    }

    @Override
    public void getIntervals(final int attributeValue, @Nullable final BEInputAttributeCategory matchedInput, @Nonnull final BEAttributeCategoryMatchedIntervalConsumer consumer)
    {
        callConsumer((List<BEInterval>) this.rangeIndex.get(valueOf(attributeValue)), matchedInput, consumer);
    }

    @Override
    public void getIntervals(final long attributeValue, @Nonnull final Consumer<List<BEInterval>> consumer)
    {
        callConsumer((List<BEInterval>) this.rangeIndex.get(valueOf(attributeValue)), consumer);
    }

    @Override
    public void getIntervals(final long attributeValue, @Nullable final BEInputAttributeCategory matchedInput, @Nonnull final BEAttributeCategoryMatchedIntervalConsumer consumer)
    {
        callConsumer((List<BEInterval>) this.rangeIndex.get(valueOf(attributeValue)), matchedInput, consumer);
    }

    @Override
    public void getIntervals(@Nonnull final String attributeValue, @Nonnull final Consumer<List<BEInterval>> consumer)
    {
        final Range<T> range = RangeUtils.createRange(attributeValue, this::valueOf);
        callConsumer((List<BEInterval>) this.rangeIndex.get(range), consumer);
    }

    @Override
    public void getIntervals(
            @Nonnull final String attributeValue,
            @Nullable final BEInputAttributeCategory matchedInput,
            @Nonnull final BEAttributeCategoryMatchedIntervalConsumer consumer)
    {
        final Range<T> range = RangeUtils.createRange(attributeValue, this::valueOf);
        callConsumer((List<BEInterval>) this.rangeIndex.get(range), matchedInput, consumer);
    }

    protected T valueOf(final byte value)
    {
        throw new UnsupportedOperationException("Cowardly refusal to risk loosing precision");
    }

    protected T valueOf(final double value)
    {
        throw new UnsupportedOperationException("Cowardly refusal to risk loosing precision");
    }

    protected T valueOf(final int value)
    {
        throw new UnsupportedOperationException("Cowardly refusal to risk loosing precision");
    }

    protected T valueOf(final long value)
    {
        throw new UnsupportedOperationException("Cowardly refusal to risk loosing precision");
    }

    protected T valueOf(@Nonnull final String value)
    {
        throw new UnsupportedOperationException("Cowardly refusal to risk loosing precision");
    }

    @Override
    protected void compact()
    {
        this.unique.values().forEach(value -> ((ArrayList<BEInterval>) value).trimToSize());
        this.rangeIndex.compact();
    }

}
