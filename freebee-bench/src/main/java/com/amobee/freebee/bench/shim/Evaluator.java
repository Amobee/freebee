package com.amobee.freebee.bench.shim;

import com.amobee.freebee.evaluator.evaluator.BEInput;

import java.util.Set;
import javax.annotation.Nonnull;

// This is a temporary shim interface. In a future PR, this interface is pushed down into freebee-core.
public interface Evaluator
{

    Set<String> evaluate(@Nonnull BEInput input);

}
