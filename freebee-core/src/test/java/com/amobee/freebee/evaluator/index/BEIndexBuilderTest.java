package com.amobee.freebee.evaluator.index;

import com.amobee.freebee.evaluator.interval.BEDefaultIntervalLabeler;
import com.amobee.freebee.evaluator.interval.BEIntervalOptimizer;

import org.junit.Test;

import java.util.Collections;

import static com.amobee.freebee.ExpressionUtil.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class BEIndexBuilderTest
{

    @Test
    public void testIndexMetrics() throws Exception
    {

        // Arrange
        final BEIntervalOptimizer mockOptimizer = (intervalLength, intervals) -> intervals.size() <= 2;
        final String exprPart1 = idExpr("ep1", "domain", "foo.com", "bar.com");
        final String exprPart2 = idExpr("ep2", "gender", "M");
        final String expr1 = exprConj("AND",
                expr("age", "[18,24]"),
                expr("ref", "ep1"));
        final String expr2 = exprConj("OR",
                expr("age", "[18,24]"),
                expr("ref", "ep1"),
                expr("ref", "ep2"));

        // Act
        final BEIndexBuilder<String> indexBuilder = new BEIndexBuilder<>(new BEDefaultIntervalLabeler(), mockOptimizer, new BEDefaultExpressionHashProvider<String>(), Collections.emptyList());
        indexBuilder.addPartialExpression(exprPart1);
        indexBuilder.addPartialExpression(exprPart2);
        indexBuilder.addExpression("e1", expr1);
        indexBuilder.addExpression("e2", expr2);
        final BEIndex<String> index = indexBuilder.build();

        // Assert
        assertNotNull(index);
        final BEIndexMetrics metrics = index.getIndexMetrics();
        assertNotNull(metrics);
        assertEquals(7, metrics.getIntervalCount());
        assertEquals(4, metrics.getExpressionCount());
        assertEquals(2, metrics.getFullExpressionCount());
        assertEquals(2, metrics.getPartialExpressionCount());
        assertEquals(3, metrics.getExpressionCountWithBitSetEvaluation());
        assertEquals(1, metrics.getExpressionCountWithIntervalEvaluation());

    }



}