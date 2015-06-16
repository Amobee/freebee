package com.amobee.freebee.expression;

/**
 * An implementation of {@link BEFormNormalizer} that replaces negative conjunction nodes with
 * positive conjunctions using De_Morgan's Laws, which states that negation on a conjunction
 * can be "distributed" to the child nodes by flipping the conjunction type.
 *
 * For example:
 *     normalize( "A and not (B or C or not (D and E))" )
 *
 * Will return:
 *     "A and (not B and not C and (not D or not E))
 *
 *  [1] https://en.wikipedia.org/wiki/De_Morgan's_laws
 *
 * @author Kevin Doran
 */
@SuppressWarnings("checkstyle:ReturnCount")
public class PositiveConjunctionFormNormalizer implements BEFormNormalizer
{
    @Override
    public boolean canNormalize(final BENode expression)
    {
        if (!(expression instanceof BEConjunctionNode))
        {
            // This normalizer does not have any transformations
            // it can apply to leaf nodes (predicates or references)
            return false;
        }

        final BEConjunctionNode conjunctionNode = (BEConjunctionNode) expression;
        if (conjunctionNode.isNegative())
        {
            return true;
        }

        // Recurse to all the child nodes of this conjunction node, and if
        // any subtree can be normalized, return true. Otherwise, return false.
        return conjunctionNode.getValues().stream().anyMatch(this::canNormalize);
    }

    @Override
    public BENode normalize(final BENode expression)
    {
        if (!canNormalize(expression))
        {
            return expression;
        }
        final BENode result = BENode.from(expression);
        doNormalize(result, false);
        return result;
    }

    /**
     * A recursive implementation of De_Morgan's Laws to remove negation from all conjunction
     * nodes by distributing it to their children and flipping the conjunction type.
     */
    private void doNormalize(final BENode node, final boolean parentWasFlipped)
    {
        if (parentWasFlipped)
        {
            node.setNegative(!node.isNegative());
        }

        if (node instanceof BEConjunctionNode)
        {
            final BEConjunctionNode conjunctionNode = (BEConjunctionNode) node;
            final boolean flip = conjunctionNode.isNegative();
            if (flip)
            {
                conjunctionNode.setNegative(false);
                conjunctionNode.setType(flipType(conjunctionNode.getTypeEnum()));
            }
            conjunctionNode.getValues().forEach(child -> doNormalize(child, flip));
        }

    }

    private static BEConjunctionType flipType(final BEConjunctionType type)
    {
        switch (type)
        {
            case OR: return BEConjunctionType.AND;
            case AND: return BEConjunctionType.OR;
            default: throw new IllegalArgumentException("Unknown Conjunction Type: " + type);
        }
    }

}
