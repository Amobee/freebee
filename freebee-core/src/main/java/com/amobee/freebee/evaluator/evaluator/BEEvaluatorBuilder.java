package com.amobee.freebee.evaluator.evaluator;

import com.amobee.freebee.config.BEDataTypeConfig;
import com.amobee.freebee.evaluator.index.BEIndexBuilder;
import com.amobee.freebee.expression.BENode;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;

/**
 * Expression evaluator builder.
 *
 * This is the interface that applications will use to create an expression evaluator to be used to evaluate a set of
 * expressions against some input such as a user profile. The resulting evaluator is thread safe so it only needs to be
 * created once for a set of expressions.
 *
 * @author Michael Bond
 */
@SuppressWarnings("unused")
public class BEEvaluatorBuilder<T>
{
    private final BEIndexBuilder<T> indexBuilder = new BEIndexBuilder<>();

    public BEEvaluatorBuilder()
    {
    }

    /**
     * Add data type configuration for an attribute category. Any attribute categories that don't have data type
     * configurations will default to the "string" data type.
     *
     * @param dataTypeConfig
     *         Data type configuration to add.
     * @return this
     * @throws IOException if the JSON cannot be parsed.
     */
    public BEEvaluatorBuilder<T> addDataTypeConfig(@Nonnull final String dataTypeConfig) throws IOException
    {
        this.indexBuilder.addDataTypeConfig(dataTypeConfig);
        return this;
    }

    /**
     * Add data type configuration for an attribute category. Any attribute categories that don't have data type
     * configurations will default to the "string" data type.
     *
     * @param dataTypeConfig
     *         Data type configuration to add.
     * @return this
     */
    public BEEvaluatorBuilder<T> addDataTypeConfig(@Nonnull final BEDataTypeConfig dataTypeConfig)
    {
        this.indexBuilder.addDataTypeConfig(dataTypeConfig);
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
    public BEEvaluatorBuilder<T> addDataTypeConfigs(@Nonnull final List<BEDataTypeConfig> dataTypeConfigs)
    {
        this.indexBuilder.addDataTypeConfigs(dataTypeConfigs);
        return this;
    }

    /**
     * Add an expression to the builder to be included in the resulting expression index.
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
    public BEEvaluatorBuilder<T> addExpression(@Nonnull final T data, @Nonnull final String expression) throws IOException
    {
        this.indexBuilder.addExpression(data, expression);
        return this;
    }

    /**
     * Add an expression to the builder to be included in the resulting expression index.
     *
     * @param data
     *         Data to associate with the expression
     * @param expression
     *         Expression to add to the index
     * @return this
     */
    @Nonnull
    public BEEvaluatorBuilder<T> addExpression(@Nonnull final T data, @Nonnull final BENode expression)
    {
        this.indexBuilder.addExpression(data, expression);
        return this;
    }

    /**
     * Remove expressions from the builder associated with the specified data.
     *
     * @param data
     *         The data associated with the expressions to remove
     * @return this
     */
    @Nonnull
    public BEEvaluatorBuilder<T> removeExpressions(@Nonnull final T data)
    {
        this.indexBuilder.removeExpressions(data);
        return this;
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
    public BEEvaluatorBuilder<T> addPartialExpression(@Nonnull final String id, @Nonnull final String expression)
    throws IOException
    {
        this.indexBuilder.addPartialExpression(id, expression);
        return this;
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
    public BEEvaluatorBuilder<T> addPartialExpression(@Nonnull final String id, @Nonnull final BENode expression)
    {
        this.indexBuilder.addPartialExpression(id, expression);
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
    public BEEvaluatorBuilder<T> removePartialExpression(@Nonnull final String id)
    {
        this.indexBuilder.removePartialExpression(id);
        return this;
    }

    /**
     * Whether or not unspecified data types should default to case insensitive matching.
     *
     * @param caseInsensitive - true for caseInsensitive, false (default) for caseSensitive
     * @return this Builder
     */
    @Nonnull
    public BEEvaluatorBuilder<T> caseInsensitive(final boolean caseInsensitive)
    {
        this.indexBuilder.caseInsensitive(caseInsensitive);
        return this;
    }

    @Nonnull
    public BEEvaluator<T> build()
    {
        final BEHybridEvaluator<T> evaluator = new BEHybridEvaluator<>(this.indexBuilder.build());
        return evaluator;
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
        final BEEvaluatorBuilder<?> that = (BEEvaluatorBuilder<?>) o;
        return Objects.equals(this.indexBuilder, that.indexBuilder);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(this.indexBuilder);
    }
}
