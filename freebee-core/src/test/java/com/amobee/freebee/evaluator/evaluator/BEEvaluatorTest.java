package com.amobee.freebee.evaluator.evaluator;

import com.amobee.freebee.ExpressionUtil;
import com.amobee.freebee.config.BEDataTypeConfig;
import com.amobee.freebee.expression.BENode;
import com.amobee.freebee.expression.BEPredicateNode;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Michael Bond
 */
public class BEEvaluatorTest
{
    private static final String DATA_TYPE_CONFIG = "[" +
            "{\"type\":\"gender\",\"ignorecase\":true}," +
            "{\"type\":\"age\",\"dataType\":\"byte\",\"range\":true}," +
            "{\"type\":\"domain\",\"ignorecase\":true,\"partial\":true,\"reverse\":true}," +
            "{\"type\":\"country\",\"ignorecase\":true}]";

    private static final String G1 = "{\"type\":\"gender\",\"values\":[{\"id\":\"M\"}]}";
    private static final String G2 = "{\"type\":\"gender\",\"values\":[{\"id\":\"M\"},{\"id\":\"F\"}]}";
    private static final String AR1 = "{\"type\":\"age\",\"values\":[{\"id\":\"[18,34)\"}]}";
    private static final String AR2 = "{\"type\":\"age\",\"values\":[{\"id\":\"[18,34)\"},{\"id\":\"52\"}]}";
    private static final String CY1 = "{\"type\":\"country\",\"values\":[{\"id\":\"US\"}]}";
    private static final String CY2 = "{\"type\":\"country\",\"values\":[{\"id\":\"US\"},{\"id\":\"CA\"}]}";
    private static final String D1N = "{\"type\":\"domain\",\"negative\":true,\"values\":[{\"id\":\"baddomain.com\"}]}";
    // check how negative works - does it negate the whole thing or each one individually?
    private static final String D2N = "{\"type\":\"domain\",\"negative\":true,\"values\":[{\"id\":\"baddomain.com\"},{\"id\":\"baddomain2.com\"}]}";
    private static final String D1 = "{\"type\":\"domain\",\"negative\":false,\"values\":[{\"id\":\"gooddomain.com\"}]}";
    private static final String D2 = "{\"type\":\"domain\",\"negative\":false,\"values\":[{\"id\":\"gooddomain.com\"},{\"id\":\"gooddomain2.com\"}]}";

    private static final String GA = "{\"type\":\"gender\",\"values\":[{\"id\":\"a\"}]}";
    private static final String GB = "{\"type\":\"gender\",\"values\":[{\"id\":\"b\"}]}";
    private static final String GC = "{\"type\":\"gender\",\"values\":[{\"id\":\"c\"}]}";
    private static final String GD = "{\"type\":\"gender\",\"values\":[{\"id\":\"d\"}]}";
    private static final String GE = "{\"type\":\"gender\",\"values\":[{\"id\":\"e\"}]}";
    private static final String GF = "{\"type\":\"gender\",\"values\":[{\"id\":\"f\"}]}";
    private static final String GG = "{\"type\":\"gender\",\"values\":[{\"id\":\"g\"}]}";
    private static final String GH = "{\"type\":\"gender\",\"values\":[{\"id\":\"h\"}]}";
    private static final String GI = "{\"type\":\"gender\",\"values\":[{\"id\":\"i\"}]}";
    private static final String GJ = "{\"type\":\"gender\",\"values\":[{\"id\":\"j\"}]}";
    private static final String GK = "{\"type\":\"gender\",\"values\":[{\"id\":\"k\"}]}";
    private static final String GL = "{\"type\":\"gender\",\"values\":[{\"id\":\"l\"}]}";
    private static final String GM = "{\"type\":\"gender\",\"values\":[{\"id\":\"m\"}]}";
    private static final String GN = "{\"type\":\"gender\",\"values\":[{\"id\":\"n\"}]}";
    private static final String GO = "{\"type\":\"gender\",\"values\":[{\"id\":\"o\"}]}";
    private static final String GP = "{\"type\":\"gender\",\"values\":[{\"id\":\"p\"}]}";
    private static final String GQ = "{\"type\":\"gender\",\"values\":[{\"id\":\"q\"}]}";
    private static final String GR = "{\"type\":\"gender\",\"values\":[{\"id\":\"r\"}]}";

    private String exprConj(final String conj, final String... exprs)
    {
        final String exprList = StringUtils.join(exprs, ",");
        return "{\"type\":\"" + conj + "\",\"values\":[" + exprList + "]}";
    }

    private void assertResultContains(final Set<String> result, final Boolean positive, final String exprName) throws Exception
    {
        if (positive)
        {
            assertTrue(exprName, result.contains(exprName));
        } else
        {
            assertFalse(exprName, result.contains(exprName));
        }
    }

    @Test // single elements in expressions, and, or
    public void test0() throws Exception
    {
        final BEEvaluatorBuilder<String> builder = new BEEvaluatorBuilder<>();
        builder.addDataTypeConfig(DATA_TYPE_CONFIG);

        builder.addExpression("G1", G1);
        builder.addExpression("AR1", AR1);
        builder.addExpression("G1 and AR1", exprConj("and", G1, AR1));
        builder.addExpression("G1 or AR1", exprConj("or", G1, AR1));
        final BEEvaluator<String> evaluator = builder.build();

        BEInput input;
        Set<String> result;

        input = new BEInput();
        input.getOrCreateStringCategory("age").add("20"); // match
        input.getOrCreateStringCategory("gender").add("M"); // match

        result = evaluator.evaluate(input);
        assertEquals(4, result.size());
        assertTrue("G1", result.contains("G1"));
        assertTrue("AR1", result.contains("AR1"));
        assertTrue("G1 and AR1", result.contains("G1 and AR1"));
        assertTrue("G1 or AR1", result.contains("G1 or AR1"));

        input = new BEInput();
        input.getOrCreateStringCategory("age").add("15"); // no match
        input.getOrCreateStringCategory("gender").add("M"); // match

        result = evaluator.evaluate(input);
        assertEquals(2, result.size());
        assertTrue("G1", result.contains("G1"));
        assertFalse("AR1", result.contains("AR1"));
        assertFalse("G1 and AR1", result.contains("G1 and AR1"));
        assertTrue("G1 or AR1", result.contains("G1 or AR1"));

        input = new BEInput();
        input.getOrCreateStringCategory("age").add("15"); // no match
        input.getOrCreateStringCategory("gender").add("F"); // no match

        result = evaluator.evaluate(input);
        assertEquals(0, result.size());
        assertFalse("G1", result.contains("G1"));
        assertFalse("AR1", result.contains("AR1"));
        assertFalse("G1 and AR1", result.contains("G1 and AR1"));
        assertFalse("G1 or AR1", result.contains("G1 or AR1"));
    }

    @Test // multiple elements in expressions, and, or
    public void test1() throws Exception
    {
        final BEEvaluatorBuilder<String> builder = new BEEvaluatorBuilder<>();
        builder.addDataTypeConfig(DATA_TYPE_CONFIG);

        builder.addExpression("AR2", AR2);
        builder.addExpression("D2", D2);
        builder.addExpression("AR2 and D2", exprConj("and", AR2, D2));
        builder.addExpression("AR2 or D2", exprConj("or", AR2, D2));
        final BEEvaluator<String> evaluator = builder.build();

        BEInput input;
        Set<String> result;

        // single values in record
        input = new BEInput();
        input.getOrCreateStringCategory("age").add("20"); // match
        input.getOrCreateStringCategory("domain").add("gooddomain.com"); // match

        result = evaluator.evaluate(input);
        assertEquals(4, result.size());
        assertTrue("AR2", result.contains("AR2"));
        assertTrue("D2", result.contains("D2"));
        assertTrue("AR2 and D2", result.contains("AR2 and D2"));
        assertTrue("AR2 or D2", result.contains("AR2 or D2"));

        input = new BEInput();
        input.getOrCreateStringCategory("age").add("20"); // match
        input.getOrCreateStringCategory("domain").add("baddomain.com"); // no match

        result = evaluator.evaluate(input);
        assertEquals(2, result.size());
        assertTrue("AR2", result.contains("AR2"));
        assertFalse("D2", result.contains("D2"));
        assertFalse("AR2 and D2", result.contains("AR2 and D2"));
        assertTrue("AR2 or D2", result.contains("AR2 or D2"));

        input = new BEInput();
        input.getOrCreateStringCategory("age").add("50"); // no match
        input.getOrCreateStringCategory("domain").add("baddomain.com"); // no match

        result = evaluator.evaluate(input);
        assertEquals(0, result.size());
        assertFalse("AR2", result.contains("AR2"));
        assertFalse("D2", result.contains("D2"));
        assertFalse("AR2 and D2", result.contains("AR2 and D2"));
        assertFalse("AR2 or D2", result.contains("AR2 or D2"));

        // multiple values in record
        input = new BEInput();
        input.getOrCreateStringCategory("age").add("20"); // match
        input.getOrCreateStringCategory("age").add("52"); // match
        input.getOrCreateStringCategory("domain").add("gooddomain.com"); // match
        input.getOrCreateStringCategory("domain").add("gooddomain2.com"); // match

        result = evaluator.evaluate(input);
        assertEquals(4, result.size());
        assertTrue("AR2", result.contains("AR2"));
        assertTrue("D2", result.contains("D2"));
        assertTrue("AR2 and D2", result.contains("AR2 and D2"));
        assertTrue("AR2 or D2", result.contains("AR2 or D2"));

        input = new BEInput();
        input.getOrCreateStringCategory("age").add("20"); // match
        input.getOrCreateStringCategory("age").add("45"); // no match
        input.getOrCreateStringCategory("domain").add("gooddomain.com"); // match
        input.getOrCreateStringCategory("domain").add("baddomain.com"); // no match

        result = evaluator.evaluate(input);
        assertEquals(4, result.size());
        assertTrue("AR2", result.contains("AR2"));
        assertTrue("D2", result.contains("D2"));
        assertTrue("AR2 and D2", result.contains("AR2 and D2"));
        assertTrue("AR2 or D2", result.contains("AR2 or D2"));

        input = new BEInput();
        input.getOrCreateStringCategory("age").add("20"); // match
        input.getOrCreateStringCategory("age").add("52"); // match
        input.getOrCreateStringCategory("domain").add("baddomain.com"); // no match
        input.getOrCreateStringCategory("domain").add("baddomain2.com"); // no match

        result = evaluator.evaluate(input);
        assertEquals(2, result.size());
        assertTrue("AR2", result.contains("AR2"));
        assertFalse("D2", result.contains("D2"));
        assertFalse("AR2 and D2", result.contains("AR2 and D2"));
        assertTrue("AR2 or D2", result.contains("AR2 or D2"));

        input = new BEInput();
        input.getOrCreateStringCategory("age").add("20"); // match
        input.getOrCreateStringCategory("age").add("40"); // no match
        input.getOrCreateStringCategory("domain").add("baddomain.com"); // no match
        input.getOrCreateStringCategory("domain").add("baddomain2.com"); // no match

        result = evaluator.evaluate(input);
        assertEquals(2, result.size());
        assertTrue("AR2", result.contains("AR2"));
        assertFalse("D2", result.contains("D2"));
        assertFalse("AR2 and D2", result.contains("AR2 and D2"));
        assertTrue("AR2 or D2", result.contains("AR2 or D2"));

        input = new BEInput();
        input.getOrCreateStringCategory("age").add("10"); // no match
        input.getOrCreateStringCategory("age").add("40"); // no match
        input.getOrCreateStringCategory("domain").add("baddomain.com"); // no match
        input.getOrCreateStringCategory("domain").add("baddomain2.com"); // no match

        result = evaluator.evaluate(input);
        assertEquals(0, result.size());
        assertFalse("AR2", result.contains("AR2"));
        assertFalse("D2", result.contains("D2"));
        assertFalse("AR2 and D2", result.contains("AR2 and D2"));
        assertFalse("AR2 or D2", result.contains("AR2 or D2"));
    }

