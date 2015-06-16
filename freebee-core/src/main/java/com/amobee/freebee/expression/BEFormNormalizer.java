package com.amobee.freebee.expression;

/**
 * An interface for detecting normal forms of boolean expressions and
 * applying normalization transformations that maintain logical equivalency.
 *
 * @author Kevin Doran
 */
public interface BEFormNormalizer
{
    /**
     * Detect whether a given expression can be normalized by this normalizer implementation.
     *
     * @return true if this normalizer can apply normalizations to the given expression,
     *         false if the given expression is already in normal form for this normalizer
     */
    boolean canNormalize(BENode expression);

    /**
     * Normalize the given expression.
     *
     * The resulting expression will be transformed according to
     * the normalization rules of this normalizer, but it will
     * be logically equivalent to the input expression.
     *
     * If the expression is already in normal form,
     * the resulting expression should match the input argument.
     *
     * @param expression the expression to be normalized
     * @return a logically equivalent expression in normal form according to this normalizer
     */
    BENode normalize(BENode expression);
}
