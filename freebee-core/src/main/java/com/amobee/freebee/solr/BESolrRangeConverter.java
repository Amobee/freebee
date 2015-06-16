package com.amobee.freebee.solr;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;

import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import org.apache.commons.lang3.StringUtils;

import com.amobee.freebee.expression.BEAttributeValue;

/**
 * Base class for all range converters.
 *
 * @author Michael Bond
 */
public abstract class BESolrRangeConverter<C extends Comparable<C>> implements BESolrTypeConverter
{
    public BESolrRangeConverter()
    {
    }

    /**
     * Convert start and end values represented as strings to a range.
     *
     * @param start
     *         Range start value
     * @param end
     *         Range end value
     * @param value
     *         boolean expression object to access properties
     * @param ranges
     *         Range set to add ranges to
     */
    public abstract void convertRange(
            @Nonnull String start,
            @Nonnull String end,
            @Nonnull BEAttributeValue value,
            @Nonnull RangeSet<C> ranges);

    public abstract C getStart(@Nonnull Range<C> range);

    public abstract C getEnd(@Nonnull Range<C> range);

    /**
     * Convert a range set to one or more attribute range matches, combining ranges if they overlap or are contiguous.
     *
     * @param solrField
     *         SOLR field to map set to
     * @param values
     *         Set values to map
     * @param builder
     *         String builder to append converted query to.
     * @throws IllegalArgumentException
     *         if attribute is not mapped.
     */
    @Override
    public void convert(
            @Nonnull final BESolrField solrField,
            @Nonnull final List<BEAttributeValue> values,
            @Nonnull final StringBuilder builder)
    {
        // pattern to match "18" or "18-24" or "18-"
        final Pattern numberPattern = Pattern.compile("[0-9]+-?[0-9]*");
        // pattern to match daypart names such as "wkd" (assumes alpha characters only)
        final Pattern namePattern = Pattern.compile("[^0-9]*");
        // pattern to match "[18]" or "[18,24]" or "[18,]"
        final Pattern bracketPattern = Pattern.compile("\\[{1}[0-9]+,?[0-9]*\\]{1}");

        // extract possible range for each value
        final RangeSet<C> ranges = TreeRangeSet.create();
        for (final BEAttributeValue v : values)
        {
            final String value = v.getId();
            if (numberPattern.matcher(value).matches())
            {
                final String[] split = StringUtils.split(value, '-');
                final String start = split[0];

                final String end;

                if (split.length > 1)
                {
                    end = split[1];
                }
                else if (split.length == 1 && value.indexOf('-') == value.length() - 1)
                {
                    end = "";
                }
                else
                {
                    end = start;
                }
                convertRange(start, end, v, ranges);
            }
            else if (namePattern.matcher(value).matches())
            {
                convertRange(value, value, v, ranges);
            }
            else if (bracketPattern.matcher(value).matches())
            {
                final String[] split = StringUtils.split(value, ',');
                final String start = split[0].replaceAll("\\[|\\]", "");
                final String end = split.length == 1 ? start : split[1].replaceAll("\\[|\\]", "");
                convertRange(start, end, v, ranges);
            }
            else
            {
                throw new IllegalArgumentException("Cannot parse " + value);
            }
        }

        // format SOLR query based on ranges extracted above
        boolean first = true;
        final String solrFieldName = solrField.getName();
        final Set<Range<C>> rangeSet = ranges.asRanges();
        if (rangeSet.size() == 1)
        {
            builder.append(' ');
            convertRange(solrFieldName, rangeSet.iterator().next(), builder);
        }
        else
        {
            builder.append(" (");
            for (final Range<C> range : rangeSet)
            {
                if (first)
                {
                    first = false;
                }
                else
                {
                    builder.append(" OR ");
                }

                convertRange(solrFieldName, range, builder);
            }
            builder.append(')');
        }
    }

    /**
     * Convert a range to either {field}:{start} or
     * {field}:[{start} TO {end}] where type is mapped to field using the attribute to field mapping.
     *
     * @param solrField
     *         SOLR field
     * @param range
     *         Numeric range
     * @param builder
     *         String builder to append converted query to.
     */
    private void convertRange(
            @Nonnull final String solrField,
            @Nonnull final Range<C> range,
            @Nonnull final StringBuilder builder)
    {
        final C begin = getStart(range);
        final C end = getEnd(range);

        builder.append(solrField).append(':');

        if (begin.equals(end))
        {
            // just add single value
            builder.append(begin);
        }
        else if (end == null)
        {
            // add beginning to infinity
            builder.append('[').append(begin).append(" TO ").append("*").append(']');
        }
        else
        {
            // add beginning to end
            builder.append('[').append(begin).append(" TO ").append(end).append(']');
        }
    }
}
