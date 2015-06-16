package com.amobee.freebee.bench.simple;

import com.amobee.freebee.bench.ExpressionGenerator;
import com.amobee.freebee.expression.BEConjunctionNode;
import com.amobee.freebee.expression.BEConjunctionType;
import com.amobee.freebee.expression.BENode;
import com.amobee.freebee.expression.BEPredicateNode;
import com.amobee.freebee.expression.BooleanExpression;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SimpleExpressionGenerator implements ExpressionGenerator
{

    @Override
    public BooleanExpression generate()
    {

        // Fixed expression:
        //           OR
        //          /  \
        //        AND   6
        //       / | \
        //      OR OR 5
        //     /\  /\
        //    1 2 3 4

        final List<BENode> target1 = new ArrayList<>();
        target1.add(new BEPredicateNode("domain", "foo.com", "bar.org"));  // 1, 2
        target1.add(new BEPredicateNode("country", "US", "CA"));           // 3, 4
        target1.add(new BEPredicateNode("gender", "M"));                   // 5
        final BEPredicateNode target2 = new BEPredicateNode("genre", "comedy");  // 6

        return new BooleanExpression(
                "OR",
                Arrays.asList(
                        new BEConjunctionNode(BEConjunctionType.AND, target1),
                        target2
                )
        );
    }

}
