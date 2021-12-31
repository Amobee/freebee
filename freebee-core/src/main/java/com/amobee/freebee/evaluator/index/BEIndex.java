package com.amobee.freebee.evaluator.index;

import com.amobee.freebee.config.BEDataTypeConfig;
import com.amobee.freebee.evaluator.BEInterval;
import com.amobee.freebee.evaluator.evaluator.BEInput;
import com.amobee.freebee.evaluator.evaluator.BEInputAttributeCategory;
import com.amobee.freebee.evaluator.evaluator.BEStringInputAttributeCategory;
import com.amobee.freebee.expression.BEConstants;
import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.api.map.primitive.IntObjectMap;
import org.eclipse.collections.api.map.primitive.MutableIntObjectMap;
import org.eclipse.collections.api.set.primitive.MutableIntSet;
import org.eclipse.collections.impl.factory.Maps;
import org.eclipse.collections.impl.map.mutable.primitive.IntObjectHashMap;
import org.eclipse.collections.impl.set.mutable.primitive.IntHashSet;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.BitSet;
import java.util.Set;

/**
 * Expression index runtime.
 *
 * The expression index is an optimization that limits the number of expressions that need to be evaluated to only those
 * expressions that reference attribute values found in an input such as a user profile. This particular implementation
 * goes further by breaking expressions into intervals, so that only intervals within the expressions in question that
 * have attribute value matches will be evaluated.
 *
 * Applications should never create instances of this class directly, use
 * {@link com.amobee.freebee.evaluator.evaluator.BEEvaluatorBuilder} instead.
 *
 * @author Michael Bond
 * @see com.amobee.freebee.evaluator.evaluator.BEEvaluatorBuilder
 * @see <a href="https://videologygroup.atlassian.net/wiki/pages/viewpage.action?pageId=24119554">Design: Boolean Expressions & Evaluator</a>
 */
public class BEIndex<T> implements Serializable
{
    private static final long serialVersionUID = -1533843770427265550L;
    private static final String REF_INPUT_ATTRIBUTE_CATEGORY = "REFERENCE_EXPRESSIONS";

    private BEExpressionMetadataProvider expressionMetadataProvider;
    private BEExpressionDataProvider<T> expressionDataProvider;
    private BEIndexMetrics indexMetrics;
    private int hashCode;

    private final MutableMap<String, BEIndexAttributeCategory> attributeCategories = Maps.mutable.of();
    private final BEIndexAttributeCategory refAttributeCategory = new BEStringIndexAttributeCategory(false);
    private final BEDataTypeConfigSupplier dataTypeConfigSupplier;

    BEIndex(@Nonnull final BEDataTypeConfigSupplier dataTypeConfigSupplier)
    {
        this.dataTypeConfigSupplier = dataTypeConfigSupplier;
    }

    BEExpressionMetadataProvider getExpressionMetadataProvider()
    {
        return this.expressionMetadataProvider;
    }

    void setExpressionMetadataProvider(final BEExpressionMetadataProvider expressionMetadataProvider)
    {
        this.expressionMetadataProvider = expressionMetadataProvider;
    }

    public BEExpressionDataProvider<T> getExpressionDataProvider()
    {
        return this.expressionDataProvider;
    }

    void setExpressionDataProvider(final BEExpressionDataProvider<T> expressionDataProvider)
    {
        this.expressionDataProvider = expressionDataProvider;
    }

    public T getExpressionsData(final int expressionId)
    {
        return this.expressionDataProvider.get(expressionId);
    }

    public BEIndexMetrics getIndexMetrics()
    {
        return this.indexMetrics;
    }

    void setIndexMetrics(final BEIndexMetrics indexMetrics)
    {
        this.indexMetrics = indexMetrics;
    }

    void setHashCode(final int hashCode)
    {
        this.hashCode = hashCode;
    }

    @Override
    public int hashCode()
    {
        return this.hashCode;
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
        final BEIndex<?> beIndex = (BEIndex<?>) o;
        return this.hashCode == beIndex.hashCode;
    }

    /**
     * Gets the attribute category for the specified attribute category name. If one does not already exist it is
     * created.
     *
     * @param attributeCategory
     *         Name of attribute category to get.
     * @return Attribute category that matches the specified name.
     */
    @Nonnull
    public BEIndexAttributeCategory getOrAddAttributeCategory(@Nonnull final String attributeCategory)
    {
        if (BEConstants.NODE_TYPE_REFERENCE.equalsIgnoreCase(attributeCategory))
        {
            return this.refAttributeCategory;
        }
        return this.attributeCategories.getIfAbsentPut(attributeCategory.toUpperCase(), () -> newAttributeCategory(attributeCategory.toUpperCase()));
    }

    /**
     * Gets the index result for the specified input.
     *
     * @param input
     *         Input attribute/value assignments to get results for
     * @return Index result for the specified input
     */
    @Deprecated  // migrate to findMatchingExpressionIntervals
    @Nonnull
    public IntObjectMap<BitSet> getIndexResult(@Nonnull final BEInput input)
    {
        final MutableIntObjectMap<BitSet> indexResult = new IntObjectHashMap<>(this.indexMetrics.getExpressionCount());

        getIndexResult(input, indexResult);

        return indexResult;
    }

    /**
     * Gets the index result for the specified input.
     *
     * @param input
     *         Input attribute/value assignments to get results for.
     * @param indexResult
     *         Index result for the specified input.
     */
    @Deprecated  // migrate to findMatchingExpressionIntervals
    public void getIndexResult(@Nonnull final BEInput input, @Nonnull final MutableIntObjectMap<BitSet> indexResult)
    {
        this.attributeCategories.forEach((attributeCategory, indexAttributeCategory) -> evaluateAttributeCategory(
                input.getCategory(attributeCategory),
                indexAttributeCategory,
                indexResult,
                new IntHashSet(this.indexMetrics.getIntervalCount())));
    }

