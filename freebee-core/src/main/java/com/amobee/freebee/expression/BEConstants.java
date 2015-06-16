package com.amobee.freebee.expression;

import org.eclipse.collections.api.set.ImmutableSet;
import org.eclipse.collections.impl.factory.Sets;

/**
 * @author Michael Bond
 */
public class BEConstants
{
    public static final String NODE_TYPE_AND = "AND";
    public static final String NODE_TYPE_OR = "OR";
    public static final String NODE_TYPE_REFERENCE = "REF";

    public static final ImmutableSet<String> RESERVED_TYPES = Sets.immutable.of(
            NODE_TYPE_AND,
            NODE_TYPE_OR,
            NODE_TYPE_REFERENCE);

    private BEConstants()
    {
    }
}
