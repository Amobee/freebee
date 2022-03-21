package com.amobee.freebee.evaluator.evaluator;

import com.amobee.freebee.evaluator.BEInterval;
import com.amobee.freebee.evaluator.index.BEAttributeCategoryMatchedIntervalConsumer;
import com.amobee.freebee.evaluator.index.BEIndexAttributeCategory;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import lombok.EqualsAndHashCode;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * @author Michael Bond
 */
@EqualsAndHashCode(callSuper = true)
public class BEStringInputAttributeCategory extends BEBaseInputAttributeCategory
{
    private final List<String> values = new ArrayList<>();

    public BEStringInputAttributeCategory(@Nonnull final String attributeCategoryName)
    {
        super(attributeCategoryName);
    }

    public BEStringInputAttributeCategory(@Nonnull final String attributeCategoryName, final boolean trackingEnabled)
    {
        super(attributeCategoryName, trackingEnabled);
    }

    private BEStringInputAttributeCategory(@Nonnull final BEStringInputAttributeCategory category)
    {
        super(category.getName(), category.isTrackingEnabled());
        this.values.addAll(category.values);
    }

    public void add(final String value)
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
            BEStringInputAttributeCategory matchedInput = null;
            if (this.isTrackingEnabled())
            {
                matchedInput = new BEStringInputAttributeCategory(this.getName(), true);
                matchedInput.add(value);
            }
            indexAttributeCategory.getIntervals(value, matchedInput, consumer);
        });
    }

    @Override
    public <C extends BEInputAttributeCategory> void addAll(final C other)
    {
        if (!(other instanceof BEStringInputAttributeCategory))
        {
            throw new IllegalArgumentException("Expected "
                    + BEStringInputAttributeCategory.class.getSimpleName()
                    + " but was passed " + other.getClass().getSimpleName());
        }
        this.values.addAll(((BEStringInputAttributeCategory) other).values);
    }

    @Override
    public BEStringInputAttributeCategory clone()
    {
        return new BEStringInputAttributeCategory(this);
    }

    @VisibleForTesting
    public ImmutableList<String> getValues()
    {
        return ImmutableList.copyOf(this.values);
    }

    @Override
    public Set<? extends BEInputAttributeCategory> split()
    {
        final Set<BEStringInputAttributeCategory> outputSet = new HashSet<>();
        this.values.forEach(value -> {
            final BEStringInputAttributeCategory i = new BEStringInputAttributeCategory(this.name, this.trackingEnabled);
            i.add(value);
            outputSet.add(i);
        });
        return outputSet;
    }

    @Override
    public String toString()
    {
        return "BEStringInputAttributeCategory{" +
                "name='" + this.name + '\'' +
                ", trackingEnabled=" + this.trackingEnabled +
                ", values=" + this.values +
                '}';
    }
}