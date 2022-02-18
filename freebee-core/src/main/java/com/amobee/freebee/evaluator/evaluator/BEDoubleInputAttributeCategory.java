package com.amobee.freebee.evaluator.evaluator;

import com.amobee.freebee.evaluator.BEInterval;
import com.amobee.freebee.evaluator.index.BEAttributeCategoryMatchedIntervalConsumer;
import com.amobee.freebee.evaluator.index.BEIndexAttributeCategory;
import com.google.common.annotations.VisibleForTesting;
import lombok.EqualsAndHashCode;
import org.eclipse.collections.api.list.primitive.ImmutableDoubleList;
import org.eclipse.collections.api.list.primitive.MutableDoubleList;
import org.eclipse.collections.impl.list.mutable.primitive.DoubleArrayList;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author Michael Bond
 */
@EqualsAndHashCode(callSuper = true)
public class BEDoubleInputAttributeCategory extends BEBaseInputAttributeCategory
{
    private final MutableDoubleList values = new DoubleArrayList();

    public BEDoubleInputAttributeCategory(@Nonnull final String attributeCategoryName)
    {
        super(attributeCategoryName);
    }

    public BEDoubleInputAttributeCategory(@Nonnull final String attributeCategoryName, final boolean trackingEnabled)
    {
        super(attributeCategoryName, trackingEnabled);
    }

    private BEDoubleInputAttributeCategory(@Nonnull final BEDoubleInputAttributeCategory category)
    {
        super(category.getName(), category.isTrackingEnabled());
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
    public void forEachMatchedInterval(
            @Nonnull final BEIndexAttributeCategory indexAttributeCategory,
            @Nonnull final BEAttributeCategoryMatchedIntervalConsumer consumer)
    {
        this.values.forEach(value -> {
            BEDoubleInputAttributeCategory matchedInput = null;
            if (this.isTrackingEnabled())
            {
                matchedInput = new BEDoubleInputAttributeCategory(this.getName(), true);
                matchedInput.add(value);
            }
            indexAttributeCategory.getIntervals(value, matchedInput, consumer);
        });
    }

    @Override
    public <C extends BEInputAttributeCategory> void addAll(final C other)
    {
        if (!(other instanceof BEDoubleInputAttributeCategory))
        {
            throw new IllegalArgumentException("Expected "
                    + BEDoubleInputAttributeCategory.class.getSimpleName()
                    + " but was passed " + other.getClass().getSimpleName());
        }
        this.values.addAll(((BEDoubleInputAttributeCategory) other).values);
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

    @Override
    public String toString()
    {
        return "BEDoubleInputAttributeCategory{" +
                "name='" + this.name + '\'' +
                ", trackingEnabled=" + this.trackingEnabled +
                ", values=" + this.values +
                '}';
    }
}
