package com.amobee.freebee.evaluator.index;

import java.util.Collection;

/**
 *  A simple interface for retrieving expression metadata by expression id,
 *  used internally in the evaluator and index.
 *
 * @author Kevin Doran
 */
public interface BEExpressionMetadataProvider
{
    BEExpressionMetadata get(int expressionId);

    Collection<BEExpressionMetadata> getAll();
}
