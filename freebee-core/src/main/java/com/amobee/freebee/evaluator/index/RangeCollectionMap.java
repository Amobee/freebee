package com.amobee.freebee.evaluator.index;

import java.util.Collection;

import com.google.common.collect.Range;

/**
 *
 * A RangeCollectionMap is a data structure of the form:
 *
 *     {
 *        [0..1): ["A", "B", "C"],
 *        [1..2]: ["C", "D"],
 *        [4..5]: ["E"],
 *        [7..9]: ["F"]
 *     }
 *
 * Essentially: disjoint, non-overlapping ranges, each of which is mapped to a collection of values.
 *
 * putAdd(range, value) modifies the structure such that the value is added to the mapped
 * collection(s) for the given range.
 *
 * Using the above example, putAdd([2..8], "X") results in:
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
 *
 * As you can see, an interesting characteristic of this data structure is that range that acts as the "key"
 * when putting is not actually maintained. It is only used to create an internal mapping to a collection of
 * values across many intersecting ranges.
 *
 * @param <K> The type used for Range bounds
 * @param <V> The value stored in the Collection values of the map
 *
 * @author Kevin Doran
 */
interface RangeCollectionMap<K extends Comparable<K>, V>
{

    /**
     * Give a key value, find the associated values.
     *
     * @param key the value to query against the various ranges that have been associated to values
     * @return all the values for all ranges that match the given key
     */
    Collection<V> get(K key);

    /**
     * Like {@link #get}, but instead of looking up a
     * single key, a range of keys is evaluated.
     *
     * The matching values across the entire lookup range
     * are combined into a single collection and returned.
     *
     * If no values are matched, null is returned.
     *
     * @param keyRange the range of key values to get
     * @return a collection of all matched values for the given range of keys, or null if no values are matched.
     */
    Collection<V> get(Range<K> keyRange);

    /**
     * Associate a value to a range. Is additive in that it does not overwrite
     * previously associated values; rather, it adds the new value to previously
     * associated values for the same range.
     *
     * A RangeCollectionMap can be thought of conceptually as having the form:
     *
     *     {
     *        [0..1): ["A", "B", "C"],
     *        [1..2]: ["C", "D"],
     *        [4..5]: ["E"],
     *        [7..9]: ["F"]
     *     }
     *
     * Essentially: disjoint, non-overlapping ranges, each of which is mapped to a collection of values.
     *
     * putAdd(range, value) modifies the structure such that the value is added to the mapped
     * collection(s) for the given range.
     *
     * Using the above example, putAdd([2..8], "X") results in:
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
     *
     * @param key the range to associate the give value to
     * @param valueToAdd the value to additively associate with the given range.
     */
    void putAdd(Range<K> key, V valueToAdd);

    /**
     * Clears all entries from the map
     */
    void clear();

    /**
     * Minimizes the internal memory footprint to the extent possible.
     */
    void compact();

}
