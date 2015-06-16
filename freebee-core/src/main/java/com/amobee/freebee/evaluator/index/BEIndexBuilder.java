package com.amobee.freebee.evaluator.index;

import com.amobee.freebee.config.BEDataTypeConfig;
import com.amobee.freebee.evaluator.BEInterval;
import com.amobee.freebee.evaluator.interval.BEDefaultIntervalLabeler;
import com.amobee.freebee.evaluator.interval.BEDefaultIntervalOptimizer;
import com.amobee.freebee.evaluator.interval.BEIntervalLabeler;
import com.amobee.freebee.evaluator.interval.BEIntervalOptimizer;
import com.amobee.freebee.evaluator.interval.BENodeInterval;
import com.amobee.freebee.expression.BEConstants;
import com.amobee.freebee.expression.BEFormNormalizer;
import com.amobee.freebee.expression.BENode;
import com.amobee.freebee.expression.BEPredicateNode;
import com.amobee.freebee.expression.BEReferenceNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nonnull;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.amobee.freebee.expression.PositiveConjunctionFormNormalizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Expression index builder.
 *
 * Applications should never need to use this directly, use
 * {@link com.amobee.freebee.evaluator.evaluator.BEEvaluatorBuilder} instead.
 *
 * @author Michael Bond
 * @see com.amobee.freebee.evaluator.evaluator.BEEvaluatorBuilder
 */
@SuppressWarnings("illegaltype")
public class BEIndexBuilder<T>
{
    private static final Logger logger = LoggerFactory.getLogger(BEIndexBuilder.class);

    private static final ObjectMapper MAPPER = new ObjectMapper();

    static
    {
        MAPPER.configure(SerializationFeature.FAIL_ON_UNWRAPPED_TYPE_IDENTIFIERS, false);
    }

    private final BEIntervalLabeler intervalLabeler;
    private final BEIntervalOptimizer intervalOptimizer;
    private final BEExpressionHashProvider<T> hashProvider;
    private final List<BEFormNormalizer> expressionFormNormalizers;

    private final List<BEDataTypeConfig> dataTypeConfigs = new ArrayList<>();
    private final List<BEExpressionInfo<T>> expressions = new ArrayList<>();
    private final Map<String, BENode> partialExpressions = new HashMap<>();

    private boolean defaultToCaseInsensitiveDataTypeConfig;

    public BEIndexBuilder()
    {
        this(
                new BEDefaultIntervalLabeler(),
                new BEDefaultIntervalOptimizer(),
                new BEDefaultExpressionHashProvider<>(),
                Collections.singletonList(new PositiveConjunctionFormNormalizer())
        );
    }

    BEIndexBuilder(
            final BEIntervalLabeler intervalLabeler,
            final BEIntervalOptimizer intervalOptimizer,
            final BEExpressionHashProvider<T> hashProvider,
            final List<BEFormNormalizer> formNormalizers)
    {
        this.intervalLabeler = intervalLabeler;
        this.intervalOptimizer = intervalOptimizer;
        this.hashProvider = hashProvider;
        this.expressionFormNormalizers = formNormalizers;
    }

    /**
     * Add data type configuration for an attribute category. Any attribute categories that don't have data type
     * configurations will default to the "string" data type.
     *
     * @param dataTypeConfig
     *         Data type configuration to add.
     * @return this
     * @throws IOException
     *         if the JSON cannot be parsed.
     */
    @SuppressWarnings("checkstyle:LeftCurly")
    public BEIndexBuilder<T> addDataTypeConfig(@Nonnull final String dataTypeConfig) throws IOException
    {
        try
        {
            return addDataTypeConfig(MAPPER.readValue(dataTypeConfig, BEDataTypeConfig.class));
        }
        catch (final IOException ignored)
        {
            return addDataTypeConfigs(MAPPER.readValue(
                    dataTypeConfig,
                    new TypeReference<List<BEDataTypeConfig>>() { }));
        }
    }

    /**
     * Add data type configuration for an attribute category. Any attribute categories that don't have data type
     * configurations will default to the "string" data type.
     *
     * @param dataTypeConfig
     *         Data type configuration to add.
     * @return this
     */
    public BEIndexBuilder<T> addDataTypeConfig(@Nonnull final BEDataTypeConfig dataTypeConfig)
    {
        this.dataTypeConfigs.add(dataTypeConfig);
        return this;
    }

