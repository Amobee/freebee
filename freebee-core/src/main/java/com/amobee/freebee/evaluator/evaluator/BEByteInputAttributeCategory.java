package com.amobee.freebee.evaluator.evaluator;

import java.util.List;
import java.util.function.Consumer;
import javax.annotation.Nonnull;

import com.amobee.freebee.evaluator.BEInterval;
import com.amobee.freebee.evaluator.index.BEIndexAttributeCategory;
import com.google.common.annotations.VisibleForTesting;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.eclipse.collections.api.list.primitive.ImmutableByteList;
import org.eclipse.collections.api.list.primitive.MutableByteList;
import org.eclipse.collections.impl.list.mutable.primitive.ByteArrayList;

/**
 * @author Michael Bond
 */
@ToString
@NoArgsConstructor
public class BEByteInputAttributeCategory implements BEInputAttributeCategory
{
    private final MutableByteList values = new ByteArrayList();

    private BEByteInputAttributeCategory(@Nonnull final BEByteInputAttributeCategory category)
    {
        this.values.addAll(category.values);
    }

    public void add(final byte value)
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
    public BEByteInputAttributeCategory clone()
    {
        return new BEByteInputAttributeCategory(this);
    }

    @VisibleForTesting
    public ImmutableByteList getValues()
    {
        return this.values.toImmutable();
    }
}