    /**
     * Gets the index result for references.
     *
     * @param inputAttributeCategory
     *         Reference input attribute category to get results for.
     * @param indexResult
     *         Index result for the specified input.
     */
    @Deprecated  // migrate to addRefIntervalsForMatchedPartialExpressions
    public void getRefIndexResult(
            @Nonnull final BEInputAttributeCategory inputAttributeCategory,
            @Nonnull final MutableIntObjectMap<BitSet> indexResult)
    {
        evaluateAttributeCategory(
                inputAttributeCategory,
                this.refAttributeCategory,
                indexResult,
                new IntHashSet(this.indexMetrics.getIntervalCount()));
    }

    /**
     * Adds any matching intervals for the specified attribute values to the specified result.
     *
     * @param inputAttributeCategory
     *         Input attribute category to get index results for.
     * @param indexAttributeCategory
     *         Index attribute category to get intervals from.
     * @param indexResult
     *         Index result for the specified input.
     * @param negativeIntervalIds
     *         Map of negative intervals that matched the attribute values.
     */
    @Deprecated  // migrate to new method based on BEIndexResult
    private void evaluateAttributeCategory(
            @Nullable final BEInputAttributeCategory inputAttributeCategory,
            @Nonnull final BEIndexAttributeCategory indexAttributeCategory,
            @Nonnull final MutableIntObjectMap<BitSet> indexResult,
            @Nonnull final MutableIntSet negativeIntervalIds)
    {
        negativeIntervalIds.clear();

        if (null != inputAttributeCategory)
        {
            // add intervals that match values of this attribute category in the input
            inputAttributeCategory.forEachMatchedInterval(indexAttributeCategory, intervals ->
            {
                for (final BEInterval interval : intervals)
                {
                    if (interval.isNegative())
                    {
                        // keep track of the fact that this negative interval was matched
                        negativeIntervalIds.add(interval.getIntervalId());
                    }
                    else
                    {
                        // add the interval to the results
                        indexResult.getIfAbsentPut(interval.getExpressionId(), BitSet::new).or(interval.getBits());
                    }
                }
            });
        }

        // add intervals for all unmatched negative predicates for this attribute
        // @formatter:off
        indexAttributeCategory.getNegativeIntervals()
                .stream()
                .filter(interval -> !negativeIntervalIds.contains(interval.getIntervalId()))
                .forEach(interval -> indexResult.getIfAbsentPut(interval.getExpressionId(), BitSet::new).or(interval.getBits()));
        // @formatter:on
    }

    public BEIndexResults findMatchingExpressionIntervals(@Nonnull final BEInput input)
    {
        final BEIndexResults indexResults = new BEIndexResults(this.expressionMetadataProvider);

        this.attributeCategories.forEach((attributeCategoryName, indexAttributeCategory) ->
                evaluateAttributeCategory(input.getCategory(attributeCategoryName), indexAttributeCategory, indexResults));

        return indexResults;
    }

    public void addRefIntervalsForMatchedPartialExpressions(
            @Nonnull final Set<String> matchedPartialExpressionNames,
            @Nonnull final BEIndexResults indexResults)
    {
        final BEStringInputAttributeCategory refInputAttributeCategory = new BEStringInputAttributeCategory(REF_INPUT_ATTRIBUTE_CATEGORY);
        matchedPartialExpressionNames.forEach(refInputAttributeCategory::add);
        evaluateAttributeCategory(refInputAttributeCategory, this.refAttributeCategory, indexResults);
    }

    /**
     * Adds any matching intervals for the specified attribute category values to the specified result.
     *
     * @param inputAttributeCategory Input attribute category for which the index must be evaluated
     * @param indexAttributeCategory Index attribute category to evaluator for matching intervals
     * @param indexResults Output parameter containing the matched intervals
     */
    private void evaluateAttributeCategory(
            @Nullable final BEInputAttributeCategory inputAttributeCategory,
            @Nonnull final BEIndexAttributeCategory indexAttributeCategory,
            @Nonnull final BEIndexResults indexResults)
    {
        final MutableIntSet matchedNegativeIntervalIds = new IntHashSet();

        if (null != inputAttributeCategory)
        {
            // add intervals that match values of this attribute category in the input
            inputAttributeCategory.forEachMatchedInterval(indexAttributeCategory, (matchedInputValue, matchedIntervals) ->
            {
                for (final BEInterval interval : matchedIntervals)
                {
                    if (interval.isNegative())
                    {
                        // keep track of the fact that this negative interval was matched
                        matchedNegativeIntervalIds.add(interval.getIntervalId());
                    }
                    else
                    {
                        indexResults.addInterval(interval, matchedInputValue);
                    }
                }
            });
        }

        // add intervals for all unmatched negative predicates for this attribute
        indexAttributeCategory.getNegativeIntervals()
                .stream()
                .filter(interval -> !matchedNegativeIntervalIds.contains(interval.getIntervalId()))
                .forEach(indexResults::addInterval);
    }

    private BEIndexAttributeCategory newAttributeCategory(@Nonnull final String attributeCategory)
    {
        if (BEConstants.RESERVED_TYPES.contains(attributeCategory))
        {
            throw new IllegalArgumentException("Illegal use of reserved attribute category " + attributeCategory);
        }

        final BEDataTypeConfig dataTypeConfig = this.dataTypeConfigSupplier.get(attributeCategory);
        return dataTypeConfig.getDataType().newIndexAttributeCategory(dataTypeConfig);
    }

    public void compact()
    {
        this.attributeCategories.forEach(BEIndexAttributeCategory::compact);
        this.refAttributeCategory.compact();
    }
}
