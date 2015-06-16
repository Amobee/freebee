package com.amobee.freebee.expression;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Root expression node that contains metadata about the overall expression in addition to the root node.
 *
 * @author Michael Bond
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class BooleanExpression extends BEConjunctionNode
{
    public BooleanExpression(@Nonnull final BEConjunctionType type)
    {
        super(type);
    }

    public BooleanExpression(
           @Nonnull final String type,
           @Nonnull final List<BENode> values)
    {
        super(BEConjunctionType.valueOf(type.toUpperCase()), values);
    }

    @JsonCreator
    public BooleanExpression(
            @JsonProperty(value = "id") @Nullable final String id,
            @JsonProperty(value = "type", required = true) @Nonnull final String type,
            @JsonProperty(value = "values", required = true) @Nonnull final List<BENode> values)
    {
        super(id, BEConjunctionType.valueOf(type.toUpperCase()), values);
    }

    BooleanExpression(@Nonnull final BooleanExpression other)
    {
        super(other);
    }
}
