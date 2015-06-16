package com.amobee.freebee.util.trie;

import java.io.Serializable;
import java.util.AbstractCollection;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A {@link Map} implementation of a trie.
 *
 * This trie implementation operates on bits rather than array elements which avoids maintaining maps of children
 * within each node. Instead, each node only has a right node for the unset bit and a left node for the set bit. Each
 * node contains only the bits that node represents.
 *
 * {@link java.util.BitSet} was considered but not within nodes because there is no easy way to append and prepend
 * bit sets or operate on groups of bits.
 *
 * The longest bit-set at time of entry creation is used rather than just the bits that entry represents to avoid
 * constantly converting bit and index offsets to local offsets. Ultimately even the memory overhead should be smaller
 * because the longest key always needs to exist anyway.
 *
 * @param <K>
 *         The type of keys maintained by this trie.
 * @param <V>
 *         The type of mapped values.
 * @author Michael Bond
 */
public class BitTrie<K, V> extends AbstractMap<K, V> implements Trie<K, V>, Cloneable, Serializable
{
    private static final long serialVersionUID = -1753229653384088341L;

    /**
     * Used to shift left or right for a partial word mask
     */
    private static final long WORD_MASK = 0xffffffffffffffffL;

    /**
     * Key analyzer
     */
    private BitKeyAnalyzer<K> keyAnalyzer;

    /**
     * Root entry of trie
     */
    private Entry root;

    /**
     * Number of keys in trie;
     */
    private int size;

    /**
     * The number of structural modifications to the tree.
     */
    private transient int modCount;

    /**
     * Views of this collection for {@link #entrySet()}, {@link #keySet()}
     * and {@link #values()}.
     */
    private transient volatile Set<Map.Entry<K, V>> entries;
    private transient volatile Set<K> keys;
    private transient volatile Collection<V> values;

    public BitTrie(@Nonnull final BitKeyAnalyzer<K> keyAnalyzer)
    {
        this.keyAnalyzer = keyAnalyzer;
        this.root = new Entry(null, 0);
    }

    @Override
    public void clear()
    {
        this.root.clear();
        this.size = 0;
    }

    @Override
    public V get(final Object key)
    {
        return getOrDefault(key, null);
    }

    @Override
    public void getAll(@Nonnull final K key, @Nonnull final Consumer<Map.Entry<? extends K, ? extends V>> consumer)
    {
        final int keyLengthInBits = getKeyLengthInBits(key);
        getEntry(key, keyLengthInBits, consumer);
    }

    @Override
    public V getOrDefault(@Nullable final Object key, @Nullable final V defaultValue)
    {
        @SuppressWarnings("unchecked") final Entry entry = getEntry((K) key);
        return null != entry ? entry.value : defaultValue;
    }

    @Override
    public V computeIfAbsent(@Nullable final K key, @Nonnull final Function<? super K, ? extends V> mappingFunction)
    {
        final Entry entry = getIfAbsentAddEntry(key);
        if (null == entry.value)
        {
            // always set the key even though it might already be set to avoid the extra condition
            entry.value = mappingFunction.apply(key);
            entry.key = key;
            ++this.size;
        }

        return entry.value;
    }

    @Override
    public V put(final K key, final V value)
    {
        final Entry entry = getIfAbsentAddEntry(key);
        final V oldValue = entry.value;
        entry.value = value;
        if (null == oldValue)
        {
            entry.key = key;
            ++this.size;
        }
        return oldValue;
    }

    @Override
    public V putIfAbsent(final K key, final V value)
    {
        final Entry entry = getIfAbsentAddEntry(key);
        final V oldValue = entry.value;
        if (null == oldValue)
        {
            entry.value = value;
            entry.key = key;
            ++this.size;
        }
        return oldValue;
    }

    @Override
    public V remove(final Object key)
    {
        @SuppressWarnings("unchecked") final Entry entry = getEntry((K) key);
        if (entry == null)
        {
            return null;
        }

        final V value = entry.value;
        removeEntry(entry);

        return value;
    }

    @Override
    public boolean remove(final Object key, final Object value)
    {
        @SuppressWarnings("unchecked") final Entry entry = getEntry((K) key);
        return null != entry && Objects.equals(value,  entry.value) && removeEntry(entry);
    }