    @Test // single elements in expressions, nested and, or
    public void test2() throws Exception
    {
        final BEEvaluatorBuilder<String> builder = new BEEvaluatorBuilder<>();
        builder.addDataTypeConfig(DATA_TYPE_CONFIG);

        builder.addExpression("G1", G1);
        builder.addExpression("AR1", AR1);
        builder.addExpression("D1", D1);
        builder.addExpression("CY1", CY1);
        builder.addExpression("G1 and AR1", exprConj("and", G1, AR1));
        builder.addExpression("D1 and CY1", exprConj("and", D1, CY1));
        builder.addExpression("G1 or AR1", exprConj("or", G1, AR1));
        builder.addExpression("D1 or CY1", exprConj("or", D1, CY1));
        builder.addExpression("(G1 and AR1) and (D1 and CY1)", exprConj("and", exprConj("and", G1, AR1), exprConj("and", D1, CY1)));
        builder.addExpression("(G1 and AR1) or (D1 and CY1)", exprConj("or", exprConj("and", G1, AR1), exprConj("and", D1, CY1)));
        builder.addExpression("(G1 or AR1) and (D1 or CY1)", exprConj("and", exprConj("or", G1, AR1), exprConj("or", D1, CY1)));
        builder.addExpression("(G1 or AR1) or (D1 or CY1)", exprConj("or", exprConj("or", G1, AR1), exprConj("or", D1, CY1)));
        builder.addExpression("(G1 and AR1) and (D1 or CY1)", exprConj("and", exprConj("and", G1, AR1), exprConj("or", D1, CY1)));
        builder.addExpression("(G1 and AR1) or (D1 or CY1)", exprConj("or", exprConj("and", G1, AR1), exprConj("or", D1, CY1)));
        builder.addExpression("(G1 or AR1) and (D1 and CY1)", exprConj("and", exprConj("or", G1, AR1), exprConj("and", D1, CY1)));
        builder.addExpression("(G1 or AR1) or (D1 and CY1)", exprConj("or", exprConj("or", G1, AR1), exprConj("and", D1, CY1)));
        final BEEvaluator<String> evaluator = builder.build();

        BEInput input;
        Set<String> result;

        input = new BEInput();
        input.getOrCreateStringCategory("gender").add("M"); // match
        input.getOrCreateStringCategory("age").add("20"); // match
        input.getOrCreateStringCategory("domain").add("gooddomain.com"); // match
        input.getOrCreateStringCategory("country").add("US"); // match

        result = evaluator.evaluate(input);
        assertEquals(16, result.size());
        assertResultContains(result, true, "G1");
        assertResultContains(result, true, "AR1");
        assertResultContains(result, true, "D1");
        assertResultContains(result, true, "CY1");
        assertResultContains(result, true, "G1 and AR1");
        assertResultContains(result, true, "D1 and CY1");
        assertResultContains(result, true, "G1 or AR1");
        assertResultContains(result, true, "D1 or CY1");
        assertResultContains(result, true, "(G1 and AR1) and (D1 and CY1)");
        assertResultContains(result, true, "(G1 and AR1) or (D1 and CY1)");
        assertResultContains(result, true, "(G1 or AR1) and (D1 or CY1)");
        assertResultContains(result, true, "(G1 or AR1) or (D1 or CY1)");
        assertResultContains(result, true, "(G1 and AR1) and (D1 or CY1)");
        assertResultContains(result, true, "(G1 and AR1) or (D1 or CY1)");
        assertResultContains(result, true, "(G1 or AR1) and (D1 and CY1)");
        assertResultContains(result, true, "(G1 or AR1) or (D1 and CY1)");

        // one not match
        input = new BEInput();
        input.getOrCreateStringCategory("gender").add("F"); // no match
        input.getOrCreateStringCategory("age").add("20"); // match
        input.getOrCreateStringCategory("domain").add("gooddomain.com"); // match
        input.getOrCreateStringCategory("country").add("US"); // match

        result = evaluator.evaluate(input);
        assertEquals(12, result.size());
        assertResultContains(result, false, "G1");
        assertResultContains(result, true, "AR1");
        assertResultContains(result, true, "D1");
        assertResultContains(result, true, "CY1");
        assertResultContains(result, false, "G1 and AR1");
        assertResultContains(result, true, "D1 and CY1");
        assertResultContains(result, true, "G1 or AR1");
        assertResultContains(result, true, "D1 or CY1");
        assertResultContains(result, false, "(G1 and AR1) and (D1 and CY1)");
        assertResultContains(result, true, "(G1 and AR1) or (D1 and CY1)");
        assertResultContains(result, true, "(G1 or AR1) and (D1 or CY1)");
        assertResultContains(result, true, "(G1 or AR1) or (D1 or CY1)");
        assertResultContains(result, false, "(G1 and AR1) and (D1 or CY1)");
        assertResultContains(result, true, "(G1 and AR1) or (D1 or CY1)");
        assertResultContains(result, true, "(G1 or AR1) and (D1 and CY1)");
        assertResultContains(result, true, "(G1 or AR1) or (D1 and CY1)");

        // two not match, same side
        input = new BEInput();
        input.getOrCreateStringCategory("gender").add("F"); // no match
        input.getOrCreateStringCategory("age").add("40"); // no match
        input.getOrCreateStringCategory("domain").add("gooddomain.com"); // match
        input.getOrCreateStringCategory("country").add("US"); // match

        result = evaluator.evaluate(input);
        assertEquals(8, result.size());
        assertResultContains(result, false, "G1");
        assertResultContains(result, false, "AR1");
        assertResultContains(result, true, "D1");
        assertResultContains(result, true, "CY1");
        assertResultContains(result, false, "G1 and AR1");
        assertResultContains(result, true, "D1 and CY1");
        assertResultContains(result, false, "G1 or AR1");
        assertResultContains(result, true, "D1 or CY1");
        assertResultContains(result, false, "(G1 and AR1) and (D1 and CY1)");
        assertResultContains(result, true, "(G1 and AR1) or (D1 and CY1)");
        assertResultContains(result, false, "(G1 or AR1) and (D1 or CY1)");
        assertResultContains(result, true, "(G1 or AR1) or (D1 or CY1)");
        assertResultContains(result, false, "(G1 and AR1) and (D1 or CY1)");
        assertResultContains(result, true, "(G1 and AR1) or (D1 or CY1)");
        assertResultContains(result, false, "(G1 or AR1) and (D1 and CY1)");
        assertResultContains(result, true, "(G1 or AR1) or (D1 and CY1)");

        // two not match, opposite sides
        input = new BEInput();
        input.getOrCreateStringCategory("gender").add("F"); // no match
        input.getOrCreateStringCategory("age").add("20"); // match
        input.getOrCreateStringCategory("domain").add("baddomain.com"); // no match
        input.getOrCreateStringCategory("country").add("US"); // match

        result = evaluator.evaluate(input);
        assertEquals(8, result.size());
        assertResultContains(result, false, "G1");
        assertResultContains(result, true, "AR1");
        assertResultContains(result, false, "D1");
        assertResultContains(result, true, "CY1");
        assertResultContains(result, false, "G1 and AR1");
        assertResultContains(result, false, "D1 and CY1");
        assertResultContains(result, true, "G1 or AR1");
        assertResultContains(result, true, "D1 or CY1");
        assertResultContains(result, false, "(G1 and AR1) and (D1 and CY1)");
        assertResultContains(result, false, "(G1 and AR1) or (D1 and CY1)");
        assertResultContains(result, true, "(G1 or AR1) and (D1 or CY1)");
        assertResultContains(result, true, "(G1 or AR1) or (D1 or CY1)");
        assertResultContains(result, false, "(G1 and AR1) and (D1 or CY1)");
        assertResultContains(result, true, "(G1 and AR1) or (D1 or CY1)");
        assertResultContains(result, false, "(G1 or AR1) and (D1 and CY1)");
        assertResultContains(result, true, "(G1 or AR1) or (D1 and CY1)");

        // three not match
        input = new BEInput();
        input.getOrCreateStringCategory("gender").add("F"); // no match
        input.getOrCreateStringCategory("age").add("40"); // no match
        input.getOrCreateStringCategory("domain").add("baddomain.com"); // no match
        input.getOrCreateStringCategory("country").add("US"); // match

        result = evaluator.evaluate(input);
        assertEquals(4, result.size());
        assertResultContains(result, false, "G1");
        assertResultContains(result, false, "AR1");
        assertResultContains(result, false, "D1");
        assertResultContains(result, true, "CY1");
        assertResultContains(result, false, "G1 and AR1");
        assertResultContains(result, false, "D1 and CY1");
        assertResultContains(result, false, "G1 or AR1");
        assertResultContains(result, true, "D1 or CY1");
        assertResultContains(result, false, "(G1 and AR1) and (D1 and CY1)");
        assertResultContains(result, false, "(G1 and AR1) or (D1 and CY1)");
        assertResultContains(result, false, "(G1 or AR1) and (D1 or CY1)");
        assertResultContains(result, true, "(G1 or AR1) or (D1 or CY1)");
        assertResultContains(result, false, "(G1 and AR1) and (D1 or CY1)");
        assertResultContains(result, true, "(G1 and AR1) or (D1 or CY1)");
        assertResultContains(result, false, "(G1 or AR1) and (D1 and CY1)");
        assertResultContains(result, false, "(G1 or AR1) or (D1 and CY1)");

        // all not match
        input = new BEInput();
        input.getOrCreateStringCategory("gender").add("F"); // no match
        input.getOrCreateStringCategory("age").add("40"); // no match
        input.getOrCreateStringCategory("domain").add("baddomain.com"); // no match
        input.getOrCreateStringCategory("country").add("DE"); // no match

        result = evaluator.evaluate(input);
        assertEquals(0, result.size());
        assertResultContains(result, false, "G1");
        assertResultContains(result, false, "AR1");
        assertResultContains(result, false, "D1");
        assertResultContains(result, false, "CY1");
        assertResultContains(result, false, "G1 and AR1");
        assertResultContains(result, false, "D1 and CY1");
        assertResultContains(result, false, "G1 or AR1");
        assertResultContains(result, false, "D1 or CY1");
        assertResultContains(result, false, "(G1 and AR1) and (D1 and CY1)");
        assertResultContains(result, false, "(G1 and AR1) or (D1 and CY1)");
        assertResultContains(result, false, "(G1 or AR1) and (D1 or CY1)");
        assertResultContains(result, false, "(G1 or AR1) or (D1 or CY1)");
        assertResultContains(result, false, "(G1 and AR1) and (D1 or CY1)");
        assertResultContains(result, false, "(G1 and AR1) or (D1 or CY1)");
        assertResultContains(result, false, "(G1 or AR1) and (D1 and CY1)");
        assertResultContains(result, false, "(G1 or AR1) or (D1 and CY1)");
    }

