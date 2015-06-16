package com.amobee.freebee.evaluator.evaluator;

import java.util.List;
import java.util.function.Consumer;
import javax.annotation.Nonnull;

import com.google.common.annotations.VisibleForTesting;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.eclipse.collections.api.list.primitive.ImmutableDoubleList;
import org.eclipse.collections.api.list.primitive.MutableDoubleList;
import org.eclipse.collections.impl.list.mutable.primitive.DoubleArrayList;

import com.amobee.freebee.evaluator.BEInterval;
import com.amobee.freebee.evaluator.index.BEIndexAttributeCategory;

/**
 * @author Michael Bond
 */
@ToString
@NoArgsConstructor
public class BEDoubleInputAttributeCategory implements BEInputAttributeCategory
{
    private final MutableDoubleList values = new DoubleArrayList();

    private BEDoubleInputAttributeCategory(@Nonnull final BEDoubleInputAttributeCategory category)
    {
        this.values.addAll(category.values);
    }

    public void add(final double value)
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
    public BEDoubleInputAttributeCategory clone()
    {
        return new BEDoubleInputAttributeCategory(this);
    }

    @VisibleForTesting
    public ImmutableDoubleList getValues()
    {
        return this.values.toImmutable();
    }
}
