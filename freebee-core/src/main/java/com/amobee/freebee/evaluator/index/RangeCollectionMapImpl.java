package com.amobee.freebee.evaluator.index;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.BoundType;
import com.google.common.collect.ImmutableRangeMap;
import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.TreeRangeMap;

/**
 * An implemented of the {@link RangeCollectionMap} interface backed
 * by Guava's {@link RangeMap}.
 *
 * For example usage, see the RangeCollectionMapTest unit tests.
 *
 * @param <K> The type used for Range bounds
 * @param <V> The value stored in the Collection values of the map
 *
 * @author Kevin Doran
 */
@SuppressWarnings("UnstableApiUsage")
class RangeCollectionMapImpl<K extends Comparable<K>, V> implements RangeCollectionMap<K, V>, Serializable
{
    private static final long serialVersionUID = 6628140772761362477L;

    private transient Supplier<Collection<V>> collectionSupplier;
    private transient Supplier<RangeMap<K, Collection<V>>> rangeMapSupplier;
    private transient RangeMap<K, Collection<V>> rangeMap;
    private RangeMap<K, Collection<V>> serializableRangeMap;

    RangeCollectionMapImpl()
    {
        this(ArrayList::new);
    }

    RangeCollectionMapImpl(final Supplier<Collection<V>> collectionSupplier)
    {
        this(collectionSupplier, TreeRangeMap::create);
    }

    RangeCollectionMapImpl(
            @Nonnull final Supplier<Collection<V>> collectionSupplier,
            @Nonnull final Supplier<RangeMap<K, Collection<V>>> rangeMapSupplier)
    {
        this.collectionSupplier = collectionSupplier;
        this.rangeMapSupplier = rangeMapSupplier;
        this.rangeMap = rangeMapSupplier.get();
        if (this.rangeMap == null)
        {
            throw new IllegalArgumentException("Invalid rangeMapSupplier. Supplier must not supply null RangeMap.");
        }
    }

    @Override
    public void clear()
    {
        this.serializableRangeMap = null;
        this.rangeMap.clear();
    }

    @Override
    public void compact()
    {
        this.serializableRangeMap = null;
        this.rangeMap.asMapOfRanges().forEach((range, collection) -> {
            if (collection instanceof ArrayList)
            {
                ((ArrayList) collection).trimToSize();
            }
        });
    }

    @Nullable
    @Override
    public Collection<V> get(final K key)
    {
        return this.rangeMap.get(key);
    }

    /**
     * A RangeCollectionMap, is conceptually a data structure of the form:
     *
     *     {
     *        [0..1): ["A", "B", "C"],
     *        [1..2]: ["C", "D"],
     *        [4..5]: ["E"],
     *        [7..9]: ["F"]
     *     }
     *
     * Essentially: disjoint, non-overlapping ranges that are mapped to a collection of values.
     *
     * Using the above example, get([1..7]) should result in: ["C", "D", "E", "F"]
     */
    @Nullable
    @Override
    public Collection<V> get(final Range<K> keyRange)
    {
        final Collection<V> result = this.collectionSupplier.get();

        this.rangeMap.asMapOfRanges().forEach((range, values) -> {
            if (range.isConnected(keyRange) && !range.intersection(keyRange).isEmpty())
            {
                result.addAll(values);
            }
        });

        return result.isEmpty() ? null : result;
    }