    /**
     * Add a collection of data type configurations for an attribute category. Any attribute categories that don't have
     * data type configurations will default to the "string" data type.
     *
     * @param dataTypeConfigs
     *         Data type configurations to add.
     * @return this
     */
    public BEIndexBuilder<T> addDataTypeConfigs(@Nonnull final List<BEDataTypeConfig> dataTypeConfigs)
    {
        this.dataTypeConfigs.addAll(dataTypeConfigs);
        return this;
    }

    /**
     * Add an expression to the builder to be included in the resulting BooleanExpression index.
     *
     * @param data
     *         Data to associate with this expression
     * @param expression
     *         Expression to add to the index
     * @return this
     * @throws IOException
     *         if the JSON cannot be parsed.
     */
    @Nonnull
    public BEIndexBuilder<T> addExpression(@Nonnull final T data, @Nonnull final String expression) throws IOException
    {
        return addExpression(data, MAPPER.readValue(expression, BENode.class));
    }

    /**
     * Add an expression to the builder to be included in the resulting BooleanExpression index.
     *
     * @param data
     *         Data to associate with this expression
     * @param expression
     *         Expression to add to the index
     * @return this
     */
    @Nonnull
    public BEIndexBuilder<T> addExpression(@Nonnull final T data, @Nonnull final BENode expression)
    {
        this.expressions.add(new BEExpressionInfo<>(data, expression));
        return this;
    }

    /**
     * Removes expressions from the builder associated with the specified data.
     *
     * @param data
     *         The data associated with the expressions to remove
     * @return this
     */
    @Nonnull
    public BEIndexBuilder<T> removeExpressions(@Nonnull final T data)
    {
        this.expressions.removeIf(info -> info.getData().equals(data));
        return this;
    }

    /**
     * Add a partial expression to the builder that can then be used in a reference node in another normal or partial
     * expression.
     *
     * @param expression
     *         Expression to add. Expression must contain 'id' property.
     * @return this
     * @throws IOException
     *         if the JSON cannot be parsed.
     */
    @Nonnull
    public BEIndexBuilder<T> addPartialExpression(@Nonnull final String expression)
    throws IOException
    {
        @SuppressWarnings("unchecked")
        final BENode value = MAPPER.readValue(expression, BENode.class);
        if (null == value.getId())
        {
            throw new IllegalArgumentException("Expression must contain 'id' property: " + expression);
        }

        return addPartialExpression(value.getId(), expression);
    }

    /**
     * Add a partial expression to the builder that can then be used in a reference node in another normal or partial
     * expression.
     *
     * @param id
     *         Id of partial expression to add.
     * @param expression
     *         Expression to add.
     * @return this
     * @throws IOException
     *         if the JSON cannot be parsed.
     */
    @Nonnull
    public BEIndexBuilder<T> addPartialExpression(@Nonnull final String id, @Nonnull final String expression)
    throws IOException
    {
        return addPartialExpression(id, MAPPER.readValue(expression, BENode.class));
    }

    /**
     * Add a partial expression to the builder that can then be used in a reference node in another normal or partial
     * expression.
     *
     * @param id
     *         Id of partial expression to add.
     * @param expression
     *         Expression to add.
     * @return this
     */
    @Nonnull
    public BEIndexBuilder<T> addPartialExpression(@Nonnull final String id, @Nonnull final BENode expression)
    {
        this.partialExpressions.put(id, expression);
        return this;
    }

    /**
     * Remove a partial expression from the builder associated with the specified id.
     *
     * @param id
     *         Id of partial expression to remove.
     * @return this
     */
    @Nonnull
    public BEIndexBuilder<T> removePartialExpression(@Nonnull final String id)
    {
        this.partialExpressions.remove(id);
        return this;
    }

    /**
     * Whether or not unspecified data types should default to case insensitive matching.
     *
     * @param caseInsensitive - true for caseInsensitive, false (default) for caseSensitive
     * @return the Builder
     */
    public BEIndexBuilder<T> caseInsensitive(final boolean caseInsensitive)
    {
        this.defaultToCaseInsensitiveDataTypeConfig = caseInsensitive;
        return this;
    }

