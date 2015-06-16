package com.amobee.freebee.evaluator.evaluator;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.annotation.Nonnull;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import lombok.NoArgsConstructor;
import lombok.ToString;

import com.amobee.freebee.evaluator.BEInterval;
import com.amobee.freebee.evaluator.index.BEIndexAttributeCategory;

/**
 * @author Michael Bond
 */
@ToString
@NoArgsConstructor
public class BEStringInputAttributeCategory implements BEInputAttributeCategory
{
    private final List<String> values = new ArrayList<>();

    private BEStringInputAttributeCategory(@Nonnull final BEStringInputAttributeCategory category)
    {
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
    public BEStringInputAttributeCategory clone()
    {
        return new BEStringInputAttributeCategory(this);
    }

    @VisibleForTesting
    public ImmutableList<String> getValues()
    {
        return ImmutableList.copyOf(this.values);
    }
}