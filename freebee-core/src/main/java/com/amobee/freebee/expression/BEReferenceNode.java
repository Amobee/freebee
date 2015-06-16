package com.amobee.freebee.expression;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Expression reference node.
 * This node type represents a reference to one or more partial
 * expressions that will be logically added to the parent operator.
 *
 * @author Michael Bond
 * @see com.amobee.freebee.evaluator.evaluator.BEEvaluatorBuilder#addPartialExpression(String, BENode)
 */
@SuppressWarnings("unused")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class BEReferenceNode extends BENode
{
    // This is a property to satisfy Jackson in the simplest way possible
    @Nonnull
    private final String type = BEConstants.NODE_TYPE_REFERENCE;

    @Nonnull
    private final List<BEReferenceValue> values;

    public BEReferenceNode(@Nonnull final List<String> values)
    {
        this(null, false, toReferenceValue(values));
    }

    public BEReferenceNode(final boolean negative, @Nonnull final List<String> values)
    {
        this(null, negative, toReferenceValue(values));
    }

    public BEReferenceNode(@Nullable final String id, @Nonnull final List<BEReferenceValue> value)
    {
        this(id, false, value);
    }

    public BEReferenceNode(
            @Nullable final String id,
            final boolean negative,
            @Nonnull final List<BEReferenceValue> values)
    {
        super(id, negative);
        this.values = values;
    }

    @JsonCreator
    public BEReferenceNode(
            @JsonProperty(value = "id") @Nullable final String id,
            @JsonProperty(value = "negative") final boolean negative,
            @JsonProperty(value = "type", required = true) @Nonnull final String type,
            @JsonProperty(value = "values", required = true) @Nonnull final List<BEReferenceValue> values)
    {
        this(id, negative, values);
        assert BEConstants.NODE_TYPE_REFERENCE.equalsIgnoreCase(type) : "type must be \"ref\"";
    }

    BEReferenceNode(@Nonnull final BEReferenceNode other)
    {
        super(other);
        this.values = other.values.stream().map(BEReferenceValue::new).collect(Collectors.toList());
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
    public short getNumLeftPredicates(final BEConjunctionNode descendantInQuestion)
    {
        return 1;
    }

    @Override
    Pair<Short, Boolean> getNumLeftPredicatesInternal(final BEConjunctionNode descendantInQuestion)
    {
        // We are treating the partial expression reference node as a leaf node
        return new ImmutablePair<>((short) 1, false);
    }

    private static List<BEReferenceValue> toReferenceValue(final @Nonnull List<String> values)
    {
        return values.stream().map(BEReferenceValue::new).collect(Collectors.toList());
    }
}
