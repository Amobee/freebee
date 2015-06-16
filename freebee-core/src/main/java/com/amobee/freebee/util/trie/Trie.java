package com.amobee.freebee.util.trie;

import java.util.Map;
import java.util.function.Consumer;
import javax.annotation.Nonnull;

/**
 * An extension of the {@link Map} interface that adds methods specific to trie operations.
 *
 * @author Michael Bond
 */
public interface Trie<K, V> extends Map<K, V>
{
    /**
     * Gets values of all entries whose key is a match or prefix of the specified key.
     *
     * @param key
     *         key to get corresponding value for.
     * @param consumer
     *         Consumer to call for each matched value.
     */
    void getAll(@Nonnull K key, @Nonnull Consumer<Entry<? extends K, ? extends V>> consumer);
}
