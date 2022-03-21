package com.amobee.freebee.evaluator.evaluator;

import com.amobee.freebee.evaluator.BEInterval;
import com.amobee.freebee.evaluator.index.BEAttributeCategoryMatchedIntervalConsumer;
import com.amobee.freebee.evaluator.index.BEIndexAttributeCategory;
import com.google.common.annotations.VisibleForTesting;
import lombok.EqualsAndHashCode;
import org.eclipse.collections.api.list.primitive.ImmutableLongList;
import org.eclipse.collections.api.list.primitive.MutableLongList;
import org.eclipse.collections.impl.list.mutable.primitive.LongArrayList;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * @author Michael Bond
 */
@EqualsAndHashCode(callSuper = true)
public class BELongInputAttributeCategory extends BEBaseInputAttributeCategory
{
    private final MutableLongList values = new LongArrayList();

    public BELongInputAttributeCategory(@Nonnull final String attributeCategoryName)
    {
        super(attributeCategoryName);
    }

    public BELongInputAttributeCategory(@Nonnull final String attributeCategoryName, final boolean trackingEnabled)
    {
        super(attributeCategoryName, trackingEnabled);
    }

    private BELongInputAttributeCategory(@Nonnull final BELongInputAttributeCategory category)
    {
        super(category.getName(), category.isTrackingEnabled());
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
    public void forEachMatchedInterval(
            @Nonnull final BEIndexAttributeCategory indexAttributeCategory,
            @Nonnull final BEAttributeCategoryMatchedIntervalConsumer consumer)
    {
        this.values.forEach(value -> {
            BELongInputAttributeCategory matchedInput = null;
            if (this.isTrackingEnabled())
            {
                matchedInput = new BELongInputAttributeCategory(this.getName(), true);
                matchedInput.add(value);
            }
            indexAttributeCategory.getIntervals(value, matchedInput, consumer);
        });
    }

    @Override
    public <C extends BEInputAttributeCategory> void addAll(final C other)
    {
        if (!(other instanceof BELongInputAttributeCategory))
        {
            throw new IllegalArgumentException("Expected "
                    + BELongInputAttributeCategory.class.getSimpleName()
                    + " but was passed " + other.getClass().getSimpleName());
        }
        this.values.addAll(((BELongInputAttributeCategory) other).values);
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

    @Override
    public Set<? extends BEInputAttributeCategory> split()
    {
        final Set<BELongInputAttributeCategory> outputSet = new HashSet<>();
        this.values.forEach(value -> {
            final BELongInputAttributeCategory i = new BELongInputAttributeCategory(this.name, this.trackingEnabled);
            i.add(value);
            outputSet.add(i);
        });
        return outputSet;
    }

    @Override
    public String toString()
    {
        return "BELongInputAttributeCategory{" +
                "name='" + this.name + '\'' +
                ", trackingEnabled=" + this.trackingEnabled +
                ", values=" + this.values +
                '}';
    }
}