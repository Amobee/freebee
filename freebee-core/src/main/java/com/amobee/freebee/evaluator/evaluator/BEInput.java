package com.amobee.freebee.evaluator.evaluator;

import java.util.function.BiConsumer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import lombok.NoArgsConstructor;
import lombok.ToString;
import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.impl.factory.Maps;

/**
 * @author Michael Bond
 */
@ToString
@NoArgsConstructor
public class BEInput implements Cloneable
{
    private final MutableMap<String, BEInputAttributeCategory> attributeCategories = Maps.mutable.of();

    private BEInput(@Nonnull final BEInput beInput)
    {
        this.attributeCategories
            .putAll(beInput.attributeCategories.collectValues((key, category) -> category.clone()));
    }

    @Nullable
    public BEInputAttributeCategory getCategory(@Nonnull final String attributeCategory)
    {
        return this.attributeCategories.get(attributeCategory.toUpperCase());
    }

    /**
     * Get or create a new byte attribute category for the input.
     *
     * @param attributeCategory
     *         Attribute category to create.
     * @return New {@link BEByteInputAttributeCategory}.
     */
    @Nonnull
    public BEByteInputAttributeCategory getOrCreateByteCategory(@Nonnull final String attributeCategory)
    {
        return (BEByteInputAttributeCategory) this.attributeCategories.getIfAbsentPut(
                attributeCategory.toUpperCase(),
                BEByteInputAttributeCategory::new);
    }

    /**
     * Get or create a new double attribute category for the input.
     *
     * @param attributeCategory
     *         Attribute category to create.
     * @return New {@link BEIntInputAttributeCategory}.
     */
    @Nonnull
    public BEDoubleInputAttributeCategory getOrCreateDoubleCategory(@Nonnull final String attributeCategory)
    {
        return (BEDoubleInputAttributeCategory) this.attributeCategories.getIfAbsentPut(
                attributeCategory.toUpperCase(),
                BEDoubleInputAttributeCategory::new);
    }

    /**
     * Get or create a new int attribute category for the input.
     *
     * @param attributeCategory
     *         Attribute category to create.
     * @return New {@link BEIntInputAttributeCategory}.
     */
    @Nonnull
    public BEIntInputAttributeCategory getOrCreateIntCategory(@Nonnull final String attributeCategory)
    {
        return (BEIntInputAttributeCategory) this.attributeCategories.getIfAbsentPut(
                attributeCategory.toUpperCase(),
                BEIntInputAttributeCategory::new);
    }

    /**
     * Get or create a new long attribute category for the input.
     *
     * @param attributeCategory
     *         Attribute category to create.
     * @return New {@link BELongInputAttributeCategory}.
     */
    @Nonnull
    public BELongInputAttributeCategory getOrCreateLongCategory(@Nonnull final String attributeCategory)
    {
        return (BELongInputAttributeCategory) this.attributeCategories.getIfAbsentPut(
                attributeCategory.toUpperCase(),
                BELongInputAttributeCategory::new);
    }

    /**
     * Get or create a new string attribute category for the input.
     *
     * @param attributeCategory
     *         Attribute category to create.
     * @return New {@link BEStringInputAttributeCategory}.
     */
    @Nonnull
    public BEStringInputAttributeCategory getOrCreateStringCategory(@Nonnull final String attributeCategory)
    {
        return (BEStringInputAttributeCategory) this.attributeCategories.getIfAbsentPut(
                attributeCategory.toUpperCase(),
                BEStringInputAttributeCategory::new);
    }

    public void removeCategory(@Nonnull final String attributeCategory)
    {
        this.attributeCategories.removeKey(attributeCategory.toUpperCase());
    }

    /**
     * Iterate over each attribute category in the input.
     *
     * @param consumer
     *         Attribute consumer that accepts (attributeCategory, beInputAttributeCategory).
     */
    public void forEach(@Nonnull final BiConsumer<String, BEInputAttributeCategory> consumer)
    {
        this.attributeCategories.forEach(consumer);
    }

    @Override
    public BEInput clone()
    {
        return new BEInput(this);
    }
}