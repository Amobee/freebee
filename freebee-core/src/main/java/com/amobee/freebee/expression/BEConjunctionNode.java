package com.amobee.freebee.expression;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.ToString;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Expression conjunction node. This node type contains the {@link BEConjunctionType} and child nodes.
 *
 * @author Michael Bond
 */
@Data
@ToString(callSuper = true)
public class BEConjunctionNode extends BENode
{
    private static final short UNSET = -1;

    @Nonnull
    private BEConjunctionType type;
    @Nonnull
    private final List<BENode> values;
    private short numNodes = UNSET;
    private short numPredicates = UNSET;

    public BEConjunctionNode(@Nonnull final BEConjunctionType type)
    {
        this(null, type, new ArrayList<>());
    }

    @JsonCreator
    public BEConjunctionNode(
            @JsonProperty(value = "id") @Nullable final String id,
            @JsonProperty(value = "type", required = true) @Nonnull final String type,
            @JsonProperty(value = "negative") final boolean negative,
            @JsonProperty(value = "values", required = true) @Nonnull final List<BENode> values)
    {
        this(id, BEConjunctionType.valueOf(type.toUpperCase()), values);
    }

    public BEConjunctionNode(
            @Nullable final String id,
            @Nonnull final BEConjunctionType type)
    {
        this(id, type, new ArrayList<>());
    }

    public BEConjunctionNode(
            @Nonnull final BEConjunctionType type,
            @Nonnull final List<BENode> values)
    {
        this(null, type, values);
    }

    public BEConjunctionNode(
            @Nullable final String id,
            @Nonnull final BEConjunctionType type,
            @Nonnull final List<BENode> values)
    {
        this(id, type, false, values);
    }

    public BEConjunctionNode(
            @Nullable final String id,
            @Nonnull final BEConjunctionType type,
            final boolean negative,
            @Nonnull final List<BENode> values)
    {
        super(id, negative);
        this.type = type;
        this.values = values;
    }

    BEConjunctionNode(@Nonnull final BEConjunctionNode other)
    {
        super(other);
        this.type = other.type;
        this.values = other.values.stream().map(BENode::from).collect(Collectors.toList());
    }

    @Override
    public short getNumNodes()
    {
        if (this.numNodes == UNSET)
        {
            this.numNodes = 1;
            this.values.forEach(v -> this.numNodes += v.getNumNodes());
        }
        return this.numNodes;
    }

    @Override
    public short getNumPredicates()
    {
        if (this.numPredicates == UNSET)
        {
            this.numPredicates = 0;
            this.values.forEach(v -> this.numPredicates += v.getNumPredicates());
        }
        return this.numPredicates;
    }

    /**
     * Get the number of predicates for all descendants left of the specified descendant.
     *
     * @param descendantInQuestion
     *         Descendant to get number of left predicates for
     * @return Number of predicates left of the specified child.
     */
    public short getNumLeftPredicates(@Nonnull final BEConjunctionNode descendantInQuestion)
    {
        return getNumLeftPredicatesInternal(descendantInQuestion).getLeft();
    }

    /**
     * Internal recursive helper method to get left predicates for all descendants.  Includes a flag
     * "found" in the return value in order to break out of the recursion.
     *
     * @param descendantInQuestion
     *          Descendant to get number of left predicates for
     * @return (Number of predicates left of the specified descendant, flag indicating whether descendant was found)
     */
    Pair<Short, Boolean> getNumLeftPredicatesInternal(@Nonnull final BEConjunctionNode descendantInQuestion)
    {
        final MutablePair<Short, Boolean> predsFound = new MutablePair<>((short) 0, false);

        for (final BENode child : this.values)
        {
            if (child == descendantInQuestion)
            {
                predsFound.setRight(true);
                break;

            } else if (predsFound.getRight())
            {
                break;
            }

            // Tally the child predicates.
            final Pair<Short, Boolean> childPredsFound = child.getNumLeftPredicatesInternal(descendantInQuestion);
            predsFound.setLeft((short) (predsFound.getLeft() + childPredsFound.getLeft()));

            // Propagate "found" up the stack to break out of the recursion.
            predsFound.setRight(childPredsFound.getRight());
        }
        return predsFound;
    }

    public void addValue(@Nonnull final BENode node)
    {
        this.numNodes = UNSET;
        this.numPredicates = UNSET;
        this.values.add(node);
    }

    public void addValues(@Nonnull final Collection<BENode> nodes)
    {
        this.numNodes = UNSET;
        this.numPredicates = UNSET;
        this.values.addAll(nodes);
    }

    @Nonnull
    @Override
    public String getType()
    {
        return this.type.toString().toLowerCase();
    }

    /* package */ @JsonIgnore BEConjunctionType getTypeEnum()
    {
        return this.type;
    }

    /* package */ void setType(final BEConjunctionType type)
    {
        this.type = type;
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
        if (!super.equals(o))
        {
            return false;
        }
        final BEConjunctionNode that = (BEConjunctionNode) o;
        return this.type == that.type &&
                // TODO in v2.0, change the values of a conjunction node to be a Set instead of a List
                // Convert values to set to get order-agnostic equality semantics
                new HashSet<>(this.values).equals(new HashSet<>(that.values));
    }

    @Override
    public int hashCode()
    {
        // TODO in v2.0, change the values of a conjunction node to be a Set instead of a List
        // Convert values to set to get order-agnostic hashing semantics
        final Set<BENode> valueSet = new HashSet<>(this.values);
        return Objects.hash(super.hashCode(), this.type, valueSet);
    }
}