    @Override
    public boolean replace(final K key, final V oldValue, final V newValue)
    {
        final Entry entry = getEntry(key);
        if (null != entry && Objects.equals(oldValue, entry.value))
        {
            entry.value = newValue;
            return true;
        }
        return false;
    }

    @Override
    public V replace(final K key, final V value)
    {
        final Entry entry = getEntry(key);
        if (null != entry)
        {
            final V oldValue = entry.value;
            entry.value = value;
            return oldValue;
        }
        return null;
    }

    @Override
    public int size()
    {
        return this.size;
    }

    @Nonnull
    @Override
    public Set<Map.Entry<K, V>> entrySet()
    {
        if (null == this.entries)
        {
            this.entries = new EntrySet();
        }
        return this.entries;
    }

    @Nonnull
    @Override
    public Set<K> keySet()
    {
        if (null == this.keys)
        {
            this.keys = new KeySet();
        }
        return this.keys;
    }

    @Nonnull
    @Override
    public Collection<V> values()
    {
        if (null == this.values)
        {
            this.values = new Values();
        }
        return this.values;
    }

    @Override
    public BitTrie<K, V> clone()
    {
        try
        {
            @SuppressWarnings("unchecked") final BitTrie<K, V> clone = (BitTrie<K, V>) super.clone();
            clone.keyAnalyzer = this.keyAnalyzer;
            // TODO Clone root. This is complicated because of the parent back-reference.
            clone.root = this.root;
            clone.size = this.size;
            return clone;
        }
        catch (final CloneNotSupportedException e)
        {
            throw new RuntimeException(e);
        }
    }

    public int getTotalNodes()
    {
        Entry entry = getFirstEntryInternal();
        int count = 0;
        while (entry != null)
        {
            ++count;
            entry = successorInternal(entry);
        }
        return count;
    }

    public void validate()
    {
        Entry entry = getFirstEntry();
        int count = 0;
        while (entry != null)
        {
            if (null != entry.value)
            {
                ++count;
            }

            if (entry.parent != null && entry.endInBits < entry.parent.endInBits)
            {
                throw new IllegalStateException("Child node is shorter than parent");
            }

            if (entry.left == null && entry.right == null && entry.value == null)
            {
                throw new IllegalStateException("Leaf node should not be an internal node");
            }

            entry = successorInternal(entry);
        }
        if (this.size != count)
        {
            throw new IllegalStateException("Size does not match actual node count");
        }
    }

    /**
     * Returns the successor of the specified Entry, or null there is no successor.
     */
    @SuppressWarnings("checkstyle:ReturnCount")
    protected Entry successor(@Nullable final Entry entry)
    {
        if (null == entry)
        {
            return null;
        }
        else if (null != entry.right)
        {
            Entry p = entry.right;
            while (null != p.left)
            {
                p = p.left;
            }

            if (null == p.value)
            {
                p = successor(p);
            }

            return p;
        }
        else
        {
            Entry p = entry.parent;
            Entry ch = entry;
            while (null != p && ch == p.right)
            {
                ch = p;
                p = p.parent;
            }

            if (null != p && null == p.value)
            {
                p = successor(p);
            }

            return p;
        }
    }

    /**
     * Returns the successor of the specified Entry, or null there is no successor.
     */
    @SuppressWarnings("checkstyle:ReturnCount")
    protected Entry successorInternal(@Nullable final Entry entry)
    {
        if (null == entry)
        {
            return null;
        }
        else if (null != entry.right)
        {
            Entry p = entry.right;
            while (null != p.left)
            {
                p = p.left;
            }

            return p;
        }
        else
        {
            Entry p = entry.parent;
            Entry ch = entry;
            while (null != p && ch == p.right)
            {
                ch = p;
                p = p.parent;
            }

            return p;
        }
    }

    protected final int getKeyLengthInBits(@Nullable final K key)
    {
        if (null == key)
        {
            throw new NullPointerException("Key must not be null");
        }

        final int keyLengthInBits = this.keyAnalyzer.getLengthInBits(key);

        if (0 == keyLengthInBits)
        {
            throw new IllegalArgumentException("Key must not be zero length");
        }

        return keyLengthInBits;
    }

    /**
     * Returns the first external Entry in the trie.
     */
    protected final Entry getFirstEntry()
    {
        Entry entry = this.root;

        while (null != entry.left)
        {
            entry = entry.left;
        }

        // If the first entry has no value it is an internal entry so find the first external entry. This will happen
        // when the root has no left nodes.
        if (null == entry.value)
        {
            entry = successor(entry);
        }

        return entry;
    }

