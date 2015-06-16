package com.amobee.freebee.solr;

import com.amobee.freebee.expression.BENode;

import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import static com.amobee.freebee.ExpressionUtil.*;
import static org.junit.Assert.assertEquals;

/**
 * @author Michael Bond
 */
public class BEToSolrQueryConverterTest
{
    private final BEToSolrQueryConverter converter = new BEToSolrQueryConverter(new TestAttributeMappingStoreImpl());

    @Test
    public void testSingleString_SingleInt()
    {
        final BENode expression = createExpression(
                exprConj("and",
                        expr("gender", "F"),
                        expr("age", "18")));

        assertEquals(" gender_s:\"F\" AND age_i:18", this.converter.convert(expression));
    }

    @Test
    public void testSingleString_SingleIntInfinity()
    {
        final BENode expression = createExpression(
                exprConj("and",
                        expr("gender", "F"),
                        expr("age", "18-")));

        assertEquals(" gender_s:\"F\" AND age_i:[18 TO *]", this.converter.convert(expression));
    }

    @Test
    public void testSingleString_SingleIntNegate()
    {
        final BENode expression = createExpression(
                exprConj("and",
                        expr("gender", "F"),
                        expr("age", true, "18")));

        assertEquals(" gender_s:\"F\" AND (*:* NOT age_i:18)", this.converter.convert(expression));
    }

    @Test
    public void testSingleString_SingleIntNegateOr()
    {
        final BENode expression = createExpression(
                exprConj("or",
                        expr("gender", "F"),
                        expr("age", true, "18")));

        assertEquals(" gender_s:\"F\" OR (*:* NOT age_i:18)", this.converter.convert(expression));
    }

    @Test
    public void testSingleString_MultiContiguousInt()
    {
        final BENode expression = createExpression(
                exprConj("and",
                        expr("gender", "F"),
                        expr("age", "18-24", "25-34")));

        assertEquals(" gender_s:\"F\" AND age_i:[18 TO 34]", this.converter.convert(expression));
    }

    @Test
    public void testSingleString_MultiNonContiguousInt()
    {
        final BENode expression = createExpression(
                exprConj("and",
                        expr("gender", "F"),
                        expr("age", "18-24", "35-44")));
        assertEquals(" gender_s:\"F\" AND (age_i:[18 TO 24] OR age_i:[35 TO 44])", this.converter.convert(expression));
    }

    @Test
    public void testMultiString_MultiNonContiguousInt()
    {
        final BENode expression = createExpression(
                exprConj("and",
                        expr("gender", "M", "F"),
                        expr("age", "18-24", "35-44")));
        assertEquals(
                " gender_s:( \"M\" \"F\") AND (age_i:[18 TO 24] OR age_i:[35 TO 44])",
                this.converter.convert(expression));
    }

    @Test
    public void testPredicate_Exclude()
    {
        final BENode expression = createExpression(
                exprConj("and",
                        expr("gender", "F"),
                        expr("age", "18-24", "25-34")));
        assertEquals(
                " gender_s:\"F\"",
                this.converter.convert(expression, o -> true, o -> !"age".equals(o.getType())));
    }

    @Test
    public void testAge_Include()
    {
        final BENode expression = createExpression(
                exprConj("and",
                        expr("gender", "F"),
                        expr("age", "18-24", "25-34")));
        assertEquals(
                " age_i:[18 TO 34]",
                this.converter.convert(expression, o -> true, o -> "age".equals(o.getType())));
    }

    @Test
    public void testGenderIncludeSingleNestedOperation()
    {
        final BENode expression = createExpression(
                exprConj("and",
                        expr("gender", "F"),
                        exprConj("or",
                                expr("age", "18-24"),
                                expr("education", "highschoolgraduate"))));
        assertEquals(
                " gender_s:\"F\"",
                this.converter.convert(expression, o -> true, o -> "gender".equals(o.getType())));
    }

    @Test
    public void testGenderIncludeMultipleNestedOperation()
    {
        final BENode expression = createExpression(
                exprConj("and",
                        expr("gender", "F"),
                        exprConj("or",
                                exprConj("or",
                                        expr("age", "18-24"),
                                        expr("education", "highschoolgraduate")
                                ))));

        assertEquals(" gender_s:\"F\"", this.converter.convert(expression, o -> true, o -> "gender".equals(o.getType())));
    }

    @Test
    public void testNestedOperations_OrOfAnds()
    {
        final BENode expression = createExpression(
                exprConj("or",
                        exprConj("and",
                                expr("age", "18"),
                                expr("gender", "F")),
                        exprConj("and",
                                expr("age", "18"),
                                expr("gender", "M"))));

        assertEquals(" ( age_i:18 AND gender_s:\"F\" ) OR ( age_i:18 AND gender_s:\"M\" )", this.converter.convert(expression));
    }

