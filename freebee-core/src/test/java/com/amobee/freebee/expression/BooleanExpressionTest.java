package com.amobee.freebee.expression;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;

import static org.junit.Assert.assertEquals;

/**
 * @author Michael Bond
 */
public class BooleanExpressionTest
{
    private final ObjectMapper mapper = new ObjectMapper();

    @Before
    public void setUp() throws Exception
    {
    }

    @Test
    public void testSerializeTwoLevels() throws Exception
    {
        final BENode expression = BETestUtils.createExpression();
        final String expected = "{\"id\":\"test1\",\"type\":\"and\",\"values\":[{\"type\":\"or\",\"values\":[{\"type\":\"gender\",\"values\":[{\"id\":\"M\"},{\"id\":\"F\"}]},{\"type\":\"age\",\"values\":[{\"id\":\"18-24\"},{\"id\":\"25-34\"}]}]},{\"type\":\"geo\",\"values\":[{\"id\":\"US\"}]},{\"type\":\"domain\",\"negative\":true,\"values\":[{\"id\":\"baddomain.com\"}]},{\"type\":\"daypart\",\"values\":[{\"id\":\"WKD\",\"tz\":\"EST\"}]}]}";

        final String serializedExpression = this.mapper.writeValueAsString(expression);

        assertEquals(expected, serializedExpression);
    }

    @Test
    public void testDeserializeTwoLevels() throws Exception
    {
        final BENode expression = BETestUtils.createExpression();
        final BENode mapped = this.mapper.readValue(
                "{\"id\":\"test1\",\"type\":\"and\",\"values\":[{\"type\":\"or\",\"values\":[{\"type\":\"gender\",\"values\":[{\"id\":\"M\"},{\"id\":\"F\"}]},{\"type\":\"age\",\"values\":[{\"id\":\"18-24\"},{\"id\":\"25-34\"}]}]},{\"type\":\"geo\",\"values\":[{\"id\":\"US\"}]},{\"type\":\"domain\",\"negative\":true,\"values\":[{\"id\":\"baddomain.com\"}]},{\"type\":\"daypart\",\"values\":[{\"id\":\"WKD\",\"tz\":\"EST\"}]}]}",
                BENode.class);
        assertEquals(expression, mapped);
    }

    @Test
    public void testDeserializeUpperCaseAndConjunction() throws Exception
    {
        final BEConjunctionNode expression = new BEConjunctionNode("test", BEConjunctionType.AND);
        expression.addValue(new BEPredicateNode("gender", "M"));
        final BENode mapped = this.mapper.readValue(
                "{\"id\":\"test\",\"type\":\"AND\",\"values\":[{\"type\":\"gender\",\"values\":[{\"id\":\"M\"}]}]}",
                BENode.class);
        assertEquals(expression, mapped);
    }

    @Test
    public void testDeserializeLowerCaseAndConjunction() throws Exception
    {
        final BEConjunctionNode expression = new BEConjunctionNode("test", BEConjunctionType.AND);
        expression.addValue(new BEPredicateNode("gender", "M"));
        final BENode mapped = this.mapper.readValue(
                "{\"id\":\"test\",\"type\":\"and\",\"values\":[{\"type\":\"gender\",\"values\":[{\"id\":\"M\"}]}]}",
                BENode.class);
        assertEquals(expression, mapped);
    }

    @Test
    public void testDeserializeUpperCaseOrConjunction() throws Exception
    {
        final BEConjunctionNode expression = new BEConjunctionNode("test", BEConjunctionType.OR);
        expression.addValue(new BEPredicateNode("gender", "M"));
        final BENode mapped = this.mapper.readValue(
                "{\"id\":\"test\",\"type\":\"OR\",\"values\":[{\"type\":\"gender\",\"values\":[{\"id\":\"M\"}]}]}",
                BENode.class);
        assertEquals(expression, mapped);
    }

    @Test
    public void testDeserializeLowerCaseOrConjunction() throws Exception
    {
        final BEConjunctionNode expression = new BEConjunctionNode("test", BEConjunctionType.OR);
        expression.addValue(new BEPredicateNode("gender", "M"));
        final BENode mapped = this.mapper.readValue(
                "{\"id\":\"test\",\"type\":\"or\",\"values\":[{\"type\":\"gender\",\"values\":[{\"id\":\"M\"}]}]}",
                BENode.class);
        assertEquals(expression, mapped);
    }

    @Test
    public void testDeserializeUpperCaseReference() throws Exception
    {
        final BEReferenceNode expression = new BEReferenceNode(Lists.newArrayList("test"));
        final BENode mapped = this.mapper.readValue(
                "{\"type\":\"REF\",\"values\":[{\"id\":\"test\"}]}",
                BENode.class);
        assertEquals(expression, mapped);
    }

    @Test
    public void testDeserializeLowerCaseReference() throws Exception
    {
        final BEReferenceNode expression = new BEReferenceNode(Lists.newArrayList("test"));
        final BENode mapped = this.mapper.readValue(
                "{\"type\":\"ref\",\"values\":[{\"id\":\"test\"}]}",
                BENode.class);
        assertEquals(expression, mapped);
    }
}
