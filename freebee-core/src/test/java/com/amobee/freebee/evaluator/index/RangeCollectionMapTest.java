package com.amobee.freebee.evaluator.index;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.TreeRangeMap;
import org.junit.Test;

import static com.google.common.collect.BoundType.CLOSED;
import static com.google.common.collect.BoundType.OPEN;
import static com.google.common.collect.Range.range;
import static org.junit.Assert.*;

@SuppressWarnings({"UnstableApiUsage", "ArraysAsListWithZeroOrOneArgument"})
public class RangeCollectionMapTest
{
    /*  {
     *    [0..1): ["A", "B", "C"],
     *    [1..2]: ["C", "D"],
     *    [4..5]: ["E"],
     *    [7..9]: ["F"]
     *  }
     */
    private static final RangeMap<Integer, Collection<String>> PREPOPULATED_RANGE_MAP = TreeRangeMap.create();
    static {
        PREPOPULATED_RANGE_MAP.put(range(0, CLOSED, 1, OPEN),   Arrays.asList("A", "B", "C"));
        PREPOPULATED_RANGE_MAP.put(range(1, CLOSED, 2, CLOSED),   Arrays.asList("C", "D"));
        PREPOPULATED_RANGE_MAP.put(range(4, CLOSED, 5, CLOSED), Arrays.asList("E"));
        PREPOPULATED_RANGE_MAP.put(range(7, CLOSED, 9, CLOSED), Arrays.asList("F"));
    }

    @Test
    public void testGet()
    {
        // Given these preconditions
        final RangeCollectionMap<Integer, String> rangeCollectionMap =
                new RangeCollectionMapImpl<>(
                        ArrayList::new,
                        () -> PREPOPULATED_RANGE_MAP);

        // Expect these results
        assertNull(rangeCollectionMap.get(-1));
        assertEquals(Arrays.asList("A", "B", "C"), rangeCollectionMap.get(0));
        assertEquals(Arrays.asList("C", "D"), rangeCollectionMap.get(1));
        assertEquals(Arrays.asList("C", "D"), rangeCollectionMap.get(2));
        assertNull(rangeCollectionMap.get(3));
        assertEquals(Arrays.asList("E"), rangeCollectionMap.get(4));
        assertEquals(Arrays.asList("E"), rangeCollectionMap.get(5));
        assertNull(rangeCollectionMap.get(6));
        assertEquals(Arrays.asList("F"), rangeCollectionMap.get(7));
        assertEquals(Arrays.asList("F"), rangeCollectionMap.get(8));
        assertEquals(Arrays.asList("F"), rangeCollectionMap.get(9));
        assertNull(rangeCollectionMap.get(10));

    }

    public void testGetRange()
    {
        // Given these preconditions
        final RangeCollectionMap<Integer, String> rangeCollectionMap =
                new RangeCollectionMapImpl<>(
                        ArrayList::new,
                        () -> PREPOPULATED_RANGE_MAP);

        // Expect these results
        assertNull(rangeCollectionMap.get(range(-2, CLOSED, -1, CLOSED)));
        assertEquals(Arrays.asList("C", "D", "E"), rangeCollectionMap.get(range(1, CLOSED, 7, OPEN)));
        assertEquals(Arrays.asList("C", "D", "E", "F"), rangeCollectionMap.get(range(1, CLOSED, 7, CLOSED)));
        assertNull(rangeCollectionMap.get(range(10, CLOSED, 11, CLOSED)));

    }

