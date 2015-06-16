package com.amobee.freebee.util.trie;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Michael Bond
 */
public class StringBitKeyAnalyzerTest
{
    @Test
    public void testFourCharacters() throws Exception
    {
        final StringBitKeyAnalyzer keyAnalyzer = new StringBitKeyAnalyzer();

        final String key = ".com";
        final int lengthInBits = keyAnalyzer.getLengthInBits(key);
        assertEquals(key.length() * Character.SIZE, lengthInBits);

        int index = 0;

        // '.' 00000000 00101110
        assertEquals('.', keyAnalyzer.getElement(key, 0));
        assertFalse(keyAnalyzer.isBitSet(key, index++));
        assertTrue(keyAnalyzer.isBitSet(key, index++));
        assertTrue(keyAnalyzer.isBitSet(key, index++));
        assertTrue(keyAnalyzer.isBitSet(key, index++));
        assertFalse(keyAnalyzer.isBitSet(key, index++));
        assertTrue(keyAnalyzer.isBitSet(key, index++));
        assertFalse(keyAnalyzer.isBitSet(key, index++));
        assertFalse(keyAnalyzer.isBitSet(key, index++));
        assertFalse(keyAnalyzer.isBitSet(key, index++));
        assertFalse(keyAnalyzer.isBitSet(key, index++));
        assertFalse(keyAnalyzer.isBitSet(key, index++));
        assertFalse(keyAnalyzer.isBitSet(key, index++));
        assertFalse(keyAnalyzer.isBitSet(key, index++));
        assertFalse(keyAnalyzer.isBitSet(key, index++));
        assertFalse(keyAnalyzer.isBitSet(key, index++));
        assertFalse(keyAnalyzer.isBitSet(key, index++));

        // 'c' 00000000 01100011
        assertEquals('c', keyAnalyzer.getElement(key, 1));
        assertTrue(keyAnalyzer.isBitSet(key, index++));
        assertTrue(keyAnalyzer.isBitSet(key, index++));
        assertFalse(keyAnalyzer.isBitSet(key, index++));
        assertFalse(keyAnalyzer.isBitSet(key, index++));
        assertFalse(keyAnalyzer.isBitSet(key, index++));
        assertTrue(keyAnalyzer.isBitSet(key, index++));
        assertTrue(keyAnalyzer.isBitSet(key, index++));
        assertFalse(keyAnalyzer.isBitSet(key, index++));
        assertFalse(keyAnalyzer.isBitSet(key, index++));
        assertFalse(keyAnalyzer.isBitSet(key, index++));
        assertFalse(keyAnalyzer.isBitSet(key, index++));
        assertFalse(keyAnalyzer.isBitSet(key, index++));
        assertFalse(keyAnalyzer.isBitSet(key, index++));
        assertFalse(keyAnalyzer.isBitSet(key, index++));
        assertFalse(keyAnalyzer.isBitSet(key, index++));
        assertFalse(keyAnalyzer.isBitSet(key, index++));

        // 'o' 00000000 01101111
        assertEquals('o', keyAnalyzer.getElement(key, 2));
        assertTrue(keyAnalyzer.isBitSet(key, index++));
        assertTrue(keyAnalyzer.isBitSet(key, index++));
        assertTrue(keyAnalyzer.isBitSet(key, index++));
        assertTrue(keyAnalyzer.isBitSet(key, index++));
        assertFalse(keyAnalyzer.isBitSet(key, index++));
        assertTrue(keyAnalyzer.isBitSet(key, index++));
        assertTrue(keyAnalyzer.isBitSet(key, index++));
        assertFalse(keyAnalyzer.isBitSet(key, index++));
        assertFalse(keyAnalyzer.isBitSet(key, index++));
        assertFalse(keyAnalyzer.isBitSet(key, index++));
        assertFalse(keyAnalyzer.isBitSet(key, index++));
        assertFalse(keyAnalyzer.isBitSet(key, index++));
        assertFalse(keyAnalyzer.isBitSet(key, index++));
        assertFalse(keyAnalyzer.isBitSet(key, index++));
        assertFalse(keyAnalyzer.isBitSet(key, index++));
        assertFalse(keyAnalyzer.isBitSet(key, index++));

        // 'm' 00000000 01101101
        assertEquals('m', keyAnalyzer.getElement(key, 3));
        assertTrue(keyAnalyzer.isBitSet(key, index++));
        assertFalse(keyAnalyzer.isBitSet(key, index++));
        assertTrue(keyAnalyzer.isBitSet(key, index++));
        assertTrue(keyAnalyzer.isBitSet(key, index++));
        assertFalse(keyAnalyzer.isBitSet(key, index++));
        assertTrue(keyAnalyzer.isBitSet(key, index++));
        assertTrue(keyAnalyzer.isBitSet(key, index++));
        assertFalse(keyAnalyzer.isBitSet(key, index++));
        assertFalse(keyAnalyzer.isBitSet(key, index++));
        assertFalse(keyAnalyzer.isBitSet(key, index++));
        assertFalse(keyAnalyzer.isBitSet(key, index++));
        assertFalse(keyAnalyzer.isBitSet(key, index++));
        assertFalse(keyAnalyzer.isBitSet(key, index++));
        assertFalse(keyAnalyzer.isBitSet(key, index++));
        assertFalse(keyAnalyzer.isBitSet(key, index++));
        assertFalse(keyAnalyzer.isBitSet(key, index));
    }

