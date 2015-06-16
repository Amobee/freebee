package com.amobee.freebee.evaluator.interval;

import com.amobee.freebee.expression.BEConjunctionNode;
import com.amobee.freebee.expression.BEConstants;
import com.amobee.freebee.expression.BENode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Implements interval labeling algorithm (Algorithm 4) from Boolean Expression Evaluator white paper [1].
 *
 * There is some lack of clarity and arguably some error in the pseudocode presented in the white paper:
 *  - Pseudocode uses (begin, end) inclusive, but here end is exclusive.
 *  - Pseudocode assumes count of left predicates starts at root of tree, not at parent.
 *  - Pseudocode incorrectly states to use "size" as count of descendant nodes,
 *    but it actually needs to be count of descendant leaf (predicate) nodes.
 *  - Pseudocode also does not indicate that when there is a single node it should be processed as "last",
 *    rather than "first".
 *
 * [1] Fontoura, Marcus, Suhas Sadanandan, Jayavel Shanmugasundaram, Sergei Vassilvitski, Erik Vee, Srihari Venkatesan, and Jason Zien. 2010.
 *    “Efficiently Evaluating Complex Boolean Expressions.”
 *    In Proceedings of the 2010 International Conference on Management of Data - SIGMOD ’10, 3.
 *    Indianapolis, Indiana, USA: ACM Press. https://doi.org/10.1145/1807167.1807171.
 *    http://theory.stanford.edu/~sergei/papers/sigmod10-index.pdf
 *
 * @author Kevin Doran
 */
public class BEDefaultIntervalLabeler implements BEIntervalLabeler
{

    @Override
    public Collection<BENodeInterval> labelExpression(@Nonnull final BENode expr)
    {
        final LabelingContext context = new LabelingContext();
        addExpression(context, expr);
        return context.getIntervals();
    }

    private void addExpression(@Nonnull final LabelingContext context, @Nonnull final BENode expr)
    {
        // build intervals for the expression
        final short intervalLength = expr.getNumPredicates();
        final Interval interval = new BESimpleInterval((short) 0, intervalLength);
        addNode(context, null, expr, interval, (short) 0, intervalLength);
    }

    private void addNode(
            @Nonnull final LabelingContext context,
            @Nullable final BEConjunctionNode parent,
            @Nonnull final BENode node,
            @Nonnull final Interval interval,
            final short intervalStart,
            final short intervalEnd)
    {
        node.setParent(parent);
        switch (node.getType().toUpperCase())
        {
            case BEConstants.NODE_TYPE_AND:
                addAnd(context, (BEConjunctionNode) node, intervalStart, intervalEnd);
                break;
            case BEConstants.NODE_TYPE_OR:
                addOr(context, (BEConjunctionNode) node, interval, intervalStart, intervalEnd);
                break;
            case BEConstants.NODE_TYPE_REFERENCE:
            default:
                addLeafNode(context, node, interval);
                break;
        }
    }

    private void addAnd(
            @Nonnull final LabelingContext context,
            @Nonnull final BEConjunctionNode node,
            final short intervalStart,
            final short intervalEnd)
    {
        final List<BENode> childNodes = node.getValues();
        if (!childNodes.isEmpty())
        {
            BENode child;
            Interval childInterval;
            short start = intervalStart;
            short end;

            child = childNodes.get(0);
            // process the first node this way only if there are more than one
            if (childNodes.size() > 1)
            {
                final BENode root = node.getRootNode();
                final short numLeftPredicates = root != node ? root.getNumLeftPredicates(node) : 0;
                end = (short) (numLeftPredicates + child.getNumPredicates());
                childInterval = new BESimpleInterval(start, end);
                addNode(context, node, child, childInterval, start, end);
                start = end;
            }

            // process all but the first and last nodes
            final int lastChild = childNodes.size() - 1;
            for (int i = 1; i < lastChild; i++)
            {
                child = childNodes.get(i);
                end = (short) (start + child.getNumPredicates());
                childInterval = new BESimpleInterval(start, end);
                addNode(context, node, child, childInterval, start, end);
                start = end;
            }

            // process the last node
            child = childNodes.get(lastChild);
            childInterval = new BESimpleInterval(start, intervalEnd);
            addNode(context, node, child, childInterval, start, intervalEnd);
        }
    }

    private void addOr(
            @Nonnull final LabelingContext context,
            @Nonnull final BEConjunctionNode node,
            @Nonnull final Interval interval,
            final short intervalStart,
            final short intervalEnd)
    {
        // process each child node of OR nodes using the full interval
        node.getValues().forEach(child -> {
            final Interval childInterval = new BESimpleInterval(interval.getStart(), interval.getEnd());
            addNode(context, node, child, childInterval, intervalStart, intervalEnd);
        });
    }

    private void addLeafNode(
            @Nonnull final LabelingContext context,
            @Nonnull final BENode node,
            @Nonnull final Interval interval)
    {
        final BENodeInterval intervalLabel = new BENodeInterval(interval.getStart(), interval.getEnd(), node);
        context.addInterval(intervalLabel);
    }


    private static class LabelingContext
    {
        private final Collection<BENodeInterval> intervals = new ArrayList<>();

        Collection<BENodeInterval> getIntervals()
        {
            return this.intervals;
        }

        void addInterval(final BENodeInterval interval)
        {
            this.intervals.add(interval);
        }
    }
}
