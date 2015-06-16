package com.amobee.freebee.bench.shim;

import com.amobee.freebee.evaluator.evaluator.BEEvaluator;
import com.amobee.freebee.evaluator.evaluator.BEInput;

import java.util.Set;
import javax.annotation.Nonnull;

// This is a temporary adapter for the current freebee evaluator class that wraps it in the shim interafce.
public class FreebeeEvaluator implements Evaluator
{

    private final BEEvaluator<String> evaluator;

    public FreebeeEvaluator(final BEEvaluator<String> evaluator)
    {
        this.evaluator = evaluator;
    }

    @Override
    public Set<String> evaluate(@Nonnull final BEInput input)
    {
        return this.evaluator.evaluate(input);
    }
}
