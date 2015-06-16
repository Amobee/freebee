package com.amobee.freebee.evaluator;

import java.util.BitSet;

import com.amobee.freebee.evaluator.evaluator.BEInput;
import com.amobee.freebee.evaluator.index.BEIndex;
import com.amobee.freebee.evaluator.index.BEIndexBuilder;
import org.eclipse.collections.api.map.primitive.IntObjectMap;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Michael Bond
 */
public class BEIndexTest
{
    @Test
    public void testSingleExpression_MatchNegativeSet() throws Exception
    {
        final BEIndexBuilder<String> builder = new BEIndexBuilder<>();
        builder.addDataTypeConfig("[{\"type\":\"dma\",\"dataType\":\"int\"},{\"type\":\"age\",\"dataType\":\"byte\",\"range\":true}]");
        builder.addExpression(
                "test1",
                "{\n" +
                        "    \"type\": \"and\",\n" +
                        "    \"values\": [\n" +
                        "        {\n" +
                        "            \"type\": \"or\",\n" +
                        "            \"values\": [\n" +
                        "                {\n" +
                        "                    \"type\": \"gender\",\n" +
                        "                    \"values\": [\n" +
                        "                        {\n" +
                        "                            \"id\": \"M\"\n" +
                        "                        },\n" +
                        "                        {\n" +
                        "                            \"id\": \"F\"\n" +
                        "                        }\n" +
                        "                    ]\n" +
                        "                },\n" +
                        "                {\n" +
                        "                    \"type\": \"age\",\n" +
                        "                    \"values\": [\n" +
                        "                        {\n" +
                        "                            \"id\": \"[18,34)\"\n" +
                        "                        }\n" +
                        "                    ]\n" +
                        "                }\n" +
                        "            ]\n" +
                        "        },\n" +
                        "        {\n" +
                        "            \"type\": \"country\",\n" +
                        "            \"values\": [\n" +
                        "                {\n" +
                        "                    \"id\": \"US\"\n" +
                        "                }\n" +
                        "            ]\n" +
                        "        },\n" +
                        "        {\n" +
                        "            \"type\": \"domain\",\n" +
                        "            \"negative\": true,\n" +
                        "            \"values\": [\n" +
                        "                {\n" +
                        "                    \"id\": \"baddomain.com\"\n" +
                        "                }\n" +
                        "            ]\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}");

        final BEIndex index = builder.build();
        // gender: bits {0, 1}
        // age: bits {0, 1}
        // country: bits {2}
        // domain: bits {3}

        final BEInput input = new BEInput();
        input.getOrCreateStringCategory("age").add("[18,24)");
        input.getOrCreateStringCategory("gender").add("M");
        input.getOrCreateStringCategory("country").add("US");
        input.getOrCreateStringCategory("domain").add("bar.com");
        input.getOrCreateIntCategory("dma").add(1234);

        final IntObjectMap<BitSet> result = index.getIndexResult(input);

        assertEquals(1, result.size());
        final BitSet bitSet = new BitSet();
        bitSet.set(0, 4);
        assertEquals(bitSet, result.get(0));
    }

