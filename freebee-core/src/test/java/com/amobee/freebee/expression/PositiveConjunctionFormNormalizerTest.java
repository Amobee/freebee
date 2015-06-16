package com.amobee.freebee.expression;

import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.amobee.freebee.ExpressionUtil.*;
import static org.junit.Assert.*;

public class PositiveConjunctionFormNormalizerTest
{

    private static class ExpectedResult
    {
        boolean canNormalize;
        BENode normalizedExpression;
        String label;

        static ExpectedResult of(final boolean canNormalize, final BENode normalizedExpression, final String label)
        {
            final ExpectedResult result = new ExpectedResult();
            result.canNormalize = canNormalize;
            result.normalizedExpression = normalizedExpression;
            result.label = label;
            return result;
        }
    }

    private static Map<BENode, ExpectedResult> TEST_CASES;

    @BeforeClass
    public static void beforeClass()
    {
        /* TEST CASES
         * ==========
         * false: A -> A
         * false: !A -> !A
         * false: A and B -> A and B
         * false: !A and !B -> !A and !B
         * false: !A or !B -> !A or !B
         * false: REF -> REF
         * false: !REF -> !REF
         * false: A and REF -> A and !REF
         * false: A or !REF -> A or !REF
         * false: A AND (B OR C) -> same
         *
         * true: !(A and B) -> !A or !B
         * true: !(!A and !B) -> A or B
         * true: !(A and B and !C) -> !A or !B or C
         * true: !(A or B) -> !A and !B
         * true: !(!A or !B) -> !A and !B
         * true: !(A and (B or C)) -> !A or !(B AND C) -> !A OR (!B AND !C)
         */

        TEST_CASES = new HashMap<>();
        final BENode A = new BEPredicateNode("A", "a");
        final BENode NOT_A = new BEPredicateNode("A", true, "a");
        final BENode B = new BEPredicateNode("B", "b");
        final BENode NOT_B = new BEPredicateNode("B", true, "b");
        final BENode C = new BEPredicateNode("C", "c");
        final BENode NOT_C = new BEPredicateNode("C", true, "c");
        final BENode REF = new BEReferenceNode(Collections.singletonList("REF"));
        final BENode NOT_REF = new BEReferenceNode(true, Collections.singletonList("REF"));

        final BENode A_AND_B = and(A, B);
        final BENode notA_AND_notB = and(NOT_A, NOT_B);
        final BENode notA_or_notB = or(NOT_A, NOT_B);
        final BENode A_AND_REF = and(A, REF);
        final BENode A_OR_notREF = or(A, NOT_REF);
        final BENode A_AND__B_OR_C = and(A, or(B, C));

        final BENode A_NAND_B = nand(A, B);
        final BENode notA_OR_notB = or(NOT_A, NOT_B);
        final BENode notA_NAND_notB = nand(NOT_A, NOT_B);
        final BENode A_OR_B = or(A, B);
        final BENode NAND_A_B_notC = nand(A, B, NOT_C);
        final BENode OR_notA_notB_C = or(NOT_A, NOT_B, C);
        final BENode A_NOR_B = nor(A, B);
        final BENode notA_NOR_notB = nor(NOT_A, NOT_B);
        final BENode NAND_A__OR_B_C = nand(A, or(B, C));
        final BENode NOT_A_OR__NOT_B_AND_NOT_C = or(NOT_A, and(NOT_B, NOT_C));

        addTestCase("A -> A", A);
        addTestCase("!A -> !A", NOT_A);
        addTestCase("A and B -> A and B", A_AND_B);
        addTestCase("!A and !B -> !A and !B", notA_AND_notB);
        addTestCase(" !A or !B -> !A or !B", notA_or_notB);
        addTestCase("REF -> REF", REF);
        addTestCase("!REF -> !REF", NOT_REF);
        addTestCase("A and REF -> A and !REF", A_AND_REF);
        addTestCase("A or !REF -> A or !REF", A_OR_notREF);
        addTestCase("A AND (B OR C) -> same", A_AND__B_OR_C);

        addTestCase("!(A and B) -> !A or !B", A_NAND_B, notA_OR_notB);
        addTestCase("!(!A and !B) -> A or B", notA_NAND_notB, A_OR_B);
        addTestCase("!(A and B and !C) -> !A or !B or C", NAND_A_B_notC, OR_notA_notB_C);
        addTestCase("!(A or B) -> !A and !B", A_NOR_B, notA_AND_notB);
        addTestCase("!(!A or !B) -> A and B", notA_NOR_notB, A_AND_B);
        addTestCase("!(A and (B or C)) -> !A or !(B AND C) -> !A OR (!B AND !C)", NAND_A__OR_B_C, NOT_A_OR__NOT_B_AND_NOT_C);

    }

    @Test
    public void canNormalize()
    {
        final BEFormNormalizer normalizer = new PositiveConjunctionFormNormalizer();

        TEST_CASES.forEach((node, expectedResult) ->
            assertEquals(expectedResult.label, expectedResult.canNormalize, normalizer.canNormalize(node)));
    }

    @Test
    public void normalize()
    {
        final BEFormNormalizer normalizer = new PositiveConjunctionFormNormalizer();

        TEST_CASES.forEach((node, expectedResult) ->
                assertEquals(expectedResult.label, expectedResult.normalizedExpression, normalizer.normalize(node)));
    }

    // Helper methods

    private static void addTestCase(final String testCaseName, final BENode expressionThatCannotBeNormalized)
    {
        TEST_CASES.put(expressionThatCannotBeNormalized, ExpectedResult.of(false, expressionThatCannotBeNormalized, testCaseName));
    }

    private static void addTestCase(final String testCaseName, final BENode expression, final BENode normalizedExpression)
    {
        TEST_CASES.put(expression, ExpectedResult.of(true, normalizedExpression, testCaseName));
    }
}