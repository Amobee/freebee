package com.amobee.freebee.expression;

import java.util.Objects;

import lombok.Data;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import lombok.ToString;
import org.apache.commons.lang3.tuple.Pair;

/**
 * The basic expression node interface.
 *
 * @author Michael Bond
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "type",
        visible = true
)
@JsonTypeIdResolver(BETypeIdResolver.class)
@Data
@ToString(exclude = {"parent"})
public abstract class BENode
{
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @Nullable
    private final String id;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private boolean negative;

    /**
     * Only used by index builder; not populated upon deserialization
     */
    @JsonIgnore
    private BENode parent;

    public BENode(@Nullable final String id)
    {
        this.id = id;
    }

    public BENode(@Nullable final String id, final boolean negative)
    {
        this.id = id;
        this.negative = negative;
    }

    BENode(@Nonnull final BENode other)
    {
        this.id = other.id;
        this.negative = other.negative;
        this.parent = BENode.from(other.parent);
    }

    /**
     * Get the total number of nodes including this node and all descendants.
     *
     * @return Total number of nodes
     */
    @JsonIgnore
    public abstract short getNumNodes();

    /**
     * Get the total number of predicates in this node and all descendants.
     *
     * @return Number of predicates
     */
    @JsonIgnore
    public abstract short getNumPredicates();

    /**
     * Get the total number of left (of child) predicates in this node and all descendants,
     * assuming depth-first traversal.
     *
     * @return Number of left predicates
     */
    @JsonIgnore
    public abstract short getNumLeftPredicates(BEConjunctionNode descendantInQuestion);

    @JsonIgnore
    abstract Pair<Short, Boolean> getNumLeftPredicatesInternal(BEConjunctionNode descendantInQuestion);

    /**
     * Get the root node of the tree
     */
    @JsonIgnore
    public BENode getRootNode()
    {
        return (null != this.parent) ? this.parent.getRootNode() : this;
    }

    /**
     * Get the type of this node
     *
     * @return Type of the node. Will be "and" or "or" for conjunction nodes, attribute name for predicate nodes.
     */
    @Nonnull
    public abstract String getType();

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
        final BENode beNode = (BENode) o;
        return this.negative == beNode.negative &&
                Objects.equals(this.id, beNode.id);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(this.id, this.negative);
    }

    /**
     * A utility method for performing a deep clone of the BENode.
     *
     * @param other the node to copy
     * @return a deep copy of the given node
     */
    @SuppressWarnings("checkstyle:ReturnCount")
    static BENode from(final BENode other)
    {
        if (other == null)
        {
            return null;
        }

        if (other.getClass().equals(BEConjunctionNode.class))
        {
            return new BEConjunctionNode((BEConjunctionNode) other);
        }

        if (other.getClass().equals(BEPredicateNode.class))
        {
            return new BEPredicateNode((BEPredicateNode) other);
        }

        if (other.getClass().equals(BEReferenceNode.class))
        {
            return new BEReferenceNode((BEReferenceNode) other);
        }

        if (other.getClass().equals(BooleanExpression.class))
        {
            return new BooleanExpression((BooleanExpression) other);
        }

        throw new IllegalArgumentException("Unknown concrete BENode type: " + other.getClass());

    }
}
