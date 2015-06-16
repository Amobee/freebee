package com.amobee.freebee.solr;

import java.util.List;
import javax.annotation.Nonnull;

import com.amobee.freebee.expression.BEAttributeValue;

/**
 * Expression to SOLR value converter interface.
 *
 * @author Michael Bond
 */
public interface BESolrTypeConverter
{
    void convert(
            @Nonnull BESolrField solrField,
            @Nonnull List<BEAttributeValue> values,
            @Nonnull StringBuilder builder);
}
