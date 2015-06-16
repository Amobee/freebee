package com.amobee.freebee.util.trie;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

import org.eclipse.collections.impl.factory.Sets;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Michael Bond
 */
public class BitTrieTest
{
    @Test
    public void testStringKeys_SingleMapping() throws Exception
    {
        final Trie<String, String> trie = new BitTrie<>(new StringBitKeyAnalyzer());

        assertEquals(0, trie.size());

        // entries to add
        trie.put("a.b.c", "1");

        assertEquals(1, trie.size());
        assertNull(trie.get("a.b.c.d.e"));
        assertEquals("default", trie.getOrDefault("a.b.c.d.e", "default"));
        assertNull(trie.get("a.b"));
        assertEquals("default", trie.getOrDefault("a.b", "default"));
        assertNull(trie.get("bad.value"));
        assertEquals("default", trie.getOrDefault("bad.value", "default"));
        assertEquals("1", trie.get("a.b.c"));
        assertEquals(Sets.mutable.of("1"), trie.values().stream().collect(Collectors.toSet()));
        assertEquals(
                Sets.mutable.of("a.b.c"),
                trie.entrySet().stream().map(Map.Entry::getKey).collect(Collectors.toSet()));
        assertEquals(
                Sets.mutable.of("1"),
                trie.entrySet().stream().map(Map.Entry::getValue).collect(Collectors.toSet()));
        assertEquals(Sets.mutable.of("a.b.c"), trie.keySet().stream().collect(Collectors.toSet()));

        testGetAll(trie, "a.b", Sets.mutable.of(), Sets.mutable.of());
        testGetAll(trie, "a.b.c", Sets.mutable.of("a.b.c"), Sets.mutable.of("1"));
        testGetAll(trie, "a.b.c.d", Sets.mutable.of("a.b.c"), Sets.mutable.of("1"));
        testGetAll(trie, "bad.value", Sets.mutable.of(), Sets.mutable.of());
    }

    @Test
    public void testStringKeys_SingleMappingWithRemoves() throws Exception
    {
        final Trie<String, String> trie = new BitTrie<>(new StringBitKeyAnalyzer());

        assertEquals(0, trie.size());

        // entries to add
        trie.put("a.b.c", "1");
        trie.put("a.b", "0.1");
        trie.put("a.b.c.d.e", "0.2");

        // entries to remove
        assertTrue(trie.remove("a.b", "0.1"));
        assertFalse(trie.remove("a.b", "0.1"));
        assertNull(trie.remove("a.b"));
        assertEquals("0.2", trie.remove("a.b.c.d.e"));
        assertNull(trie.remove("a.b.c.d.e"));

        // entries to not really remove because value doesn't match
        assertFalse(trie.remove("a.b.c", "bad_value"));

        assertEquals(1, trie.size());
        assertNull(trie.get("a.b.c.d.e"));
        assertEquals("default", trie.getOrDefault("a.b.c.d.e", "default"));
        assertNull(trie.get("a.b"));
        assertEquals("default", trie.getOrDefault("a.b", "default"));
        assertNull(trie.get("bad.value"));
        assertEquals("default", trie.getOrDefault("bad.value", "default"));
        assertEquals("1", trie.get("a.b.c"));
        assertEquals(Sets.mutable.of("1"), trie.values().stream().collect(Collectors.toSet()));
        assertEquals(
                Sets.mutable.of("a.b.c"),
                trie.entrySet().stream().map(Map.Entry::getKey).collect(Collectors.toSet()));
        assertEquals(
                Sets.mutable.of("1"),
                trie.entrySet().stream().map(Map.Entry::getValue).collect(Collectors.toSet()));
        assertEquals(Sets.mutable.of("a.b.c"), trie.keySet().stream().collect(Collectors.toSet()));

        testGetAll(trie, "a.b", Sets.mutable.of(), Sets.mutable.of());
        testGetAll(trie, "a.b.c", Sets.mutable.of("a.b.c"), Sets.mutable.of("1"));
        testGetAll(trie, "a.b.c.d", Sets.mutable.of("a.b.c"), Sets.mutable.of("1"));
        testGetAll(trie, "bad.value", Sets.mutable.of(), Sets.mutable.of());
    }