    @Test
    public void testSingleExpression_NoMatchNegativeSet() throws Exception
    {
        final BEIndexBuilder<String> builder = new BEIndexBuilder<>();
        builder.addDataTypeConfig("[{\"type\":\"dma\",\"dataType\":\"int\"},{\"type\":\"age\",\"dataType\":\"byte\",\"range\":true}]");
        builder.addExpression(
                "test1",
                "{\n" +
                        "    \"type\": \"and\",\n" +
                        "    \"values\": [\n" +
                        "        {\n" +
                        "            \"type\": \"or\",\n" +
                        "            \"values\": [\n" +
                        "                {\n" +
                        "                    \"type\": \"gender\",\n" +
                        "                    \"values\": [\n" +
                        "                        {\n" +
                        "                            \"id\": \"M\"\n" +
                        "                        },\n" +
                        "                        {\n" +
                        "                            \"id\": \"F\"\n" +
                        "                        }\n" +
                        "                    ]\n" +
                        "                },\n" +
                        "                {\n" +
                        "                    \"type\": \"age\",\n" +
                        "                    \"values\": [\n" +
                        "                        {\n" +
                        "                            \"id\": \"[18,34)\"\n" +
                        "                        }\n" +
                        "                    ]\n" +
                        "                }\n" +
                        "            ]\n" +
                        "        },\n" +
                        "        {\n" +
                        "            \"type\": \"country\",\n" +
                        "            \"values\": [\n" +
                        "                {\n" +
                        "                    \"id\": \"US\"\n" +
                        "                }\n" +
                        "            ]\n" +
                        "        },\n" +
                        "        {\n" +
                        "            \"type\": \"domain\",\n" +
                        "            \"negative\": true,\n" +
                        "            \"values\": [\n" +
                        "                {\n" +
                        "                    \"id\": \"baddomain.com\"\n" +
                        "                }\n" +
                        "            ]\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}");

        final BEIndex index = builder.build();
        // gender: bits {0, 1}
        // age: bits {0, 1}
        // country: bits {2}
        // domain: bits {3}

        final BEInput input = new BEInput();
        input.getOrCreateStringCategory("age").add("[18,24)");
        input.getOrCreateStringCategory("gender").add("M");
        input.getOrCreateStringCategory("country").add("US");
        input.getOrCreateStringCategory("domain").add("baddomain.com");
        input.getOrCreateIntCategory("dma").add(1234);

        final IntObjectMap<BitSet> result = index.getIndexResult(input);

        assertEquals(1, result.size());
        final BitSet bitSet = new BitSet();
        bitSet.set(0, 3);
        assertEquals(bitSet, result.get(0));
    }

    @Test
    public void testSingleExpression_NoMatchPositiveSet() throws Exception
    {
        final BEIndexBuilder<String> builder = new BEIndexBuilder<>();
        builder.addDataTypeConfig("[{\"type\":\"dma\",\"dataType\":\"int\"},{\"type\":\"age\",\"dataType\":\"byte\",\"range\":true}]");
        builder.addExpression(
                "test1",
                "{\n" +
                        "    \"type\": \"and\",\n" +
                        "    \"values\": [\n" +
                        "        {\n" +
                        "            \"type\": \"or\",\n" +
                        "            \"values\": [\n" +
                        "                {\n" +
                        "                    \"type\": \"gender\",\n" +
                        "                    \"values\": [\n" +
                        "                        {\n" +
                        "                            \"id\": \"M\"\n" +
                        "                        },\n" +
                        "                        {\n" +
                        "                            \"id\": \"F\"\n" +
                        "                        }\n" +
                        "                    ]\n" +
                        "                },\n" +
                        "                {\n" +
                        "                    \"type\": \"age\",\n" +
                        "                    \"values\": [\n" +
                        "                        {\n" +
                        "                            \"id\": \"[18,34)\"\n" +
                        "                        }\n" +
                        "                    ]\n" +
                        "                }\n" +
                        "            ]\n" +
                        "        },\n" +
                        "        {\n" +
                        "            \"type\": \"country\",\n" +
                        "            \"values\": [\n" +
                        "                {\n" +
                        "                    \"id\": \"US\"\n" +
                        "                }\n" +
                        "            ]\n" +
                        "        },\n" +
                        "        {\n" +
                        "            \"type\": \"domain\",\n" +
                        "            \"negative\": true,\n" +
                        "            \"values\": [\n" +
                        "                {\n" +
                        "                    \"id\": \"baddomain.com\"\n" +
                        "                }\n" +
                        "            ]\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}");

        final BEIndex index = builder.build();
        // gender: bits {0, 1}
        // age: bits {0, 1}
        // country: bits {2}
        // domain: bits {3}

        final BEInput input = new BEInput();
        input.getOrCreateStringCategory("age").add("[18,24)");
        input.getOrCreateStringCategory("gender").add("M");
        input.getOrCreateStringCategory("country").add("CA");
        input.getOrCreateStringCategory("domain").add("baddomain.com");
        input.getOrCreateIntCategory("dma").add(1234);

        final IntObjectMap<BitSet> result = index.getIndexResult(input);

        assertEquals(1, result.size());
        final BitSet bitSet = new BitSet();
        bitSet.set(0, 2);
        assertEquals(bitSet, result.get(0));
    }

