package com.amobee.freebee.evaluator.evaluator;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.annotation.Nonnull;

/**
 * A base implementation class for BEInputAttributeCategory.
 * This class contains common fields such as attribute category name and a boolean flag
 * flor enabling tracking.
 * @author Kevin Doran
 */
@ToString
@EqualsAndHashCode
public abstract class BEBaseInputAttributeCategory implements BEInputAttributeCategory
{
    protected final String name;
    protected boolean trackingEnabled;

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
    public abstract BEInputAttributeCategory clone();
}
