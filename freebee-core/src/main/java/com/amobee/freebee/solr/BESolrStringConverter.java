package com.amobee.freebee.solr;

import java.util.List;
import javax.annotation.Nonnull;

import com.amobee.freebee.expression.BEAttributeValue;

/**
 * Convert a string set to either {field}:"{value}" or {field}:("{value}"[ "{value}"]...).
 *
 * @author Michael Bond
 */
public class BESolrStringConverter implements BESolrTypeConverter
{
    public BESolrStringConverter()
    {
    }

    @Override
    public void convert(
            @Nonnull final BESolrField solrField,
            @Nonnull final List<BEAttributeValue> values,
            @Nonnull final StringBuilder builder)
    {
        builder.append(' ');

        if (values.isEmpty())
        {
            // no values so treat as searching for not present by prepending "*:* -" to variable name
            // along with suffix below. This says find any record that doesn't have variable
            // value in document. To operate correctly, this query must be fully encapsulated with ()
            builder.append("(*:* -");
        }
        builder.append(solrField.getName()).append(':');

        if (values.isEmpty())
        {
            // no values so treat as not present by using [* TO *] with prepend above
            builder.append('[').append("*").append(" TO ").append("*").append(']').append(')');
        }
        else if (values.size() == 1)
        {
            builder.append('"').append(values.get(0).getId()).append('"');
        }
        else
        {
            builder.append('(');
            values.forEach(v -> builder.append(" \"").append(v.getId()).append('"'));
            builder.append(')');
        }
    }
}
