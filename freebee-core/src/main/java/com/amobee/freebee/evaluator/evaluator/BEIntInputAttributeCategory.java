package com.amobee.freebee.evaluator.evaluator;

import java.util.List;
import java.util.function.Consumer;
import javax.annotation.Nonnull;

import com.google.common.annotations.VisibleForTesting;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.eclipse.collections.api.list.primitive.ImmutableIntList;
import org.eclipse.collections.api.list.primitive.MutableIntList;
import org.eclipse.collections.impl.list.mutable.primitive.IntArrayList;

import com.amobee.freebee.evaluator.BEInterval;
import com.amobee.freebee.evaluator.index.BEIndexAttributeCategory;

/**
 * @author Michael Bond
 */
@ToString
@NoArgsConstructor
public class BEIntInputAttributeCategory implements BEInputAttributeCategory
{
    private final MutableIntList values = new IntArrayList();

    private BEIntInputAttributeCategory(@Nonnull final BEIntInputAttributeCategory category)
    {
        this.values.addAll(category.values);
    }

    public void add(final int value)
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
    public BEIntInputAttributeCategory clone()
    {
        return new BEIntInputAttributeCategory(this);
    }

    @VisibleForTesting
    public ImmutableIntList getValues()
    {
        return this.values.toImmutable();
    }
}
