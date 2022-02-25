package com.amobee.freebee.evaluator.evaluator;

import com.amobee.freebee.evaluator.BEInterval;
import com.amobee.freebee.evaluator.index.BEAttributeCategoryMatchedIntervalConsumer;
import com.amobee.freebee.evaluator.index.BEIndexAttributeCategory;
import com.google.common.annotations.VisibleForTesting;
import lombok.EqualsAndHashCode;
import org.eclipse.collections.api.list.primitive.ImmutableByteList;
import org.eclipse.collections.api.list.primitive.MutableByteList;
import org.eclipse.collections.impl.list.mutable.primitive.ByteArrayList;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author Michael Bond
 */
@EqualsAndHashCode(callSuper = true)
public class BEByteInputAttributeCategory extends BEBaseInputAttributeCategory
{
    private final MutableByteList values = new ByteArrayList();

    public BEByteInputAttributeCategory(@Nonnull final String attributeCategoryName)
    {
        super(attributeCategoryName);
    }

    public BEByteInputAttributeCategory(@Nonnull final String attributeCategoryName, final boolean trackingEnabled)
    {
        super(attributeCategoryName, trackingEnabled);
    }

    private BEByteInputAttributeCategory(@Nonnull final BEByteInputAttributeCategory category)
    {
        super(category.getName(), category.isTrackingEnabled());
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
    public void forEachMatchedInterval(
            @Nonnull final BEIndexAttributeCategory indexAttributeCategory,
            @Nonnull final BEAttributeCategoryMatchedIntervalConsumer consumer)
    {
        this.values.forEach(value -> {
            BEByteInputAttributeCategory matchedInput = null;
            if (this.isTrackingEnabled())
            {
                matchedInput = new BEByteInputAttributeCategory(this.getName(), true);
                matchedInput.add(value);
            }
            indexAttributeCategory.getIntervals(value, matchedInput, consumer);
        });
    }

    @Override
    public BEByteInputAttributeCategory clone()
    {
        return new BEByteInputAttributeCategory(this);
    }

    @Override
    public <C extends BEInputAttributeCategory> void addAll(final C other)
    {
        if (!(other instanceof BEByteInputAttributeCategory))
        {
            throw new IllegalArgumentException("Expected "
                    + BEByteInputAttributeCategory.class.getSimpleName()
                    + " but was passed " + other.getClass().getSimpleName());
        }
        this.values.addAll(((BEByteInputAttributeCategory) other).values);
    }

    @VisibleForTesting
    public ImmutableByteList getValues()
    {
        return this.values.toImmutable();
    }

    @Override
    public String toString()
    {
        return "BEByteInputAttributeCategory{" +
                "name='" + this.name + '\'' +
                ", trackingEnabled=" + this.trackingEnabled +
                ", values=" + this.values +
                '}';
    }
}
