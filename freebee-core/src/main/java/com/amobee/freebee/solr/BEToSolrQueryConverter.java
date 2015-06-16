package com.amobee.freebee.solr;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableMap;
import com.amobee.freebee.expression.BEAttributeValue;
import com.amobee.freebee.expression.BEConjunctionNode;
import com.amobee.freebee.expression.BEFormNormalizer;
import com.amobee.freebee.expression.BENode;
import com.amobee.freebee.expression.BEPredicateNode;
import com.amobee.freebee.expression.PositiveConjunctionFormNormalizer;

/**
 * Convert a {@link BENode} to a SOLR expression usable either as a query or filter query.
 *
 * @author Michael Bond
 */
public class BEToSolrQueryConverter
{
    private static final String SOLR_NOT_OPERATION = " (*:* NOT";

    /* package */ static final Map<Class<?>, BESolrTypeConverter> CONVERTERS = ImmutableMap.<Class<?>, BESolrTypeConverter>builder()
            .put(Integer.class, new BESolrIntConverter())
            .put(int.class, new BESolrIntConverter())
            .put(String.class, new BESolrStringConverter())
            .build();

    private BESolrAttributeMappingStore attributeMappingStore;
    private List<BEFormNormalizer> expressionFormNormalizers;

    public BEToSolrQueryConverter(@Nonnull final BESolrAttributeMappingStore attributeMappingStore)
    {
        this(attributeMappingStore, Collections.singletonList(new PositiveConjunctionFormNormalizer()));
        this.attributeMappingStore = attributeMappingStore;
    }

    public BEToSolrQueryConverter(
            final BESolrAttributeMappingStore attributeMappingStore,
            final List<BEFormNormalizer> expressionFormNormalizers)
    {
        this.attributeMappingStore = attributeMappingStore;
        this.expressionFormNormalizers = expressionFormNormalizers;
    }

    public void setAttributeMappingStore(@Nonnull final BESolrAttributeMappingStore attributeMappingStore)
    {
        this.attributeMappingStore = attributeMappingStore;
    }

    public void setExpressionFormNormalizers(final List<BEFormNormalizer> expressionFormNormalizers)
    {
        this.expressionFormNormalizers = expressionFormNormalizers;
    }

    /**
     * Convert a boolean expression to a SOLR query.
     *
     * @param expression
     *         Boolean expression to convert
     * @return SOLR query
     * @throws IllegalArgumentException
     *         if attribute is not mapped.
     */
    @Nonnull
    public String convert(@Nonnull final BENode expression) throws IllegalArgumentException
    {
        return convert(expression, o -> true, o -> true);
    }

    /**
     * Convert nodes within an expression that pass the specified predicate into a SOLR query.
     *
     * @param expression
     *         Boolean expression to convert
     * @param operationCheck
     *         Predicate to test operations, if predicate returns true then operation is added
     * @param predicateCheck
     *         Predicate to test nodes, if predicate returns true then node is added
     * @throws IllegalArgumentException
     *         if attribute is not mapped.
     * @return SOLR query
     */
    @Nonnull
    public String convert(
            @Nonnull final BENode expression,
            @Nonnull final Predicate<BENode> operationCheck,
            @Nonnull final Predicate<BENode> predicateCheck)
            throws IllegalArgumentException
    {
        final BENode normalizedExpresion = normalize(expression);
        final StringBuilder builder = new StringBuilder();
        if (normalizedExpresion instanceof BEConjunctionNode)
        {
            convertConjunction((BEConjunctionNode) normalizedExpresion, operationCheck, predicateCheck, builder);
        }
        else if (normalizedExpresion instanceof BEPredicateNode)
        {
            convertPredicate((BEPredicateNode) normalizedExpresion, builder);
        }
        else
        {
            throw new IllegalArgumentException("Unsupported root node type=" + normalizedExpresion.getType());
        }
        return builder.toString();
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

    private void convertConjunction(
            @Nonnull final BEConjunctionNode node,
            @Nonnull final Predicate<BENode> operationCheck,
            @Nonnull final Predicate<BENode> predicateCheck,
            @Nonnull final StringBuilder builder)
    throws IllegalArgumentException
    {
        final String solrOperation = ' ' + node.getType().toUpperCase();
        final int startSize = builder.length();

        node.getValues().stream().filter(operationCheck.or(predicateCheck)).forEach(
                child -> {

                    if (child instanceof BEConjunctionNode)
                    {
                        if (operationCheck.test(child))
                        {
                            final int opStartSize = builder.length();
                            if (builder.length() != startSize)
                            {
                                builder.append(solrOperation);
                            }
                            builder.append(" (");

                            final int opSize = builder.length();
                            convertConjunction((BEConjunctionNode) child, operationCheck, predicateCheck, builder);

                            // if builder length grew, add parenthesis
                            if (builder.length() != opSize)
                            {
                                builder.append(" )");
                            }
                            // if builder length did not grow then remove operation
                            else
                            {
                                builder.setLength(opStartSize);
                            }
                        }
                    }
                    else if (predicateCheck.test(child))
                    {
                        if (builder.length() != startSize)
                        {
                            builder.append(solrOperation);
                        }

                        convertPredicate((BEPredicateNode) child, builder);
                    }
                });
    }

    /**
     * Convert a predicate to a SOLR expression.
     *
     * @param node
     *         Node to convert
     * @param builder
     *         String builder to append converted query to.
     * @throws IllegalArgumentException
     *         if attribute is not mapped
     */
    private void convertPredicate(@Nonnull final BEPredicateNode node, @Nonnull final StringBuilder builder)
            throws IllegalArgumentException
    {
        if (node.isNegative())
        {
            builder.append(SOLR_NOT_OPERATION);
        }

        final List<BEAttributeValue> values = node.getValues();

        // look up the SOLR field the attribute in this predicate maps to
        final BESolrField solrField = this.attributeMappingStore.getSolrField(node.getType());
        if (solrField == null)
        {
            throw new IllegalArgumentException("No mapping to SOLR field for attribute " + node.getType());
        }
        solrField.getConverter().convert(solrField, values, builder);

        if (node.isNegative())
        {
            builder.append(')');
        }
    }
}
