package com.amobee.freebee.evaluator.evaluator;

import com.amobee.freebee.evaluator.BEInterval;
import com.amobee.freebee.evaluator.index.BEAttributeCategoryMatchedIntervalConsumer;
import com.amobee.freebee.evaluator.index.BEIndexAttributeCategory;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author Michael Bond
 */
public interface BEInputAttributeCategory extends Cloneable
{

    /**
     * Returns the name of the attribute category for these input values
     *
     * @return the attribute category name
     */
    String getName();

    /**
     * Iterates over all values in this input attribute category and invokes the consumer for each value that has
     * intervals in the specified index.
     *
     * @param indexAttributeCategory Index to find intervals matching the input values.
     * @param consumer               Consumer to call with matching intervals.
     * @deprecated Use {@link #forEachMatchedInterval(BEIndexAttributeCategory, BEAttributeCategoryMatchedIntervalConsumer)}.
     */
    @Deprecated
    void forEachMatchedInterval(
            @Nonnull BEIndexAttributeCategory indexAttributeCategory,
            @Nonnull Consumer<List<BEInterval>> consumer);

    void forEachMatchedInterval(
            @Nonnull BEIndexAttributeCategory indexAttributeCategory,
            @Nonnull BEAttributeCategoryMatchedIntervalConsumer consumer);

    <C extends BEInputAttributeCategory> void addAll(C other);

    BEInputAttributeCategory clone();
}