    @Test
    public void testPutAdd()
    {
        // Given
        final RangeCollectionMap<Integer, String> rangeCollectionMap = new RangeCollectionMapImpl<>();

        // Act
        PREPOPULATED_RANGE_MAP.asMapOfRanges().forEach((range, values) ->
                values.forEach(v -> rangeCollectionMap.putAdd(range, v)));

        // Assert
        assertNull(rangeCollectionMap.get(-1));
        assertEquals(Arrays.asList("A", "B", "C"), rangeCollectionMap.get(0));
        assertEquals(Arrays.asList("C", "D"), rangeCollectionMap.get(1));
        assertEquals(Arrays.asList("C", "D"), rangeCollectionMap.get(1));
        assertNull(rangeCollectionMap.get(3));
        assertEquals(Arrays.asList("E"), rangeCollectionMap.get(4));
        assertEquals(Arrays.asList("E"), rangeCollectionMap.get(5));
        assertNull(rangeCollectionMap.get(6));
        assertEquals(Arrays.asList("F"), rangeCollectionMap.get(7));
        assertEquals(Arrays.asList("F"), rangeCollectionMap.get(8));
        assertEquals(Arrays.asList("F"), rangeCollectionMap.get(9));
        assertNull(rangeCollectionMap.get(10));

        /*
         * Using the above preconditions, putAdd([2..8], "X") should result in:
         *
         *     {
         *        [0..1): ["A", "B", "C"],
         *        [1..2): ["C", "D"],
         *        [2, 2]: ["C", "D", "X"],
         *        (2..4): ["X"],
         *        [4..5]: ["E", "X"],
         *        (5..7): ["X"],
         *        [7..8]: ["F", "X"],
         *        (8..9]: ["F"]
         *     }
         */
        rangeCollectionMap.putAdd(range(2, CLOSED, 8, CLOSED), "X");

        assertNull(rangeCollectionMap.get(-1));
        assertEquals(Arrays.asList("A", "B", "C"), rangeCollectionMap.get(0));
        assertEquals(Arrays.asList("C", "D"), rangeCollectionMap.get(1));
        assertEquals(Arrays.asList("C", "D", "X"), rangeCollectionMap.get(2));
        assertEquals(Arrays.asList("X"), rangeCollectionMap.get(3));
        assertEquals(Arrays.asList("E", "X"), rangeCollectionMap.get(4));
        assertEquals(Arrays.asList("E", "X"), rangeCollectionMap.get(5));
        assertEquals(Arrays.asList("X"), rangeCollectionMap.get(6));
        assertEquals(Arrays.asList("F", "X"), rangeCollectionMap.get(7));
        assertEquals(Arrays.asList("F", "X"), rangeCollectionMap.get(8));
        assertEquals(Arrays.asList("F"), rangeCollectionMap.get(9));
        assertNull(rangeCollectionMap.get(10));


        rangeCollectionMap.putAdd(Range.atLeast(8), "Y");

        assertNull(rangeCollectionMap.get(-1));
        assertEquals(Arrays.asList("F", "X"), rangeCollectionMap.get(7));
        assertEquals(Arrays.asList("F", "X", "Y"), rangeCollectionMap.get(8));
        assertEquals(Arrays.asList("F", "Y"), rangeCollectionMap.get(9));
        assertEquals(Arrays.asList("Y"), rangeCollectionMap.get(10));
        assertEquals(Arrays.asList("Y"), rangeCollectionMap.get(Integer.MAX_VALUE));


        rangeCollectionMap.putAdd(Range.lessThan(11), "Z");

        assertEquals(Arrays.asList("Z"), rangeCollectionMap.get(Integer.MIN_VALUE));
        assertEquals(Arrays.asList("Z"), rangeCollectionMap.get(-1));
        assertEquals(Arrays.asList("F", "Y", "Z"), rangeCollectionMap.get(9));
        assertEquals(Arrays.asList("Y", "Z"), rangeCollectionMap.get(10));
        assertEquals(Arrays.asList("Y"), rangeCollectionMap.get(11));

    }