    /**
     * Returns the first Entry in the trie.
     */
    protected final Entry getFirstEntryInternal()
    {
        Entry entry = this.root;

        while (null != entry.left)
        {
            entry = entry.left;
        }

        return entry;
    }

    /**
     * Returns the entry mapped to the specified key.
     *
     * @param key
     *         Key to match.
     * @return Entry matching the specified key or null if there is no match.
     */
    @Nullable
    protected final Entry getEntry(@Nullable final K key)
    {
        final int keyLengthInBits = getKeyLengthInBits(key);

        // get the nearest match
        final Entry entry = getEntry(key, keyLengthInBits, null);

        // verify exact match
        return entry.endInBits == keyLengthInBits && null != entry.value ? entry : null;
    }

    @Nonnull
    protected final Entry getIfAbsentAddEntry(@Nullable final K key)
    {
        final int keyLengthInBits = getKeyLengthInBits(key);

        // get the nearest match
        Entry entry = getEntry(key, keyLengthInBits, null);
        if (keyLengthInBits != entry.endInBits)
        {
            // the entry that was found is not an exact match, treat it as a parent and add a new entry
            entry = addEntry(entry, key, keyLengthInBits);
        }
        return entry;
    }

    /**
     * Adds a new child entry to the specified parent with the remaining bits of the specified key.
     *
     * @param parent
     *         Parent to add entry to.
     * @param key
     *         Key being added.
     * @param keyLengthInBits
     *         Index after the last bit in key to copy to new entry.
     * @return New child entry
     */
    @Nonnull
    protected final Entry addEntry(@Nonnull final Entry parent, @Nonnull final K key, final int keyLengthInBits)
    {
        // create a new entry and add it to the parent
        final Entry entry = new Entry(key, keyLengthInBits);
        parent.addChild(entry);

        // update the collection size
        ++this.modCount;

        return entry;
    }

    /**
     * Returns the entry that most closely matches the specified key.
     *
     * @param key
     *         Key to match.
     * @param keyEndInBits
     *         Number of bits in key.
     * @param consumer
     *         Consumer to call for each entry that is a partial match or null.
     * @return Entry that most closely matches the specified key.
     */
    @Nonnull
    protected final Entry getEntry(
            @Nonnull final K key,
            final int keyEndInBits,
            @Nullable final Consumer<Map.Entry<? extends K, ? extends V>> consumer)
    {
        Entry entry = this.root;
        Entry child = entry;

        if (null == consumer)
        {
            while (child != null && child.isPrefix(key, keyEndInBits))
            {
                // update current entry
                entry = child;

                // get the next child
                child = entry.getChild(key, keyEndInBits);
            }
        }
        else
        {
            while (child != null && child.isPrefix(key, keyEndInBits))
            {
                // update current entry
                entry = child;

                // if the current entry is a match call the consumer
                if (null != entry.value)
                {
                    consumer.accept(entry);
                }

                // get the next child
                child = entry.getChild(key, keyEndInBits);
            }
        }

        return entry;
    }

    protected final boolean removeEntry(@Nullable final Entry entry)
    {
        if (null != entry)
        {
            entry.remove();
            ++this.modCount;
            --this.size;
            return true;
        }

        return false;
    }