    @Test
    public void testFiveCharacters() throws Exception
    {
        final StringBitKeyAnalyzer keyAnalyzer = new StringBitKeyAnalyzer();

        final String key = "a.b.c";
        final int lengthInBits = keyAnalyzer.getLengthInBits(key);
        assertEquals(key.length() * Character.SIZE, lengthInBits);

        int index = 0;

        // 'a' 00000000 01100001
        assertEquals('a', keyAnalyzer.getElement(key, 0));
        assertTrue(keyAnalyzer.isBitSet(key, index++));
        assertFalse(keyAnalyzer.isBitSet(key, index++));
        assertFalse(keyAnalyzer.isBitSet(key, index++));
        assertFalse(keyAnalyzer.isBitSet(key, index++));
        assertFalse(keyAnalyzer.isBitSet(key, index++));
        assertTrue(keyAnalyzer.isBitSet(key, index++));
        assertTrue(keyAnalyzer.isBitSet(key, index++));
        assertFalse(keyAnalyzer.isBitSet(key, index++));
        assertFalse(keyAnalyzer.isBitSet(key, index++));
        assertFalse(keyAnalyzer.isBitSet(key, index++));
        assertFalse(keyAnalyzer.isBitSet(key, index++));
        assertFalse(keyAnalyzer.isBitSet(key, index++));
        assertFalse(keyAnalyzer.isBitSet(key, index++));
        assertFalse(keyAnalyzer.isBitSet(key, index++));
        assertFalse(keyAnalyzer.isBitSet(key, index++));
        assertFalse(keyAnalyzer.isBitSet(key, index++));

        // '.' 00000000 00101110
        assertEquals('.', keyAnalyzer.getElement(key, 1));
        assertFalse(keyAnalyzer.isBitSet(key, index++));
        assertTrue(keyAnalyzer.isBitSet(key, index++));
        assertTrue(keyAnalyzer.isBitSet(key, index++));
        assertTrue(keyAnalyzer.isBitSet(key, index++));
        assertFalse(keyAnalyzer.isBitSet(key, index++));
        assertTrue(keyAnalyzer.isBitSet(key, index++));
        assertFalse(keyAnalyzer.isBitSet(key, index++));
        assertFalse(keyAnalyzer.isBitSet(key, index++));
        assertFalse(keyAnalyzer.isBitSet(key, index++));
        assertFalse(keyAnalyzer.isBitSet(key, index++));
        assertFalse(keyAnalyzer.isBitSet(key, index++));
        assertFalse(keyAnalyzer.isBitSet(key, index++));
        assertFalse(keyAnalyzer.isBitSet(key, index++));
        assertFalse(keyAnalyzer.isBitSet(key, index++));
        assertFalse(keyAnalyzer.isBitSet(key, index++));
        assertFalse(keyAnalyzer.isBitSet(key, index++));

        // 'b' 00000000 01100010
        assertEquals('b', keyAnalyzer.getElement(key, 2));
        assertFalse(keyAnalyzer.isBitSet(key, index++));
        assertTrue(keyAnalyzer.isBitSet(key, index++));
        assertFalse(keyAnalyzer.isBitSet(key, index++));
        assertFalse(keyAnalyzer.isBitSet(key, index++));
        assertFalse(keyAnalyzer.isBitSet(key, index++));
        assertTrue(keyAnalyzer.isBitSet(key, index++));
        assertTrue(keyAnalyzer.isBitSet(key, index++));
        assertFalse(keyAnalyzer.isBitSet(key, index++));
        assertFalse(keyAnalyzer.isBitSet(key, index++));
        assertFalse(keyAnalyzer.isBitSet(key, index++));
        assertFalse(keyAnalyzer.isBitSet(key, index++));
        assertFalse(keyAnalyzer.isBitSet(key, index++));
        assertFalse(keyAnalyzer.isBitSet(key, index++));
        assertFalse(keyAnalyzer.isBitSet(key, index++));
        assertFalse(keyAnalyzer.isBitSet(key, index++));
        assertFalse(keyAnalyzer.isBitSet(key, index++));

        // '.' 00000000 00101110
        assertEquals('.', keyAnalyzer.getElement(key, 3));
        assertFalse(keyAnalyzer.isBitSet(key, index++));
        assertTrue(keyAnalyzer.isBitSet(key, index++));
        assertTrue(keyAnalyzer.isBitSet(key, index++));
        assertTrue(keyAnalyzer.isBitSet(key, index++));
        assertFalse(keyAnalyzer.isBitSet(key, index++));
        assertTrue(keyAnalyzer.isBitSet(key, index++));
        assertFalse(keyAnalyzer.isBitSet(key, index++));
        assertFalse(keyAnalyzer.isBitSet(key, index++));
        assertFalse(keyAnalyzer.isBitSet(key, index++));
        assertFalse(keyAnalyzer.isBitSet(key, index++));
        assertFalse(keyAnalyzer.isBitSet(key, index++));
        assertFalse(keyAnalyzer.isBitSet(key, index++));
        assertFalse(keyAnalyzer.isBitSet(key, index++));
        assertFalse(keyAnalyzer.isBitSet(key, index++));
        assertFalse(keyAnalyzer.isBitSet(key, index++));
        assertFalse(keyAnalyzer.isBitSet(key, index++));

        // 'c' 00000000 01100011
        assertEquals('c', keyAnalyzer.getElement(key, 4));
        assertTrue(keyAnalyzer.isBitSet(key, index++));
        assertTrue(keyAnalyzer.isBitSet(key, index++));
        assertFalse(keyAnalyzer.isBitSet(key, index++));
        assertFalse(keyAnalyzer.isBitSet(key, index++));
        assertFalse(keyAnalyzer.isBitSet(key, index++));
        assertTrue(keyAnalyzer.isBitSet(key, index++));
        assertTrue(keyAnalyzer.isBitSet(key, index++));
        assertFalse(keyAnalyzer.isBitSet(key, index++));
        assertFalse(keyAnalyzer.isBitSet(key, index++));
        assertFalse(keyAnalyzer.isBitSet(key, index++));
        assertFalse(keyAnalyzer.isBitSet(key, index++));
        assertFalse(keyAnalyzer.isBitSet(key, index++));
        assertFalse(keyAnalyzer.isBitSet(key, index++));
        assertFalse(keyAnalyzer.isBitSet(key, index++));
        assertFalse(keyAnalyzer.isBitSet(key, index++));
        assertFalse(keyAnalyzer.isBitSet(key, index));
    }

    @Test
    public void testSerialization() throws Exception
    {
        // Given
        final File tempSerializationFile = File.createTempFile("TestEvaluator", "testSerialization.bin");
        final StringBitKeyAnalyzer keyAnalyzer = new StringBitKeyAnalyzer();

        final String key = "a.b.c";
        final int lengthInBits = keyAnalyzer.getLengthInBits(key);

        // Act
        // Serialize and then deserialize the data structure, then test it for correctness
        final FileOutputStream fileOutputStream = new FileOutputStream(tempSerializationFile);
        final ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
        objectOutputStream.writeObject(keyAnalyzer);
        objectOutputStream.flush();
        objectOutputStream.close();

        final FileInputStream fileInputStream = new FileInputStream(tempSerializationFile);
        final ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
        final StringBitKeyAnalyzer deserializedReverseStringBitKeyAnalyzer  = (StringBitKeyAnalyzer) objectInputStream.readObject();
        objectInputStream.close();

        // Assert
        assertNotNull(deserializedReverseStringBitKeyAnalyzer);
        assertEquals(lengthInBits, deserializedReverseStringBitKeyAnalyzer.getLengthInBits(key));
    }
}