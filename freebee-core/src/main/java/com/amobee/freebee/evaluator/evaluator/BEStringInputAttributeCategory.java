package com.amobee.freebee.evaluator.evaluator;

import com.amobee.freebee.evaluator.BEInterval;
import com.amobee.freebee.evaluator.index.BEAttributeCategoryMatchedIntervalConsumer;
import com.amobee.freebee.evaluator.index.BEIndexAttributeCategory;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import lombok.ToString;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * @author Michael Bond
 */
@ToString
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

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        if (!super.equals(o))
        {
            return false;
        }
        final BEStringInputAttributeCategory that = (BEStringInputAttributeCategory) o;
        return Objects.equals(this.values, that.values);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(super.hashCode(), this.values);
    }

    @VisibleForTesting
    public ImmutableList<String> getValues()
    {
        return ImmutableList.copyOf(this.values);
    }
}