package com.amobee.freebee.util.trie;

import javax.annotation.Nonnull;

/**
 * @author Michael Bond
 */
public class ReverseStringBitKeyAnalyzer extends StringBitKeyAnalyzer
{
    private static final long serialVersionUID = 1433219037051740805L;

    @Override
    public boolean isBitSet(@Nonnull final String key, final int indexInBits)
    {
        return (key.charAt(key.length() - indexInBits / Character.SIZE - 1) & 1L << indexInBits % Character.SIZE) != 0;
    }

    @Override
    public long getElement(@Nonnull final String key, final int index)
    {
        return key.charAt(key.length() - index - 1);
    }

    @Nonnull
    @Override
    public String trimToSize(@Nonnull final String key, final int lengthInBits)
    {
        final int length = lengthInBits / Character.SIZE + (lengthInBits % Character.SIZE > 0 ? 1 : 0);
        final int keyLength = key.length();
        if (keyLength > length)
        {
            return key.substring(keyLength - length, keyLength);
        }
        return key;
    }
}
