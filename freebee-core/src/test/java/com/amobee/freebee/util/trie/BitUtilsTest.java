package com.amobee.freebee.util.trie;

import com.amobee.freebee.util.BitUtils;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Michael Bond
 */
public class BitUtilsTest
{
    @Test
    public void testIsBitSet() throws Exception
    {
        final long[] bits = {
                0b00100000_00000000_00000010_00000000_00100000_00000000_00000010_00000000L,
                0b00000000_00000001_00000000_00000000_00000010_00000000_00000000_00000001L,
        };

        // first long

        assertFalse(BitUtils.isBitSet(bits, 0));
        assertFalse(BitUtils.isBitSet(bits, 1));

        assertFalse(BitUtils.isBitSet(bits, 8));
        assertTrue(BitUtils.isBitSet(bits, 9));
        assertFalse(BitUtils.isBitSet(bits, 10));

        assertFalse(BitUtils.isBitSet(bits, 20));
        assertTrue(BitUtils.isBitSet(bits, 29));
        assertFalse(BitUtils.isBitSet(bits, 30));

        assertFalse(BitUtils.isBitSet(bits, 40));
        assertTrue(BitUtils.isBitSet(bits, 41));
        assertFalse(BitUtils.isBitSet(bits, 42));

        assertFalse(BitUtils.isBitSet(bits, 60));
        assertTrue(BitUtils.isBitSet(bits, 61));
        assertFalse(BitUtils.isBitSet(bits, 62));
        assertFalse(BitUtils.isBitSet(bits, 63));

        // second long

        assertTrue(BitUtils.isBitSet(bits, 64));
        assertFalse(BitUtils.isBitSet(bits, 65));

        assertFalse(BitUtils.isBitSet(bits, 88));
        assertTrue(BitUtils.isBitSet(bits, 89));
        assertFalse(BitUtils.isBitSet(bits, 90));

        assertFalse(BitUtils.isBitSet(bits, 88));
        assertTrue(BitUtils.isBitSet(bits, 89));
        assertFalse(BitUtils.isBitSet(bits, 90));

        assertFalse(BitUtils.isBitSet(bits, 111));
        assertTrue(BitUtils.isBitSet(bits, 112));
        assertFalse(BitUtils.isBitSet(bits, 113));

        assertFalse(BitUtils.isBitSet(bits, 126));
        assertFalse(BitUtils.isBitSet(bits, 127));

        try
        {
            assertTrue(BitUtils.isBitSet(bits, 128));
            fail("Exception expected");
        }
        catch (final ArrayIndexOutOfBoundsException ignored)
        {
        }
    }

    @Test
    public void testFindFirstMismatchedBit_Match_SameLength() throws Exception
    {
        final long[] aBits = {
                0b00100000_00000000_00000010_00000000_00100000_00000000_00000010_00000000L,
                0b00000000_00000001_00000000_00000000_00000010_00000000_00000000_00000001L
        };
        final long[] bBits = {
                0b00100000_00000000_00000010_00000000_00100000_00000000_00000010_00000000L,
                0b00000000_00000001_00000000_00000000_00000010_00000000_00000000_00000001L
        };

        final int aEndInBits = aBits.length * Long.SIZE;
        final int bEndInBits = bBits.length * Long.SIZE - 16;
        assertEquals(
                bEndInBits,
                BitUtils.findFirstMismatchedBit(aBits, 0, aEndInBits, bBits, 0, bEndInBits));
    }

    @Test
    public void testFindFirstMismatchedBit_Match_ABitsLonger_Partial() throws Exception
    {
        final long[] aBits = {
                0b00100000_00000000_00000010_00000000_00100000_00000000_00000010_00000000L,
                0b00000000_00000001_00000000_00000000_00000010_00000000_00000000_00000001L
        };
        final long[] bBits = {
                0b00100000_00000000_00000010_00000000_00100000_00000000_00000010_00000000L,
                0b10000000_00000001_00000000_00000000_00000010_00000000_00000000_00000001L
        };

        final int aEndInBits = aBits.length * Long.SIZE - 1;
        final int bEndInBits = bBits.length * Long.SIZE - 16;
        assertEquals(
                bEndInBits,
                BitUtils.findFirstMismatchedBit(aBits, 0, aEndInBits, bBits, 0, bEndInBits));
    }

    @Test
    public void testFindFirstMismatchedBit_Match_BBitsLonger_Partial() throws Exception
    {
        final long[] aBits = {
                0b00100000_00000000_00000010_00000000_00100000_00000000_00000010_00000000L,
                0b00000000_00000001_00000000_00000000_00000010_00000000_00000000_00000001L
        };
        final long[] bBits = {
                0b00100000_00000000_00000010_00000000_00100000_00000000_00000010_00000000L,
                0b00000000_00000001_00000000_00000000_00000010_00000000_00000000_00000001L
        };

        final int aEndInBits = aBits.length * Long.SIZE - 4;
        final int bEndInBits = bBits.length * Long.SIZE - 1;
        assertEquals(
                aEndInBits,
                BitUtils.findFirstMismatchedBit(aBits, 0, aEndInBits, bBits, 0, bEndInBits));
    }

