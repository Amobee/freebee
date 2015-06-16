# FreeBEE Benchmark and Functionality Test Harness

FreeBEE includes a module (`freebee-bench`) that provides a test harness that can be used for *performance benchmarking* and *evaluator functionality testing*.

The module includes a number of interfaces that allow the test harness to be configured or extended.

The module also includes useful companion classes, including:

- A configurable, random boolean expression generator.
- A reference boolean expression evaluator that is not optimized for performance but is easier to understand. (Note, this is a work in progress and currently has limited features.)
  
  
## Usage

The benchmarking test harness currently consists of two Java `main` classes:

- BenchmarkRunner: Stress tests the FreeBEE library and produces a runtime performance report.
- ReferenceComparisonRunner: Compares the FreeBEE logic against a reference evaluator for correctness analysis.


### BenchmarkRunner

The following steps run the benchmarking tool:

1. Configure BenchmarkRunner. Currently, this is managed at the top of the main class as static variables. 
   In the future, this will be replaced with external configuration properties. For now, modify these values in
   BenchmarkRunner.java:
   
       private static final int INPUT_COUNT = 5_000;
       private static final int MAX_VALUES_PER_INPUT = 100;
       private static final int EXPRESSION_COUNT = 1_000;
       private static final int MAX_EXPRESSION_WIDTH = 1_000;
       private static final int MAX_EXPRESSION_DEPTH = 3;
       private static final long RANDOM_SEED = 14888790436548L;
   
   Here is the effect of these variables:
   
   | Config Variable | Description |
   |-----------------|-------------|
   | `INPUT_COUNT` | The number of input records/requests to run against the evaluator. |
   | `MAX_VALUES_PER_INPUT` | The maximum number of attribute values to generate per input record |
   | `EXPRESSION_COUNT` | The number of boolean expressions to generate for the evaluator |
   | `MAX_EXPRESSION_WIDTH` | The maximum number of predicate (leaf) nodes for the boolean expression trees that are generated |
   | `MAX_EXPRESSION_DEPTH` | The maximum depth of the boolean expression trees that are generated |
   | `RANDOM_SEED` | The random seed to use for all pseudorandom generators. Useful for reproducing results. |
   

2. Create an IDE Run Configuration for the BenchmarkRunner.java main class.
    
3. Run the BenchmarkRunner Run Configuration. The output will go to the console. Here is an example:

        Evaluated 5000 requests against 1000 expressions
        
        Total time: 90414 ms
          Setup benchmark:        12440 ms
          Build evaluator index:  2282 ms
          Evaluate requests:      75692 ms
        
        Average and Max Metrics
          Average evaluation time:  15.138400 ms (wall clock)
          Average evaluation time:  15.133384 ms (instrument)
          Standard deviation:       12.868864 ms
          50th percentile:          13.676861 ms
          75th percentile:          19.150101 ms
          95th percentile:          28.939144 ms
          97th percentile:          33.324185 ms
          98th percentile:          38.730450 ms
          99th percentile:          51.663963 ms
          Max evaluation time:      572.646660 ms
        
        Request Input Stats
          Matched:      5000 requests
          Not matched:  0 requests
          
    Additional detailed output will be logged to `stderr`.


### ReferenceComparisonRunner

The following steps run the reference comparision tool:

1. Configure ReferenceComparisonRunner. Currently, this is managed at the top of the main class as static variables. 
   In the future, this will be replaced with external configuration properties. For now, modify these values in
   ReferenceComparisonRunner.java:
   
       private static final int INPUT_COUNT = 100;
       private static final int MAX_VALUES_PER_INPUT = 100;
       private static final int EXPRESSION_COUNT = 100;
       private static final int MAX_EXPRESSION_WIDTH = 100;
       private static final int MAX_EXPRESSION_DEPTH = 4;
       private static final long RANDOM_SEED = 14888790436548L;
   
   For the details regarding the meaning of these variables, see the above section for BenchmarkRunner.java

2. Create an IDE Run Configuration for the ReferenceComparisonRunner.java main class.
    
3. Run the BenchmarkRunner Run Configuration. The output will go to the console. Here is an example:

        Evaluated 100 requests against 100 expressions
        
        Total time: 14512 ms
          Setup benchmark:        1708 ms
          Build evaluator index:  569 ms
          Evaluate requests:      12235 ms
        
        Expression evaluation results
          Total failed expressions:  0 (0.0)%
    
    Additional detailed output will be logged to `stderr`.