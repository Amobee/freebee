package com.amobee.freebee.evaluator.evaluator;

import java.util.List;
import java.util.function.Consumer;
import javax.annotation.Nonnull;

import com.google.common.annotations.VisibleForTesting;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.eclipse.collections.api.list.primitive.ImmutableLongList;
import org.eclipse.collections.api.list.primitive.MutableLongList;
import org.eclipse.collections.impl.list.mutable.primitive.LongArrayList;

import com.amobee.freebee.evaluator.BEInterval;
import com.amobee.freebee.evaluator.index.BEIndexAttributeCategory;

/**
 * @author Michael Bond
 */
@ToString
@NoArgsConstructor
public class BELongInputAttributeCategory implements BEInputAttributeCategory
{
    private final MutableLongList values = new LongArrayList();

    private BELongInputAttributeCategory(@Nonnull final BELongInputAttributeCategory category)
    {
        this.values.addAll(category.values);
    }

    public void add(final long value)
    {
        this.values.add(value);
    }

    @Override
    public void forEachMatchedInterval(
            @Nonnull final BEIndexAttributeCategory indexAttributeCategory,
            @Nonnull final Consumer<List<BEInterval>> consumer)
    {
        this.values.forEach(value -> indexAttributeCategory.getIntervals(value, consumer));
    }

    @Override
    public BELongInputAttributeCategory clone()
    {
        return new BELongInputAttributeCategory(this);
    }

    @VisibleForTesting
    public ImmutableLongList getValues()
    {
        return this.values.toImmutable();
    }
}