    @Test
    public void testMultiExpression() throws Exception
    {
        final BEIndexBuilder<String> builder = new BEIndexBuilder<>();
        builder.addDataTypeConfig("[{\"type\":\"dma\",\"dataType\":\"int\"},{\"type\":\"age\",\"dataType\":\"byte\",\"range\":true}]");
        builder.addExpression(
                "test1",
                "{\n" +
                        "    \"type\": \"and\",\n" +
                        "    \"values\": [\n" +
                        "        {\n" +
                        "            \"type\": \"or\",\n" +
                        "            \"values\": [\n" +
                        "                {\n" +
                        "                    \"type\": \"gender\",\n" +
                        "                    \"values\": [\n" +
                        "                        {\n" +
                        "                            \"id\": \"M\"\n" +
                        "                        },\n" +
                        "                        {\n" +
                        "                            \"id\": \"F\"\n" +
                        "                        }\n" +
                        "                    ]\n" +
                        "                },\n" +
                        "                {\n" +
                        "                    \"type\": \"age\",\n" +
                        "                    \"values\": [\n" +
                        "                        {\n" +
                        "                            \"id\": \"[18,34)\"\n" +
                        "                        }\n" +
                        "                    ]\n" +
                        "                }\n" +
                        "            ]\n" +
                        "        },\n" +
                        "        {\n" +
                        "            \"type\": \"country\",\n" +
                        "            \"values\": [\n" +
                        "                {\n" +
                        "                    \"id\": \"US\"\n" +
                        "                }\n" +
                        "            ]\n" +
                        "        },\n" +
                        "        {\n" +
                        "            \"type\": \"domain\",\n" +
                        "            \"negative\": true,\n" +
                        "            \"values\": [\n" +
                        "                {\n" +
                        "                    \"id\": \"baddomain.com\"\n" +
                        "                }\n" +
                        "            ]\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}");
        builder.addExpression(
                "test2",
                "{\n" +
                        "    \"type\": \"and\",\n" +
                        "    \"values\": [\n" +
                        "        {\n" +
                        "            \"type\": \"or\",\n" +
                        "            \"values\": [\n" +
                        "                {\n" +
                        "                    \"type\": \"gender\",\n" +
                        "                    \"values\": [\n" +
                        "                        {\n" +
                        "                            \"id\": \"F\"\n" +
                        "                        }\n" +
                        "                    ]\n" +
                        "                },\n" +
                        "                {\n" +
                        "                    \"type\": \"age\",\n" +
                        "                    \"values\": [\n" +
                        "                        {\n" +
                        "                            \"id\": \"[18,34)\"\n" +
                        "                        }\n" +
                        "                    ]\n" +
                        "                }\n" +
                        "            ]\n" +
                        "        },\n" +
                        "        {\n" +
                        "            \"type\": \"country\",\n" +
                        "            \"values\": [\n" +
                        "                {\n" +
                        "                    \"id\": \"US\"\n" +
                        "                }\n" +
                        "            ]\n" +
                        "        },\n" +
                        "        {\n" +
                        "            \"type\": \"domain\",\n" +
                        "            \"negative\": true,\n" +
                        "            \"values\": [\n" +
                        "                {\n" +
                        "                    \"id\": \"baddomain.com\"\n" +
                        "                }\n" +
                        "            ]\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}");
        builder.addExpression(
                "test3",
                "{\n" +
                        "    \"type\": \"and\",\n" +
                        "    \"values\": [\n" +
                        "        {\n" +
                        "            \"type\": \"or\",\n" +
                        "            \"values\": [\n" +
                        "                {\n" +
                        "                    \"type\": \"gender\",\n" +
                        "                    \"values\": [\n" +
                        "                        {\n" +
                        "                            \"id\": \"M\"\n" +
                        "                        },\n" +
                        "                        {\n" +
                        "                            \"id\": \"F\"\n" +
                        "                        }\n" +
                        "                    ]\n" +
                        "                },\n" +
                        "                {\n" +
                        "                    \"type\": \"age\",\n" +
                        "                    \"values\": [\n" +
                        "                        {\n" +
                        "                            \"id\": \"[18,34)\"\n" +
                        "                        }\n" +
                        "                    ]\n" +
                        "                }\n" +
                        "            ]\n" +
                        "        },\n" +
                        "        {\n" +
                        "            \"type\": \"country\",\n" +
                        "            \"values\": [\n" +
                        "                {\n" +
                        "                    \"id\": \"US\"\n" +
                        "                }\n" +
                        "            ]\n" +
                        "        },\n" +
                        "        {\n" +
                        "            \"type\": \"domain\",\n" +
                        "            \"values\": [\n" +
                        "                {\n" +
                        "                    \"id\": \"baddomain.com\"\n" +
                        "                }\n" +
                        "            ]\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}");
        builder.addExpression(
                "test4",
                "{\n" +
                        "    \"type\": \"and\",\n" +
                        "    \"values\": [\n" +
                        "        {\n" +
                        "            \"type\": \"or\",\n" +
                        "            \"values\": [\n" +
                        "                {\n" +
                        "                    \"type\": \"gender\",\n" +
                        "                    \"values\": [\n" +
                        "                        {\n" +
                        "                            \"id\": \"F\"\n" +
                        "                        }\n" +
                        "                    ]\n" +
                        "                },\n" +
                        "                {\n" +
                        "                    \"type\": \"age\",\n" +
                        "                    \"values\": [\n" +
                        "                        {\n" +
                        "                            \"id\": \"[18,34)\"\n" +
                        "                        }\n" +
                        "                    ]\n" +
                        "                }\n" +
                        "            ]\n" +
                        "        },\n" +
                        "        {\n" +
                        "            \"type\": \"country\",\n" +
                        "            \"values\": [\n" +
                        "                {\n" +
                        "                    \"id\": \"US\"\n" +
                        "                }\n" +
                        "            ]\n" +
                        "        },\n" +
                        "        {\n" +
                        "            \"type\": \"domain\",\n" +
                        "            \"values\": [\n" +
                        "                {\n" +
                        "                    \"id\": \"baddomain.com\"\n" +
                        "                }\n" +
                        "            ]\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}");

        final BEIndex index = builder.build();
        // gender: bits {0, 1}
        // age: bits {0, 1}
        // country: bits {2}
        // domain: bits {3}

        final BEInput input = new BEInput();
        input.getOrCreateStringCategory("age").add("[18,24)");
        input.getOrCreateStringCategory("gender").add("M");
        input.getOrCreateStringCategory("country").add("US");
        input.getOrCreateStringCategory("domain").add("bar.com");
        input.getOrCreateIntCategory("dma").add(1234);

        final IntObjectMap<BitSet> result = index.getIndexResult(input);

        assertEquals(4, result.size());
        final BitSet bitSet = new BitSet();
        bitSet.set(0, 4);
        assertEquals(bitSet, result.get(0));
        assertEquals(bitSet, result.get(1));
        bitSet.clear();
        bitSet.set(0, 3);
        assertEquals(bitSet, result.get(2));
        assertEquals(bitSet, result.get(3));
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidReferenceWithinPartial() throws Exception
    {
        final BEIndexBuilder<String> builder = new BEIndexBuilder<>();
        builder.addPartialExpression("{\"id\":\"test1\",\"type\":\"test\",\"values\":[\"value1\", \"value2\"]}");
        builder.addPartialExpression("{\"id\":\"test2\",\"type\":\"ref\",\"values\":[\"test1\"]}");
        builder.build();
    }

    @Test
    public void testRemoveExpressions() throws Exception
    {
        final BEIndexBuilder<String> builder = new BEIndexBuilder<>();
        builder.addExpression("test1", "{\"type\":\"and\",\"values\":[{\"type\":\"format\",\"values\":[{\"id\":\"VIDEO\"}]},{\"type\":\"and\",\"values\":[{\"type\":\"gender\",\"values\":[{\"id\":\"M\"}]}]}]}}");
        builder.addExpression("test2", "{\"type\":\"and\",\"values\":[{\"type\":\"format\",\"values\":[{\"id\":\"DISPLAY\"}]},{\"type\":\"and\",\"values\":[{\"type\":\"gender\",\"values\":[{\"id\":\"M\"}]}]}]}}");

        builder.removeExpressions("test1");

        final BEIndex index = builder.build();

        // only matches gender input criteria
        BEInput input = new BEInput();
        input.getOrCreateStringCategory("format").add("VIDEO");
        input.getOrCreateStringCategory("gender").add("M");

        IntObjectMap<BitSet> result = index.getIndexResult(input);
        assertEquals(1, result.size());
        BitSet bitSet = new BitSet();
        bitSet.set(1);
        assertEquals(bitSet, result.get(0));

        // matches both input criteria
        input = new BEInput();
        input.getOrCreateStringCategory("format").add("DISPLAY");
        input.getOrCreateStringCategory("gender").add("M");

        result = index.getIndexResult(input);
        assertEquals(1, result.size());
        bitSet = new BitSet();
        bitSet.set(0, 2);
        assertEquals(bitSet, result.get(0));
    }

    @Test
    public void testRemoveExpressions_multipleExpressions() throws Exception
    {
        final BEIndexBuilder<String> builder = new BEIndexBuilder<>();
        builder.addExpression("test1", "{\"type\":\"and\",\"values\":[{\"type\":\"format\",\"values\":[{\"id\":\"VIDEO\"}]},{\"type\":\"and\",\"values\":[{\"type\":\"gender\",\"values\":[{\"id\":\"M\"}]}]}]}}");
        builder.addExpression("test1", "{\"type\":\"and\",\"values\":[{\"type\":\"format\",\"values\":[{\"id\":\"DISPLAY\"}]},{\"type\":\"and\",\"values\":[{\"type\":\"gender\",\"values\":[{\"id\":\"F\"}]}]}]}}");
        builder.addExpression("test2", "{\"type\":\"and\",\"values\":[{\"type\":\"format\",\"values\":[{\"id\":\"DISPLAY\"}]},{\"type\":\"and\",\"values\":[{\"type\":\"gender\",\"values\":[{\"id\":\"M\"}]}]}]}}");

        builder.removeExpressions("test1");

        final BEIndex index = builder.build();

        // only matches gender input criteria
        BEInput input = new BEInput();
        input.getOrCreateStringCategory("format").add("VIDEO");
        input.getOrCreateStringCategory("gender").add("M");

        IntObjectMap<BitSet> result = index.getIndexResult(input);
        assertEquals(1, result.size());
        BitSet bitSet = new BitSet();
        bitSet.set(1);
        assertEquals(bitSet, result.get(0));

        // only matches format input criteria
        input = new BEInput();
        input.getOrCreateStringCategory("format").add("DISPLAY");
        input.getOrCreateStringCategory("gender").add("F");

        result = index.getIndexResult(input);
        assertEquals(1, result.size());
        bitSet = new BitSet();
        bitSet.set(0);
        assertEquals(bitSet, result.get(0));

        // matches both input criteria
        input = new BEInput();
        input.getOrCreateStringCategory("format").add("DISPLAY");
        input.getOrCreateStringCategory("gender").add("M");

        result = index.getIndexResult(input);
        assertEquals(1, result.size());
        bitSet = new BitSet();
        bitSet.set(0, 2);
        assertEquals(bitSet, result.get(0));
    }

    @Test
    public void testRemoveExpressions_notFound() throws Exception
    {
        final BEIndexBuilder<String> builder = new BEIndexBuilder<>();
        builder.addExpression("test1", "{\"type\":\"and\",\"values\":[{\"type\":\"format\",\"values\":[{\"id\":\"VIDEO\"}]},{\"type\":\"and\",\"values\":[{\"type\":\"gender\",\"values\":[{\"id\":\"M\"}]}]}]}}");
        builder.addExpression("test2", "{\"type\":\"and\",\"values\":[{\"type\":\"format\",\"values\":[{\"id\":\"DISPLAY\"}]},{\"type\":\"and\",\"values\":[{\"type\":\"gender\",\"values\":[{\"id\":\"M\"}]}]}]}}");

        builder.removeExpressions("test3");

        final BEIndex index = builder.build();

        BEInput input = new BEInput();
        input.getOrCreateStringCategory("format").add("VIDEO");
        input.getOrCreateStringCategory("gender").add("M");
        IntObjectMap<BitSet> result = index.getIndexResult(input);
        assertEquals(2, result.size());
        BitSet bitSet = new BitSet();
        bitSet.set(0, 2);
        assertEquals(bitSet, result.get(0));
        bitSet = new BitSet();
        bitSet.set(1);
        assertEquals(bitSet, result.get(1));

        input = new BEInput();
        input.getOrCreateStringCategory("format").add("DISPLAY");
        input.getOrCreateStringCategory("gender").add("M");
        result = index.getIndexResult(input);
        assertEquals(2, result.size());
        bitSet = new BitSet();
        bitSet.set(1);
        assertEquals(bitSet, result.get(0));
        bitSet = new BitSet();
        bitSet.set(0, 2);
        assertEquals(bitSet, result.get(1));
    }

    @Test
    public void testRemovePartialExpression() throws Exception
    {
        final BEIndexBuilder<String> builder = new BEIndexBuilder<>();
        builder.addPartialExpression("test1", "{\"type\":\"and\",\"values\":[{\"type\":\"format\",\"values\":[{\"id\":\"VIDEO\"}]},{\"type\":\"and\",\"values\":[{\"type\":\"gender\",\"values\":[{\"id\":\"M\"}]}]}]}}");
        builder.addPartialExpression("test2", "{\"type\":\"and\",\"values\":[{\"type\":\"format\",\"values\":[{\"id\":\"DISPLAY\"}]},{\"type\":\"and\",\"values\":[{\"type\":\"gender\",\"values\":[{\"id\":\"M\"}]}]}]}}");

        builder.removePartialExpression("test1");

        final BEIndex index = builder.build();

        BEInput input = new BEInput();
        input.getOrCreateStringCategory("format").add("VIDEO");
        input.getOrCreateStringCategory("gender").add("M");
        IntObjectMap<BitSet> result = index.getIndexResult(input);
        assertEquals(1, result.size());
        BitSet bitSet = new BitSet();
        bitSet.set(1);
        assertEquals(bitSet, result.get(0));

        input = new BEInput();
        input.getOrCreateStringCategory("format").add("DISPLAY");
        input.getOrCreateStringCategory("gender").add("M");
        result = index.getIndexResult(input);
        assertEquals(1, result.size());
        bitSet = new BitSet();
        bitSet.set(0, 2);
        assertEquals(bitSet, result.get(0));
    }

    @Test
    public void testRemovePartialExpression_notFound() throws Exception
    {
        final BEIndexBuilder<String> builder = new BEIndexBuilder<>();
        builder.addPartialExpression("test1", "{\"type\":\"and\",\"values\":[{\"type\":\"format\",\"values\":[{\"id\":\"VIDEO\"}]},{\"type\":\"and\",\"values\":[{\"type\":\"gender\",\"values\":[{\"id\":\"M\"}]}]}]}}");
        builder.addPartialExpression("test2", "{\"type\":\"and\",\"values\":[{\"type\":\"format\",\"values\":[{\"id\":\"DISPLAY\"}]},{\"type\":\"and\",\"values\":[{\"type\":\"gender\",\"values\":[{\"id\":\"M\"}]}]}]}}");

        builder.removePartialExpression("test3");

        final BEIndex index = builder.build();

        BEInput input = new BEInput();
        input.getOrCreateStringCategory("format").add("VIDEO");
        input.getOrCreateStringCategory("gender").add("M");
        IntObjectMap<BitSet> result = index.getIndexResult(input);
        assertEquals(2, result.size());
        BitSet bitSet = new BitSet();
        bitSet.set(1);
        assertEquals(bitSet, result.get(0));
        bitSet = new BitSet();
        bitSet.set(0, 2);
        assertEquals(bitSet, result.get(1));

        input = new BEInput();
        input.getOrCreateStringCategory("format").add("DISPLAY");
        input.getOrCreateStringCategory("gender").add("M");
        result = index.getIndexResult(input);
        assertEquals(2, result.size());
        bitSet = new BitSet();
        bitSet.set(0, 2);
        assertEquals(bitSet, result.get(0));
        bitSet = new BitSet();
        bitSet.set(1);
        assertEquals(bitSet, result.get(1));
    }
}