    @SuppressWarnings("checkstyle:ReturnCount")
    protected int findFirstMismatchedBit(
            @Nonnull final K a,
            final int aStartInBits,
            final int aEndInBits,
            @Nonnull final K b,
            final int bStartInBits,
            final int bEndInBits)
    {
        final int elementSize = this.keyAnalyzer.getElementSizeInBits();
        if (aStartInBits % elementSize != bStartInBits % elementSize)
        {
            throw new IllegalArgumentException("Key and other value bit offsets must be the same number of bits from the nearest long boundary");
        }

        // find the offset into the long array of both the key and other, rounding to the nearest long
        int aWordIndex = aStartInBits / elementSize;
        int bWordIndex = bStartInBits / elementSize;

        // find the number of long comparisons we can do for both
        final int bEndInWords = bEndInBits / elementSize;
        final int aEndInWords = Math.min(aEndInBits / elementSize, aWordIndex + bEndInWords - bWordIndex);

        // Compare full long values first. There will be some bits in the first long that technically don't need
        // to be compared but 'other' will contain these bits from the parent so it will still match.
        for (; aWordIndex < aEndInWords; ++aWordIndex, ++bWordIndex)
        {
            final long aWord = this.keyAnalyzer.getElement(a, aWordIndex);
            final long bWord = this.keyAnalyzer.getElement(b, bWordIndex);
            if (aWord != bWord)
            {
                // return first mismatched bit
                return aWordIndex * elementSize + Long.numberOfTrailingZeros(aWord ^ bWord);
            }
        }

        // compare remaining bits if necessary
        final int aRemainingBits = aEndInBits - aWordIndex * elementSize;
        final int bRemainingBits = bEndInBits - bWordIndex * elementSize;
        final int remainingBits = Math.min(aRemainingBits, bRemainingBits);
        if (remainingBits > 0)
        {
            // mask bits that should not be compared
            final long mask = WORD_MASK >>> -remainingBits;
            final long aWord = this.keyAnalyzer.getElement(a, aWordIndex) & mask;
            final long bWord = this.keyAnalyzer.getElement(b, bWordIndex) & mask;

            if (aWord != bWord)
            {
                // return first mismatched bit
                return aWordIndex * elementSize + Long.numberOfTrailingZeros(aWord ^ bWord);
            }
        }

        // we have a complete match
        return aRemainingBits < bRemainingBits ? aEndInBits : bEndInBits;
    }

    protected final class Entry implements Map.Entry<K, V>, Serializable
    {
        private static final long serialVersionUID = 4024553113521826981L;

        /**
         * Parent entry
         */
        private Entry parent;

        /**
         * Left (0 bit) entry.
         */
        private Entry left;

        /**
         * Right (1 bit) entry.
         */
        private Entry right;

        /**
         * Elements of key that this entry represents.
         */
        private K bits;

        /**
         * Index after the last bit in bits for this entry.
         */
        private final int endInBits;

        /**
         * Key associated with this entry or null.
         */
        private K key;

        /**
         * Value associated with this entry or null.
         */
        private V value;

        Entry(@Nullable final K bits, final int endInBits)
        {
            this.bits = bits;
            this.endInBits = endInBits;
        }

        /**
         * Removes children and value
         */
        public void clear()
        {
            this.right = null;
            this.left = null;
            this.key = null;
            this.value = null;
        }

        @Override
        public K getKey()
        {
            return this.key;
        }

        public void setKey(final K key)
        {
            this.key = key;
        }

        @Override
        public V getValue()
        {
            return this.value;
        }

        /**
         * Sets the value associated with this entry
         *
         * @param value
         *         New value to associate with this entry
         */
        @Override
        public V setValue(final V value)
        {
            final V oldValue = this.value;
            this.value = value;
            return oldValue;
        }

        public Entry getParent()
        {
            return this.parent;
        }

        public K getBits()
        {
            return this.bits;
        }

        /**
         * Determines if the entry is a matching entry.
         *
         * @return true if the entry is a matching entry
         */
        public boolean isMatch()
        {
            return null == this.value;
        }

        public void addChild(@Nonnull final Entry child)
        {
            if (BitTrie.this.keyAnalyzer.isBitSet(child.bits, this.endInBits))
            {
                this.right = addChild(this.right, child);
                this.right.parent = this;

                // set the bits of this node to the new child bits to reduce redundant memory usage
                setAncestorBits(this.right.bits);
            }
            else
            {
                this.left = addChild(this.left, child);
                this.left.parent = this;

                // set the bits of this node to the new child bits to reduce redundant memory usage
                setAncestorBits(this.left.bits);
            }
        }

        @Override
        public String toString()
        {
            return "Entry{" +
                    "parent=" + (this.parent != null ? Integer.toHexString(this.parent.hashCode()) : "null") +
                    ", left=" + (this.left != null ? Integer.toHexString(this.left.hashCode()) : "null") +
                    ", right=" + (this.right != null ? Integer.toHexString(this.right.hashCode()) : "null") +
                    ", endInBits=" + this.endInBits +
                    ", key=" + this.key +
                    ", value=" + this.value +
                    '}';
        }