    @Test
    public void testNestedOperations_OrOfAndsOfOrs()
    {
        final BENode expression = createExpression(
                exprConj("or",
                        exprConj("and",
                                exprConj("or",
                                        expr("age", "18"),
                                        expr("gender", "F")),
                                exprConj("or",
                                        expr("behavioral", "ABC"),
                                        expr("behavioral", "XYZ"))),
                        expr("gender", "M")));

        assertEquals(" ( ( age_i:18 OR gender_s:\"F\" ) AND ( behavioral_s:\"ABC\" OR behavioral_s:\"XYZ\" ) ) OR gender_s:\"M\"",
                this.converter.convert(expression));
    }

    @Test
    public void testNestedOperationsNegate()
    {
        final BENode expression = createExpression(
                exprConj("or",
                        exprConj("and",
                                expr("age", "18"),
                                expr("gender", "F")),
                        exprConj("and",
                                expr("age", true, "18"),
                                expr("gender", "M"))));

        assertEquals(" ( age_i:18 AND gender_s:\"F\" ) OR ( (*:* NOT age_i:18) AND gender_s:\"M\" )", this.converter.convert(expression));
    }

    @Test
    public void testOperation_Exclude()
    {
        final BENode expression = createExpression(
                exprConj("and",
                        exprConj("or",
                                expr("age", "18"),
                                expr("gender", "F")),
                        exprConj("and",
                                expr("age", "18"),
                                expr("gender", "M"))));
        
        assertEquals(" ( age_i:18 AND gender_s:\"M\" )",
                this.converter.convert(expression, o -> !"or".equals(o.getType()), o -> true));
    }

    @Test
    public void testNestedOperationsExcludeOperation()
    {
        final BENode expression = createExpression(
                exprConj("and",
                        exprConj("or",
                                expr("age", "18"),
                                expr("gender", "F")),
                        exprConj("and",
                                expr("age", "18"),
                                expr("gender", "M"))));

        assertEquals(" ( age_i:18 AND gender_s:\"M\" )", this.converter.convert(expression, o -> !"or".equals(o.getType()), o -> true));
    }

    @Test
    public void testNestedOperationsExcludeOperationWithNegate()
    {
        final BENode expression = createExpression(
                exprConj("and",
                        exprConj("or",
                                expr("age", true,"18"),
                                expr("gender", "F")),
                        exprConj("and",
                                expr("age", "18"),
                                expr("gender", "M"))));

        assertEquals(" ( age_i:18 AND gender_s:\"M\" )", this.converter.convert(expression, o -> !"or".equals(o.getType()), o -> true));
    }

    @Test
    public void testNestedOperationsExcludePredicate()
    {
        final BENode expression = createExpression(
                exprConj("or",
                        exprConj("and",
                                expr("behavioral", "EX:ABC"),
                                expr("behavioral", "EX:XYZ")),
                        exprConj("and",
                                expr("age", "18"),
                                expr("gender", "M"))));

        assertEquals(" ( age_i:18 AND gender_s:\"M\" )", this.converter.convert(expression, o -> true, o -> !"behavioral".equals(o.getType())));
    }

    @Test
    public void testNestedOperationsExcludePredicateWithNegate()
    {
        final BENode expression = createExpression(
                exprConj("or",
                        exprConj("and",
                                expr("behavioral", true,"EX:ABC"),
                                expr("behavioral", "EX:XYZ")),
                        exprConj("and",
                                expr("age", "18"),
                                expr("gender", "M"))));

        assertEquals(" ( age_i:18 AND gender_s:\"M\" )",
                this.converter.convert(expression, o -> true, o -> !"behavioral".equals(o.getType())));
    }

