package com.amobee.freebee.evaluator.evaluator;

import com.amobee.freebee.evaluator.BEInterval;
import com.amobee.freebee.evaluator.index.BEAttributeCategoryMatchedIntervalConsumer;
import com.amobee.freebee.evaluator.index.BEIndexAttributeCategory;
import com.google.common.annotations.VisibleForTesting;
import lombok.EqualsAndHashCode;
import org.eclipse.collections.api.list.primitive.ImmutableIntList;
import org.eclipse.collections.api.list.primitive.MutableIntList;
import org.eclipse.collections.impl.list.mutable.primitive.IntArrayList;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * @author Michael Bond
 */
@EqualsAndHashCode(callSuper = true)
public class BEIntInputAttributeCategory extends BEBaseInputAttributeCategory
{
    private final MutableIntList values = new IntArrayList();

    public BEIntInputAttributeCategory(@Nonnull final String attributeCategoryName)
    {
        super(attributeCategoryName);
    }

    public BEIntInputAttributeCategory(@Nonnull final String attributeCategoryName, final boolean trackingEnabled)
    {
        super(attributeCategoryName, trackingEnabled);
    }

    private BEIntInputAttributeCategory(@Nonnull final BEIntInputAttributeCategory category)
    {
        super(category.getName(), category.isTrackingEnabled());
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
    public void forEachMatchedInterval(
            @Nonnull final BEIndexAttributeCategory indexAttributeCategory,
            @Nonnull final BEAttributeCategoryMatchedIntervalConsumer consumer)
    {
        this.values.forEach(value -> {
            BEIntInputAttributeCategory matchedInput = null;
            if (this.isTrackingEnabled())
            {
                matchedInput = new BEIntInputAttributeCategory(this.getName(), true);
                matchedInput.add(value);
            }
            indexAttributeCategory.getIntervals(value, matchedInput, consumer);
        });
    }

    @Override
    public <C extends BEInputAttributeCategory> void addAll(final C other)
    {
        if (!(other instanceof BEIntInputAttributeCategory))
        {
            throw new IllegalArgumentException("Expected "
                    + BEIntInputAttributeCategory.class.getSimpleName()
                    + " but was passed " + other.getClass().getSimpleName());
        }
        this.values.addAll(((BEIntInputAttributeCategory) other).values);
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

    @Override
    public Set<? extends BEInputAttributeCategory> split()
    {
        final Set<BEIntInputAttributeCategory> outputSet = new HashSet<>();
        this.values.forEach(value -> {
            final BEIntInputAttributeCategory i = new BEIntInputAttributeCategory(this.name, this.trackingEnabled);
            i.add(value);
            outputSet.add(i);
        });
        return outputSet;
    }

    @Override
    public String toString()
    {
        return "BEIntInputAttributeCategory{" +
                "name='" + this.name + '\'' +
                ", trackingEnabled=" + this.trackingEnabled +
                ", values=" + this.values +
                '}';
    }
}
