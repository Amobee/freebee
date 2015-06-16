package com.amobee.freebee.solr;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Data store that maps between attribute names in boolean expressions and SOLR field names.
 *
 * @author Michael Bond
 */
public interface BESolrAttributeMappingStore
{
    /**
     * Get the SOLR field name for the specified attribute.
     *
     * @param attribute
     *         Attribute to get SOLR field name for
     * @return SOLR field mapping information or null if the specified attribute has no mapping.
     */
    @Nullable
    BESolrField getSolrField(@Nonnull String attribute);
}
