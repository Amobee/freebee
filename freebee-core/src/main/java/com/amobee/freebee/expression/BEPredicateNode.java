package com.amobee.freebee.expression;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Expression leaf node. This node type contains an attribute type and set of values to match for that attribute type as
 * well as a flag indicating if it should be a inclusive or exclusive set.
 *
 * @author Michael Bond
 *
 */
@SuppressWarnings({"FieldMayBeFinal", "illegaltype"})
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class BEPredicateNode extends BENode
{
    @Nonnull
    private String type;

    @Nonnull
    private final List<BEAttributeValue> values;

    public BEPredicateNode(@Nonnull final String type, final String... values)
    {
        this(null, type, false, new ArrayList<>());
        addRawValues(values);
    }

    public BEPredicateNode(@Nonnull final String type, final boolean negative, final String... values)
    {
        this(null, type, negative, new ArrayList<>());
        addRawValues(values);
    }

    public BEPredicateNode(
            @Nonnull final String type,
            final boolean negative,
            @Nonnull final Collection<BEAttributeValue> values)
    {
        this(null, type, negative, values);
    }

    @JsonCreator
    public BEPredicateNode(
            @JsonProperty(value = "id") @Nullable final String id,
            @JsonProperty(value = "type", required = true) @Nonnull final String type,
            @JsonProperty("negative") final boolean negative,
            @JsonProperty(value = "values", required = true) @Nonnull final Collection<BEAttributeValue> values)
    {
        super(id, negative);
        this.type = type;
        this.values = new ArrayList<>(values);
    }

    BEPredicateNode(@Nonnull final BEPredicateNode other)
    {
        super(other);
        this.type = other.type;
        this.values = other.values.stream().map(BEAttributeValue::new).collect(Collectors.toList());
    }

    @Override
    public short getNumNodes()
    {
        return 1;
    }

    @Override
    public short getNumPredicates()
    {
        return 1;
    }

    @Override
    public short getNumLeftPredicates(@Nonnull final BEConjunctionNode descendantInQuestion)
    {
        return 1;
    }

    @Override
    Pair<Short, Boolean> getNumLeftPredicatesInternal(final BEConjunctionNode descendantInQuestion)
    {
        return new ImmutablePair<>((short) 1, false);
    }

    public void addValue(@Nonnull final BEAttributeValue value)
    {
        this.values.add(value);
    }

    public void addRawValue(@Nonnull final String value)
    {
        this.values.add(new BEAttributeValue(value));
    }

    public void addValues(@Nonnull final Collection<BEAttributeValue> values)
    {
        this.values.addAll(values);
    }

    public void addRawValues(@Nonnull final Collection<String> values)
    {
        final Collection<BEAttributeValue> attributeValues =
                values.stream().map(BEAttributeValue::new).collect(Collectors.toList());
        this.values.addAll(attributeValues);
    }

    public void addRawValues(@Nonnull final String... values)
    {
        final Collection<BEAttributeValue> attributeValues =
                Arrays.stream(values).map(BEAttributeValue::new).collect(Collectors.toList());
        this.values.addAll(attributeValues);
    }
}
