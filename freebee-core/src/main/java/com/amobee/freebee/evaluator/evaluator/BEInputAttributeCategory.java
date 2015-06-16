package com.amobee.freebee.evaluator.evaluator;

import java.util.List;
import java.util.function.Consumer;
import javax.annotation.Nonnull;

import com.amobee.freebee.evaluator.BEInterval;
import com.amobee.freebee.evaluator.index.BEIndexAttributeCategory;

/**
 * @author Michael Bond
 */
public interface BEInputAttributeCategory extends Cloneable
{
    /**
     * Iterates over all values in this input attribute category and invokes the consumer for each value that has
     * intervals in the specified index.
     *
     * @param indexAttributeCategory
     *         Index to find intervals matching the input values.
     * @param consumer
     *         Consumer to call with matching intervals.
     */
    void forEachMatchedInterval(
            @Nonnull BEIndexAttributeCategory indexAttributeCategory,
            @Nonnull Consumer<List<BEInterval>> consumer);

    BEInputAttributeCategory clone();
}
