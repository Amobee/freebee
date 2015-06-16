package com.amobee.freebee.solr;

import javax.annotation.Nonnull;

/**
 * SOLR field metadata which provides the SOLR field name and value converter.
 *
 * @author Michael Bond
 */
public class BESolrField
{
    private final String name;
    private final BESolrTypeConverter converter;

    /**
     * Create a new {@link BESolrField} instance.
     *
     * @param name
     *         SOLR field name
     * @param type
     *         SOLR field type, currently only double.class, Double.class, int.class, Integer.class, and String.class
     *         are supported.
     */
    public BESolrField(@Nonnull final String name, @Nonnull final Class<?> type)
    {
        this.name = name;
        this.converter = BEToSolrQueryConverter.CONVERTERS.get(type);
        if (this.converter == null)
        {
            throw new IllegalArgumentException("No converter for type " + type.getName());
        }
    }

    /**
     * Create a new {@link BESolrField} instance.
     *
     * @param name
     *         SOLR field name
     * @param converter
     *         TYpe converter.
     */
    public BESolrField(@Nonnull final String name, @Nonnull final BESolrDaypartConverter converter)
    {
        this.name = name;
        this.converter = converter;
    }

    public String getName()
    {
        return this.name;
    }

    public BESolrTypeConverter getConverter()
    {
        return this.converter;
    }
}