    /**
     * A RangeCollectionMap, is conceptually a data structure of the form:
     *
     *     {
     *        [0..1): ["A", "B", "C"],
     *        [1..2]: ["C", "D"],
     *        [4..5]: ["E"],
     *        [7..9]: ["F"]
     *     }
     *
     * Essentially: disjoint, non-overlapping ranges that are mapped to a collection of values.
     * Internally, we are using a RangeMap<K, Collection<V>> to store this.
     *
     * putAdd(range, value) modifies the collection map such that the value is added to the mapped
     * collection(s) for the given range.
     *
     * Using the above example, putAdd([2..8], "X") should result in:
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
     * This is a tricky algorithm to implement.
     * Here is the logic used below to accomplish this:
     *
     * 1. Create a new, empty RangeMap. This will be the destination/result to replace our existing RangeMap
     *    in a way that incorporates the new range being added.
     *
     * 2. For each existing Range key in our RangeMap, determine whether or not it
     *      *    intersects with the incoming, "new" range specified as the putAdd arg.
     *      *    This should yield two collections:
     *      *       - Map<Range, Collection<V>> connectedRanges
     *      *       - Map<Range, Collection<V>> nonConnectedRanges
     *
     * 3. For each nonConnectedRanges: add it (and its unmodified values) to the new RangeMap.
     *
     * 4. Put the new range and a new list containing its single mapped value to the new RangeMap.
     *      i.e., newRangeMap.put(range, Lists.ofValues(value)).
     *    This will cover the non-overlapping/non-intersecting sub-ranges of the new range that need to
     *    be added to the new range map.
     *
     * 5. For each connectedRanges:
     *      - Determine sub-range portion that intersects with the new range as well as the
     *        the sub-range portion(s) that do not overlap/intersect with the new range.
     *        Note that there are several possibilities:
     *          - The connectedRange only touches the new range, so the "intersection" is empty, e.g., [1, 1)
     *          - The connectedRange is a strict sub-range of the new range, so there is no non-overlapping portions
     *          - The connectedRange is a strict super-range of the new range, so there are two non-overlapping portions
     *          - The connectedRange partially overlaps with the new range, so there is one intersection and one non-overlapping portion
     *          - The connectedRange or the new range could be unbounded on one side, e.g., [10,) or (,10]
     *      - For non-empty intersection, perform the following:
     *          newRangeMap.put(intersectionRange, new Collection(existingValuesForConnectedRange + newValue);
     *      - For each non-overlapping/non-intersecting sub-range, perform the following:
     *          newRangeMap.put(sub-range, existingValuesForConnectedRange);
     *
     *    What we have done here is overlaid portions of the new range that got added to newRangeMap
     *    with new sub-ranges that contain the existing values that had been there plus the new value.
     *    Any partial, non-overlapping, "leftover" bits of existing ranges that did not intersect with
     *    the new range got added with their existing values. In other words, existing ranges may have
     *    been split into intersecting parts and non-intersecting parts, and each collection of values
     *    updated accordingly.
     *
     * 6. Replace the instance RangeMap with the newly constructed RangeMap. i.e.:
     *      this.rangeMap = newRangeMap;
     */
    @Override
    public void putAdd(final Range<K> key, final V valueToAdd)
    {
        final RangeMap<K, Collection<V>> result = this.rangeMapSupplier.get();
        final Map<Range<K>, Collection<V>> connectedRanges = new HashMap<>();

        // For each existing range, determine whether or not it intersects with the incoming range
        // Add each nonConnectedRange (and its unmodified values) to the result RangeMap.
        this.rangeMap.asMapOfRanges().forEach((range, values) -> {
            if (range.isConnected(key))
            {
                connectedRanges.put(range, values);
            }
            else
            {
                result.put(range, values);
            }
        });

        // Put the new range and a new list containing its single mapped value to the new RangeMap.
        // This will account for the non-overlapping/non-intersecting sub-ranges of the new range
        // that need to be added to the new range map.
        final Collection<V> newValueCollection = this.collectionSupplier.get();
        newValueCollection.add(valueToAdd);
        result.put(key, newValueCollection);

        // For each existing connectedRange, determine sub-range portion that intersects with the new range,
        // as well as the the sub-range portion(s) that do not overlap/intersect with the new range.
        // Add the non-intersecting portions back to the result rangeMap with their original values.
        // Add the intersecting portions, but add the new valueToAdd to the original values for that range.
        connectedRanges.forEach((range, values) -> {
            final IntersectionResult<K> intersectionResult = findIntersection(range, key);

            intersectionResult.getLeftovers().forEach(nonIntersectingRange -> result.put(nonIntersectingRange, values));

            // ifPresent check is to prevent unnecessarily adding empty ranges such as [1,1)
            intersectionResult.getIntersection().ifPresent(intersectingRange -> {
                final Collection<V> intersectionValues = this.collectionSupplier.get();
                intersectionValues.addAll(values);
                intersectionValues.add(valueToAdd);
                result.put(intersectingRange, intersectionValues);
            });
        });

        this.rangeMap = result;
    }


