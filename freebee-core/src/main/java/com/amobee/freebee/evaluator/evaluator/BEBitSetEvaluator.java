package com.amobee.freebee.evaluator.evaluator;

import com.amobee.freebee.evaluator.BEInfo;
import com.amobee.freebee.evaluator.index.BEIndex;
import com.amobee.freebee.evaluator.index.BEIndexMetrics;

import java.util.BitSet;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.eclipse.collections.api.map.primitive.IntObjectMap;
import org.eclipse.collections.api.map.primitive.MutableIntObjectMap;
import org.eclipse.collections.impl.factory.primitive.IntObjectMaps;
import org.eclipse.collections.impl.map.mutable.primitive.IntObjectHashMap;

/**
 * Expression evaluator runtime.
 *
 * The expression evaluator obtains intervals from the expression index that match the attribute values in the input.
 * It then evaluates the resulting intervals using the interval matching algorithm explained in the attached whitepaper
 * from <a href="https://videologygroup.atlassian.net/wiki/pages/viewpage.action?pageId=24119554">Design: Boolean Expressions & Evaluator</a>.
 *
 * Applications should never create instances of this class directly, use
 * {@link BEEvaluatorBuilder} instead.
 *
 * @author Michael Bond
 * @see BEEvaluatorBuilder
 * @see <a href="https://videologygroup.atlassian.net/wiki/pages/viewpage.action?pageId=24119554">Design: Boolean Expressions & Evaluator</a>
 */
@Deprecated  // migrate to BEHybridEvaluator
public class BEBitSetEvaluator<T> implements BEEvaluator<T>
{
    private static final long serialVersionUID = 2180648924047679090L;
    private static final String REF_INPUT_ATTRIBUTE_CATEGORY = "REFERENCE_EXPRESSIONS";

    private final BEIndex index;
    private final IntObjectMap<BEInfo<?>> partialExpressions;
    private final IntObjectMap<BEInfo<?>> expressions;

    /* package */ BEBitSetEvaluator(
            @Nonnull final List<BEInfo<String>> partialExpressions,
            @Nonnull final List<BEInfo<T>> expressions,
            @Nonnull final BEIndex index)
    {
        final MutableIntObjectMap<BEInfo<?>> tmpPartialExpressions = IntObjectMaps.mutable.empty();
        final MutableIntObjectMap<BEInfo<?>> tmpExpressions = IntObjectMaps.mutable.empty();
        partialExpressions.forEach(info -> tmpPartialExpressions.put(info.getExpressionId(), info));
        expressions.forEach(info -> tmpExpressions.put(info.getExpressionId(), info));
        this.partialExpressions = tmpPartialExpressions.toImmutable();
        this.expressions = tmpExpressions.toImmutable();
        this.index = index;
    }

    public BEIndex getIndex()
    {
        return this.index;
    }

    @Nullable
    @Override
    public BEIndexMetrics getMetrics()
    {
        return this.index.getIndexMetrics();
    }

    @Nonnull
    public Set<T> evaluate(@Nonnull final BEInput input)
    {
        final BEStringInputAttributeCategory refInputAttributeCategory = new BEStringInputAttributeCategory(REF_INPUT_ATTRIBUTE_CATEGORY);
        final MutableIntObjectMap<BitSet> indexResult = new IntObjectHashMap<>(this.index.getIndexMetrics().getExpressionCount());
        final Set<T> evaluatorResult = new HashSet<>();

        // get index results for the initial input
        this.index.getIndexResult(input, indexResult);

        // process partial expressions
        indexResult.forEachKeyValue((expressionId, matchedBits) ->
        {
            final BEInfo<?> beInfo = this.partialExpressions.get(expressionId);
            if (null != beInfo)
            {
                // compare the matched interval bits to the expected interval bits
                if (beInfo.getBits().equals(matchedBits))
                {
                    // add the matched partial expression to the set of references to evaluate
                    refInputAttributeCategory.add((String) beInfo.getData());
                }
            }
        });

        // get index results for the partial references
        this.index.getRefIndexResult(refInputAttributeCategory, indexResult);

        // process expressions
        indexResult.forEachKeyValue((expressionId, matchedBits) ->
        {
            final BEInfo<?> beInfo = this.expressions.get(expressionId);
            if (null != beInfo)
            {
                // compare the matched interval bits to the expected interval bits
                if (beInfo.getBits().equals(matchedBits))
                {
                    // add the matched expression to the result set
                    //noinspection unchecked
                    evaluatorResult.add((T) beInfo.getData());
                }
            }
        });

        return evaluatorResult;
    }

    @Nonnull
    @Override
    public BEEvaluatorResult<T> evaluateAndTrack(@Nonnull final BEInput input)
    {
        throw new UnsupportedOperationException(
                "This feature is not implemented in by the deprecated BitSetEvaluator. " +
                        "Please migrate to the BEHybridEvaluator.");
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        final BEBitSetEvaluator<?> that = (BEBitSetEvaluator<?>) o;
        return Objects.equals(this.index, that.index) &&
                Objects.equals(this.partialExpressions, that.partialExpressions) &&
                Objects.equals(this.expressions, that.expressions);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(this.index, this.partialExpressions, this.expressions);
    }
}