    @Test
    public void testPutAddEdgeCases()
    {
        // This test covers the following expected "edge" cases:

        // upper-closed <-> upper-closed: [20..30] + [29..30]
        // upper-closed <-> upper-open:   [20..30] + [29..30)
        // upper-open <-> upper-closed:   [20..30) + [29..30]
        // upper-open <-> upper-open:     [20..30) + [29..30)

        // upper-closed <-> lower-closed: [20..30] + [30..31]
        // upper-closed <-> lower-open:   [20..30] + (30..31]
        // upper-open <-> lower-closed:   [20..30) + [30..31]
        // upper-open <-> lower-open:     [20..30) + (30..31]

        // lower-closed <-> upper-closed: [20..30] + [19, 20]
        // lower-closed <-> upper-open:   [20..30] + [19, 20)
        // lower-open <-> upper-closed:   (20..30] + [19, 20]
        // lower-open <-> upper-open:     (20..30] + [19, 20)

        // lower-closed <-> lower-closed: [20..30] + [20..21]
        // lower-closed <-> lower-open:   [20..30] + (20..21]
        // lower-open <-> lower-closed:   (20..30] + [20..21]
        // lower-open <-> lower-open:     (20..30] + (20..21]


        // upper-closed <-> upper-closed: [20..30] + [29..30] = [20..29), [29..30]
        final RangeCollectionMap<Integer, String> rangeCollectionMap = new RangeCollectionMapImpl<>();
        rangeCollectionMap.putAdd(range(20, CLOSED, 30, CLOSED), "A");
        rangeCollectionMap.putAdd(range(29, CLOSED, 30, CLOSED), "X");
        assertEquals(Arrays.asList("A"), rangeCollectionMap.get(28));
        assertEquals(Arrays.asList("A", "X"), rangeCollectionMap.get(29));
        assertEquals(Arrays.asList("A", "X"), rangeCollectionMap.get(30));

        // upper-closed <-> upper-open: [20..30] + [29..30) = [20..29), [29..30), [30..30]
        rangeCollectionMap.clear();
        rangeCollectionMap.putAdd(range(20, CLOSED, 30, CLOSED), "A");
        rangeCollectionMap.putAdd(range(29, CLOSED, 30, OPEN), "X");
        assertEquals(Arrays.asList("A"), rangeCollectionMap.get(28));
        assertEquals(Arrays.asList("A", "X"), rangeCollectionMap.get(29));
        assertEquals(Arrays.asList("A"), rangeCollectionMap.get(30));

        // upper-open <-> upper-closed: [20..30) + [29..30] = [20..29), [29..30), [30..30]
        rangeCollectionMap.clear();
        rangeCollectionMap.putAdd(range(20, CLOSED, 30, OPEN), "A");
        rangeCollectionMap.putAdd(range(29, OPEN, 30, CLOSED), "X");
        assertEquals(Arrays.asList("A"), rangeCollectionMap.get(28));
        assertEquals(Arrays.asList("A"), rangeCollectionMap.get(29));
        assertEquals(Arrays.asList("X"), rangeCollectionMap.get(30));

        // upper-open <-> upper-open: [20..30) + [29..30) = [20..29), [29..30)
        rangeCollectionMap.clear();
        rangeCollectionMap.putAdd(range(20, CLOSED, 30, OPEN), "A");
        rangeCollectionMap.putAdd(range(29, CLOSED, 30, OPEN), "X");
        assertEquals(Arrays.asList("A"), rangeCollectionMap.get(28));
        assertEquals(Arrays.asList("A", "X"), rangeCollectionMap.get(29));
        assertNull(rangeCollectionMap.get(30));


        // upper-closed <-> lower-closed: [20..30] + [30..31] = [20..30), [30, 30], (30, 31]
        rangeCollectionMap.clear();
        rangeCollectionMap.putAdd(range(20, CLOSED, 30, CLOSED), "A");
        rangeCollectionMap.putAdd(range(30, CLOSED, 31, CLOSED), "X");
        assertEquals(Arrays.asList("A"), rangeCollectionMap.get(29));
        assertEquals(Arrays.asList("A", "X"), rangeCollectionMap.get(30));
        assertEquals(Arrays.asList("X"), rangeCollectionMap.get(31));

        // upper-closed <-> lower-open: [20..30] + (30..31] = [20..30], (30, 31]
        rangeCollectionMap.clear();
        rangeCollectionMap.putAdd(range(20, CLOSED, 30, CLOSED), "A");
        rangeCollectionMap.putAdd(range(30, OPEN, 31, CLOSED), "X");
        assertEquals(Arrays.asList("A"), rangeCollectionMap.get(29));
        assertEquals(Arrays.asList("A"), rangeCollectionMap.get(30));
        assertEquals(Arrays.asList("X"), rangeCollectionMap.get(31));

        // upper-open <-> lower-closed: [20..30) + [30..31] = [20..30), [30, 31]
        rangeCollectionMap.clear();
        rangeCollectionMap.putAdd(range(20, CLOSED, 30, OPEN), "A");
        rangeCollectionMap.putAdd(range(30, CLOSED, 31, CLOSED), "X");
        assertEquals(Arrays.asList("A"), rangeCollectionMap.get(29));
        assertEquals(Arrays.asList("X"), rangeCollectionMap.get(30));
        assertEquals(Arrays.asList("X"), rangeCollectionMap.get(31));

        // upper-open <-> lower-open: [20..30) + (30..31] = [20..30), (30, 31]
        rangeCollectionMap.clear();
        rangeCollectionMap.putAdd(range(20, CLOSED, 30, OPEN), "A");
        rangeCollectionMap.putAdd(range(30, OPEN, 31, CLOSED), "X");
        assertEquals(Arrays.asList("A"), rangeCollectionMap.get(29));
        assertNull(rangeCollectionMap.get(30));
        assertEquals(Arrays.asList("X"), rangeCollectionMap.get(31));


        // lower-closed <-> upper-closed: [20..30] + [19, 20]
        rangeCollectionMap.clear();
        rangeCollectionMap.putAdd(range(20, CLOSED, 30, CLOSED), "A");
        rangeCollectionMap.putAdd(range(19, CLOSED, 20, CLOSED), "X");
        assertEquals(Arrays.asList("X"), rangeCollectionMap.get(19));
        assertEquals(Arrays.asList("A", "X"), rangeCollectionMap.get(20));
        assertEquals(Arrays.asList("A"), rangeCollectionMap.get(21));

        // lower-closed <-> upper-open:   [20..30] + [19, 20)
        rangeCollectionMap.clear();
        rangeCollectionMap.putAdd(range(20, CLOSED, 30, CLOSED), "A");
        rangeCollectionMap.putAdd(range(19, CLOSED, 20, OPEN), "X");
        assertEquals(Arrays.asList("X"), rangeCollectionMap.get(19));
        assertEquals(Arrays.asList("A"), rangeCollectionMap.get(20));
        assertEquals(Arrays.asList("A"), rangeCollectionMap.get(21));

        // lower-open <-> upper-closed:   (20..30] + [19, 20]
        rangeCollectionMap.clear();
        rangeCollectionMap.putAdd(range(20, OPEN, 30, CLOSED), "A");
        rangeCollectionMap.putAdd(range(19, CLOSED, 20, CLOSED), "X");
        assertEquals(Arrays.asList("X"), rangeCollectionMap.get(19));
        assertEquals(Arrays.asList("X"), rangeCollectionMap.get(20));
        assertEquals(Arrays.asList("A"), rangeCollectionMap.get(21));

        // lower-open <-> upper-open:     (20..30] + [19, 20)
        rangeCollectionMap.clear();
        rangeCollectionMap.putAdd(range(20, OPEN, 30, CLOSED), "A");
        rangeCollectionMap.putAdd(range(19, CLOSED, 20, OPEN), "X");
        assertEquals(Arrays.asList("X"), rangeCollectionMap.get(19));
        assertNull(rangeCollectionMap.get(20));
        assertEquals(Arrays.asList("A"), rangeCollectionMap.get(21));


        // lower-closed <-> lower-closed: [20..30] + [20..21]
        rangeCollectionMap.clear();
        rangeCollectionMap.putAdd(range(20, CLOSED, 30, CLOSED), "A");
        rangeCollectionMap.putAdd(range(20, CLOSED, 21, CLOSED), "X");
        assertEquals(Arrays.asList("A", "X"), rangeCollectionMap.get(20));
        assertEquals(Arrays.asList("A", "X"), rangeCollectionMap.get(21));
        assertEquals(Arrays.asList("A"), rangeCollectionMap.get(22));

        // lower-closed <-> lower-open:   [20..30] + (20..21]
        rangeCollectionMap.clear();
        rangeCollectionMap.putAdd(range(20, CLOSED, 30, CLOSED), "A");
        rangeCollectionMap.putAdd(range(20, OPEN, 21, CLOSED), "X");
        assertEquals(Arrays.asList("A"), rangeCollectionMap.get(20));
        assertEquals(Arrays.asList("A", "X"), rangeCollectionMap.get(21));
        assertEquals(Arrays.asList("A"), rangeCollectionMap.get(22));

        // lower-open <-> lower-closed:   (20..30] + [20..21]
        rangeCollectionMap.clear();
        rangeCollectionMap.putAdd(range(20, OPEN, 30, CLOSED), "A");
        rangeCollectionMap.putAdd(range(20, CLOSED, 21, CLOSED), "X");
        assertEquals(Arrays.asList("X"), rangeCollectionMap.get(20));
        assertEquals(Arrays.asList("A", "X"), rangeCollectionMap.get(21));
        assertEquals(Arrays.asList("A"), rangeCollectionMap.get(22));

        // lower-open <-> lower-open:     (20..30] + (20..21]
        rangeCollectionMap.clear();
        rangeCollectionMap.putAdd(range(20, OPEN, 30, CLOSED), "A");
        rangeCollectionMap.putAdd(range(20, OPEN, 21, CLOSED), "X");
        assertNull(rangeCollectionMap.get(20));
        assertEquals(Arrays.asList("A", "X"), rangeCollectionMap.get(21));
        assertEquals(Arrays.asList("A"), rangeCollectionMap.get(22));

    }

