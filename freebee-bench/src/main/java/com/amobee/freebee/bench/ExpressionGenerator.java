package com.amobee.freebee.bench;

import com.amobee.freebee.expression.BENode;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface ExpressionGenerator
{

    default List<BENode> generateList(long count)
    {
        return generateStream().limit(count).collect(Collectors.toList());
    }

    default Stream<BENode> generateStream()
    {
        return Stream.generate(getSupplier());
    }

    default Supplier<BENode> getSupplier()
    {
        return this::generate;
    }

    BENode generate();

}