    @Test
    public void testFindFirstMismatchedBit_Mismatch_SameLength() throws Exception
    {
        final long[] aBits = {
                0b00100000_00000000_00000010_00000000_00010000_00000000_00000010_00000000L,
                0b00000000_00000001_00000000_00000000_00000010_00000000_00000000_00000001L
        };
        final long[] bBits = {
                0b00100000_00000000_00000010_00000000_00100000_00000000_00000010_00000000L,
                0b00000000_00000001_00000000_00000000_00000010_00000000_00000000_00000001L
        };

        assertEquals(
                28,
                BitUtils.findFirstMismatchedBit(aBits, 0, aBits.length * Long.SIZE, bBits, 0, Long.SIZE));
    }

    @Test
    public void testFindFirstMismatchedBit_Mismatch_SameLength_Partial() throws Exception
    {
        final long[] aBits = {
                0b00100000_00000000_00000010_00000000_00010000_00000000_00000010_00000000L,
                0b00000000_00000001_00000000_00000000_00000010_00000000_00000000_00000001L
        };
        final long[] bBits = {
                0b00100000_00000000_00000010_00000000_00100000_00000000_00000010_00000000L,
                0b00000000_00000001_00000000_00000000_00000010_00000000_00000000_00000001L
        };

        final int aEndInBits = aBits.length * Long.SIZE - 1;
        final int bEndInBits = Long.SIZE - 1;
        assertEquals(
                28,
                BitUtils.findFirstMismatchedBit(aBits, 0, aEndInBits, bBits, 0, bEndInBits));
    }

    @Test
    public void testFindFirstMismatchedBit_Match_ALonger() throws Exception
    {
        final long[] aBits = {
                0b00100000_00000000_00000010_00000000_00100000_00000000_00000010_00000000L,
                0b00000000_00000001_00000000_00000000_00000010_00000000_00000000_00000001L
        };
        final long[] bBits = {
                0b00100000_00000000_00000010_00000000_00100000_00000000_00000010_00000000L
        };

        final int aEndInBits = aBits.length * Long.SIZE;
        final int bEndInBits = Long.SIZE;
        assertEquals(
                bEndInBits,
                BitUtils.findFirstMismatchedBit(aBits, 0, aEndInBits, bBits, 0, bEndInBits));
    }

    @Test
    public void testFindFirstMismatchedBit_Match_ALonger_Partial() throws Exception
    {
        final long[] aBits = {
                0b00100000_00000000_00000010_00000000_00100000_00000000_00000010_00000000L,
                0b00000000_00000001_00000000_00000000_00000010_00000000_00000000_00000001L
        };
        final long[] bBits = {
                0b00100000_00000000_00000010_00000000_00100000_00000000_00000010_00000000L
        };

        final int aEndInBits = aBits.length * Long.SIZE - 1;
        final int bEndInBits = bBits.length * Long.SIZE - 1;
        assertEquals(
                bEndInBits,
                BitUtils.findFirstMismatchedBit(aBits, 0, aEndInBits, bBits, 0, bEndInBits));
    }

    @Test
    public void testFindFirstMismatchedBit_Mismatch_ALonger() throws Exception
    {
        final long[] aBits = {
                0b00100000_00000000_00000010_00000000_00010000_00000000_00000010_00000000L,
                0b00000000_00000001_00000000_00000000_00000010_00000000_00000000_00000001L
        };
        final long[] bBits = {
                0b00100000_00000000_00000010_00000000_00100000_00000000_00000010_00000000L
        };

        assertEquals(
                28,
                BitUtils.findFirstMismatchedBit(aBits, 0, aBits.length * Long.SIZE, bBits, 0, Long.SIZE));
    }

    @Test
    public void testFindFirstMismatchedBit_Mismatch_ALonger_Partial() throws Exception
    {
        final long[] aBits = {
                0b00100000_00000000_00000010_00000000_00010000_00000000_00000010_00000000L,
                0b00000000_00000001_00000000_00000000_00000010_00000000_00000000_00000001L
        };
        final long[] bBits = {
                0b00100000_00000000_00000010_00000000_00100000_00000000_00000010_00000000L
        };

        final int aEndInBits = aBits.length * Long.SIZE - 1;
        final int bEndInBits = bBits.length * Long.SIZE - 1;
        assertEquals(
                28,
                BitUtils.findFirstMismatchedBit(aBits, 0, aEndInBits, bBits, 0, bEndInBits));
    }

    @Test
    public void testFindFirstMismatchedBit_Match_BLonger() throws Exception
    {
        final long[] aBits = {
                0b00100000_00000000_00000010_00000000_00100000_00000000_00000010_00000000L
        };
        final long[] bBits = {
                0b00100000_00000000_00000010_00000000_00100000_00000000_00000010_00000000L,
                0b00000000_00000001_00000000_00000000_00000010_00000000_00000000_00000001L
        };

        final int aEndInBits = aBits.length * Long.SIZE;
        assertEquals(
                aEndInBits,
                BitUtils.findFirstMismatchedBit(aBits, 0, aEndInBits, bBits, 0, Long.SIZE));
    }

    @Test
    public void testFindFirstMismatchedBit_Mismatch_BLonger() throws Exception
    {
        final long[] aBits = {
                0b00100000_00000000_00000010_00000000_00010000_00000000_00000010_00000000L
        };
        final long[] bBits = {
                0b00100000_00000000_00000010_00000000_00100000_00000000_00000010_00000000L,
                0b00000000_00000001_00000000_00000000_00000010_00000000_00000000_00000001L
        };

        assertEquals(
                28,
                BitUtils.findFirstMismatchedBit(aBits, 0, aBits.length * Long.SIZE, bBits, 0, Long.SIZE));
    }
}