    @Test
    public void test2DeepOrOfAnds() throws Exception
    {
        final BEEvaluatorBuilder<String> builder = new BEEvaluatorBuilder<>();
        builder.addDataTypeConfig(DATA_TYPE_CONFIG);

        builder.addExpression(
                "(GA and GB) or GC or (GD and GE and GF)",
                exprConj("OR",
                        exprConj("AND", GA, GB),
                        GC,
                        exprConj("AND", GD, GE, GF)));

        final BEEvaluator<String> evaluator = builder.build();

        BEInput input;
        Set<String> result;

        // match due to first term
        input = new BEInput();
        input.getOrCreateStringCategory("gender").add("a");
        input.getOrCreateStringCategory("gender").add("b");

        result = evaluator.evaluate(input);
        assertEquals(1, result.size());
        assertResultContains(result, true, "(GA and GB) or GC or (GD and GE and GF)");

        // no match due to first term
        input = new BEInput();
        input.getOrCreateStringCategory("gender").add("a");

        result = evaluator.evaluate(input);
        assertEquals(0, result.size());

        // match due to second term
        input = new BEInput();
        input.getOrCreateStringCategory("gender").add("c");

        result = evaluator.evaluate(input);
        assertEquals(1, result.size());
        assertResultContains(result, true, "(GA and GB) or GC or (GD and GE and GF)");

        // match due to third term
        input = new BEInput();
        input.getOrCreateStringCategory("gender").add("d");
        input.getOrCreateStringCategory("gender").add("e");
        input.getOrCreateStringCategory("gender").add("f");

        result = evaluator.evaluate(input);
        assertEquals(1, result.size());
        assertResultContains(result, true, "(GA and GB) or GC or (GD and GE and GF)");

        // no match due to third term
        input = new BEInput();
        input.getOrCreateStringCategory("gender").add("e");
        input.getOrCreateStringCategory("gender").add("f");

        result = evaluator.evaluate(input);
        assertEquals(0, result.size());

        // no match due to third term
        input = new BEInput();
        input.getOrCreateStringCategory("gender").add("d");
        input.getOrCreateStringCategory("gender").add("f");

        result = evaluator.evaluate(input);
        assertEquals(0, result.size());

        // no match due to third term
        input = new BEInput();
        input.getOrCreateStringCategory("gender").add("d");
        input.getOrCreateStringCategory("gender").add("e");

        result = evaluator.evaluate(input);
        assertEquals(0, result.size());

        // no match due to third term
        input = new BEInput();
        input.getOrCreateStringCategory("gender").add("d");

        result = evaluator.evaluate(input);
        assertEquals(0, result.size());

        // no match due to third term
        input = new BEInput();
        input.getOrCreateStringCategory("gender").add("e");

        result = evaluator.evaluate(input);
        assertEquals(0, result.size());

        // no match due to third term
        input = new BEInput();
        input.getOrCreateStringCategory("gender").add("f");

        result = evaluator.evaluate(input);
        assertEquals(0, result.size());
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
    public void test3DeepMixedAndsOrs() throws Exception
    {
        final BEEvaluatorBuilder<String> builder = new BEEvaluatorBuilder<>();
        builder.addDataTypeConfig(DATA_TYPE_CONFIG);

        builder.addExpression("(((GA and GB) or (GC or GD) and ((GE or GF) and (GG and GH))",
                exprConj("AND", exprConj("OR", exprConj("AND", GA, GB), exprConj("OR", GC, GD)),
                        exprConj("AND", exprConj("OR", GE, GF), exprConj("AND", GG, GH))));

        final BEEvaluator<String> evaluator = builder.build();

        BEInput input;
        Set<String> result;

        // match a, b, e, g, h
        input = new BEInput();
        input.getOrCreateStringCategory("gender").add("a");
        input.getOrCreateStringCategory("gender").add("b");
        input.getOrCreateStringCategory("gender").add("e");
        input.getOrCreateStringCategory("gender").add("g");
        input.getOrCreateStringCategory("gender").add("h");

        result = evaluator.evaluate(input);
        assertEquals(1, result.size());

        // no match a, b, e, g, h - a
        input = new BEInput();
        input.getOrCreateStringCategory("gender").add("b");
        input.getOrCreateStringCategory("gender").add("e");
        input.getOrCreateStringCategory("gender").add("g");
        input.getOrCreateStringCategory("gender").add("h");

        result = evaluator.evaluate(input);
        assertEquals(0, result.size());

        // no match a, b, e, g, h - b
        input = new BEInput();
        input.getOrCreateStringCategory("gender").add("a");
        input.getOrCreateStringCategory("gender").add("e");
        input.getOrCreateStringCategory("gender").add("g");
        input.getOrCreateStringCategory("gender").add("h");

        result = evaluator.evaluate(input);
        assertEquals(0, result.size());

        // no match a, b, e, g, h - e
        input = new BEInput();
        input.getOrCreateStringCategory("gender").add("a");
        input.getOrCreateStringCategory("gender").add("b");
        input.getOrCreateStringCategory("gender").add("g");
        input.getOrCreateStringCategory("gender").add("h");

        result = evaluator.evaluate(input);
        assertEquals(0, result.size());

        // no match a, b, e, g, h - g
        input = new BEInput();
        input.getOrCreateStringCategory("gender").add("a");
        input.getOrCreateStringCategory("gender").add("b");
        input.getOrCreateStringCategory("gender").add("e");
        input.getOrCreateStringCategory("gender").add("h");

        result = evaluator.evaluate(input);
        assertEquals(0, result.size());

        // no match a, b, e, g, h - h
        input = new BEInput();
        input.getOrCreateStringCategory("gender").add("a");
        input.getOrCreateStringCategory("gender").add("b");
        input.getOrCreateStringCategory("gender").add("e");
        input.getOrCreateStringCategory("gender").add("g");

        result = evaluator.evaluate(input);
        assertEquals(0, result.size());

        result = evaluator.evaluate(input);
        assertEquals(0, result.size());

        // match c, e, g, h
        input = new BEInput();
        input.getOrCreateStringCategory("gender").add("c");
        input.getOrCreateStringCategory("gender").add("e");
        input.getOrCreateStringCategory("gender").add("g");
        input.getOrCreateStringCategory("gender").add("h");

        result = evaluator.evaluate(input);
        assertEquals(1, result.size());

        // match d, f, g, h
        input = new BEInput();
        input.getOrCreateStringCategory("gender").add("d");
        input.getOrCreateStringCategory("gender").add("f");
        input.getOrCreateStringCategory("gender").add("g");
        input.getOrCreateStringCategory("gender").add("h");

        result = evaluator.evaluate(input);
        assertEquals(1, result.size());
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
     */
    @Test
    public void test3DeepMixedAndsOrs2() throws Exception
    {
        final BEEvaluatorBuilder<String> builder = new BEEvaluatorBuilder<>();
        builder.addDataTypeConfig(DATA_TYPE_CONFIG);

        builder.addExpression("3 levels",
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
                                exprConj("AND", GP, GQ, GR))));

        final BEEvaluator<String> evaluator = builder.build();

        BEInput input;
        Set<String> result;

        // match a f g h i l n o p q r
        input = new BEInput();
        input.getOrCreateStringCategory("gender").add("a");
        input.getOrCreateStringCategory("gender").add("f");
        input.getOrCreateStringCategory("gender").add("g");
        input.getOrCreateStringCategory("gender").add("h");
        input.getOrCreateStringCategory("gender").add("i");
        input.getOrCreateStringCategory("gender").add("l");
        input.getOrCreateStringCategory("gender").add("n");
        input.getOrCreateStringCategory("gender").add("o");
        input.getOrCreateStringCategory("gender").add("p");
        input.getOrCreateStringCategory("gender").add("q");
        input.getOrCreateStringCategory("gender").add("r");

        result = evaluator.evaluate(input);
        assertEquals(1, result.size());

        // match a f g h i l n o p q r - a
        input = new BEInput();
        //        input.getOrCreateStringCategory("gender").add("a");
        input.getOrCreateStringCategory("gender").add("f");
        input.getOrCreateStringCategory("gender").add("g");
        input.getOrCreateStringCategory("gender").add("h");
        input.getOrCreateStringCategory("gender").add("i");
        input.getOrCreateStringCategory("gender").add("l");
        input.getOrCreateStringCategory("gender").add("n");
        input.getOrCreateStringCategory("gender").add("o");
        input.getOrCreateStringCategory("gender").add("p");
        input.getOrCreateStringCategory("gender").add("q");
        input.getOrCreateStringCategory("gender").add("r");

        result = evaluator.evaluate(input);
        assertEquals(0, result.size());

        // match a f g h i l n o p q r - f
        input = new BEInput();
        input.getOrCreateStringCategory("gender").add("a");
        //        input.getOrCreateStringCategory("gender").add("f");
        input.getOrCreateStringCategory("gender").add("g");
        input.getOrCreateStringCategory("gender").add("h");
        input.getOrCreateStringCategory("gender").add("i");
        input.getOrCreateStringCategory("gender").add("l");
        input.getOrCreateStringCategory("gender").add("n");
        input.getOrCreateStringCategory("gender").add("o");
        input.getOrCreateStringCategory("gender").add("p");
        input.getOrCreateStringCategory("gender").add("q");
        input.getOrCreateStringCategory("gender").add("r");

        result = evaluator.evaluate(input);
        assertEquals(0, result.size());

        // match a f g h i l n o p q r - g
        input = new BEInput();
        input.getOrCreateStringCategory("gender").add("a");
        input.getOrCreateStringCategory("gender").add("f");
        //        input.getOrCreateStringCategory("gender").add("g");
        input.getOrCreateStringCategory("gender").add("h");
        input.getOrCreateStringCategory("gender").add("i");
        input.getOrCreateStringCategory("gender").add("l");
        input.getOrCreateStringCategory("gender").add("n");
        input.getOrCreateStringCategory("gender").add("o");
        input.getOrCreateStringCategory("gender").add("p");
        input.getOrCreateStringCategory("gender").add("q");
        input.getOrCreateStringCategory("gender").add("r");

        result = evaluator.evaluate(input);
        assertEquals(0, result.size());

        // match a f g h i l n o p q r - h
        input = new BEInput();
        input.getOrCreateStringCategory("gender").add("a");
        input.getOrCreateStringCategory("gender").add("f");
        input.getOrCreateStringCategory("gender").add("g");
        //        input.getOrCreateStringCategory("gender").add("h");
        input.getOrCreateStringCategory("gender").add("i");
        input.getOrCreateStringCategory("gender").add("l");
        input.getOrCreateStringCategory("gender").add("n");
        input.getOrCreateStringCategory("gender").add("o");
        input.getOrCreateStringCategory("gender").add("p");
        input.getOrCreateStringCategory("gender").add("q");
        input.getOrCreateStringCategory("gender").add("r");

        result = evaluator.evaluate(input);
        assertEquals(0, result.size());

        // match a f g h i l n o p q r - i
        input = new BEInput();
        input.getOrCreateStringCategory("gender").add("a");
        input.getOrCreateStringCategory("gender").add("f");
        input.getOrCreateStringCategory("gender").add("g");
        input.getOrCreateStringCategory("gender").add("h");
        //        input.getOrCreateStringCategory("gender").add("i");
        input.getOrCreateStringCategory("gender").add("l");
        input.getOrCreateStringCategory("gender").add("n");
        input.getOrCreateStringCategory("gender").add("o");
        input.getOrCreateStringCategory("gender").add("p");
        input.getOrCreateStringCategory("gender").add("q");
        input.getOrCreateStringCategory("gender").add("r");

        result = evaluator.evaluate(input);
        assertEquals(0, result.size());

        // match a f g h i l n o p q r - l
        input = new BEInput();
        input.getOrCreateStringCategory("gender").add("a");
        input.getOrCreateStringCategory("gender").add("f");
        input.getOrCreateStringCategory("gender").add("g");
        input.getOrCreateStringCategory("gender").add("h");
        input.getOrCreateStringCategory("gender").add("i");
        //        input.getOrCreateStringCategory("gender").add("l");
        input.getOrCreateStringCategory("gender").add("n");
        input.getOrCreateStringCategory("gender").add("o");
        input.getOrCreateStringCategory("gender").add("p");
        input.getOrCreateStringCategory("gender").add("q");
        input.getOrCreateStringCategory("gender").add("r");

        result = evaluator.evaluate(input);
        assertEquals(0, result.size());

        // match a f g h i l n o p q r - n
        input = new BEInput();
        input.getOrCreateStringCategory("gender").add("a");
        input.getOrCreateStringCategory("gender").add("f");
        input.getOrCreateStringCategory("gender").add("g");
        input.getOrCreateStringCategory("gender").add("h");
        input.getOrCreateStringCategory("gender").add("i");
        input.getOrCreateStringCategory("gender").add("l");
        //        input.getOrCreateStringCategory("gender").add("n");
        input.getOrCreateStringCategory("gender").add("o");
        input.getOrCreateStringCategory("gender").add("p");
        input.getOrCreateStringCategory("gender").add("q");
        input.getOrCreateStringCategory("gender").add("r");

        result = evaluator.evaluate(input);
        assertEquals(0, result.size());

        // match a f g h i l n o p q r - o
        input = new BEInput();
        input.getOrCreateStringCategory("gender").add("a");
        input.getOrCreateStringCategory("gender").add("f");
        input.getOrCreateStringCategory("gender").add("g");
        input.getOrCreateStringCategory("gender").add("h");
        input.getOrCreateStringCategory("gender").add("i");
        input.getOrCreateStringCategory("gender").add("l");
        input.getOrCreateStringCategory("gender").add("n");
        //        input.getOrCreateStringCategory("gender").add("o");
        input.getOrCreateStringCategory("gender").add("p");
        input.getOrCreateStringCategory("gender").add("q");
        input.getOrCreateStringCategory("gender").add("r");

        result = evaluator.evaluate(input);
        assertEquals(0, result.size());

        // match a f g h i l n o p q r - p
        input = new BEInput();
        input.getOrCreateStringCategory("gender").add("a");
        input.getOrCreateStringCategory("gender").add("f");
        input.getOrCreateStringCategory("gender").add("g");
        input.getOrCreateStringCategory("gender").add("h");
        input.getOrCreateStringCategory("gender").add("i");
        input.getOrCreateStringCategory("gender").add("l");
        input.getOrCreateStringCategory("gender").add("n");
        input.getOrCreateStringCategory("gender").add("o");
        //        input.getOrCreateStringCategory("gender").add("p");
        input.getOrCreateStringCategory("gender").add("q");
        input.getOrCreateStringCategory("gender").add("r");

        result = evaluator.evaluate(input);
        assertEquals(0, result.size());

        // match a f g h i l n o p q r - q
        input = new BEInput();
        input.getOrCreateStringCategory("gender").add("a");
        input.getOrCreateStringCategory("gender").add("f");
        input.getOrCreateStringCategory("gender").add("g");
        input.getOrCreateStringCategory("gender").add("h");
        input.getOrCreateStringCategory("gender").add("i");
        input.getOrCreateStringCategory("gender").add("l");
        input.getOrCreateStringCategory("gender").add("n");
        input.getOrCreateStringCategory("gender").add("o");
        input.getOrCreateStringCategory("gender").add("p");
        //        input.getOrCreateStringCategory("gender").add("q");
        input.getOrCreateStringCategory("gender").add("r");

        result = evaluator.evaluate(input);
        assertEquals(0, result.size());

        // match a f g h i l n o p q r - r
        input = new BEInput();
        input.getOrCreateStringCategory("gender").add("a");
        input.getOrCreateStringCategory("gender").add("f");
        input.getOrCreateStringCategory("gender").add("g");
        input.getOrCreateStringCategory("gender").add("h");
        input.getOrCreateStringCategory("gender").add("i");
        input.getOrCreateStringCategory("gender").add("l");
        input.getOrCreateStringCategory("gender").add("n");
        input.getOrCreateStringCategory("gender").add("o");
        input.getOrCreateStringCategory("gender").add("p");
        input.getOrCreateStringCategory("gender").add("q");
        //        input.getOrCreateStringCategory("gender").add("r");

        result = evaluator.evaluate(input);
        assertEquals(0, result.size());

        // match b f g h i l n o p q r
        input = new BEInput();
        input.getOrCreateStringCategory("gender").add("b");
        input.getOrCreateStringCategory("gender").add("f");
        input.getOrCreateStringCategory("gender").add("g");
        input.getOrCreateStringCategory("gender").add("h");
        input.getOrCreateStringCategory("gender").add("i");
        input.getOrCreateStringCategory("gender").add("l");
        input.getOrCreateStringCategory("gender").add("n");
        input.getOrCreateStringCategory("gender").add("o");
        input.getOrCreateStringCategory("gender").add("p");
        input.getOrCreateStringCategory("gender").add("q");
        input.getOrCreateStringCategory("gender").add("r");

        result = evaluator.evaluate(input);
        assertEquals(1, result.size());

        // match c d e f g h i l n o p q r
        input = new BEInput();
        input.getOrCreateStringCategory("gender").add("c");
        input.getOrCreateStringCategory("gender").add("d");
        input.getOrCreateStringCategory("gender").add("e");
        input.getOrCreateStringCategory("gender").add("f");
        input.getOrCreateStringCategory("gender").add("g");
        input.getOrCreateStringCategory("gender").add("h");
        input.getOrCreateStringCategory("gender").add("i");
        input.getOrCreateStringCategory("gender").add("l");
        input.getOrCreateStringCategory("gender").add("n");
        input.getOrCreateStringCategory("gender").add("o");
        input.getOrCreateStringCategory("gender").add("p");
        input.getOrCreateStringCategory("gender").add("q");
        input.getOrCreateStringCategory("gender").add("r");

        result = evaluator.evaluate(input);
        assertEquals(1, result.size());

        // match c d e j k l n o p q r
        input = new BEInput();
        input.getOrCreateStringCategory("gender").add("c");
        input.getOrCreateStringCategory("gender").add("d");
        input.getOrCreateStringCategory("gender").add("e");
        input.getOrCreateStringCategory("gender").add("j");
        input.getOrCreateStringCategory("gender").add("k");
        input.getOrCreateStringCategory("gender").add("l");
        input.getOrCreateStringCategory("gender").add("n");
        input.getOrCreateStringCategory("gender").add("o");
        input.getOrCreateStringCategory("gender").add("p");
        input.getOrCreateStringCategory("gender").add("q");
        input.getOrCreateStringCategory("gender").add("r");

        result = evaluator.evaluate(input);
        assertEquals(1, result.size());

        // match c d e j k m n o p q r
        input = new BEInput();
        input.getOrCreateStringCategory("gender").add("c");
        input.getOrCreateStringCategory("gender").add("d");
        input.getOrCreateStringCategory("gender").add("e");
        input.getOrCreateStringCategory("gender").add("j");
        input.getOrCreateStringCategory("gender").add("k");
        input.getOrCreateStringCategory("gender").add("m");
        input.getOrCreateStringCategory("gender").add("n");
        input.getOrCreateStringCategory("gender").add("o");
        input.getOrCreateStringCategory("gender").add("p");
        input.getOrCreateStringCategory("gender").add("q");
        input.getOrCreateStringCategory("gender").add("r");

        result = evaluator.evaluate(input);
        assertEquals(1, result.size());

        // match c d e j k m n o p q r
        input = new BEInput();
        input.getOrCreateStringCategory("gender").add("a");
        input.getOrCreateStringCategory("gender").add("i");
        input.getOrCreateStringCategory("gender").add("j");
        input.getOrCreateStringCategory("gender").add("l");
        input.getOrCreateStringCategory("gender").add("n");
        input.getOrCreateStringCategory("gender").add("o");
        input.getOrCreateStringCategory("gender").add("p");
        input.getOrCreateStringCategory("gender").add("q");
        input.getOrCreateStringCategory("gender").add("r");

        result = evaluator.evaluate(input);
        assertEquals(0, result.size());
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
     */
    @Test
    public void test4DeepMixedAndsOrs() throws Exception
    {
        final BEEvaluatorBuilder<String> builder = new BEEvaluatorBuilder<>();
        builder.addDataTypeConfig(DATA_TYPE_CONFIG);

        builder.addExpression("4 levels",
                exprConj("AND",
                        exprConj("AND",
                                exprConj("AND", GA, GB, GC),
                                        exprConj("AND", GD, GE, GF),
                                        exprConj("OR",
                                                exprConj("OR", GG, GH),
                                                exprConj("AND", GI, GJ, GK))),
                                exprConj("OR", GL, GM),
                                exprConj("OR", GN, GO)));

        final BEEvaluator<String> evaluator = builder.build();

        BEInput input;
        Set<String> result;

        // match a b c d e f g l n
        input = new BEInput();
        input.getOrCreateStringCategory("gender").add("a");
        input.getOrCreateStringCategory("gender").add("b");
        input.getOrCreateStringCategory("gender").add("c");
        input.getOrCreateStringCategory("gender").add("d");
        input.getOrCreateStringCategory("gender").add("e");
        input.getOrCreateStringCategory("gender").add("f");
        input.getOrCreateStringCategory("gender").add("g");
        input.getOrCreateStringCategory("gender").add("l");
        input.getOrCreateStringCategory("gender").add("n");

        result = evaluator.evaluate(input);
        assertEquals(1, result.size());

        // match a b c d e f g l n - a
        input = new BEInput();
        //        input.getOrCreateStringCategory("gender").add("a");
        input.getOrCreateStringCategory("gender").add("b");
        input.getOrCreateStringCategory("gender").add("c");
        input.getOrCreateStringCategory("gender").add("d");
        input.getOrCreateStringCategory("gender").add("e");
        input.getOrCreateStringCategory("gender").add("f");
        input.getOrCreateStringCategory("gender").add("g");
        input.getOrCreateStringCategory("gender").add("l");
        input.getOrCreateStringCategory("gender").add("n");

        result = evaluator.evaluate(input);
        assertEquals(0, result.size());

        // match a b c d e f g l n - b
        input = new BEInput();
        input.getOrCreateStringCategory("gender").add("a");
        //        input.getOrCreateStringCategory("gender").add("b");
        input.getOrCreateStringCategory("gender").add("c");
        input.getOrCreateStringCategory("gender").add("d");
        input.getOrCreateStringCategory("gender").add("e");
        input.getOrCreateStringCategory("gender").add("f");
        input.getOrCreateStringCategory("gender").add("g");
        input.getOrCreateStringCategory("gender").add("l");
        input.getOrCreateStringCategory("gender").add("n");

        result = evaluator.evaluate(input);
        assertEquals(0, result.size());

        // match a b c d e f g l n - c
        input = new BEInput();
        input.getOrCreateStringCategory("gender").add("a");
        input.getOrCreateStringCategory("gender").add("b");
        //        input.getOrCreateStringCategory("gender").add("c");
        input.getOrCreateStringCategory("gender").add("d");
        input.getOrCreateStringCategory("gender").add("e");
        input.getOrCreateStringCategory("gender").add("f");
        input.getOrCreateStringCategory("gender").add("g");
        input.getOrCreateStringCategory("gender").add("l");
        input.getOrCreateStringCategory("gender").add("n");

        result = evaluator.evaluate(input);
        assertEquals(0, result.size());

        // match a b c d e f g l n - d
        input = new BEInput();
        input.getOrCreateStringCategory("gender").add("a");
        input.getOrCreateStringCategory("gender").add("b");
        input.getOrCreateStringCategory("gender").add("c");
        //        input.getOrCreateStringCategory("gender").add("d");
        input.getOrCreateStringCategory("gender").add("e");
        input.getOrCreateStringCategory("gender").add("f");
        input.getOrCreateStringCategory("gender").add("g");
        input.getOrCreateStringCategory("gender").add("l");
        input.getOrCreateStringCategory("gender").add("n");

        result = evaluator.evaluate(input);
        assertEquals(0, result.size());

        // match a b c d e f g l n - e
        input = new BEInput();
        input.getOrCreateStringCategory("gender").add("a");
        input.getOrCreateStringCategory("gender").add("b");
        input.getOrCreateStringCategory("gender").add("c");
        input.getOrCreateStringCategory("gender").add("d");
        //        input.getOrCreateStringCategory("gender").add("e");
        input.getOrCreateStringCategory("gender").add("f");
        input.getOrCreateStringCategory("gender").add("g");
        input.getOrCreateStringCategory("gender").add("l");
        input.getOrCreateStringCategory("gender").add("n");

        result = evaluator.evaluate(input);
        assertEquals(0, result.size());

        // match a b c d e f g l n - f
        input = new BEInput();
        input.getOrCreateStringCategory("gender").add("a");
        input.getOrCreateStringCategory("gender").add("b");
        input.getOrCreateStringCategory("gender").add("c");
        input.getOrCreateStringCategory("gender").add("d");
        input.getOrCreateStringCategory("gender").add("e");
        //        input.getOrCreateStringCategory("gender").add("f");
        input.getOrCreateStringCategory("gender").add("g");
        input.getOrCreateStringCategory("gender").add("l");
        input.getOrCreateStringCategory("gender").add("n");

        result = evaluator.evaluate(input);
        assertEquals(0, result.size());

        // match a b c d e f g l n - g
        input = new BEInput();
        input.getOrCreateStringCategory("gender").add("a");
        input.getOrCreateStringCategory("gender").add("b");
        input.getOrCreateStringCategory("gender").add("c");
        input.getOrCreateStringCategory("gender").add("d");
        input.getOrCreateStringCategory("gender").add("e");
        input.getOrCreateStringCategory("gender").add("f");
        //        input.getOrCreateStringCategory("gender").add("g");
        input.getOrCreateStringCategory("gender").add("l");
        input.getOrCreateStringCategory("gender").add("n");

        result = evaluator.evaluate(input);
        assertEquals(0, result.size());

        // match a b c d e f g l n - l
        input = new BEInput();
        input.getOrCreateStringCategory("gender").add("a");
        input.getOrCreateStringCategory("gender").add("b");
        input.getOrCreateStringCategory("gender").add("c");
        input.getOrCreateStringCategory("gender").add("d");
        input.getOrCreateStringCategory("gender").add("e");
        input.getOrCreateStringCategory("gender").add("f");
        input.getOrCreateStringCategory("gender").add("g");
        //        input.getOrCreateStringCategory("gender").add("l");
        input.getOrCreateStringCategory("gender").add("n");

        result = evaluator.evaluate(input);
        assertEquals(0, result.size());

        // match a b c d e f g l n - n
        input = new BEInput();
        input.getOrCreateStringCategory("gender").add("a");
        input.getOrCreateStringCategory("gender").add("b");
        input.getOrCreateStringCategory("gender").add("c");
        input.getOrCreateStringCategory("gender").add("d");
        input.getOrCreateStringCategory("gender").add("e");
        input.getOrCreateStringCategory("gender").add("f");
        input.getOrCreateStringCategory("gender").add("g");
        input.getOrCreateStringCategory("gender").add("l");
        //        input.getOrCreateStringCategory("gender").add("n");

        result = evaluator.evaluate(input);
        assertEquals(0, result.size());

        // match a b c d e f h l n
        input = new BEInput();
        input.getOrCreateStringCategory("gender").add("a");
        input.getOrCreateStringCategory("gender").add("b");
        input.getOrCreateStringCategory("gender").add("c");
        input.getOrCreateStringCategory("gender").add("d");
        input.getOrCreateStringCategory("gender").add("e");
        input.getOrCreateStringCategory("gender").add("f");
        input.getOrCreateStringCategory("gender").add("h");
        input.getOrCreateStringCategory("gender").add("l");
        input.getOrCreateStringCategory("gender").add("n");

        result = evaluator.evaluate(input);
        assertEquals(1, result.size());

        // match a b c d e f i j k l n
        input = new BEInput();
        input.getOrCreateStringCategory("gender").add("a");
        input.getOrCreateStringCategory("gender").add("b");
        input.getOrCreateStringCategory("gender").add("c");
        input.getOrCreateStringCategory("gender").add("d");
        input.getOrCreateStringCategory("gender").add("e");
        input.getOrCreateStringCategory("gender").add("f");
        input.getOrCreateStringCategory("gender").add("i");
        input.getOrCreateStringCategory("gender").add("j");
        input.getOrCreateStringCategory("gender").add("k");
        input.getOrCreateStringCategory("gender").add("l");
        input.getOrCreateStringCategory("gender").add("n");

        result = evaluator.evaluate(input);
        assertEquals(1, result.size());

        // match a b c d e f i j k m n
        input = new BEInput();
        input.getOrCreateStringCategory("gender").add("a");
        input.getOrCreateStringCategory("gender").add("b");
        input.getOrCreateStringCategory("gender").add("c");
        input.getOrCreateStringCategory("gender").add("d");
        input.getOrCreateStringCategory("gender").add("e");
        input.getOrCreateStringCategory("gender").add("f");
        input.getOrCreateStringCategory("gender").add("i");
        input.getOrCreateStringCategory("gender").add("j");
        input.getOrCreateStringCategory("gender").add("k");
        input.getOrCreateStringCategory("gender").add("m");
        input.getOrCreateStringCategory("gender").add("n");

        result = evaluator.evaluate(input);
        assertEquals(1, result.size());

        // match a b c d e f i j k m n
        input = new BEInput();
        input.getOrCreateStringCategory("gender").add("a");
        input.getOrCreateStringCategory("gender").add("b");
        input.getOrCreateStringCategory("gender").add("c");
        input.getOrCreateStringCategory("gender").add("d");
        input.getOrCreateStringCategory("gender").add("e");
        input.getOrCreateStringCategory("gender").add("f");
        input.getOrCreateStringCategory("gender").add("i");
        input.getOrCreateStringCategory("gender").add("j");
        input.getOrCreateStringCategory("gender").add("k");
        input.getOrCreateStringCategory("gender").add("m");
        input.getOrCreateStringCategory("gender").add("o");

        result = evaluator.evaluate(input);
        assertEquals(1, result.size());
    }

    @Test
    public void testSerialization() throws Exception
    {
        final File tempSerializationFile = File.createTempFile("TestEvaluator", "testSerialization.bin");

        final BEEvaluatorBuilder<String> builder = new BEEvaluatorBuilder<>();
        builder.addDataTypeConfig(DATA_TYPE_CONFIG);

        builder.addExpression("AR2", AR2);
        builder.addExpression("D2", D2);
        builder.addExpression("AR2 and D2", exprConj("and", AR2, D2));
        builder.addExpression("AR2 or D2", exprConj("or", AR2, D2));
        final BEEvaluator<String> evaluatorToSerialize = builder.build();

        // Serialize and then deserialize the evaluator, then test it for correctness
        final FileOutputStream fileOutputStream = new FileOutputStream(tempSerializationFile);
        final ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
        objectOutputStream.writeObject(evaluatorToSerialize);
        objectOutputStream.flush();
        objectOutputStream.close();

        final FileInputStream fileInputStream = new FileInputStream(tempSerializationFile);
        final ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
        final BEEvaluator<String> deserializedEvaluator = (BEEvaluator<String>) objectInputStream.readObject();
        objectInputStream.close();

        BEInput input;
        Set<String> result;

        // single values in record
        input = new BEInput();
        input.getOrCreateStringCategory("age").add("20"); // match
        input.getOrCreateStringCategory("domain").add("gooddomain.com"); // match

        result = deserializedEvaluator.evaluate(input);
        assertEquals(4, result.size());
        assertTrue("AR2", result.contains("AR2"));
        assertTrue("D2", result.contains("D2"));
        assertTrue("AR2 and D2", result.contains("AR2 and D2"));
        assertTrue("AR2 or D2", result.contains("AR2 or D2"));

        input = new BEInput();
        input.getOrCreateStringCategory("age").add("20"); // match
        input.getOrCreateStringCategory("domain").add("baddomain.com"); // no match

        result = deserializedEvaluator.evaluate(input);
        assertEquals(2, result.size());
        assertTrue("AR2", result.contains("AR2"));
        assertFalse("D2", result.contains("D2"));
        assertFalse("AR2 and D2", result.contains("AR2 and D2"));
        assertTrue("AR2 or D2", result.contains("AR2 or D2"));

        input = new BEInput();
        input.getOrCreateStringCategory("age").add("50"); // no match
        input.getOrCreateStringCategory("domain").add("baddomain.com"); // no match

        result = deserializedEvaluator.evaluate(input);
        assertEquals(0, result.size());
        assertFalse("AR2", result.contains("AR2"));
        assertFalse("D2", result.contains("D2"));
        assertFalse("AR2 and D2", result.contains("AR2 and D2"));
        assertFalse("AR2 or D2", result.contains("AR2 or D2"));

        // multiple values in record
        input = new BEInput();
        input.getOrCreateStringCategory("age").add("20"); // match
        input.getOrCreateStringCategory("age").add("52"); // match
        input.getOrCreateStringCategory("domain").add("gooddomain.com"); // match
        input.getOrCreateStringCategory("domain").add("gooddomain2.com"); // match

        result = deserializedEvaluator.evaluate(input);
        assertEquals(4, result.size());
        assertTrue("AR2", result.contains("AR2"));
        assertTrue("D2", result.contains("D2"));
        assertTrue("AR2 and D2", result.contains("AR2 and D2"));
        assertTrue("AR2 or D2", result.contains("AR2 or D2"));

        input = new BEInput();
        input.getOrCreateStringCategory("age").add("20"); // match
        input.getOrCreateStringCategory("age").add("45"); // no match
        input.getOrCreateStringCategory("domain").add("gooddomain.com"); // match
        input.getOrCreateStringCategory("domain").add("baddomain.com"); // no match

        result = deserializedEvaluator.evaluate(input);
        assertEquals(4, result.size());
        assertTrue("AR2", result.contains("AR2"));
        assertTrue("D2", result.contains("D2"));
        assertTrue("AR2 and D2", result.contains("AR2 and D2"));
        assertTrue("AR2 or D2", result.contains("AR2 or D2"));

        input = new BEInput();
        input.getOrCreateStringCategory("age").add("20"); // match
        input.getOrCreateStringCategory("age").add("52"); // match
        input.getOrCreateStringCategory("domain").add("baddomain.com"); // no match
        input.getOrCreateStringCategory("domain").add("baddomain2.com"); // no match

        result = deserializedEvaluator.evaluate(input);
        assertEquals(2, result.size());
        assertTrue("AR2", result.contains("AR2"));
        assertFalse("D2", result.contains("D2"));
        assertFalse("AR2 and D2", result.contains("AR2 and D2"));
        assertTrue("AR2 or D2", result.contains("AR2 or D2"));

        input = new BEInput();
        input.getOrCreateStringCategory("age").add("20"); // match
        input.getOrCreateStringCategory("age").add("40"); // no match
        input.getOrCreateStringCategory("domain").add("baddomain.com"); // no match
        input.getOrCreateStringCategory("domain").add("baddomain2.com"); // no match

        result = deserializedEvaluator.evaluate(input);
        assertEquals(2, result.size());
        assertTrue("AR2", result.contains("AR2"));
        assertFalse("D2", result.contains("D2"));
        assertFalse("AR2 and D2", result.contains("AR2 and D2"));
        assertTrue("AR2 or D2", result.contains("AR2 or D2"));

        input = new BEInput();
        input.getOrCreateStringCategory("age").add("10"); // no match
        input.getOrCreateStringCategory("age").add("40"); // no match
        input.getOrCreateStringCategory("domain").add("baddomain.com"); // no match
        input.getOrCreateStringCategory("domain").add("baddomain2.com"); // no match

        result = deserializedEvaluator.evaluate(input);
        assertEquals(0, result.size());
        assertFalse("AR2", result.contains("AR2"));
        assertFalse("D2", result.contains("D2"));
        assertFalse("AR2 and D2", result.contains("AR2 and D2"));
        assertFalse("AR2 or D2", result.contains("AR2 or D2"));

    }

    @Test
    public void testSingleExpression_MatchNegativeSet() throws Exception
    {
        final BEEvaluatorBuilder<String> builder = new BEEvaluatorBuilder<>();
        builder.addDataTypeConfig(DATA_TYPE_CONFIG);
        builder.addExpression("test1", "{\"type\":\"and\",\"values\":[{\"type\":\"or\",\"values\":[{\"type\":\"gender\",\"values\":[{\"id\":\"M\"},{\"id\":\"F\"}]},{\"type\":\"age\",\"values\":[{\"id\":\"[18,34)\"}]}]},{\"type\":\"country\",\"values\":[{\"id\":\"US\"}]},{\"type\":\"domain\",\"negative\":true,\"values\":[{\"id\":\".baddomain.com\"}]}]}");

        final BEEvaluator<String> evaluator = builder.build();

        final BEInput input = new BEInput();
        input.getOrCreateStringCategory("age").add("[18,24)");
        input.getOrCreateStringCategory("gender").add("M");
        input.getOrCreateStringCategory("country").add("US");
        input.getOrCreateStringCategory("domain").add(".bar.com");
        input.getOrCreateIntCategory("dma").add(1234);

        final Set<String> result = evaluator.evaluate(input);
        assertEquals(1, result.size());
        assertTrue("test1", result.contains("test1"));
    }

    @Test
    public void testSingleExpression_MatchNonCDF() throws Exception
    {
        final BEEvaluatorBuilder<String> builder = new BEEvaluatorBuilder<>();
        builder.addDataTypeConfig(DATA_TYPE_CONFIG);

        builder.addExpression("test1_ga", "{\"type\":\"and\",\"values\":[{\"type\":\"gender\",\"values\":[{\"id\":\"M\"},{\"id\":\"F\"}]},{\"type\":\"age\",\"values\":[{\"id\":\"[18,34)\"}]}]}");
        builder.addExpression("test1_country", "{\"type\":\"country\",\"values\":[{\"id\":\"US\"}]}");
        //        builder.addExpression("test1_domain", "{\"type\":\"domain\",\"negative\":true,\"values\":[{\"id\":\"baddomain.com\"}]}");
        // todo: fix problem with bare domain predicate node
        // todo: are there problems with other bare predicate nodes?
        builder.addExpression("test1_domain2", "{\"type\":\"or\",\"values\":[{\"type\":\"domain\",\"negative\":true,\"values\":[{\"id\":\"baddomain.com\"}]}]}");
        builder.addExpression("test1", "{\"type\":\"or\",\"values\":[{\"type\":\"and\",\"values\":[{\"type\":\"gender\",\"values\":[{\"id\":\"M\"},{\"id\":\"F\"}]},{\"type\":\"age\",\"values\":[{\"id\":\"[18,34)\"}]}]},{\"type\":\"country\",\"values\":[{\"id\":\"US\"}]},{\"type\":\"domain\",\"negative\":true,\"values\":[{\"id\":\"baddomain.com\"}]}]}");

        final BEEvaluator<String> evaluator = builder.build();

        BEInput input;
        Set<String> result;

        // match based on age and gender only
        input = new BEInput();
        input.getOrCreateStringCategory("domain").add("baddomain.com"); // no match
        input.getOrCreateStringCategory("age").add("[18,24)"); // age + gender = match
        input.getOrCreateStringCategory("gender").add("M");
        input.getOrCreateStringCategory("country").add("DE"); // no match
        result = evaluator.evaluate(input);
        assertEquals(2, result.size());
        assertTrue("test1_ga", result.contains("test1_ga"));
        assertFalse("test1_country", result.contains("test1_country"));
        //        assertFalse("test1_domain", result.contains("test1_domain"));
        assertFalse("test1_domain2", result.contains("test1_domain2"));
        assertTrue("test1", result.contains("test1"));

        // match with single age
        input = new BEInput();
        input.getOrCreateStringCategory("domain").add("baddomain.com"); // no match
        input.getOrCreateStringCategory("age").add("33"); // age + gender = match
        input.getOrCreateStringCategory("gender").add("M");
        input.getOrCreateStringCategory("country").add("DE"); // no match
        result = evaluator.evaluate(input);
        assertEquals(2, result.size());
        assertTrue("test1_ga", result.contains("test1_ga"));
        assertFalse("test1_country", result.contains("test1_country"));
        //        assertFalse("test1_domain", result.contains("test1_domain"));
        assertFalse("test1_domain2", result.contains("test1_domain2"));
        assertTrue("test1", result.contains("test1"));

        // break match with single age
        input = new BEInput();
        input.getOrCreateStringCategory("domain").add("baddomain.com"); // no match
        input.getOrCreateStringCategory("age").add("34"); // age + gender = no match
        input.getOrCreateStringCategory("gender").add("M");
        input.getOrCreateStringCategory("country").add("DE"); // no match
        result = evaluator.evaluate(input);
        assertEquals(0, result.size());
        assertFalse("test1_ga", result.contains("test1_ga"));
        assertFalse("test1_country", result.contains("test1_country"));
        //        assertFalse("test1_domain", result.contains("test1_domain"));
        assertFalse("test1_domain2", result.contains("test1_domain2"));
        assertFalse("test1", result.contains("test1"));


        // re-introduce match by changing domain
        input = new BEInput();
        input.getOrCreateStringCategory("domain").add("gooddomain.com"); // match
        input.getOrCreateStringCategory("age").add("34"); // age + gender = no match
        input.getOrCreateStringCategory("gender").add("M");
        input.getOrCreateStringCategory("country").add("DE"); // no match
        result = evaluator.evaluate(input);
        assertEquals(2, result.size());
        assertFalse("test1_ga", result.contains("test1_ga"));
        assertFalse("test1_country", result.contains("test1_country"));
        //        assertFalse("test1_domain", result.contains("test1_domain"));
        assertTrue("test1_domain2", result.contains("test1_domain2"));
        assertTrue("test1", result.contains("test1"));

        // make all 3 parts match
        input = new BEInput();
        input.getOrCreateStringCategory("domain").add("gooddomain.com"); // match
        input.getOrCreateStringCategory("age").add("33"); // age + gender = match
        input.getOrCreateStringCategory("gender").add("M");
        input.getOrCreateStringCategory("country").add("US"); // match
        result = evaluator.evaluate(input);
        assertEquals(4, result.size());
        assertTrue("test1_ga", result.contains("test1_ga"));
        assertTrue("test1_country", result.contains("test1_country"));
        //        assertFalse("test1_domain", result.contains("test1_domain"));
        assertTrue("test1_domain2", result.contains("test1_domain2"));
        assertTrue("test1", result.contains("test1"));

        // only country match
        input = new BEInput();
        input.getOrCreateStringCategory("domain").add("baddomain.com"); // no match
        input.getOrCreateStringCategory("age").add("34"); // age + gender = no match
        input.getOrCreateStringCategory("gender").add("M");
        input.getOrCreateStringCategory("country").add("US"); // match
        result = evaluator.evaluate(input);
        assertEquals(2, result.size());
        assertFalse("test1_ga", result.contains("test1_ga"));
        assertTrue("test1_country", result.contains("test1_country"));
        //        assertFalse("test1_domain", result.contains("test1_domain"));
        assertFalse("test1_domain2", result.contains("test1_domain2"));
        assertTrue("test1", result.contains("test1"));

    }

    @Test
    public void testCaseInsensitiveBuilder() throws Exception
    {

        final BEEvaluatorBuilder<String> builder = new BEEvaluatorBuilder<>();
        builder.addDataTypeConfig("[{\"type\":\"gender\",\"ignorecase\":false}]");
        builder.caseInsensitive(true);

        final String G1 = "{\"type\":\"gender\",\"values\":[{\"id\":\"M\"}]}";
        final String C1 = "{\"type\":\"country\",\"values\":[{\"id\":\"us\"}]}";

        builder.addExpression("G1", G1);
        builder.addExpression("C1", C1);
        final BEEvaluator<String> evaluator = builder.build();

        BEInput input;
        Set<String> result;

        input = new BEInput();
        input.getOrCreateStringCategory("gender").add("M"); // match
        result = evaluator.evaluate(input);
        assertEquals(1, result.size());
        assertTrue(result.contains("G1"));

        input = new BEInput();
        input.getOrCreateStringCategory("gender").add("m"); // no match
        result = evaluator.evaluate(input);
        assertEquals(0, result.size());

        input = new BEInput();
        input.getOrCreateStringCategory("country").add("US"); // match
        result = evaluator.evaluate(input);
        assertEquals(1, result.size());
        assertTrue(result.contains("C1"));

        input = new BEInput();
        input.getOrCreateStringCategory("country").add("uS"); // match
        result = evaluator.evaluate(input);
        assertEquals(1, result.size());
        assertTrue(result.contains("C1"));

    }

    @Test
    public void testCaseInsensitiveCategoryTypes() throws Exception
    {
        final String DATA_TYPE_CONFIG = "[{\"type\":\"GENDER\",\"ignorecase\":true},{\"type\":\"AGE\",\"dataType\":\"byte\",\"range\":true}]";

        final BEEvaluatorBuilder<String> builder = new BEEvaluatorBuilder<>();
        builder.addDataTypeConfig(DATA_TYPE_CONFIG);

        final String G1_UPPER_CASE = "{\"type\":\"GENDER\",\"values\":[{\"id\":\"M\"}]}";
        final String G1_LOWER_CASE = "{\"type\":\"gender\",\"values\":[{\"id\":\"M\"}]}";
        final String AR1_UPPER_CASE = "{\"type\":\"AGE\",\"values\":[{\"id\":\"[18,34)\"}]}";
        final String AR1_LOWER_CASE = "{\"type\":\"age\",\"values\":[{\"id\":\"[18,34)\"}]}";

        builder.addExpression("G1U", G1_UPPER_CASE);
        builder.addExpression("G1L", G1_LOWER_CASE);
        builder.addExpression("AR1U", AR1_UPPER_CASE);
        builder.addExpression("AR1L", AR1_LOWER_CASE);
        builder.addExpression("G1U and AR1U", exprConj("AND", G1_UPPER_CASE, AR1_UPPER_CASE));
        builder.addExpression("G1L and AR1L", exprConj("and", G1_LOWER_CASE, AR1_LOWER_CASE));
        builder.addExpression("G1U or AR1U", exprConj("OR", G1_UPPER_CASE, AR1_UPPER_CASE));
        builder.addExpression("G1L or AR1L", exprConj("OR", G1_LOWER_CASE, AR1_LOWER_CASE));
        final BEEvaluator<String> evaluator = builder.build();

        BEInput input;
        Set<String> result;

        input = new BEInput();
        input.getOrCreateStringCategory("AgE").add("20"); // match
        input.getOrCreateStringCategory("gEnDeR").add("M"); // match

        result = evaluator.evaluate(input);
        assertEquals(8, result.size());
        assertTrue(result.contains("G1U"));
        assertTrue(result.contains("G1L"));
        assertTrue(result.contains("AR1U"));
        assertTrue(result.contains("AR1L"));
        assertTrue(result.contains("G1U and AR1U"));
        assertTrue(result.contains("G1L and AR1L"));
        assertTrue(result.contains("G1U or AR1U"));
        assertTrue(result.contains("G1L or AR1L"));

        input = new BEInput();
        input.getOrCreateStringCategory("agE").add("15"); // no match
        input.getOrCreateStringCategory("GeNdeR").add("M"); // match

        result = evaluator.evaluate(input);
        assertEquals(4, result.size());
        assertTrue(result.contains("G1U"));
        assertTrue(result.contains("G1L"));
        assertTrue(result.contains("G1U or AR1U"));
        assertTrue(result.contains("G1L or AR1L"));

        input = new BEInput();
        input.getOrCreateStringCategory("aGe").add("15"); // no match
        input.getOrCreateStringCategory("gEnDer").add("F"); // no match

        result = evaluator.evaluate(input);
        assertEquals(0, result.size());
    }

    @Test
    public void testSingleExpression_MatchComplicated() throws Exception
    {
        final BEEvaluatorBuilder<String> builder = new BEEvaluatorBuilder<>();
        builder.addDataTypeConfig(DATA_TYPE_CONFIG);

        builder.addExpression("test_A_ga", "{\"type\":\"and\",\"values\":[{\"type\":\"gender\",\"values\":[{\"id\":\"M\"},{\"id\":\"F\"}]},{\"type\":\"age\",\"values\":[{\"id\":\"[18,34)\"}]}]}");
        builder.addExpression("test_A_country", "{\"type\":\"country\",\"values\":[{\"id\":\"US\"}]}");
        //        builder.addExpression("test_A_domain", "{\"type\":\"domain\",\"negative\":true,\"values\":[{\"id\":\"baddomain.com\"}]}");
        builder.addExpression("test_A_domain2", "{\"type\":\"or\",\"values\":[{\"type\":\"domain\",\"negative\":true,\"values\":[{\"id\":\"baddomain.com\"}]}]}");
        builder.addExpression("test_A", "{\"type\":\"or\",\"values\":[{\"type\":\"and\",\"values\":[{\"type\":\"gender\",\"values\":[{\"id\":\"M\"},{\"id\":\"F\"}]},{\"type\":\"age\",\"values\":[{\"id\":\"[18,34)\"}]}]},{\"type\":\"country\",\"values\":[{\"id\":\"US\"}]},{\"type\":\"domain\",\"negative\":true,\"values\":[{\"id\":\"baddomain.com\"}]}]}");

        builder.addExpression("test_B_ga", "{\"type\":\"and\",\"values\":[{\"type\":\"gender\",\"values\":[{\"id\":\"F\"}]},{\"type\":\"age\",\"values\":[{\"id\":\"[30,50)\"}]}]}");
        builder.addExpression("test_B_country", "{\"type\":\"country\",\"values\":[{\"id\":\"DE\"}]}");
        //        builder.addExpression("test_B_domain", "{\"type\":\"domain\",\"negative\":false,\"values\":[{\"id\":\"gooddomain.com\"}]}");
        builder.addExpression("test_B_domain2", "{\"type\":\"or\",\"values\":[{\"type\":\"domain\",\"negative\":false,\"values\":[{\"id\":\"gooddomain.com\"}]}]}");
        builder.addExpression("test_B", "{\"type\":\"or\",\"values\":[{\"type\":\"and\",\"values\":[{\"type\":\"gender\",\"values\":[{\"id\":\"F\"}]},{\"type\":\"age\",\"values\":[{\"id\":\"[30,50)\"}]}]},{\"type\":\"country\",\"values\":[{\"id\":\"DE\"}]},{\"type\":\"domain\",\"negative\":false,\"values\":[{\"id\":\"gooddomain.com\"}]}]}");

        builder.addExpression("test_A_and_B", "{\"type\": \"and\", \"values\":[\n" +
                "  {\"type\":\"or\",\"values\":[{\"type\":\"and\",\"values\":[{\"type\":\"gender\",\"values\":[{\"id\":\"M\"},{\"id\":\"F\"}]},{\"type\":\"age\",\"values\":[{\"id\":\"[18,34)\"}]}]},{\"type\":\"country\",\"values\":[{\"id\":\"US\"}]},{\"type\":\"domain\",\"negative\":true,\"values\":[{\"id\":\"baddomain.com\"}]}]},\n" +
                "  {\"type\":\"or\",\"values\":[{\"type\":\"and\",\"values\":[{\"type\":\"gender\",\"values\":[{\"id\":\"F\"}]},{\"type\":\"age\",\"values\":[{\"id\":\"[30,50)\"}]}]},{\"type\":\"country\",\"values\":[{\"id\":\"DE\"}]},{\"type\":\"domain\",\"negative\":false,\"values\":[{\"id\":\"gooddomain.com\"}]}]}]}");


        final BEEvaluator<String> evaluator = builder.build();

        BEInput input;
        Set<String> result;

        // match first subexpression but not second
        input = new BEInput();
        input.getOrCreateStringCategory("domain").add("baddomain.com"); // no match
        input.getOrCreateStringCategory("age").add("[18,24)"); // age + gender = match A
        input.getOrCreateStringCategory("gender").add("M");
        input.getOrCreateStringCategory("country").add("FR"); // no match
        result = evaluator.evaluate(input);
        assertEquals(2, result.size());
        assertTrue("test_A_ga", result.contains("test_A_ga"));
        assertFalse("test_A_country", result.contains("test_A_country"));
        //        assertFalse("test_A_domain", result.contains("test_A_domain"));
        assertFalse("test_A_domain2", result.contains("test_A_domain2"));
        assertTrue("test_A", result.contains("test_A"));
        assertFalse("test_B_ga", result.contains("test_B_ga"));
        assertFalse("test_B_country", result.contains("test_A_country"));
        //        assertFalse("test_B_domain", result.contains("test_B_domain"));
        assertFalse("test_B_domain2", result.contains("test_B_domain2"));
        assertFalse("test_B", result.contains("test_B"));
        assertFalse("test_A_and_B", result.contains("test_A_and_B"));

        // match second subexpression but not first
        input = new BEInput();
        input.getOrCreateStringCategory("domain").add("baddomain.com"); // no match
        input.getOrCreateStringCategory("age").add("45"); // age + gender = match second
        input.getOrCreateStringCategory("gender").add("F");
        input.getOrCreateStringCategory("country").add("FR"); // no match
        result = evaluator.evaluate(input);
        assertEquals(2, result.size());
        assertFalse("test_A_ga", result.contains("test_A_ga"));
        assertFalse("test_A_country", result.contains("test_A_country"));
        //        assertFalse("test_A_domain", result.contains("test_A_domain"));
        assertFalse("test_A_domain2", result.contains("test_A_domain2"));
        assertFalse("test_A", result.contains("test_A"));
        assertTrue("test_B_ga", result.contains("test_B_ga"));
        assertFalse("test_B_country", result.contains("test_A_country"));
        //        assertFalse("test_B_domain", result.contains("test_B_domain"));
        assertFalse("test_B_domain2", result.contains("test_B_domain2"));
        assertTrue("test_B", result.contains("test_B"));
        assertFalse("test_A_and_B", result.contains("test_A_and_B"));

        // matching both results in overall match
        input = new BEInput();
        input.getOrCreateStringCategory("domain").add("gooddomain.com"); // match
        input.getOrCreateStringCategory("age").add("32"); // age + gender = match both
        input.getOrCreateStringCategory("gender").add("F");
        input.getOrCreateStringCategory("country").add("FR"); // no match
        result = evaluator.evaluate(input);

        assertEquals(7, result.size());
        assertTrue("test_A_ga", result.contains("test_A_ga"));
        assertFalse("test_A_country", result.contains("test_A_country"));
        //        assertFalse("test_A_domain", result.contains("test_A_domain"));
        assertTrue("test_A_domain2", result.contains("test_A_domain2"));
        assertTrue("test_A", result.contains("test_A"));
        assertTrue("test_B_ga", result.contains("test_B_ga"));
        assertFalse("test_B_country", result.contains("test_A_country"));
        //        assertFalse("test_B_domain", result.contains("test_B_domain"));
        assertTrue("test_B_domain2", result.contains("test_B_domain2"));
        assertTrue("test_B", result.contains("test_B"));
        assertTrue("test_A_and_B", result.contains("test_A_and_B"));

    }

    @Test
    public void testSingleExpression_NoMatchNegativeSet() throws Exception
    {
        final BEEvaluatorBuilder<String> builder = new BEEvaluatorBuilder<>();
        builder.addDataTypeConfig(DATA_TYPE_CONFIG);
        builder.addExpression("test1", "{\"type\":\"and\",\"values\":[{\"type\":\"or\",\"values\":[{\"type\":\"gender\",\"values\":[{\"id\":\"M\"},{\"id\":\"F\"}]},{\"type\":\"age\",\"values\":[{\"id\":\"[18,34)\"}]}]},{\"type\":\"country\",\"values\":[{\"id\":\"US\"}]},{\"type\":\"domain\",\"negative\":true,\"values\":[{\"id\":\".baddomain.com\"}]}]}");

        final BEEvaluator<String> evaluator = builder.build();

        final BEInput input = new BEInput();
        input.getOrCreateStringCategory("age").add("[18,24)");
        input.getOrCreateStringCategory("gender").add("M");
        input.getOrCreateStringCategory("country").add("US");
        input.getOrCreateStringCategory("domain").add(".baddomain.com");
        input.getOrCreateIntCategory("dma").add(1234);

        final Set<String> result = evaluator.evaluate(input);
        assertEquals(0, result.size());
    }

    @Test
    public void testOrWithNegativePredicate() throws Exception
    {
        final BEEvaluatorBuilder<String> builder = new BEEvaluatorBuilder<>();
        builder.addDataTypeConfig(DATA_TYPE_CONFIG);
        builder.addExpression("test1", "{\"type\":\"and\",\"values\":[{\"type\":\"or\",\"values\":[{\"type\":\"gender\",\"values\":[{\"id\":\"M\"}]},{\"type\":\"age\",\"negative\":true,\"values\":[{\"id\":\"[18,34)\"}]}]},{\"type\":\"country\",\"values\":[{\"id\":\"US\"}]},{\"type\":\"domain\",\"negative\":true,\"values\":[{\"id\":\".baddomain.com\"}]}]}");

        final BEEvaluator<String> evaluator = builder.build();

        BEInput input = new BEInput();
        input.getOrCreateStringCategory("age").add("[18,24)");
        input.getOrCreateStringCategory("gender").add("F");
        input.getOrCreateStringCategory("country").add("US");
        input.getOrCreateStringCategory("domain").add(".bar.com");
        input.getOrCreateIntCategory("dma").add(1234);

        Set<String> result = evaluator.evaluate(input);
        assertEquals(0, result.size());

        input = new BEInput();
        input.getOrCreateStringCategory("age").add("[35,44)");
        input.getOrCreateStringCategory("gender").add("F");
        input.getOrCreateStringCategory("country").add("US");
        input.getOrCreateStringCategory("domain").add(".bar.com");
        input.getOrCreateIntCategory("dma").add(1234);

        result = evaluator.evaluate(input);
        assertEquals(1, result.size());
        assertTrue("test1", result.contains("test1"));
    }

    @Test
    public void testSingleExpression_NoMatchPositiveSet() throws Exception
    {
        final BEEvaluatorBuilder<String> builder = new BEEvaluatorBuilder<>();
        builder.addDataTypeConfig(DATA_TYPE_CONFIG);
        builder.addExpression("test1", "{\"type\":\"and\",\"values\":[{\"type\":\"or\",\"values\":[{\"type\":\"gender\",\"values\":[{\"id\":\"M\"},{\"id\":\"F\"}]},{\"type\":\"age\",\"values\":[{\"id\":\"[18,34)\"}]}]},{\"type\":\"country\",\"values\":[{\"id\":\"US\"}]},{\"type\":\"domain\",\"negative\":true,\"values\":[{\"id\":\".baddomain.com\"}]}]}");

        final BEEvaluator<String> evaluator = builder.build();

        final BEInput input = new BEInput();
        input.getOrCreateStringCategory("age").add("[18,24)");
        input.getOrCreateStringCategory("gender").add("M");
        input.getOrCreateStringCategory("country").add("CA");
        input.getOrCreateStringCategory("domain").add(".baddomain.com");
        input.getOrCreateIntCategory("dma").add(1234);

        final Set<String> result = evaluator.evaluate(input);
        assertEquals(0, result.size());
    }

    @Test
    public void testSingleExpression_InputMissingNegativeCategory() throws Exception
    {
        final BEEvaluatorBuilder<String> builder = new BEEvaluatorBuilder<>();
        builder.addDataTypeConfig(DATA_TYPE_CONFIG);
        builder.addExpression("test1", "{\"type\":\"and\",\"values\":[{\"type\":\"gender\",\"values\":[{\"id\":\"M\"},{\"id\":\"F\"}]},{\"type\":\"age\",\"negative\":true,\"values\":[{\"id\":\"[18,34)\"}]},{\"type\":\"country\",\"values\":[{\"id\":\"US\"}]},{\"type\":\"domain\",\"negative\":true,\"values\":[{\"id\":\".baddomain.com\"}]}]}");

        final BEEvaluator<String> evaluator = builder.build();

        final BEInput input = new BEInput();
        input.getOrCreateStringCategory("gender").add("M");
        input.getOrCreateStringCategory("country").add("US");
        input.getOrCreateStringCategory("domain").add(".bar.com");
        input.getOrCreateIntCategory("dma").add(1234);

        final Set<String> result = evaluator.evaluate(input);
        assertEquals(1, result.size());
        assertTrue("test1", result.contains("test1"));
    }

    @Test
    public void testMultiExpression_OneMatch() throws Exception
    {
        final BEEvaluatorBuilder<String> builder = new BEEvaluatorBuilder<>();
        builder.addDataTypeConfig(DATA_TYPE_CONFIG);
        builder.addExpression("test1", "{\"type\":\"and\",\"values\":[{\"type\":\"or\",\"values\":[{\"type\":\"gender\",\"values\":[{\"id\":\"M\"},{\"id\":\"F\"}]},{\"type\":\"age\",\"values\":[{\"id\":\"[18,34)\"}]}]},{\"type\":\"country\",\"values\":[{\"id\":\"US\"}]},{\"type\":\"domain\",\"negative\":true,\"values\":[{\"id\":\".baddomain.com\"}]}]}");
        builder.addExpression("test2", "{\"type\":\"and\",\"values\":[{\"type\":\"or\",\"values\":[{\"type\":\"gender\",\"values\":[{\"id\":\"F\"}]},{\"type\":\"age\",\"values\":[{\"id\":\"[18,34)\"}]}]},{\"type\":\"country\",\"values\":[{\"id\":\"US\"}]},{\"type\":\"domain\",\"negative\":true,\"values\":[{\"id\":\".baddomain.com\"}]}]}");
        builder.addExpression("test3", "{\"type\":\"and\",\"values\":[{\"type\":\"or\",\"values\":[{\"type\":\"gender\",\"values\":[{\"id\":\"M\"},{\"id\":\"F\"}]},{\"type\":\"age\",\"values\":[{\"id\":\"[18,34)\"}]}]},{\"type\":\"country\",\"values\":[{\"id\":\"US\"}]},{\"type\":\"domain\",\"values\":[{\"id\":\".baddomain.com\"}]}]}");
        builder.addExpression("test4", "{\"type\":\"and\",\"values\":[{\"type\":\"or\",\"values\":[{\"type\":\"gender\",\"values\":[{\"id\":\"F\"}]},{\"type\":\"age\",\"values\":[{\"id\":\"[18,34)\"}]}]},{\"type\":\"country\",\"values\":[{\"id\":\"US\"}]},{\"type\":\"domain\",\"values\":[{\"id\":\".baddomain.com\"}]}]}");

        final BEEvaluator<String> evaluator = builder.build();

        final BEInput input = new BEInput();
        input.getOrCreateStringCategory("age").add("[18,24)");
        input.getOrCreateStringCategory("gender").add("M");
        input.getOrCreateStringCategory("country").add("US");
        input.getOrCreateStringCategory("domain").add(".bar.com");
        input.getOrCreateIntCategory("dma").add(1234);

        final Set<String> result = evaluator.evaluate(input);
        assertEquals(2, result.size());
        assertTrue("test1", result.contains("test1"));
        assertTrue("test2", result.contains("test2"));
    }

    @Test
    public void testStringTrie_Reverse() throws Exception
    {
        final BEEvaluatorBuilder<String> builder = new BEEvaluatorBuilder<>();
        builder.addDataTypeConfig(DATA_TYPE_CONFIG);
        builder.addExpression("test1", "{\"type\":\"domain\",\"values\":[{\"id\":\".videologygroup.com\"}]}");
        builder.addExpression("test2", "{\"type\":\"domain\",\"values\":[{\"id\":\".com\"}]}");
        builder.addExpression("test3", "{\"type\":\"domain\",\"values\":[{\"id\":\".baddomain.com\"}]}");
        builder.addExpression("test4", "{\"type\":\"domain\",\"values\":[{\"id\":\".npr.org\"}]}");

        final BEEvaluator<String> evaluator = builder.build();

        BEInput input = new BEInput();
        input.getOrCreateStringCategory("domain").add("www.videologygroup.com");
        Set<String> result = evaluator.evaluate(input);
        assertEquals(2, result.size());
        assertTrue("test1", result.contains("test1"));
        assertTrue("test2", result.contains("test2"));

        input = new BEInput();
        input.getOrCreateStringCategory("domain").add("WWW.VIDEOLOGYGROUP.COM");
        result = evaluator.evaluate(input);
        assertEquals(2, result.size());
        assertTrue("test1", result.contains("test1"));
        assertTrue("test2", result.contains("test2"));

        input = new BEInput();
        input.getOrCreateStringCategory("domain").add("xvideologygroup.com");
        result = evaluator.evaluate(input);
        assertEquals(1, result.size());
        assertTrue("test2", result.contains("test2"));
    }

    @Test
    public void testSimplePartialExpressions() throws Exception
    {
        final BEEvaluatorBuilder<String> builder = new BEEvaluatorBuilder<>();
        builder.addDataTypeConfig(DATA_TYPE_CONFIG);
        builder.addPartialExpression("dg:1", "{\"type\":\"domain\",\"values\":[{\"id\":\".videologygroup.com\"}]}");
        builder.addPartialExpression("ag:1", "{\"type\":\"age\",\"values\":[{\"id\":\"[18,24)\"}]}");
        builder.addExpression("test1", "{\"type\":\"and\",\"values\":[{\"type\":\"ref\",\"values\":[{\"id\":\"dg:1\"}]},{\"type\":\"gender\",\"values\":[{\"id\":\"M\"}]}]}");
        builder.addExpression("test2", "{\"type\":\"and\",\"values\":[{\"type\":\"ref\",\"values\":[{\"id\":\"dg:1\"}]},{\"type\":\"ref\",\"values\":[{\"id\":\"ag:1\"}]},{\"type\":\"gender\",\"values\":[{\"id\":\"F\"}]}]}");

        final BEEvaluator<String> evaluator = builder.build();

        BEInput input = new BEInput();
        input.getOrCreateStringCategory("domain").add("www.videologygroup.com");
        input.getOrCreateStringCategory("age").add("[18,24)");
        input.getOrCreateStringCategory("gender").add("M");

        Set<String> result = evaluator.evaluate(input);
        assertEquals(1, result.size());
        assertTrue("test1", result.contains("test1"));

        input = new BEInput();
        input.getOrCreateStringCategory("domain").add("www.videologygroup.com");
        input.getOrCreateStringCategory("age").add("[18,24)");
        input.getOrCreateStringCategory("gender").add("F");

        result = evaluator.evaluate(input);
        assertEquals(1, result.size());
        assertTrue("test2", result.contains("test2"));
    }

    @Test
    public void testMultiplePartialExpressionsWithNegative() throws Exception
    {
        final BEEvaluatorBuilder<String> builder = new BEEvaluatorBuilder<>();
        builder.addDataTypeConfig(DATA_TYPE_CONFIG);
        builder.addPartialExpression("dg:1", ExpressionUtil.expr("domain", ".videologygroup.com"));
        builder.addPartialExpression("ag:1", ExpressionUtil.expr("age", "[18,24)"));
        builder.addExpression("test1",
                exprConj("and",
                        ExpressionUtil.expr("ref", "dg:1"),
                        ExpressionUtil.expr("gender", "M")));
        builder.addExpression("test2",
                exprConj("and",
                        ExpressionUtil.expr("ref", true,"ag:1"),
                        ExpressionUtil.expr("gender", "F")));

        final BEEvaluator<String> evaluator = builder.build();

        BEInput input = new BEInput();
        input.getOrCreateStringCategory("domain").add("www.videologygroup.com");
        input.getOrCreateStringCategory("age").add("[18,24)");
        input.getOrCreateStringCategory("gender").add("M");

        Set<String> result = evaluator.evaluate(input);
        assertEquals(1, result.size());
        assertTrue("test1", result.contains("test1"));

        input = new BEInput();
        input.getOrCreateStringCategory("domain").add("www.videologygroup.com");
        input.getOrCreateStringCategory("age").add("[18,24)");
        input.getOrCreateStringCategory("gender").add("F");

        result = evaluator.evaluate(input);
        assertEquals(0, result.size());

        input = new BEInput();
        input.getOrCreateStringCategory("domain").add("www.videologygroup.com");
        input.getOrCreateStringCategory("age").add("[25,34)");
        input.getOrCreateStringCategory("gender").add("F");

        result = evaluator.evaluate(input);
        assertEquals(1, result.size());
        assertTrue("test2", result.contains("test2"));
    }

    @Test
    public void testSimpleNegativePartialExpression() throws Exception
    {
        final BEEvaluatorBuilder<String> builder = new BEEvaluatorBuilder<>();
        builder.addDataTypeConfig(DATA_TYPE_CONFIG);
        builder.addPartialExpression("dg:1", "{\"type\":\"domain\",\"values\":[{\"id\":\".cnn.com\"}]}");
        builder.addPartialExpression("dg:2", "{\"type\":\"domain\",\"values\":[{\"id\":\".go.com\"}]}");
        builder.addExpression("test1",
                exprConj("and",
                        ExpressionUtil.expr("ref", true, "dg:1", "dg:2")));

        final BEEvaluator<String> evaluator = builder.build();

        BEInput input = new BEInput();
        input.getOrCreateStringCategory("domain").add(".abc.com");

        Set<String> result = evaluator.evaluate(input);
        assertEquals(1, result.size());
        assertTrue("test1", result.contains("test1"));

        input = new BEInput();
        input.getOrCreateStringCategory("domain").add("www.cnn.com");
        result = evaluator.evaluate(input);
        assertEquals(0, result.size());

        input = new BEInput();
        input.getOrCreateStringCategory("domain").add("WWW.CNN.COM");
        result = evaluator.evaluate(input);
        assertEquals(0, result.size());

        input = new BEInput();
        input.getOrCreateStringCategory("domain").add("www.go.com");
        result = evaluator.evaluate(input);
        assertEquals(0, result.size());
    }

    @Test
    public void testNestedAnd() throws Exception
    {
        final BEEvaluatorBuilder<String> builder = new BEEvaluatorBuilder<>();
        builder.addExpression("test1", "{\"type\":\"and\",\"values\":[{\"type\":\"format\",\"values\":[{\"id\":\"VIDEO\"}]},{\"type\":\"and\",\"values\":[{\"type\":\"gender\",\"values\":[{\"id\":\"M\"}]}]}]}}");

        final BEEvaluator<String> evaluator = builder.build();

        BEInput input = new BEInput();
        input.getOrCreateStringCategory("format").add("VIDEO");
        input.getOrCreateStringCategory("gender").add("M");
        Set<String> result = evaluator.evaluate(input);
        assertEquals(1, result.size());
        assertTrue("test1", result.contains("test1"));

        input = new BEInput();
        input.getOrCreateStringCategory("format").add("VIDEO");
        input.getOrCreateStringCategory("gender").add("F");
        result = evaluator.evaluate(input);
        assertEquals(0, result.size());

        input = new BEInput();
        input.getOrCreateStringCategory("format").add("DISPLAY");
        input.getOrCreateStringCategory("gender").add("M");
        result = evaluator.evaluate(input);
        assertEquals(0, result.size());
    }

    @Test
    public void testSingleExpression_MatchNegativeSet2() throws Exception
    {
        final BEEvaluatorBuilder<String> builder = new BEEvaluatorBuilder<>();
        builder.addDataTypeConfig(BEEvaluatorTest.DATA_TYPE_CONFIG);
        builder.addExpression("test1", "{" +
                "  \"type\" : \"country\"," +
                "  \"negative\" : true," +
                "  \"values\" : [ {" +
                "    \"id\" : \"SI\"" +
                "  } ]" +
                "}");

        final BEEvaluator<String> evaluator = builder.build();

        final BEInput input = new BEInput();
        input.getOrCreateStringCategory("country").add("US");

        final Set<String> result = evaluator.evaluate(input);
        assertEquals(1, result.size());
        assertTrue(result.contains("test1"));
    }

    @Test
    public void testSingleExpression_MatchNegativeSet3() throws Exception
    {
        final BEEvaluatorBuilder<String> builder = new BEEvaluatorBuilder<>();
        builder.addDataTypeConfig(BEEvaluatorTest.DATA_TYPE_CONFIG);
        builder.addExpression("test1", "{" +
                "  \"type\" : \"and\"," +
                "  \"values\" : [ {" +
                "      \"type\" : \"country\"," +
                "      \"negative\" : true," +
                "      \"values\" : [ {" +
                "        \"id\" : \"SI\"" +
                "      } ]" +
                "  } ]" +
                "}");

        final BEEvaluator<String> evaluator = builder.build();

        final BEInput input = new BEInput();
        input.getOrCreateStringCategory("country").add("US");

        final Set<String> result = evaluator.evaluate(input);
        assertEquals(1, result.size());
        assertTrue(result.contains("test1"));
    }

    @Test
    public void testSingleExpression_MatchNegativeSet4() throws Exception
    {
        final BEEvaluatorBuilder<String> builder = new BEEvaluatorBuilder<>();
        builder.addDataTypeConfig(BEEvaluatorTest.DATA_TYPE_CONFIG);
        builder.addExpression("test1", "{" +
                "  \"type\" : \"or\"," +
                "  \"values\" : [ {" +
                "      \"type\" : \"country\"," +
                "      \"negative\" : true," +
                "      \"values\" : [ {" +
                "        \"id\" : \"SI\"" +
                "      } ]" +
                "  } ]" +
                "}");

        final BEEvaluator<String> evaluator = builder.build();

        final BEInput input = new BEInput();
        input.getOrCreateStringCategory("country").add("US");

        final Set<String> result = evaluator.evaluate(input);
        assertEquals(1, result.size());
        assertTrue(result.contains("test1"));
    }

    @Ignore // this case is known not to work - TODO change the evaluator builder to reject expressions that do not have a root node that is a conjunction node
    @Test
    public void testSingleExpression_RootPredicateNode() throws Exception
    {
        final BEEvaluatorBuilder<String> builder = new BEEvaluatorBuilder<>();
        builder.addDataTypeConfig(BEEvaluatorTest.DATA_TYPE_CONFIG);
        builder.addExpression("test1", "{" +
                "  \"type\" : \"country\"," +
                "  \"negative\" : true," +
                "  \"values\" : [ {" +
                "    \"id\" : \"SI\"" +
                "  } ]" +
                "}");

        final BEEvaluator<String> evaluator = builder.build();

        final BEInput input = new BEInput();
        input.getOrCreateStringCategory("country").add("SI");

        final Set<String> result = evaluator.evaluate(input);
        assertEquals(0, result.size());
        assertTrue(result.contains("test1"));
    }

    @Test
    public void testSingleExpression_NoMatchNegativeSet2() throws Exception
    {
        final BEEvaluatorBuilder<String> builder = new BEEvaluatorBuilder<>();
        builder.addDataTypeConfig(BEEvaluatorTest.DATA_TYPE_CONFIG);
        builder.addExpression("test1", "{" +
                "  \"type\" : \"and\"," +
                "  \"values\" : [ {" +
                "      \"type\" : \"country\"," +
                "      \"negative\" : true," +
                "      \"values\" : [ {" +
                "        \"id\" : \"SI\"" +
                "      } ]" +
                "  } ]" +
                "}");

        final BEEvaluator<String> evaluator = builder.build();

        final BEInput input = new BEInput();
        input.getOrCreateStringCategory("country").add("SI");

        final Set<String> result = evaluator.evaluate(input);
        assertEquals(0, result.size());
    }

    @Test
    public void testSingleExpression_NoMatchNegativeSet3() throws Exception
    {
        final BEEvaluatorBuilder<String> builder = new BEEvaluatorBuilder<>();
        builder.addDataTypeConfig(BEEvaluatorTest.DATA_TYPE_CONFIG);
        builder.addExpression("test1", "{" +
                "  \"type\" : \"or\"," +
                "  \"values\" : [ {" +
                "      \"type\" : \"country\"," +
                "      \"negative\" : true," +
                "      \"values\" : [ {" +
                "        \"id\" : \"SI\"" +
                "      } ]" +
                "  } ]" +
                "}");

        final BEEvaluator<String> evaluator = builder.build();

        final BEInput input = new BEInput();
        input.getOrCreateStringCategory("country").add("SI");

        final Set<String> result = evaluator.evaluate(input);
        assertEquals(0, result.size());
    }

    /**
     *            ---- NAND(a)----
     *          /                \
     *      --NOR(b)--          OR(c)
     *    /           \        /     \
     *   A             B      C       D    */
    @Test
    public void testSingleExpression_NegativeConjunctions() throws Exception
    {
        final BEEvaluatorBuilder<String> builder = new BEEvaluatorBuilder<>();
        final BENode A = new BEPredicateNode("A", "a");
        final BENode B = new BEPredicateNode("B", "b");
        final BENode C = new BEPredicateNode("C", "c");
        final BENode D = new BEPredicateNode("D", "d");
        builder.addExpression("negative conjunction test", ExpressionUtil.nand(ExpressionUtil.nor(A, B), ExpressionUtil.or(C, D)));

        final BEEvaluator<String> evaluator = builder.build();

        final BEInput matchingInput = new BEInput();
        matchingInput.getOrCreateStringCategory("A").add("a");
        matchingInput.getOrCreateStringCategory("B").add("b");
        matchingInput.getOrCreateStringCategory("C").add("-");
        matchingInput.getOrCreateStringCategory("D").add("-");

        Set<String> result = evaluator.evaluate(matchingInput);
        assertEquals(1, result.size());

        final BEInput missingInput= new BEInput();
        missingInput.getOrCreateStringCategory("A").add("-");
        missingInput.getOrCreateStringCategory("B").add("-");
        missingInput.getOrCreateStringCategory("C").add("c");
        missingInput.getOrCreateStringCategory("D").add("d");

        result = evaluator.evaluate(missingInput);
        assertEquals(0, result.size());

    }

    /**
     *                        ---- OR(a)----
     *                      /                \
     *              ----- AND(b)--          AND(c)
     *            /                \      /     \
     *      ---- (D)---            M(E)  M(F) clayton.net(G)
     *    /             \
     * ajiang.net pconline.com.cn
     *
     *   0  1  2  3
     * a x  x  x  x
     * b x  x  x  x
     * c x  x  x  x
     * D x
     * E    x  x  x
     * F x  x  x
     * G          x
     *
     */
    @Test
    public void testSingleExpressionWithOrOfAnd_NoMatch1() throws Exception
    {
        final BEEvaluatorBuilder<String> builder = new BEEvaluatorBuilder<>();
        builder.addDataTypeConfig(BEEvaluatorTest.DATA_TYPE_CONFIG);
        builder.addExpression("test1", "{\n" +
                "  \"type\": \"or\",\n" +
                "  \"values\": [\n" +
                "    {\n" +
                "      \"type\": \"and\",\n" +
                "      \"values\": [\n" +
                "        { \"type\": \"domain\", \"values\": [ { \"id\": \"ajiang.net\" }, { \"id\": \"pconline.com.cn\" } ] },\n" +
                "        { \"type\": \"gender\", \"values\": [ { \"id\": \"M\" } ] }\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"type\": \"and\",\n" +
                "      \"values\": [\n" +
                "        { \"type\": \"gender\", \"values\": [ { \"id\": \"M\" } ] },\n" +
                "        { \"type\": \"domain\", \"values\": [ { \"id\": \"clayton.net\" } ] }\n" +
                "      ]\n" +
                "    }\n" +
                "  ]\n" +
                "}");

        final BEEvaluator<String> evaluator = builder.build();

        final BEInput input = new BEInput();
        input.getOrCreateStringCategory("gender").add("M");
        input.getOrCreateStringCategory("domain").add("example.com");

        final Set<String> result = evaluator.evaluate(input);
        assertEquals(0, result.size());
    }

    @Test
    public void testSingleExpressionWithOrOfAnd_NoMatch2() throws Exception
    {
        final BEEvaluatorBuilder<String> builder = new BEEvaluatorBuilder<>();
        builder.addDataTypeConfig(BEEvaluatorTest.DATA_TYPE_CONFIG);
        builder.addExpression("test1", "{\n" +
                "  \"type\": \"or\",\n" +
                "  \"values\": [\n" +
                "    {\n" +
                "      \"type\": \"and\",\n" +
                "      \"values\": [\n" +
                "        { \"type\": \"country\", \"values\": [ { \"id\": \"US\" }, { \"id\": \"CA\" } ] },\n" +
                "        { \"type\": \"gender\", \"values\": [ { \"id\": \"M\" } ] }\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"type\": \"and\",\n" +
                "      \"values\": [\n" +
                "        { \"type\": \"gender\", \"values\": [ { \"id\": \"M\" } ] },\n" +
                "        { \"type\": \"country\", \"values\": [ { \"id\": \"UK\" } ] }\n" +
                "      ]\n" +
                "    }\n" +
                "  ]\n" +
                "}");

        final BEEvaluator<String> evaluator = builder.build();

        final BEInput input = new BEInput();
        input.getOrCreateStringCategory("gender").add("M");
        input.getOrCreateStringCategory("country").add("IT");

        final Set<String> result = evaluator.evaluate(input);
        assertEquals(0, result.size());
    }

    /**
     *                        ---- OR(a)----
     *                      /                \
     *              ----- AND(b)--          AND(c)
     *            /                \      /     \
     *      ---- (D)---            M(E)  F(F)   UK(G)
     *    /             \
     *   US             CA
     *
     *   0  1  2  3
     * a x  x  x  x
     * b x  x  x  x
     * c x  x  x  x
     * D x
     * E    x  x  x
     * F x  x  x
     * G          x
     *
     */
    @Test
    public void testSingleExpressionWithOrOfAnd_NoMatch3() throws Exception
    {
        final BEEvaluatorBuilder<String> builder = new BEEvaluatorBuilder<>();
        builder.addDataTypeConfig(BEEvaluatorTest.DATA_TYPE_CONFIG);
        builder.addExpression("test1", "{\n" +
                "  \"type\": \"or\",\n" +
                "  \"values\": [\n" +
                "    {\n" +
                "      \"type\": \"and\",\n" +
                "      \"values\": [\n" +
                "        { \"type\": \"country\", \"values\": [ { \"id\": \"US\" }, { \"id\": \"CA\" } ] },\n" +
                "        { \"type\": \"gender\", \"values\": [ { \"id\": \"M\" } ] }\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"type\": \"and\",\n" +
                "      \"values\": [\n" +
                "        { \"type\": \"gender\", \"values\": [ { \"id\": \"F\" } ] },\n" +
                "        { \"type\": \"country\", \"values\": [ { \"id\": \"UK\" } ] }\n" +
                "      ]\n" +
                "    }\n" +
                "  ]\n" +
                "}");

        final BEEvaluator<String> evaluator = builder.build();

        BEInput input = new BEInput();
        input.getOrCreateStringCategory("gender").add("M");
        input.getOrCreateStringCategory("gender").add("F");

        Set<String> result = evaluator.evaluate(input);
        assertEquals(0, result.size());

        input = new BEInput();
        input.getOrCreateStringCategory("gender").add("unknown");
        input.getOrCreateStringCategory("country").add("US");
        input.getOrCreateStringCategory("country").add("UK");

        result = evaluator.evaluate(input);
        assertEquals(0, result.size());
    }

    @Test
    public void testRemoveExpressions() throws Exception
    {
        final BEEvaluatorBuilder<String> builder = new BEEvaluatorBuilder<>();
        builder.addExpression("test1", "{\"type\":\"and\",\"values\":[{\"type\":\"format\",\"values\":[{\"id\":\"VIDEO\"}]},{\"type\":\"and\",\"values\":[{\"type\":\"gender\",\"values\":[{\"id\":\"M\"}]}]}]}}");
        builder.addExpression("test2", "{\"type\":\"and\",\"values\":[{\"type\":\"format\",\"values\":[{\"id\":\"DISPLAY\"}]},{\"type\":\"and\",\"values\":[{\"type\":\"gender\",\"values\":[{\"id\":\"M\"}]}]}]}}");

        builder.removeExpressions("test1");

        final BEEvaluator<String> evaluator = builder.build();

        BEInput input = new BEInput();
        input.getOrCreateStringCategory("format").add("VIDEO");
        input.getOrCreateStringCategory("gender").add("M");
        Set<String> result = evaluator.evaluate(input);
        assertEquals(0, result.size());

        input = new BEInput();
        input.getOrCreateStringCategory("format").add("DISPLAY");
        input.getOrCreateStringCategory("gender").add("M");
        result = evaluator.evaluate(input);
        assertEquals(1, result.size());
        assertTrue("test2", result.contains("test2"));
    }

    @Test
    public void testRemoveExpressions_multipleExpressions() throws Exception
    {
        final BEEvaluatorBuilder<String> builder = new BEEvaluatorBuilder<>();
        builder.addExpression("test1", "{\"type\":\"and\",\"values\":[{\"type\":\"format\",\"values\":[{\"id\":\"VIDEO\"}]},{\"type\":\"and\",\"values\":[{\"type\":\"gender\",\"values\":[{\"id\":\"M\"}]}]}]}}");
        builder.addExpression("test1", "{\"type\":\"and\",\"values\":[{\"type\":\"format\",\"values\":[{\"id\":\"DISPLAY\"}]},{\"type\":\"and\",\"values\":[{\"type\":\"gender\",\"values\":[{\"id\":\"F\"}]}]}]}}");
        builder.addExpression("test2", "{\"type\":\"and\",\"values\":[{\"type\":\"format\",\"values\":[{\"id\":\"DISPLAY\"}]},{\"type\":\"and\",\"values\":[{\"type\":\"gender\",\"values\":[{\"id\":\"M\"}]}]}]}}");

        builder.removeExpressions("test1");

        final BEEvaluator<String> evaluator = builder.build();

        BEInput input = new BEInput();
        input.getOrCreateStringCategory("format").add("VIDEO");
        input.getOrCreateStringCategory("gender").add("M");
        Set<String> result = evaluator.evaluate(input);
        assertEquals(0, result.size());

        input = new BEInput();
        input.getOrCreateStringCategory("format").add("VIDEO");
        input.getOrCreateStringCategory("gender").add("F");
        result = evaluator.evaluate(input);
        assertEquals(0, result.size());

        input = new BEInput();
        input.getOrCreateStringCategory("format").add("DISPLAY");
        input.getOrCreateStringCategory("gender").add("M");
        result = evaluator.evaluate(input);
        assertEquals(1, result.size());
        assertTrue("test2", result.contains("test2"));
    }

    @Test
    public void testRemoveExpressions_notFound() throws Exception
    {
        final BEEvaluatorBuilder<String> builder = new BEEvaluatorBuilder<>();
        builder.addExpression("test1", "{\"type\":\"and\",\"values\":[{\"type\":\"format\",\"values\":[{\"id\":\"VIDEO\"}]},{\"type\":\"and\",\"values\":[{\"type\":\"gender\",\"values\":[{\"id\":\"M\"}]}]}]}}");
        builder.addExpression("test2", "{\"type\":\"and\",\"values\":[{\"type\":\"format\",\"values\":[{\"id\":\"DISPLAY\"}]},{\"type\":\"and\",\"values\":[{\"type\":\"gender\",\"values\":[{\"id\":\"M\"}]}]}]}}");

        builder.removeExpressions("test3");

        final BEEvaluator<String> evaluator = builder.build();

        BEInput input = new BEInput();
        input.getOrCreateStringCategory("format").add("VIDEO");
        input.getOrCreateStringCategory("gender").add("M");
        Set<String> result = evaluator.evaluate(input);
        assertEquals(1, result.size());
        assertTrue("test1", result.contains("test1"));

        input = new BEInput();
        input.getOrCreateStringCategory("format").add("DISPLAY");
        input.getOrCreateStringCategory("gender").add("M");
        result = evaluator.evaluate(input);
        assertEquals(1, result.size());
        assertTrue("test2", result.contains("test2"));
    }

    @Test
    public void testRemoveExpressions_afterBuild() throws Exception
    {
        final BEEvaluatorBuilder<String> builder = new BEEvaluatorBuilder<>();
        builder.addExpression("test1", "{\"type\":\"and\",\"values\":[{\"type\":\"format\",\"values\":[{\"id\":\"VIDEO\"}]},{\"type\":\"and\",\"values\":[{\"type\":\"gender\",\"values\":[{\"id\":\"M\"}]}]}]}}");
        builder.addExpression("test2", "{\"type\":\"and\",\"values\":[{\"type\":\"format\",\"values\":[{\"id\":\"DISPLAY\"}]},{\"type\":\"and\",\"values\":[{\"type\":\"gender\",\"values\":[{\"id\":\"M\"}]}]}]}}");

        BEEvaluator<String> evaluator = builder.build();

        BEInput input = new BEInput();
        input.getOrCreateStringCategory("format").add("VIDEO");
        input.getOrCreateStringCategory("gender").add("M");
        Set<String> result = evaluator.evaluate(input);
        assertEquals(1, result.size());

        input = new BEInput();
        input.getOrCreateStringCategory("format").add("DISPLAY");
        input.getOrCreateStringCategory("gender").add("M");
        result = evaluator.evaluate(input);
        assertEquals(1, result.size());
        assertTrue("test2", result.contains("test2"));

        // remove the "test1" expression and rebuild
        builder.removeExpressions("test1");
        evaluator = builder.build();

        input = new BEInput();
        input.getOrCreateStringCategory("format").add("VIDEO");
        input.getOrCreateStringCategory("gender").add("M");
        result = evaluator.evaluate(input);
        assertEquals(0, result.size());

        input = new BEInput();
        input.getOrCreateStringCategory("format").add("DISPLAY");
        input.getOrCreateStringCategory("gender").add("M");
        result = evaluator.evaluate(input);
        assertEquals(1, result.size());
        assertTrue("test2", result.contains("test2"));

        // add a new "test1" expression and rebuild
        builder.addExpression("test1", "{\"type\":\"and\",\"values\":[{\"type\":\"format\",\"values\":[{\"id\":\"DISPLAY\"}]},{\"type\":\"and\",\"values\":[{\"type\":\"gender\",\"values\":[{\"id\":\"F\"}]}]}]}}");
        evaluator = builder.build();

        input = new BEInput();
        input.getOrCreateStringCategory("format").add("VIDEO");
        input.getOrCreateStringCategory("gender").add("M");
        result = evaluator.evaluate(input);
        assertEquals(0, result.size());

        input = new BEInput();
        input.getOrCreateStringCategory("format").add("DISPLAY");
        input.getOrCreateStringCategory("gender").add("M");
        result = evaluator.evaluate(input);
        assertEquals(1, result.size());
        assertTrue("test2", result.contains("test2"));

        input = new BEInput();
        input.getOrCreateStringCategory("format").add("DISPLAY");
        input.getOrCreateStringCategory("gender").add("F");
        result = evaluator.evaluate(input);
        assertEquals(1, result.size());
        assertTrue("test1", result.contains("test1"));
    }

    @Test
    public void testRemovePartialExpression() throws Exception
    {

        final BEEvaluatorBuilder<String> builder = new BEEvaluatorBuilder<>();
        builder.addDataTypeConfig(DATA_TYPE_CONFIG);
        builder.addPartialExpression("dg:1", "{\"type\":\"domain\",\"values\":[{\"id\":\".videologygroup.com\"}]}");
        builder.addPartialExpression("ag:1", "{\"type\":\"age\",\"values\":[{\"id\":\"[18,24)\"}]}");
        builder.addExpression("test1", "{\"type\":\"and\",\"values\":[{\"type\":\"ref\",\"values\":[{\"id\":\"dg:1\"}]},{\"type\":\"gender\",\"values\":[{\"id\":\"M\"}]}]}");
        builder.addExpression("test2", "{\"type\":\"and\",\"values\":[{\"type\":\"ref\",\"values\":[{\"id\":\"dg:1\"}]},{\"type\":\"ref\",\"values\":[{\"id\":\"ag:1\"}]},{\"type\":\"gender\",\"values\":[{\"id\":\"F\"}]}]}");

        final BEEvaluator<String> evaluator = builder.build();

        BEInput input = new BEInput();
        input.getOrCreateStringCategory("domain").add("www.videologygroup.com");
        input.getOrCreateStringCategory("age").add("[18,24)");
        input.getOrCreateStringCategory("gender").add("M");

        Set<String> result = evaluator.evaluate(input);
        assertEquals(1, result.size());
        assertTrue("test1", result.contains("test1"));

        input = new BEInput();
        input.getOrCreateStringCategory("domain").add("www.videologygroup.com");
        input.getOrCreateStringCategory("age").add("[18,24)");
        input.getOrCreateStringCategory("gender").add("F");

        result = evaluator.evaluate(input);
        assertEquals(1, result.size());
        assertTrue("test2", result.contains("test2"));
    }

    @Test
    public void testEqualsAndHashCode() throws Exception
    {
        // Given
        final List<BEDataTypeConfig> dataTypeConfigs = Arrays.asList(
                new BEDataTypeConfig("gender", "string", true, false, false, false),
                new BEDataTypeConfig("age", "byte", false, false, true, false),
                new BEDataTypeConfig("domain", "string", true, true, false, true),
                new BEDataTypeConfig("country", "string", false, true, false, false)
        );
        final String expr10 = ExpressionUtil.idExpr("e1", "gender", "M", "F");
        final String expr11 = ExpressionUtil.idExpr("e1", "GENDER", "f", "m");
        final String expr20 = ExpressionUtil.idExpr("e2", "age", "[18,24]", "[25,30]");
        final String expr21 = ExpressionUtil.idExpr("e2", "AGE", "[25,30]", "[18,24]");
        final String expr30 = ExpressionUtil.idExpr("e3", "domain", "foo.com", "foo.org", "BAR.com", "BAR.org");
        final String expr31 = ExpressionUtil.idExpr("e3", "DOMAIN", "bar.org", "bar.com", "FOO.org", "FOO.com");
        final String expr40 = ExpressionUtil.idExpr("e4", "country", "US", "UK");
        final String expr42 = ExpressionUtil.idExpr("e4", "country", "AU");

        // When two evaluators are built with the same builder, the resulting evaluators are the same
        final BEEvaluatorBuilder<String> builder1 = new BEEvaluatorBuilder<>();
        final BEEvaluatorBuilder<String> builder2 = new BEEvaluatorBuilder<>();
        assertEvaluatorBuildersEqual(builder1, builder2);

        // unreferenced data type configs do not impact equals and hashcode
        builder1.addDataTypeConfigs(dataTypeConfigs);
        assertEvaluatorBuildersEqual(builder1, builder2);
        builder2.addDataTypeConfigs(dataTypeConfigs.stream()
                .sorted(Comparator.comparing(BEDataTypeConfig::getType)).collect(Collectors.toList()));
        assertEvaluatorBuildersEqual(builder1, builder2);

        // expressions impact equals and hashcode
        builder1.addExpression("e1", expr10);
        assertEvaluatorBuildersNotEqual(builder1, builder2);
        builder2.addExpression("e1", expr11);
        assertEvaluatorBuildersEqual(builder1, builder2);

        builder1.addExpression("e2", expr20);
        assertEvaluatorBuildersNotEqual(builder1, builder2);
        builder2.addExpression("e2", expr21);
        assertEvaluatorBuildersEqual(builder1, builder2);

        builder1.addExpression("e3", expr30);
        assertEvaluatorBuildersNotEqual(builder1, builder2);
        builder2.addExpression("e3", expr31);
        assertEvaluatorBuildersEqual(builder1, builder2);

        builder1.addExpression("e4", expr40);
        assertEvaluatorBuildersNotEqual(builder1, builder2);
        builder2.addExpression("e4", expr42);
        assertEvaluatorBuildersNotEqual(builder1, builder2);

    }

    private void assertEvaluatorBuildersEqual(final BEEvaluatorBuilder b1, final BEEvaluatorBuilder b2) {
        assertEquals(b1.build(), b2.build());
        assertEquals(b1.build().hashCode(), b2.build().hashCode());
    }

    private void assertEvaluatorBuildersNotEqual(final BEEvaluatorBuilder b1, final BEEvaluatorBuilder b2) {
        assertNotEquals(b1.build(), b2.build());
        assertNotEquals(b1.build().hashCode(), b2.build().hashCode());
    }
}