package com.amobee.freebee.bench;

import com.amobee.freebee.evaluator.evaluator.BEInput;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface InputGenerator
{

    default List<BEInput> generateList(long count)
    {
        return generateStream().limit(count).collect(Collectors.toList());
    }

    default Stream<BEInput> generateStream()
    {
        return Stream.generate(getSupplier());
    }

    default Supplier<BEInput> getSupplier()
    {
        return this::generate;
    }

    BEInput generate();

}
