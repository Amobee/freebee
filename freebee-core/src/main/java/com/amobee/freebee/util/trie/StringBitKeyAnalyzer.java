package com.amobee.freebee.util.trie;

import javax.annotation.Nonnull;

/**
 * Implementation of {@link BitKeyAnalyzer} for strings.
 *
 * @author Michael Bond
 */
public class StringBitKeyAnalyzer implements BitKeyAnalyzer<String>
{
    private static final long serialVersionUID = -4039946442026365947L;

    @Override
    public boolean isBitSet(@Nonnull final String key, final int indexInBits)
    {
        return (key.charAt(indexInBits / Character.SIZE) & 1L << indexInBits % Character.SIZE) != 0;
    }

    @Override
    public int getLengthInBits(@Nonnull final String key)
    {
        return key.length() * Character.SIZE;
    }

    @Override
    public long getElement(@Nonnull final String key, final int index)
    {
        return key.charAt(index);
    }

    @Override
    public int getElementSizeInBits()
    {
        return Character.SIZE;
    }

    @Nonnull
    @Override
    public String trimToSize(@Nonnull final String key, final int lengthInBits)
    {
        final int length = lengthInBits / Character.SIZE + (lengthInBits % Character.SIZE > 0 ? 1 : 0);
        if (key.length() > length)
        {
            return key.substring(0, length);
        }
        return key;
    }
}
