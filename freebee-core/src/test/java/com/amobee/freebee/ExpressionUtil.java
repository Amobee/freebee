package com.amobee.freebee;

import com.amobee.freebee.evaluator.index.BEExpressionInfo;
import com.amobee.freebee.expression.BEConjunctionNode;
import com.amobee.freebee.expression.BEConjunctionType;
import com.amobee.freebee.expression.BENode;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;

/**
 * A test utility for created boolean expression JSON representations in a readable, concise, Java syntax.
 *
 * Example usage:
 *
 * <pre>
 * final String exprString =
 *     exprConj("OR",
 *         exprConj("AND", GA, GB),
 *         exprConj("AND", GC, GD, GE));
 * final BENode expression = createExpression(exprString);
 * </pre>
 *
 * @author Kevin Doran
 */
public class ExpressionUtil
{

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static BEConjunctionNode or(final BENode... children)
    {
        return conj(BEConjunctionType.OR, children);
    }

    public static BEConjunctionNode and(final BENode...children)
    {
        return conj(BEConjunctionType.AND, children);
    }

    public static BEConjunctionNode nor(final BENode... children)
    {
        return nConj(BEConjunctionType.OR, children);
    }

    public static BEConjunctionNode nand(final BENode... children)
    {
        return nConj(BEConjunctionType.AND, children);
    }

    public static BEConjunctionNode conj(final BEConjunctionType conjunctionType, final BENode...children)
    {
        return new BEConjunctionNode(conjunctionType, Arrays.asList(children));
    }

    public static BEConjunctionNode nConj(final BEConjunctionType conjunctionType, final BENode...children)
    {
        return new BEConjunctionNode(null, conjunctionType, true, Arrays.asList(children));
    }

    public static BEExpressionInfo<String> createExpressionInfo(final String data, final String exprString) {
        final BENode node = createExpression(exprString);
        return new BEExpressionInfo<>(data, node);
    }

    public static BENode createExpression(final String exprString) {
        try
        {
            return MAPPER.readValue(exprString, BENode.class);
        }
        catch (final IOException e)
        {
            throw new RuntimeException("Encountered exception while testing: " + e.getLocalizedMessage(), e);
        }
    }

    public static String exprConj(final BEConjunctionType conjType, final String... exprs)
    {
        final String joinedExprs = StringUtils.join(exprs, ",");
        return String.format("{\"type\":\"%s\",\"values\":[%s]}", conjType.toString().toLowerCase(), joinedExprs);
    }

    public static String exprConj(final String conjType, final String... exprs)
    {
        final String joinedExprs = StringUtils.join(exprs, ",");
        return String.format("{\"type\":\"%s\",\"values\":[%s]}", conjType, joinedExprs);
    }


    public static String expr(final String type, final String... values) {
        final String joinedJsonValues = values(values);
        return String.format("{\"type\":\"%s\",\"values\":[%s]}", type, joinedJsonValues);
    }

    public static String expr(final String type) {
        return String.format("{\"type\":\"%s\",\"values\":[]}", type);
    }

    public static String expr(final String type, final boolean negative, final String... values) {
        final String joinedJsonValues = values(values);
        return String.format("{\"type\":\"%s\",\"negative\":%s,\"values\":[%s]}", type, negative, joinedJsonValues);
    }

    public static String idExpr(final String id, final String type, final String... values) {
        final String joinedJsonValues = values(values);
        return String.format("{\"id\":\"%s\",\"type\":\"%s\",\"values\":[%s]}", id, type, joinedJsonValues);
    }

    public static String idExpr(final String id, final String type, final boolean negative, final String... values) {
        final String joinedJsonValues = values(values);
        return String.format("{\"id\":\"%s\",\"type\":\"%s\",\"negative\":%s,\"values\":[%s]}", id, type, negative, joinedJsonValues);
    }


    public static String values(final String... values) {
        final List<String> jsonValues = Arrays.stream(values)
                .map(v -> String.format("{\"id\":\"%s\"}", v))
                .collect(Collectors.toList());
        return String.join(",", jsonValues);
    }
}
