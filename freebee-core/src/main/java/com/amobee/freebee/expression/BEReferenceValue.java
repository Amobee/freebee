package com.amobee.freebee.expression;

import java.util.Objects;

import lombok.Data;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Michael Bond
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class BEReferenceValue
{
    @Nonnull
    private final String id;
    @Nullable
    private final Long modified;

    public BEReferenceValue(@Nonnull final String id)
    {
        this(id, null);
    }

    public BEReferenceValue(
            @JsonProperty(value = "id", required = true) @Nonnull final String id,
            @JsonProperty(value = "modified") @Nullable final Long modified)
    {
        this.id = id;
        this.modified = modified;
    }

    BEReferenceValue(final BEReferenceValue other)
    {
        this.id = other.id;
        this.modified = other.modified;
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
        final BEReferenceValue that = (BEReferenceValue) o;
        return this.id.equals(that.id);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(this.id);
    }
}