    @Nonnull
    public BEIndex<T> build()
    {

        final BEIndexIntervalMaker indexIntervalMaker = new BEIndexIntervalMaker();
        final BEExpressionMetadataProviderImpl expressionMetadataProvider = new BEExpressionMetadataProviderImpl();
        final BEExpressionDataProviderImpl<T> expressionDataProvider = new BEExpressionDataProviderImpl<>();

        final BEDataTypeConfigSupplier dataTypeConfigSupplier = BEDataTypeConfigLookupSupplier.builder()
                .addAllDataTypeConfigs(this.dataTypeConfigs)
                .caseSensitive(false)
                .defaultDataTypeConfig(
                        this.defaultToCaseInsensitiveDataTypeConfig
                                ? BEDataTypeConfigLookupSupplier.CASE_INSENSITIVE_STRING_SUPPLIER
                                : BEDataTypeConfigLookupSupplier.CASE_SENSITIVE_STRING_SUPPLIER
                ).build();

        final BEIndex<T> index = new BEIndex<>(dataTypeConfigSupplier);

        final AtomicInteger nextExpressionId = new AtomicInteger(0);
        final AtomicInteger nextIntervalId = new AtomicInteger(0);

        // The following two foreach blocks are very similar.
        // The first processes partial expressions that are referenced from the full expressions
        // The second processes full (non partial) expression
        // TODO, investigate extracting the common logic from partial/full expression indexing into shared code

        // add partial expression intervals to index
        this.partialExpressions.forEach((partialExpressionName, partialExpression) -> {
            final int expressionId = nextExpressionId.getAndIncrement();
            final BENode normalizedPartialExpression = normalize(partialExpression);
            final short intervalLength = normalizedPartialExpression.getNumPredicates();
            final Collection<BENodeInterval> intervals = this.intervalLabeler.labelExpression(normalizedPartialExpression);

            final boolean useBitSetMatchingAlgorithm =
                    this.intervalOptimizer.canUseBitSetMatching(intervalLength, intervals);

            intervals.forEach(interval -> {
                final int intervalId = nextIntervalId.getAndIncrement();
                final BEExpressionMetadata expressionMetadata =
                        new BEExpressionMetadataImpl(expressionId, intervalLength, useBitSetMatchingAlgorithm, true, partialExpressionName);

                final BENode leafNode = interval.getNode();
                final BEInterval indexInterval = indexIntervalMaker.make(expressionId, intervalId, useBitSetMatchingAlgorithm, interval);
                switch (leafNode.getType().toUpperCase())
                {
                    case BEConstants.NODE_TYPE_AND:
                    case BEConstants.NODE_TYPE_OR:
                        throw new IllegalStateException("Unexpected conjunction node for interval (only leaf nodes expected here)");
                    case BEConstants.NODE_TYPE_REFERENCE:
                            throw new IllegalArgumentException(
                                    "Reference to partial expression found in partial expression '" + expressionId +
                                            "'. Partial expressions may not contain references.");
                    default:
                        addPredicate(index, (BEPredicateNode) leafNode, indexInterval);
                        expressionMetadataProvider.put(expressionId, expressionMetadata);
                        break;
                }
            });
        });

        // add expression intervals to index
        this.expressions.forEach(expressionInfo -> {
            final int expressionId = nextExpressionId.getAndIncrement();
            final BENode normalizedExpression = normalize(expressionInfo.getExpression());
            final short intervalLength = normalizedExpression.getNumPredicates();
            final Collection<BENodeInterval> intervals = this.intervalLabeler.labelExpression(normalizedExpression);

            final boolean useBitSetMatchingAlgorithm =
                    this.intervalOptimizer.canUseBitSetMatching(intervalLength, intervals);

            intervals.forEach(interval -> {
                final int intervalId = nextIntervalId.getAndIncrement();
                final BEExpressionMetadata expressionMetadata =
                        new BEExpressionMetadataImpl(expressionId, intervalLength, useBitSetMatchingAlgorithm);

                final BENode leafNode = interval.getNode();
                final BEInterval indexInterval = indexIntervalMaker.make(expressionId, intervalId, useBitSetMatchingAlgorithm, interval);
                switch (leafNode.getType().toUpperCase())
                {
                    case BEConstants.NODE_TYPE_AND:
                    case BEConstants.NODE_TYPE_OR:
                        throw new IllegalStateException("Unexpected conjunction node for interval (only leaf nodes expected here)");
                    case BEConstants.NODE_TYPE_REFERENCE:
                        addReference(index, (BEReferenceNode) leafNode, indexInterval);
                        expressionDataProvider.put(expressionId, expressionInfo.getData());
                        expressionMetadataProvider.put(expressionId, expressionMetadata);
                        break;
                    default:
                        addPredicate(index, (BEPredicateNode) leafNode, indexInterval);
                        expressionDataProvider.put(expressionId, expressionInfo.getData());
                        expressionMetadataProvider.put(expressionId, expressionMetadata);
                        break;
                }
            });

        });

        final int expressionCount = nextExpressionId.get();
        final int intervalCount = nextIntervalId.get();
        final int bitSetExpressions = (int) expressionMetadataProvider.getAll().stream().filter(BEExpressionMetadata::canUseBitSetMatching).count();
        final int intervalExpressions = expressionCount - bitSetExpressions;

        final BEIndexMetrics metrics = new BEIndexMetrics();
        metrics.setExpressionCount(expressionCount);
        metrics.setFullExpressionCount(this.expressions.size());
        metrics.setPartialExpressionCount(this.partialExpressions.size());
        metrics.setIntervalCount(intervalCount);
        metrics.setExpressionCountWithBitSetEvaluation(bitSetExpressions);
        metrics.setExpressionCountWithIntervalEvaluation(intervalExpressions);

        final int hashCode = this.hashProvider.computeHash(this.expressions, this.partialExpressions, dataTypeConfigSupplier);

        index.setHashCode(hashCode);
        index.setIndexMetrics(metrics);
        index.setExpressionMetadataProvider(expressionMetadataProvider);
        index.setExpressionDataProvider(expressionDataProvider);
        index.compact();

        logger.debug("Finished building index: {}", metrics);

        return index;
    }

