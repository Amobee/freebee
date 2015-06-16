package com.amobee.freebee.evaluator.interval;

import com.amobee.freebee.expression.BENode;

import java.io.Serializable;

/**
 * An implementation of {@link Interval} that also stores a reference
 * to the BENode that the interval represents.
 *
 * @author Kevin Doran
 */
public class BENodeInterval implements Interval, Serializable
{

    private static final long serialVersionUID = 7376092127582612272L;

    private final short start;
    private final short end;
    private final BENode node;

    public BENodeInterval(final short start, final short end, final BENode node)
    {
        this.start = start;
        this.end = end;
        this.node = node;
    }

    @Override
    public short getStart()
    {
        return this.start;
    }

    @Override
    public short getEnd()
    {
        return this.end;
    }

    public BENode getNode()
    {
        return this.node;
    }
}