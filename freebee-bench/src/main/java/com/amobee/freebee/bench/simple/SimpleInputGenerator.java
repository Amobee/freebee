package com.amobee.freebee.bench.simple;

import com.amobee.freebee.evaluator.evaluator.BEInput;
import com.amobee.freebee.bench.InputGenerator;

public class SimpleInputGenerator implements InputGenerator
{

    @Override
    public BEInput generate()
    {

        // Note: our random inputs should match about 75% (= 50% + 25%) of our fixed expressions.

        final boolean coinFlip = System.nanoTime() % 2 == 0;   // random enough
        final boolean coinFlip2 = System.nanoTime() % 4 >= 2;

        final BEInput input = new BEInput();
        input.getOrCreateStringCategory("gender").add(coinFlip ? "M" : "F");
        input.getOrCreateStringCategory("country").add(coinFlip ? "US" : "NO");
        input.getOrCreateStringCategory("domain").add(coinFlip ? "foo.com" : "nomatch.org");
        input.getOrCreateStringCategory("genre").add(coinFlip ? "NO" : coinFlip2 ? "comedy" : "action");
        return input;
    }
}