    @Test
    public void testSerialization() throws Exception
    {
        // Given
        final File tempSerializationFile = File.createTempFile("TestEvaluator", "testSerialization.bin");
        final RangeCollectionMap<Integer, String> rangeCollectionMap = new RangeCollectionMapImpl<>();
        PREPOPULATED_RANGE_MAP.asMapOfRanges().forEach((range, values) ->
                values.forEach(v -> rangeCollectionMap.putAdd(range, v)));

        // Act
        // Serialize and then deserialize the data structure, then test it for correctness
        final FileOutputStream fileOutputStream = new FileOutputStream(tempSerializationFile);
        final ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
        objectOutputStream.writeObject(rangeCollectionMap);
        objectOutputStream.flush();
        objectOutputStream.close();

        final FileInputStream fileInputStream = new FileInputStream(tempSerializationFile);
        final ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
        final RangeCollectionMap<Integer, String> deserializedRangeCollectionMap  = (RangeCollectionMap<Integer, String>) objectInputStream.readObject();
        objectInputStream.close();

        // Assert
        assertNull(rangeCollectionMap.get(-1));
        assertEquals(Arrays.asList("A", "B", "C"), rangeCollectionMap.get(0));
        assertEquals(Arrays.asList("C", "D"), rangeCollectionMap.get(1));
        assertEquals(Arrays.asList("C", "D"), rangeCollectionMap.get(1));
        assertNull(rangeCollectionMap.get(3));
        assertEquals(Arrays.asList("E"), rangeCollectionMap.get(4));
        assertEquals(Arrays.asList("E"), rangeCollectionMap.get(5));
        assertNull(rangeCollectionMap.get(6));
        assertEquals(Arrays.asList("F"), rangeCollectionMap.get(7));
        assertEquals(Arrays.asList("F"), rangeCollectionMap.get(8));
        assertEquals(Arrays.asList("F"), rangeCollectionMap.get(9));
        assertNull(rangeCollectionMap.get(10));

    }

}