    // Custom serialization logic
    private void writeObject(final ObjectOutputStream oos) throws IOException
    {
        this.serializableRangeMap = ImmutableRangeMap.copyOf(this.rangeMap);
        oos.defaultWriteObject(); // Now call the default serialization logic
    }

    // Custom deserialization logic
    private void readObject(final ObjectInputStream ois) throws IOException, ClassNotFoundException
    {
        ois.defaultReadObject(); // First perform default deserialization logic
        this.rangeMap = TreeRangeMap.create();
        if (this.serializableRangeMap != null)
        {
            this.rangeMap.putAll(this.serializableRangeMap);
        }
        this.collectionSupplier = ArrayList::new;
        this.rangeMapSupplier = TreeRangeMap::create;
        this.serializableRangeMap = null;  // free for GC
    }


    private static BoundType flip(final BoundType other)
    {
        return BoundType.CLOSED.equals(other) ? BoundType.OPEN : BoundType.CLOSED;
    }

    private static <T extends Comparable<T>> IntersectionResult<T> findIntersection(
            @Nonnull final Range<T> existingRange,
            @Nonnull final Range<T> newRange)
    {
        final IntersectionResult<T> result = new IntersectionResult<>();

        if (!existingRange.isConnected(newRange))
        {
            result.addLeftover(existingRange);
            return result;
        }

        final Range<T> intersection = existingRange.intersection(newRange);
        result.setIntersection(intersection);

        if (!existingRange.hasLowerBound() && intersection.hasLowerBound())
        {
            final Range<T> lowerLeftover = intersection.lowerBoundType() == BoundType.CLOSED
                    ? Range.lessThan(intersection.lowerEndpoint())
                    : Range.atMost(intersection.lowerEndpoint());
            result.addLeftover(lowerLeftover);
        }
        else if (existingRange.hasLowerBound() && existingRange.lowerEndpoint().compareTo(intersection.lowerEndpoint()) <= 0)
        {
            final Range<T> lowerLeftover = Range.range(
                    existingRange.lowerEndpoint(),
                    existingRange.lowerBoundType(),
                    intersection.lowerEndpoint(),
                    flip(intersection.lowerBoundType()));
            if (!lowerLeftover.isEmpty())
            {
                result.addLeftover(lowerLeftover);
            }
        }

        if (!existingRange.hasUpperBound() && intersection.hasUpperBound())
        {
            final Range<T> upperLeftover = intersection.upperBoundType() == BoundType.CLOSED
                    ? Range.greaterThan(intersection.upperEndpoint())
                    : Range.atLeast(intersection.upperEndpoint());
            result.addLeftover(upperLeftover);
        }
        else if (existingRange.hasUpperBound() && existingRange.upperEndpoint().compareTo(intersection.upperEndpoint()) >= 0)
        {
            final Range<T> upperLeftover = Range.range(
                    intersection.upperEndpoint(),
                    flip(intersection.upperBoundType()),
                    existingRange.upperEndpoint(),
                    existingRange.upperBoundType());
            if (!upperLeftover.isEmpty())
            {
                result.addLeftover(upperLeftover);
            }
        }

        return result;

    }

    private static class IntersectionResult<T extends Comparable<T>>
    {
        private Range<T> intersection;
        private final Collection<Range<T>> leftovers = new ArrayList<>();

        void setIntersection(@Nullable final Range<T> intersection)
        {
            this.intersection = intersection;
        }

        @Nonnull
        Optional<Range<T>> getIntersection()
        {
            return (this.intersection != null && !this.intersection.isEmpty())
                    ? Optional.of(this.intersection)
                    : Optional.empty();
        }

        @Nonnull
        Collection<Range<T>> getLeftovers()
        {
            return this.leftovers;
        }

        void addLeftover(@Nonnull final Range<T> leftover)
        {
            this.leftovers.add(leftover);
        }

    }

}
