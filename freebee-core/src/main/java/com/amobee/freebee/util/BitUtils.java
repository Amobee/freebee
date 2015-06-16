package com.amobee.freebee.util;

import javax.annotation.Nonnull;

/**
 * @author Michael Bond
 */
public class BitUtils
{
    /**
     * Used to shift left or right for a partial word mask
     */
    private static final long WORD_MASK = 0xffffffffffffffffL;

    /**
     * Determines if the specified bit is set in a key.
     *
     * @param bits
     *         Bit set to compare
     * @param offsetInBits
     *         The index of the bit to compare
     * @return True of the specified bit is set in the bit set, false if the specified bit is not set in the bit set.
     */
    public static boolean isBitSet(@Nonnull final long[] bits, final int offsetInBits)
    {
        return (bits[offsetInBits / Long.SIZE] & 1L << offsetInBits % Long.SIZE) != 0;
    }

    /**
     * Compares two long arrays.
     *
     * @param aBits
     *         'a' bits to compare
     * @param aStartInBits
     *         The first bit in 'a' to compare
     * @param aEndInBits
     *         Index after the last bit in key to compare.
     * @param bBits
     *         'b' bits to compare
     * @param bStartInBits
     *         The first bit in 'b' to compare
     * @param bEndInBits
     *         Index after the last bit in 'b' to compare.
     * @return Index of first bit that does not match relative to 'a', 'aEndInBits' if if all bits match and 'a' is
     * shorter than 'b', or 'bEndInBits' if all bits match and 'a' is longer than or equal in length to 'b'.
     */
    @SuppressWarnings("checkstyle:returncount")
    public static int findFirstMismatchedBit(
            @Nonnull final long[] aBits,
            final int aStartInBits,
            final int aEndInBits,
            @Nonnull final long[] bBits,
            final int bStartInBits,
            final int bEndInBits)
    {
        if (aStartInBits % Long.SIZE != bStartInBits % Long.SIZE)
        {
            throw new IllegalArgumentException("Key and other value bit offsets must be the same number of bits from the nearest long boundary");
        }

        // find the offset into the long array of both the key and other, rounding to the nearest long
        int aWordIndex = aStartInBits / Long.SIZE;
        int bWordIndex = bStartInBits / Long.SIZE;

        // find the number of long comparisons we can do for both
        final int bEndInWords = bEndInBits / Long.SIZE;
        final int aEndInWords = Math.min(aEndInBits / Long.SIZE, aWordIndex + bEndInWords - bWordIndex);

        // Compare full long values first. There will be some bits in the first long that technically don't need
        // to be compared but 'other' will contain these bits from the parent so it will still match.
        for (; aWordIndex < aEndInWords; ++aWordIndex, ++bWordIndex)
        {
            final long aWord = aBits[aWordIndex];
            final long bWord = bBits[bWordIndex];
            if (aWord != bWord)
            {
                // return first mismatched bit
                return aWordIndex * Long.SIZE + Long.numberOfTrailingZeros(aWord ^ bWord);
            }
        }

        // compare remaining bits if necessary
        final int aRemainingBits = aEndInBits - aWordIndex * Long.SIZE;
        final int bRemainingBits = bEndInBits - bWordIndex * Long.SIZE;
        final int remainingBits = Math.min(aRemainingBits, bRemainingBits);
        if (remainingBits > 0)
        {
            // mask bits that should not be compared
            final long mask = WORD_MASK >>> -remainingBits;
            final long aWord = aBits[aWordIndex] & mask;
            final long bWord = bBits[bWordIndex] & mask;

            if (aWord != bWord)
            {
                // return first mismatched bit
                return aWordIndex * Long.SIZE + Long.numberOfTrailingZeros(aWord ^ bWord);
            }
        }

        // we have a complete match
        return aRemainingBits < bRemainingBits ? aEndInBits : bEndInBits;
    }

    private BitUtils()
    {
    }
}
