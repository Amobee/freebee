package com.amobee.freebee.expression;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Expression predicate set value. This node contains the actual predicate set value as 'id' and optional metadata about
 * the value.
 *
 * @author Michael Bond
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class BEAttributeValue
{
    @Nonnull
    private final String id;
    @Nullable
    private final String name;
    @Nullable
    private final String href;
    private final Map<String, Object> properties;

    public BEAttributeValue(@Nonnull final String id)
    {
        this(id, null, null, null);
    }

    @JsonCreator
    public BEAttributeValue(
            @JsonProperty(value = "id", required = true) @Nonnull final String id,
            @JsonProperty(value = "name") @Nullable final String name,
            @JsonProperty(value = "href") @Nullable final String href,
            @JsonProperty(value = "properties") @Nullable final Map<String, Object> properties)
    {
        this.id = id;
        this.name = name;
        this.href = href;
        this.properties = null != properties ? properties : new HashMap<>();
    }

    public BEAttributeValue(final BEAttributeValue other)
    {
        this.id = other.id;
        this.name = other.name;
        this.href = other.href;
        this.properties = other.properties != null ? new HashMap<>(other.properties) : null;
    }

    @JsonAnySetter
    public void addProperty(final String key, final Object value)
    {
        this.properties.put(key, value);
    }

    @JsonAnyGetter
    public Map<String, Object> getProperties()
    {
        return this.properties;
    }

    @Nullable
    public Object getProperty(@Nonnull final String key)
    {
        return this.properties.get(key);
    }

    @SuppressWarnings("SameParameterValue")
    public boolean hasProperty(@Nonnull final String key)
    {
        return this.properties.containsKey(key);
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
        final BEAttributeValue that = (BEAttributeValue) o;
        return this.id.equals(that.id) &&
                Objects.equals(this.name, that.name) &&
                Objects.equals(this.href, that.href) &&
                Objects.equals(this.properties, that.properties);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(this.id, this.name, this.href, this.properties);
    }
}
