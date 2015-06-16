package com.amobee.freebee.config;

import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

/**
 * @author Michael Bond
 */
@Getter
public class BEDataTypeConfig implements Serializable
{
    private static final long serialVersionUID = 3654573816100574566L;

    private final String type;
    private final BEDataType dataType;
    private final boolean ignoreCase;
    private final boolean partial;
    private final boolean range;
    private final boolean reverse;

    @JsonCreator
    public BEDataTypeConfig(
            @JsonProperty(value = "type", required = true) @Nonnull final String type,
            @JsonProperty(value = "dataType") @Nullable final String dataType,
            @JsonProperty(value = "ignorecase") @Nullable final Boolean ignoreCase,
            @JsonProperty(value = "partial") @Nullable final Boolean partial,
            @JsonProperty(value = "range") @Nullable final Boolean range,
            @JsonProperty(value = "reverse") @Nullable final Boolean reverse)
    {
        this.type = type;
        this.dataType = dataType != null ? BEDataType.valueOf(dataType.toUpperCase()) : BEDataType.STRING;
        this.ignoreCase = ignoreCase != null && ignoreCase.booleanValue();
        this.partial = partial != null && partial.booleanValue();
        this.range = range != null && range.booleanValue();
        this.reverse = reverse != null && reverse.booleanValue();
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
        final BEDataTypeConfig that = (BEDataTypeConfig) o;
        return this.ignoreCase == that.ignoreCase &&
                this.partial == that.partial &&
                this.range == that.range &&
                this.reverse == that.reverse &&
                Objects.equals(this.type, that.type) &&
                this.dataType == that.dataType;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(this.type, this.dataType, this.ignoreCase, this.partial, this.range, this.reverse);
    }
}