        /**
         * Remove this entry from its parent.
         */
        public void remove()
        {
            // only the root entry has no parent and the root should never be modified
            if (this.parent != null)
            {
                // determine if there is only one remaining child
                if (this.left == null)
                {
                    // the right entry may be null, that's ok as that means there are no child entries left
                    mergeWithChild(this.right);
                }
                else if (this.right == null)
                {
                    mergeWithChild(this.left);
                }

                // set the key and value to null to indicate that it is no longer considered a matching entry
                this.key = null;
                this.value = null;
            }
        }

        /**
         * Merge this entry with the specified child entry and replace the child with this entry in the parent of this
         * entry.
         *
         * @param child
         *         Child to merge this entry with.
         */
        public void mergeWithChild(@Nullable final Entry child)
        {
            final Entry thisParent = this.parent;

            if (child != null)
            {
                // update the child parent to be the parent of this entry
                child.parent = thisParent;
                thisParent.setAncestorBits(child.bits);
            }
            else if (thisParent.value == null)
            {
                // the parent has no children left and is an internal node so remove that as well
                thisParent.remove();
            }
            else
            {
                // the parent has no remaining children but can't be removed, remove unnecessary bits to save memory
                setAncestorBits(BitTrie.this.keyAnalyzer.trimToSize(this.bits, this.endInBits));
            }

            // replace the appropriate branch of the parent with the child
            if (this == thisParent.left)
            {
                thisParent.left = child;
            }
            else
            {
                thisParent.right = child;
            }
        }

        /**
         * Determines if this entry is a prefix of the specified key.
         *
         * @param key
         *         Key to compare
         * @param keyEndInBits
         *         Index after the last bit in key to compare.
         * @return True if all bits in this entry match the corresponding bits in the key.
         */
        @SuppressWarnings("checkstyle:ReturnCount")
        public boolean isPrefix(@Nonnull final K key, final int keyEndInBits)
        {
            if (isRoot())
            {
                // the root node matches everything and has no parent so don't continue
                return true;
            }
            else if (this.endInBits > keyEndInBits)
            {
                return false;
            }

            final int parentEndInBits = this.parent.endInBits;
            final int firstMismatchedBit = findFirstMismatchedBit(
                    key,
                    parentEndInBits,
                    keyEndInBits,
                    this.bits,
                    parentEndInBits,
                    this.endInBits);
            return firstMismatchedBit == this.endInBits;
        }

        /**
         * Add one child to another child that has at least a partial overlap.
         *
         * @param oldEntry
         *         Old child entry
         * @param newEntry
         *         New child entry
         * @return Topmost entry after the joined tree of entries
         */
        @Nonnull
        private Entry addChild(@Nullable final Entry oldEntry, @Nonnull final Entry newEntry)
        {
            if (oldEntry == null)
            {
                return newEntry;
            }

            // look for the first mismatched bit in the two child entries
            final int thisEndInBits = this.endInBits;
            final int indexInBits = findFirstMismatchedBit(
                    oldEntry.bits,
                    thisEndInBits,
                    oldEntry.endInBits,
                    newEntry.bits,
                    thisEndInBits,
                    newEntry.endInBits);

            final Entry entry;
            if (indexInBits == oldEntry.endInBits)
            {
                // all bits in the old entry match the new entry so add the new entry as a child of old entry
                oldEntry.addChild(newEntry);
                entry = oldEntry;
            }
            else if (indexInBits == newEntry.endInBits)
            {
                // all bits in the new entry match the old entry so add the old entry as a child of new entry
                newEntry.addChild(oldEntry);
                entry = newEntry;
            }
            else
            {
                // the two child entries partially overlap so create a new parent
                entry = new Entry(oldEntry.bits, indexInBits);

                // add the old entry to the new parent
                entry.addChild(oldEntry);

                // add the new entry to the new parent
                entry.addChild(newEntry);
            }

            return entry;
        }

        private boolean isRoot()
        {
            return 0 == this.endInBits;
        }

        /**
         * Sets the bits of this node and all ancestors to the new bits to reduce memory overhead
         *
         * @param bits
         *         Bits to set
         */
        private void setAncestorBits(@Nonnull final K bits)
        {
            // don't modify the root node
            if (!isRoot() && this.bits != bits)
            {
                this.bits = bits;
                if (null != this.parent)
                {
                    this.parent.setAncestorBits(bits);
                }
            }
        }

        public Entry getChild(@Nonnull final K key, final int keyEndInBits)
        {
            final Entry child;
            if (this.endInBits < keyEndInBits)
            {
                if (BitTrie.this.keyAnalyzer.isBitSet(key, this.endInBits))
                {
                    child = this.right;
                }
                else
                {
                    child = this.left;
                }
            }
            else
            {
                child = null;
            }
            return child;
        }
    }

