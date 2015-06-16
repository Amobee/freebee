package com.amobee.freebee.util.trie;

import java.io.Serializable;
import javax.annotation.Nonnull;

/**
 * Interface for analyzing keys of different types for {@link BitTrie}.
 *
 * @author Michael Bond
 */
public interface BitKeyAnalyzer<K> extends Serializable
{
    /**
     * Determines if the bit at the specified index of the key is set.
     *
     * @param key
     *         Key to evaluate.
     * @param indexInBits
     *         Index of bit to check.
     * @return True if the bit at the specified index is set, false if the bit at the specified index is not set.
     */
    boolean isBitSet(@Nonnull K key, int indexInBits);

    /**
     * Returns the length of the specified key in bits.
     *
     * @param key
     *         Key to get length for.
     * @return Length of key in bits.
     */
    int getLengthInBits(@Nonnull K key);

    /**
     * Returns the element at the specified index.
     *
     * @param key
     *         Key to get element from.
     * @param index
     *         Index in elements to get value for.
     * @return Element at the specified index.
     */
    long getElement(@Nonnull K key, int index);

    /**
     * Gets the size of each element in bits.
     *
     * @return Size of each element in bits.
     */
    int getElementSizeInBits();

    /**
     * Trim the key to the specified size.
     *
     * @param key
     *         Key to trim.
     * @param lengthInBits
     *         Desired length of key in bits.
     * @return Original key if already desired length, trimmed key if specified key is longer than desired length.
     */
    @Nonnull
    K trimToSize(@Nonnull K key, int lengthInBits);
}
