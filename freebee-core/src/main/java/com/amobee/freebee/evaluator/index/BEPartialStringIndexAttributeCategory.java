package com.amobee.freebee.evaluator.index;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.annotation.Nonnull;

import com.amobee.freebee.evaluator.BEInterval;
import com.amobee.freebee.util.trie.BitTrie;
import com.amobee.freebee.util.trie.ReverseStringBitKeyAnalyzer;
import com.amobee.freebee.util.trie.StringBitKeyAnalyzer;
import com.amobee.freebee.util.trie.Trie;

/**
 * Implementation of {@link BEIndexAttributeCategory} that performs partial string searches.
 *
 * @author Michael Bond
 */
public class BEPartialStringIndexAttributeCategory extends BEAbstractStringIndexAttributeCategory implements Serializable
{
    private static final long serialVersionUID = -6902572907432442226L;

    private final Trie<String, List<BEInterval>> values;

    public BEPartialStringIndexAttributeCategory(final boolean ignoreCase, final boolean reverse)
    {
        super(ignoreCase);
        this.values = new BitTrie<>(reverse ? new ReverseStringBitKeyAnalyzer() : new StringBitKeyAnalyzer());
    }

    @Override
    public void addInterval(@Nonnull final Object attributeValue, @Nonnull final BEInterval interval)
    {
        this.values.computeIfAbsent(getValue(attributeValue.toString()), key -> new ArrayList()).add(interval);

        // if interval is negative, add a "wildcard" interval
        if (interval.isNegative())
        {
            addNegativeInterval(interval);
        }
    }

    @Override
    public void getIntervals(@Nonnull final String attributeValue, @Nonnull final Consumer<List<BEInterval>> consumer)
    {
        this.values.getAll(getValue(attributeValue), entry -> consumer.accept(entry.getValue()));
    }

    public Trie<String, List<BEInterval>> getValues()
    {
        return this.values;
    }

    @Override
    protected void compact()
    {
        this.values.values().forEach(value -> ((ArrayList<BEInterval>) value).trimToSize());
    }
}
