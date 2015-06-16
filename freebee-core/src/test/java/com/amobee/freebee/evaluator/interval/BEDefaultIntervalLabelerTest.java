package com.amobee.freebee.evaluator.interval;

import com.amobee.freebee.expression.BENode;

import java.util.Collection;
import java.util.Optional;

import org.junit.Test;

import static com.amobee.freebee.ExpressionUtil.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class BEDefaultIntervalLabelerTest
{

    private static final String GA = idExpr("GA", "gender", "A") ;
    private static final String GB = idExpr("GB", "gender", "B") ;
    private static final String GC = idExpr("GC", "gender", "C") ;
    private static final String GD = idExpr("GD", "gender", "D") ;
    private static final String GE = idExpr("GE", "gender", "E") ;
    private static final String GF = idExpr("GF", "gender", "F") ;
    private static final String GG = idExpr("GG", "gender", "G") ;
    private static final String GH = idExpr("GH", "gender", "H") ;
    private static final String GI = idExpr("GI", "gender", "I") ;
    private static final String GJ = idExpr("GJ", "gender", "J") ;
    private static final String GK = idExpr("GK", "gender", "K") ;
    private static final String GL = idExpr("GL", "gender", "L") ;
    private static final String GM = idExpr("GM", "gender", "M") ;
    private static final String GN = idExpr("GN", "gender", "N") ;
    private static final String GO = idExpr("GO", "gender", "O") ;
    private static final String GP = idExpr("GP", "gender", "P") ;
    private static final String GQ = idExpr("GQ", "gender", "Q") ;
    private static final String GR = idExpr("GR", "gender", "R") ;


    @Test
    public void test1DeepAnd1()
    {

        // Arrange
        final BEIntervalLabeler intervalLabeler = new BEDefaultIntervalLabeler();
        final String exprString = exprConj("AND", GA);
        final BENode expression = createExpression(exprString);

        // Act
        final Collection<BENodeInterval> intervals = intervalLabeler.labelExpression(expression);

        // Assert
        assertEquals(1, intervals.size());
        assertIntervalEquals("GA", 0, 1, intervals);
    }


    @Test
    public void test1DeepOr1()
    {

        // Arrange
        final BEIntervalLabeler intervalLabeler = new BEDefaultIntervalLabeler();
        final String exprString = exprConj("OR", GA);
        final BENode expression = createExpression(exprString);

        // Act
        final Collection<BENodeInterval> intervals = intervalLabeler.labelExpression(expression);

        // Assert
        assertEquals(1, intervals.size());
        assertIntervalEquals("GA", 0, 1, intervals);
    }


    @Test
    public void test1DeepAnd2()
    {

        // Arrange
        final BEIntervalLabeler intervalLabeler = new BEDefaultIntervalLabeler();
        final String exprString = exprConj("AND", GA, GB);
        final BENode expression = createExpression(exprString);

        // Act
        final Collection<BENodeInterval> intervals = intervalLabeler.labelExpression(expression);

        // Assert
        assertEquals(2, intervals.size());
        assertIntervalEquals("GA", 0, 1, intervals);
        assertIntervalEquals("GB", 1, 2, intervals);
    }


    @Test
    public void test1DeepOr2()
    {

        // Arrange
        final BEIntervalLabeler intervalLabeler = new BEDefaultIntervalLabeler();
        final String exprString = exprConj("OR", GA, GB);
        final BENode expression = createExpression(exprString);

        // Act
        final Collection<BENodeInterval> intervals = intervalLabeler.labelExpression(expression);

        // Assert
        assertEquals(2, intervals.size());
        assertIntervalEquals("GA", 0, 2, intervals);
        assertIntervalEquals("GB", 0, 2, intervals);
    }


    /**
     *          ------OR(a)-----
     *         /                \
     *       AND(b)           --AND(c)--
     *      /  \             /     \    \
     *    A    B            C      D     E
     *
     *     0  1  2  3  4
     *  A  x
     *  B     x  x  x  x
     *  C  x  x  x
     *  D           x
     *  E              x
     */
    @Test
    public void test2DeepOrOfAnds()
    {

        // Arrange
        final BEIntervalLabeler intervalLabeler = new BEDefaultIntervalLabeler();
        final String exprString =
                exprConj("OR",
                        exprConj("AND", GA, GB),
                        exprConj("AND", GC, GD, GE));
        final BENode expression = createExpression(exprString);

        // Act
        final Collection<BENodeInterval> intervals = intervalLabeler.labelExpression(expression);

        // Assert
        assertEquals(5, intervals.size());
        assertIntervalEquals("GA", 0, 1, intervals);
        assertIntervalEquals("GB", 1, 5, intervals);
        assertIntervalEquals("GC", 0, 3, intervals);
        assertIntervalEquals("GD", 3, 4, intervals);
        assertIntervalEquals("GE", 4, 5, intervals);
    }


    /**
     *          ------AND(a)----
     *         /                \
     *       OR(b)            --OR(c)
     *      /  \             /   \   \
     *    A    B            C    D    E
     *
     *     0  1  2  3  4
     *  A  x  x
     *  B  x  x
     *  C        x  x  x
     *  D        x  x  x
     *  E        x  x  x
     */
    @Test
    public void test2DeepAndOfOrs()
    {

        // Arrange
        final BEIntervalLabeler intervalLabeler = new BEDefaultIntervalLabeler();
        final String exprString =
                exprConj("AND",
                        exprConj("OR", GA, GB),
                        exprConj("OR", GC, GD, GE));
        final BENode expression = createExpression(exprString);

        // Act
        final Collection<BENodeInterval> intervals = intervalLabeler.labelExpression(expression);

        // Assert
        assertEquals(5, intervals.size());
        assertIntervalEquals("GA", 0, 2, intervals);
        assertIntervalEquals("GB", 0, 2, intervals);
        assertIntervalEquals("GC", 2, 5, intervals);
        assertIntervalEquals("GD", 2, 5, intervals);
        assertIntervalEquals("GE", 2, 5, intervals);
    }


    /**
     *                        ---- OR(a)----
     *                      /         |      \
     *              ----- AND(b)--    C     AND(c)
     *            /                \      /  |  \
     *           A                 B     D   E  F
     *
     *    0  1  2  3  4  5  6
     * A  x
     * B     x  x  x  x  x  x
     * C  x  x  x  x  x  x  x
     * D  x  x  x  x  x
     * E                 x
     * F                    x
     */
    @Test
    public void test2DeepOrOfAnds2()
    {
        // Arrange
        final BEIntervalLabeler intervalLabeler = new BEDefaultIntervalLabeler();
        final String exprString =
                exprConj("OR",
                        exprConj("AND", GA, GB),
                        GC,
                        exprConj("AND", GD, GE, GF));
        final BENode expression = createExpression(exprString);

        // Act
        final Collection<BENodeInterval> intervals = intervalLabeler.labelExpression(expression);

        // Assert
        assertEquals(6, intervals.size());
        assertIntervalEquals("GA", 0, 1, intervals);
        assertIntervalEquals("GB", 1, 6, intervals);
        assertIntervalEquals("GC", 0, 6, intervals);
        assertIntervalEquals("GD", 0, 4, intervals);
        assertIntervalEquals("GE", 4, 5, intervals);
        assertIntervalEquals("GF", 5, 6, intervals);
    }


    /**
     *                      AND (a)
     *               /               \
     *             OR (b)            AND (e)
     *         /        \        /       \
     *       AND (c)   OR (d)  OR (f)   AND (g)
     *      /  \      /   \    /  \    /    \
     *     A   B     C    D   E   F   G     H
     *
     *     0 1 2 3 4 5 6 7
     *   a x x x x x x x x
     *   b x x x x
     *   c x x x x
     *   d x x x x
     *   e         x x x x
     *   f         x x
     *   g             x x
     *   A x
     *   B   x x x
     *   C x x x x
     *   D x x x x
     *   E         x x
     *   F         x x
     *   G             x
     *   H               x
     */
    @Test
    public void test3DeepMixedAndsOrs1()
    {
        // Arrange
        final BEIntervalLabeler intervalLabeler = new BEDefaultIntervalLabeler();
        final String exprString =
                exprConj("AND",
                        exprConj("OR",
                                exprConj("AND", GA, GB),
                                exprConj("OR", GC, GD)),
                        exprConj("AND",
                                exprConj("OR", GE, GF),
                                exprConj("AND", GG, GH)));
        final BENode expression = createExpression(exprString);

        // Act
        final Collection<BENodeInterval> intervals = intervalLabeler.labelExpression(expression);

        // Assert
        assertEquals(8, intervals.size());
        assertIntervalEquals("GA", 0, 1, intervals);
        assertIntervalEquals("GB", 1, 4, intervals);
        assertIntervalEquals("GC", 0, 4, intervals);
        assertIntervalEquals("GD", 0, 4, intervals);
        assertIntervalEquals("GE", 4, 6, intervals);
        assertIntervalEquals("GF", 4, 6, intervals);
        assertIntervalEquals("GG", 6, 7, intervals);
        assertIntervalEquals("GH", 7, 8, intervals);
    }


    /**
     *           _____________AND(a)____________
     *         /                |                \
     *       OR(b)           _OR(e)_          __AND(h)__
     *      /     \        /        \       /     |      \
     *    OR(c) AND(d)    AND(f)   AND(g) OR(i) AND(j) AND(k)
     *   / \   / |  \   / |  |  \   / \   / \   /  \  / |  \
     *  A  B  C  D  E  F  G  H  I  J  K  L  M  N  O  P  Q  R
     *
     *      0  1  2  3  4  5  6  7  8  9 10 11 12 13 14 15 16 17
     *   a  x  x  x  x  x  x  x  x  x  x  x  x  x  x  x  x  x  x
     *   b  x  x  x  x  x
     *   c  x  x  x  x  x
     *   d  x  x  x  x  x
     *   e                 x  x  x  x  x  x
     *   f                 x  x  x  x  x  x
     *   g                 x  x  x  x  x  x
     *   h                                   x  x  x  x  x  x  x
     *   i                                   x  x
     *   j                                         x  x
     *   k                                               x  x  x
     *   A  x  x  x  x  x
     *   B  x  x  x  x  x
     *   C  x  x  x
     *   D           x
     *   E              x
     *   F                 x
     *   G                    x
     *   H                       x
     *   I                          x  x  x
     *   J                 x  x  x  x  x
     *   K                                x
     *   L                                   x  x
     *   M                                   x  x
     *   N                                         x
     *   O                                            x
     *   P                                               x
     *   Q                                                  x
     *   R                                                     x
     *
     */
    @Test
    public void test3DeepMixedAndsOrs2()
    {
        // Arrange
        final BEIntervalLabeler intervalLabeler = new BEDefaultIntervalLabeler();
        final String exprString =
                exprConj("AND",
                        exprConj("OR",
                                exprConj("OR", GA, GB),
                                exprConj("AND", GC, GD, GE)),
                        exprConj("OR",
                                exprConj("AND", GF, GG, GH, GI),
                                exprConj("AND", GJ, GK)),
                        exprConj("AND",
                                exprConj("OR", GL, GM),
                                exprConj("AND", GN, GO),
                                exprConj("AND", GP, GQ, GR)));
        final BENode expression = createExpression(exprString);

        // Act
        final Collection<BENodeInterval> intervals = intervalLabeler.labelExpression(expression);

        // Assert
        assertEquals(18, intervals.size());
        assertIntervalEquals("GA", 0, 5, intervals);
        assertIntervalEquals("GB", 0, 5, intervals);
        assertIntervalEquals("GC", 0, 3, intervals);
        assertIntervalEquals("GD", 3, 4, intervals);
        assertIntervalEquals("GE", 4, 5, intervals);
        assertIntervalEquals("GF", 5, 6, intervals);
        assertIntervalEquals("GG", 6, 7, intervals);
        assertIntervalEquals("GH", 7, 8, intervals);
        assertIntervalEquals("GI", 8, 11, intervals);
        assertIntervalEquals("GJ", 5, 10, intervals);
        assertIntervalEquals("GK", 10, 11, intervals);
        assertIntervalEquals("GL", 11, 13, intervals);
        assertIntervalEquals("GM", 11, 13, intervals);
        assertIntervalEquals("GN", 13, 14, intervals);
        assertIntervalEquals("GO", 14, 15, intervals);
        assertIntervalEquals("GP", 15, 16, intervals);
        assertIntervalEquals("GQ", 16, 17, intervals);
        assertIntervalEquals("GR", 17, 18, intervals);
    }


    /**
     *                  _________________AND(a)__
     *                /                    |     \
     *        _____AND(b)______           OR(h) OR(i)
     *      /        |          \         / \   / \
     *    AND(c)   AND(d)      OR(e)     L  M  N  O
     *   / |  \   / |  \     /      \
     *  A  B  C  D  E  F   OR(f)  AND(g)
     *                     / \   / |  \
     *                    G  H  I  J  K
     *
     *      0  1  2  3  4  5  6  7  8  9 10 11 12 13 14
     *   a  x  x  x  x  x  x  x  x  x  x  x  x  x  x  x
     *   b  x  x  x  x  x  x  x  x  x  x  x
     *   c  x  x  x
     *   d           x  x  x
     *   e                    x  x  x  x  x
     *   f                    x  x  x  x  x
     *   g                    x  x  x  x  x
     *   h                                   x  x
     *   i                                         x  x
     *   A  x
     *   B     x
     *   C        x
     *   D           x
     *   E              x
     *   F                 x
     *   G                    x  x  x  x  x
     *   H                    x  x  x  x  x
     *   I                    x  x  x
     *   J                             x
     *   K                                x
     *   L                                   x  x
     *   M                                   x  x
     *   N                                         x  x
     *   O                                         x  x
     *
     */
    @Test
    public void test4DeepMixedAndsOrs()
    {
        // Arrange
        final BEIntervalLabeler intervalLabeler = new BEDefaultIntervalLabeler();
        final String exprString =
                exprConj("AND",
                        exprConj("AND",
                                exprConj("AND", GA, GB, GC),
                                exprConj("AND", GD, GE, GF),
                                exprConj("OR",
                                        exprConj("OR", GG, GH),
                                        exprConj("AND", GI, GJ, GK))),
                        exprConj("OR", GL, GM),
                        exprConj("OR", GN, GO));
        final BENode expression = createExpression(exprString);

        // Act
        final Collection<BENodeInterval> intervals = intervalLabeler.labelExpression(expression);

        // Assert
        assertEquals(15, intervals.size());
        assertIntervalEquals("GA", 0, 1, intervals);
        assertIntervalEquals("GB", 1, 2, intervals);
        assertIntervalEquals("GC", 2, 3, intervals);

        assertIntervalEquals("GD", 3, 4, intervals);
        assertIntervalEquals("GE", 4, 5, intervals);
        assertIntervalEquals("GF", 5, 6, intervals);

        assertIntervalEquals("GG", 6, 11, intervals);
        assertIntervalEquals("GH", 6, 11, intervals);

        assertIntervalEquals("GI", 6, 9, intervals);
        assertIntervalEquals("GJ", 9, 10, intervals);
        assertIntervalEquals("GK", 10, 11, intervals);

        assertIntervalEquals("GL", 11, 13, intervals);
        assertIntervalEquals("GM", 11, 13, intervals);

        assertIntervalEquals("GN", 13, 15, intervals);
        assertIntervalEquals("GO", 13, 15, intervals);
    }


    // --- Helper Assertion Methods ---

    private void assertIntervalEquals(
            final String nodeId,
            final int expectedStart,
            final int expectedEnd,
            final Collection<BENodeInterval> actualIntervals)
    {
        final Optional<BENodeInterval> actualInterval = actualIntervals.stream()
                .filter(interval -> interval.getNode() != null && nodeId.equals(interval.getNode().getId()))
                .findFirst();
        assertTrue(actualInterval.isPresent());
        assertIntervalEquals(expectedStart, expectedEnd, actualInterval.get());
    }

    private void assertIntervalEquals(
            final int expectedStart, final int expectedEnd, final Interval actualInterval)
    {
        assertEquals(expectedStart, actualInterval.getStart());
        assertEquals(expectedEnd, actualInterval.getEnd());
    }

}