    @SuppressWarnings({ "unchecked", "ConstantConditions" })
    @Test
    public void testStringKeys_MultipleMappings() throws Exception
    {
        final BitTrie<String, String> trie = new BitTrie<>(new StringBitKeyAnalyzer());

        assertEquals(0, trie.size());

        // entries to add
        trie.put("a", "0");
        trie.put("a.b.c", "1");
        trie.put("a.b", "0.1");
        trie.put("a.b.d", "3");
        trie.put("a.b.c.d.e", "0.2");
        trie.put("a.b.c.d", "2");
        trie.put("c.d.e", "4");
        trie.put("c.e.f", "5");

        // ensure that the bit arrays are all correct
        final BitTrie<String, String>.Entry abcdeEntry = trie.getEntry("a.b.c.d.e");
        trie.getAll("a.b.c.d.e", e -> assertSame(abcdeEntry.getBits(), ((BitTrie<String, String>.Entry) e).getBits()));

        // entries to remove
        assertTrue(trie.remove("a.b", "0.1"));
        assertFalse(trie.remove("a.b", "0.1"));
        assertNull(trie.remove("a.b"));
        assertEquals("0.2", trie.remove("a.b.c.d.e"));
        assertNull(trie.remove("a.b.c.d.e"));

        // entries to not really remove because value doesn't match
        assertFalse(trie.remove("a.b.c", "bad_value"));

        // ensure that the bit arrays are still all correct
        final BitTrie<String, String>.Entry abcdEntry = trie.getEntry("a.b.c.d");
        trie.getAll("a.b.c.d", e -> assertSame(abcdEntry.getBits(), ((BitTrie<String, String>.Entry) e).getBits()));

        assertEquals(6, trie.size());
        assertNull(trie.get("a.b.c.d.e"));
        assertEquals("default", trie.getOrDefault("a.b.c.d.e", "default"));
        assertNull(trie.get("a.b"));
        assertEquals("default", trie.getOrDefault("a.b", "default"));
        assertNull(trie.get("bad.value"));
        assertEquals("default", trie.getOrDefault("bad.value", "default"));
        assertEquals("1", trie.get("a.b.c"));
        assertEquals(
                Sets.mutable.of("a", "a.b.c", "a.b.c.d", "a.b.d", "c.d.e", "c.e.f"),
                trie.entrySet().stream().map(Map.Entry::getKey).collect(Collectors.toSet()));
        assertEquals(
                Sets.mutable.of("0", "1", "2", "3", "4", "5"),
                trie.entrySet().stream().map(Map.Entry::getValue).collect(Collectors.toSet()));
        assertEquals(
                Sets.mutable.of("a", "a.b.c", "a.b.c.d", "a.b.d", "c.d.e", "c.e.f"),
                trie.keySet().stream().collect(Collectors.toSet()));
        assertEquals(Sets.mutable.of("0", "1", "2", "3", "4", "5"), trie.values().stream().collect(Collectors.toSet()));

        testGetAll(trie, "a.b", Sets.mutable.of("a"), Sets.mutable.of("0"));
        testGetAll(trie, "a.b.c", Sets.mutable.of("a", "a.b.c"), Sets.mutable.of("0", "1"));
        testGetAll(trie, "a.b.c.d", Sets.mutable.of("a", "a.b.c", "a.b.c.d"), Sets.mutable.of("0", "1", "2"));
        testGetAll(trie, "bad.value", Sets.mutable.of(), Sets.mutable.of());
    }

    @Test
    public void testStringKeys_DuplicateKey() throws Exception
    {
        final BitTrie<String, String> trie = new BitTrie<>(new StringBitKeyAnalyzer());

        assertEquals(0, trie.size());

        // entries to add
        trie.put("a", "0");
        trie.put("a.b.c", "1");
        trie.put("a.b", "0.1");
        trie.put("a.b.c.d.e", "0.2");

        assertEquals("1", trie.computeIfAbsent("a.b.c", key -> "1.1"));
    }

    private void testGetAll(
            @Nonnull final Trie<String, String> trie,
            @Nonnull final String key,
            @Nonnull final Set<String> expectedKeys,
            @Nonnull final Set<String> expectedValues)
    {
        final Set<String> keys = new HashSet<>();
        final Set<String> values = new HashSet<>();

        trie.getAll(key, entry ->
        {
            keys.add(entry.getKey());
            values.add(entry.getValue());
        });

        assertEquals(expectedKeys, keys);
        assertEquals(expectedValues, values);
    }
}