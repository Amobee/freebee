package com.amobee.freebee.solr;

import java.util.List;

import com.google.common.collect.Range;

/**
 * Interface for retrieving daypart ranges for a given name
 *
 * @author Ryan Ambrose
 */
public interface BESolrDaypartRangeStore
{
    /**
     * Get a list of hour ranges for a given daypart name
     * @param daypart daypart name
     * @return list of hour ranges, if no ranges are found null is returned
     */
    List<Range<Integer>> getDaypartRange(String daypart);
}