    @Test
    public void testDaypart_SingleString()
    {

        final BENode expression = createExpression(
                "{\n" +
                        "    \"type\": \"and\",\n" +
                        "    \"values\": [\n" +
                        "        {\n" +
                        "            \"type\": \"daypart\",\n" +
                        "            \"values\": [\n" +
                        "                { \"id\": \"LF\", \"tz\": \"GMT\" }\n" +
                        "            ]\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}");
        assertEquals(
                " (hour_i:[0 TO 5] OR hour_i:23)",
                this.converter.convert(expression));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDaypart_StringRange()
    {
        final BENode expression = createExpression(
                "{\n" +
                        "    \"type\": \"and\",\n" +
                        "    \"values\": [\n" +
                        "        {\n" +
                        "            \"type\": \"daypart\",\n" +
                        "            \"values\": [\n" +
                        "                { \"id\": \"WKD-PRI\", \"tz\": \"GMT\" }\n" +
                        "            ]\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}");
        assertEquals(
                " hour_i:[10 TO 22]",
                this.converter.convert(expression));
    }

    @Test
    public void testDaypart_ContiguousString()
    {
        final BENode expression = createExpression(
                "{\n" +
                        "    \"type\": \"and\",\n" +
                        "    \"values\": [\n" +
                        "        {\n" +
                        "            \"type\": \"daypart\",\n" +
                        "            \"values\": [\n" +
                        "                { \"id\": \"WKM\", \"tz\": \"Canada/Saskatchewan\" },\n" +
                        "                { \"id\": \"WKD\", \"tz\": \"Canada/Saskatchewan\" }\n" +
                        "            ]\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}");
        assertEquals(
                " hour_i:[12 TO 23]",
                this.converter.convert(expression));
    }

    @Test
    public void testDaypart_OverlapString()
    {
        // WKD uses a different timezone than WED so that WKD does not complete encapsulate WED
        // the goal of this test is to ensure that when two dayparts overlap, they are combined
        // into a single range.
        final BENode expression = createExpression(
                "{\n" +
                        "    \"type\": \"and\",\n" +
                        "    \"values\": [\n" +
                        "        {\n" +
                        "            \"type\": \"daypart\",\n" +
                        "            \"values\": [\n" +
                        "                { \"id\": \"WED\", \"tz\": \"GMT\" },\n" +
                        "                { \"id\": \"WKD\", \"tz\": \"GMT-05:00\" }\n" +
                        "            ]\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}");
        assertEquals(
                " hour_i:[6 TO 22]",
                this.converter.convert(expression));
    }

    @Test
    public void testDaypart_NonContiguousString()
    {
        final BENode expression = createExpression(
                "{\n" +
                        "    \"type\": \"and\",\n" +
                        "    \"values\": [\n" +
                        "        {\n" +
                        "            \"type\": \"daypart\",\n" +
                        "            \"values\": [\n" +
                        "                { \"id\": \"WKM\", \"tz\": \"GMT\" },\n" +
                        "                { \"id\": \"PRI\", \"tz\": \"GMT\" }\n" +
                        "            ]\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}");
        assertEquals(
                " (hour_i:[6 TO 9] OR hour_i:[20 TO 22])",
                this.converter.convert(expression));
    }

    @Test
    public void testDaypart_SingleInt()
    {
        final BENode expression = createExpression(
                "{\n" +
                        "    \"type\": \"and\",\n" +
                        "    \"values\": [\n" +
                        "        {\n" +
                        "            \"type\": \"daypart\",\n" +
                        "            \"values\": [\n" +
                        "                { \"id\": \"17-22\", \"tz\": \"GMT+05:00\" }\n" +
                        "            ]\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}");
        assertEquals(
                " hour_i:[12 TO 17]",
                this.converter.convert(expression));
    }

    @Test
    public void testDaypart_ContiguousInt()
    {
        final BENode expression = createExpression(
                "{\n" +
                        "    \"type\": \"and\",\n" +
                        "    \"values\": [\n" +
                        "        {\n" +
                        "            \"type\": \"daypart\",\n" +
                        "            \"values\": [\n" +
                        "                { \"id\": \"10-16\", \"tz\": \"GMT\" },\n" +
                        "                { \"id\": \"17-22\", \"tz\": \"GMT\" }\n" +
                        "            ]\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}");
        assertEquals(
                " hour_i:[10 TO 22]",
                this.converter.convert(expression));
    }

    @Test
    public void testDaypart_NonContiguousInt()
    {
        final BENode expression = createExpression(
                "{\n" +
                        "    \"type\": \"and\",\n" +
                        "    \"values\": [\n" +
                        "        {\n" +
                        "            \"type\": \"daypart\",\n" +
                        "            \"values\": [\n" +
                        "                { \"id\": \"6-9\", \"tz\": \"GMT\" },\n" +
                        "                { \"id\": \"17-22\", \"tz\": \"GMT\" }\n" +
                        "            ]\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}");
        assertEquals(
                " (hour_i:[6 TO 9] OR hour_i:[17 TO 22])",
                this.converter.convert(expression));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDaypart_MixedStringInt()
    {
        final BENode expression = createExpression(
                "{\n" +
                        "    \"type\": \"and\",\n" +
                        "    \"values\": [\n" +
                        "        {\n" +
                        "            \"type\": \"daypart\",\n" +
                        "            \"values\": [\n" +
                        "                { \"id\": \"WKM-22\", \"tz\": \"GMT\" }\n" +
                        "            ]\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}");
        assertEquals(
                " hour_i:[17 TO 22]",
                this.converter.convert(expression));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDaypart_MixedIntString()
    {
        final BENode expression = createExpression(
                "{\n" +
                        "    \"type\": \"and\",\n" +
                        "    \"values\": [\n" +
                        "        {\n" +
                        "            \"type\": \"daypart\",\n" +
                        "            \"values\": [\n" +
                        "                { \"id\": \"10-PRI\", \"tz\": \"GMT\" }\n" +
                        "            ]\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}");
        assertEquals(
                " hour_i:[17 TO 22]",
                this.converter.convert(expression));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDaypart_NoTimezone()
    {
        final BENode expression = createExpression(
                "{\n" +
                        "    \"type\": \"and\",\n" +
                        "    \"values\": [\n" +
                        "        {\n" +
                        "            \"type\": \"daypart\",\n" +
                        "            \"values\": [\n" +
                        "                { \"id\": \"WKM\" }\n" +
                        "            ]\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}");
        assertEquals(
                " hour_i:[17 TO 22]",
                this.converter.convert(expression));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDaypart_InvalidTimezone()
    {
        final BENode expression = createExpression(
                "{\n" +
                        "    \"type\": \"and\",\n" +
                        "    \"values\": [\n" +
                        "        {\n" +
                        "            \"type\": \"daypart\",\n" +
                        "            \"values\": [\n" +
                        "                { \"id\": \"WKM\", \"tz\": \"ABCEFG\" }\n" +
                        "            ]\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}");
        assertEquals(
                " hour_i:[6 TO 9]",
                this.converter.convert(expression));
    }

    @Test
    public void testPredicateOnly()
    {
        final BENode expression = createExpression(
                "{\n" +
                        "    \"type\": \"behavioral\",\n" +
                        "    \"values\": [ { \"id\": \"EX:ABC\" } ]\n" +
                        "}");
        assertEquals(" behavioral_s:\"EX:ABC\"", this.converter.convert(expression));
    }

    @Test
    public void testSingleString_SingleIntWithBrackets()
    {
        final BENode expression = createExpression(
                exprConj("and",
                        expr("gender", "F"),
                        expr("age", "[18]")));
        assertEquals(" gender_s:\"F\" AND age_i:18", this.converter.convert(expression));
    }

    @Test
    public void testSingleString_SingleIntInfinityWithBrackets()
    {
        final BENode expression = createExpression(
                exprConj("and",
                        expr("gender", "F"),
                        expr("age", "[18,]")));
        assertEquals(" gender_s:\"F\" AND age_i:[18 TO *]", this.converter.convert(expression));
    }

    @Test
    public void testSingleString_MultiContiguousIntWithBrackets()
    {
        final BENode expression = createExpression(
                exprConj("and",
                        expr("gender", "F"),
                        expr("age", "[18,24]", "[25,34]")));

        assertEquals(" gender_s:\"F\" AND age_i:[18 TO 34]", this.converter.convert(expression));
    }

    @Test
    public void testSingleString_MultiNonContiguousIntWithBrackets()
    {
        final BENode expression = createExpression(
                exprConj("and",
                        expr("gender", "F"),
                        expr("age", "[18,24]", "[35,44]")));

        assertEquals(" gender_s:\"F\" AND (age_i:[18 TO 24] OR age_i:[35 TO 44])", this.converter.convert(expression));
    }

    @Test
    public void testStringNotPresent() throws Exception
    {
        final BENode expression = createExpression(
                exprConj("and",
                        expr("behavioral")));

        assertEquals(" (*:* -behavioral_s:[* TO *])", this.converter.convert(expression));
    }

    private static class TestAttributeMappingStoreImpl implements BESolrAttributeMappingStore
    {
        private final Map<String, BESolrField> attrToFieldMap = ImmutableMap.<String, BESolrField>builder()
                .put("age", new BESolrField("age_i", int.class))
                .put("behavioral", new BESolrField("behavioral_s", String.class))
                .put("gender", new BESolrField("gender_s", String.class))
                .put("daypart", new BESolrField("hour_i", new BESolrDaypartConverter(new BESolrDaypartRangeStoreImpl())))
                .build();

        protected TestAttributeMappingStoreImpl()
        {
        }

        @Override
        @Nullable
        public BESolrField getSolrField(@Nonnull final String attribute)
        {
            return this.attrToFieldMap.get(attribute.toLowerCase());
        }
    }

}