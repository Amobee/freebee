package com.amobee.freebee.evaluator.interval;

import com.amobee.freebee.expression.BENode;

import java.util.Collection;

/**
 * An interface for the interval labeling algorithm (Algorithm 4) from Boolean Expression Evaluator white paper [1].
 *
 * [1] Fontoura, Marcus, Suhas Sadanandan, Jayavel Shanmugasundaram, Sergei Vassilvitski, Erik Vee, Srihari Venkatesan, and Jason Zien. 2010.
 *     “Efficiently Evaluating Complex Boolean Expressions.”
 *     In Proceedings of the 2010 International Conference on Management of Data - SIGMOD ’10, 3.
 *     Indianapolis, Indiana, USA: ACM Press. https://doi.org/10.1145/1807167.1807171.
 *     http://theory.stanford.edu/~sergei/papers/sigmod10-index.pdf
 *
 * @author Kevin Doran
 * @see BEDefaultIntervalLabeler
 */
public interface BEIntervalLabeler
{
    /**
     * Given a boolean expression, assign an interval label to each leaf node.
     *
     * @param expr the expression to label. Must not be null and must be the root of the expression
     * @return a collection of intervals for each leaf node, with each interval containing a reference
     *         to its corresponding leaf node
     */
    Collection<BENodeInterval> labelExpression(BENode expr);
}
