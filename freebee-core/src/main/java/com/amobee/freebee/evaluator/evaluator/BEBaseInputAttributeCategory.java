package com.amobee.freebee.evaluator.evaluator;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * A base implementation class for BEInputAttributeCategory.
 * This class contains common fields such as attribute category name and a boolean flag
 * flor enabling tracking.
 * @author Kevin Doran
 */
public abstract class BEBaseInputAttributeCategory implements BEInputAttributeCategory
{
    private final String name;
    private boolean trackingEnabled;

    public BEBaseInputAttributeCategory(@Nonnull final String attributeCategoryName)
    {
        this(attributeCategoryName, false);
    }

    public BEBaseInputAttributeCategory(@Nonnull final String attributeCategoryName, final boolean trackingEnabled)
    {
        if (attributeCategoryName == null)
        {
            throw new IllegalArgumentException("attributeCategoryName must not be null");
        }
        this.name = attributeCategoryName;
        this.trackingEnabled = trackingEnabled;
    }

    @Override
    @Nonnull
    public String getName()
    {
        return this.name;
    }

    public boolean isTrackingEnabled()
    {
        return this.trackingEnabled;
    }

    public void setTrackingEnabled(final boolean trackingEnabled)
    {
        this.trackingEnabled = trackingEnabled;
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
        final BEBaseInputAttributeCategory that = (BEBaseInputAttributeCategory) o;
        return Objects.equals(this.name, that.name);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(this.name);
    }

    @Override
    public abstract BEInputAttributeCategory clone();
}