    private BENode normalize(final BENode node)
    {
        BENode normalizedExpresion = node;
        for (final BEFormNormalizer normalizer : this.expressionFormNormalizers)
        {
            if (normalizer.canNormalize(normalizedExpresion))
            {
                normalizedExpresion = normalizer.normalize(normalizedExpresion);
            }
        }
        return normalizedExpresion;
    }

    private void addPredicate(
            @Nonnull final BEIndex<T> index,
            @Nonnull final BEPredicateNode node,
            @Nonnull final BEInterval interval)
    {
        final String nodeType = node.getType();
        if (nodeTypeIsReservedType(nodeType))
        {
            throw new IllegalArgumentException(nodeType + " cannot be used as an attribute category because it is reserved");
        }
        final BEIndexAttributeCategory indexAttributeCategory = index.getOrAddAttributeCategory(nodeType);

        // add values for this attribute category to the index
        node.getValues().forEach(v -> indexAttributeCategory.addInterval(v.getId(), interval));
    }

    private void addReference(
            @Nonnull final BEIndex<T> index,
            @Nonnull final BEReferenceNode node,
            @Nonnull final BEInterval interval)
    {
        final String nodeType = node.getType();
        if (!nodeType.equalsIgnoreCase(BEConstants.NODE_TYPE_REFERENCE))
        {
            throw new IllegalArgumentException("When adding a reference node, the node type must be '"
                    + BEConstants.NODE_TYPE_REFERENCE + "'");
        }

        final BEIndexAttributeCategory indexAttributeCategory = index.getOrAddAttributeCategory(nodeType.toUpperCase());
        node.getValues().forEach(value ->
        {
            final String id = value.getId();
            if (!this.partialExpressions.containsKey(id))
            {
                throw new IllegalArgumentException("Reference to undefined partial expression " + id);
            }
            else
            {
                // add the reference id to the index
                indexAttributeCategory.addInterval(id, interval);
            }
        });
    }

    private boolean nodeTypeIsReservedType(final String nodeType)
    {
        return BEConstants.RESERVED_TYPES.contains(nodeType.toUpperCase());
    }

}