    /**
     * Base class for TreeMap Iterators
     */
    protected abstract class PrivateEntryIterator<T> implements Iterator<T>
    {
        private Entry next;
        private Entry lastReturned;
        private int expectedModCount;

        PrivateEntryIterator(@Nonnull final Entry first)
        {
            this.expectedModCount = BitTrie.this.modCount;
            this.lastReturned = null;
            this.next = first;
        }

        @Override
        public final boolean hasNext()
        {
            return this.next != null;
        }

        final Entry nextEntry()
        {
            if (!hasNext())
            {
                throw new NoSuchElementException();
            }
            final Entry e = this.next;
            if (BitTrie.this.modCount != this.expectedModCount)
            {
                throw new ConcurrentModificationException();
            }
            this.next = successor(e);
            this.lastReturned = e;
            return e;
        }

        @Override
        public void remove()
        {
            if (null == this.lastReturned)
            {
                throw new IllegalStateException();
            }
            if (BitTrie.this.modCount != this.expectedModCount)
            {
                throw new ConcurrentModificationException();
            }
            // deleted entries are replaced by their successors
            if (this.lastReturned.left != null && this.lastReturned.right != null)
            {
                this.next = this.lastReturned;
            }
            removeEntry(this.lastReturned);
            this.expectedModCount = BitTrie.this.modCount;
            this.lastReturned = null;
        }
    }

    protected final class EntryIterator extends PrivateEntryIterator<Map.Entry<K, V>>
    {
        public EntryIterator(@Nonnull final Entry first)
        {
            super(first);
        }

        @Override
        public Map.Entry<K, V> next()
        {
            if (!hasNext())
            {
                throw new NoSuchElementException();
            }
            return nextEntry();
        }
    }

    protected final class ValueIterator extends PrivateEntryIterator<V>
    {
        ValueIterator(@Nonnull final Entry first)
        {
            super(first);
        }

        @Override
        public V next()
        {
            if (!hasNext())
            {
                throw new NoSuchElementException();
            }

            return nextEntry().value;
        }
    }

    protected final class KeyIterator extends PrivateEntryIterator<K>
    {
        KeyIterator(@Nonnull final Entry first)
        {
            super(first);
        }

        @Override
        public K next()
        {
            if (!hasNext())
            {
                throw new NoSuchElementException();
            }

            return nextEntry().getKey();
        }
    }

    protected final class EntrySet extends AbstractSet<Map.Entry<K, V>>
    {
        @Nonnull
        @Override
        public Iterator<Map.Entry<K, V>> iterator()
        {
            return new EntryIterator(getFirstEntry());
        }

        @SuppressWarnings("unchecked")
        @Override
        public boolean contains(final Object o)
        {
            if (o instanceof Map.Entry)
            {
                final Map.Entry<K, V> e = (Map.Entry<K, V>) o;
                return BitTrie.this.containsKey(e.getKey());
            }
            return false;
        }

        @Override
        public boolean remove(final Object o)
        {
            return BitTrie.this.remove(o) != null;
        }

        @Override
        public int size()
        {
            return BitTrie.this.size;
        }

        @Override
        public void clear()
        {
            BitTrie.this.clear();
        }
    }

    protected final class KeySet extends AbstractSet<K>
    {
        @Nonnull
        @Override
        public Iterator<K> iterator()
        {
            return new KeyIterator(getFirstEntry());
        }

        @Override
        public int size()
        {
            return BitTrie.this.size;
        }

        @Override
        public boolean contains(final Object o)
        {
            return containsKey(o);
        }

        @Override
        public boolean remove(final Object o)
        {
            return BitTrie.this.remove(o) != null;
        }

        @Override
        public void clear()
        {
            BitTrie.this.clear();
        }
    }

    protected final class Values extends AbstractCollection<V>
    {
        @Nonnull
        @Override
        public Iterator<V> iterator()
        {
            return new ValueIterator(getFirstEntry());
        }

        @Override
        public int size()
        {
            return BitTrie.this.size;
        }

        @Override
        public boolean contains(final Object o)
        {
            return containsValue(o);
        }

        @Override
        public void clear()
        {
            BitTrie.this.clear();
        }
    }
}
