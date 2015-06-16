package com.amobee.freebee.expression;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Boolean expression test utilities.
 *
 * @author Michael Bond
 */
public class BETestUtils
{
    public static BEConjunctionNode createExpression()
    {
        final BEConjunctionNode expression = new BEConjunctionNode(
                "test1",
                BEConjunctionType.AND,
                new ArrayList<>());

        final BEConjunctionNode or = new BEConjunctionNode(BEConjunctionType.OR);
        or.addValue(new BEPredicateNode("gender", "M", "F"));
        or.addValue(new BEPredicateNode("age", "18-24", "25-34"));
        expression.addValue(or);

        expression.addValue(new BEPredicateNode("geo", "US"));
        expression.addValue(new BEPredicateNode("domain", true, "baddomain.com"));

        final BEAttributeValue value = new BEAttributeValue("WKD");
        value.addProperty("tz", "EST");
        expression.addValue(new BEPredicateNode("daypart", false, Collections.singletonList(value)));

        return expression;
    }

    private BETestUtils()
    {
    }
}
