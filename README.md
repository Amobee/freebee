# Free-form Boolean Expression Evaluator

FreeBEE is an easy-to-use boolean expression evaluator optimized for performance. 
Specifically, it is fast enough to evaluate thousands of expressions at once against a given input to find all matches milliseconds.

## Usage Guide

Here are instructions for getting started using FreeBEE.
 
For users of the library that are migrating from previous version of the evaluator, you may also want to refer to the [Migration Guide](docs/migration.md).

1. Add a Maven Dependency:

    ```
    <dependency>
        <groupId>com.amobee.freebee</groupId>
        <artifactId>freebee-core</artifactId>
        <version>2.x.y</version>
    </dependency>
    ``` 

2. Create an evaluator in your code:

    ```
    import com.amobee.be.evaluator.*;
    
    // ...
    
    BEEvaluator<String> evaluator = 
        new BEEvaluatorBuilder<String>()
            .addDataTypeConfig(...)             // See DataTypeConfig syntax below 
            .addExpression("expression1", ...)  // See Expression syntax below
            .addExpression("expression2", ...)
            .addExpression("expression3", ...)
            .build();
    ```

    For more details, see the [DataTypeConfig Example](#DataTypeConfig-Example) and [Expression Syntax Example](#Expression-Syntax-Example) sections below.

3. Evaluate inputs against your evaluator:

    ```
    BEInput = new BEInput();
    input.getOrCreateStringCategory("color").add("blue");
    input.getOrCreateStringCategory("shape").add("hexagon");
    
    Set<String> result = evaluator.evaluate(input);
    ```

See also:

- [Migration Guide](docs/migration.md)
- [Benchmarking Guide](docs/benchmark.md)

### DataTypeConfig Example

Boolean Expressions support typed attributes and flexible matching criteria. Supported types are:

  - `BYTE`
  - `DOUBLE`
  - `INT`
  - `LONG`
  - `STRING`

Attribute Types default to `STRING` and non-string types are specific using a `DataTypeConfig` class set on the `BEEvaluatorBuilder`.
DataTypeConfigs can be built a number of ways.

#### Java Example

    DataTypeConfig shapeName = new BEDataTypeConfig("shapeName", "STRING", true, false, false, false);
    DataTypeConfig numberOfSides = new BEDataTypeConfig("numberOfSides", "INT", false, false, false, false);

The `BEDataTypeConstructor` takes the following arguments:

  - `type`: The name of the type. Can be anything, but must be unique to the evaluator being built.
  - `dataType`: One of [`BYTE`, `DOUBLE`, `INT`, `LONG`, `STRING`].
  - `ignoreCase`: For `STRING` types, is matching case-sensitive.
  - `partial`: For `STRING` types, does the evaluator match partial inputs (uses `startsWith` logic)
  - `range`: For numeric types, are the expressions specified using ranges of values rather than exact values.
  - `reverse`: For `STRING` types, should the values be reversed. 
     This, combined with `partial=true`, is useful for matching hierarchal values such as domains, e.g, 
     does an input of `subdomain.example.com` match an expression with `example.com`.

#### JSON Example

    private static final String DATA_TYPE_CONFIG = "[" +
            "{\"type\":\"shapeName\",\"ignorecase\":true}," +
            "{\"type\":\"numberOfSides\",\"dataType\":\"int\"}," +
            "{\"type\":\"longestSide\",\"dataType\":\"byte\",\"range\":true}," +
            "{\"type\":\"ownerDomain\",\"ignorecase\":true,\"partial\":true,\"reverse\":true}]";


### Expression Syntax Example

#### Java Example

Note: Future versions of FreeBEE will improve the syntax for crafting expressions. For now, you may find it helpful top write utility classes to simplify this. For an example utility class, see [ExpressionUtil.java](freebee-core/src/test/java/com/amobee/freebee/ExpressionUtil.java)

    final BEConjunctionNode nameOrSides = new BEConjunctionNode(BEConjunctionType.OR);
    nameOrSides.addValue(new BEPredicateNode("shapeName", 
            "square", "rectangle", "rhombus", "trapezoid"));
    nameOrSides.addValue(new BEPredicateNode("numberOfSides", 4));

    BEPredicateNote size = new BEPredicateNode("longestSide", "100-499", "500-999", "1000+"));

    BEConjunctionNode bigQuadrillaterals = 
            new BEConjunctionNode("expression1-id", BEConjunctionType.AND, 
                    nameOrSides, 
                    size);

    BEEvaluator<String> builder = new BEEvaluatorBuilder<String>();
    builder.addExpression("expression1-id", bigQuadrillaterals)

#### JSON Example

    BEEvaluator<String> builder = new BEEvaluatorBuilder<String>();
    builder.addExpression("expression1-id", 
        "{\"type\": \"and\", \"values\":[\n" +
            {\"type\":\"or\",\"values\":[
                {\"type\":\"shapeName\",\"values\":[{\"id\":\"square\"},{\"id\":\"rectangle\"},{\"id\":\"rhombus\"},{\"id\":\"trapezoid\"}]},
                {\"type\":\"numberOfSides\",\"values\":[{\"id\":4}]}
            {\"type\":\"longestSide\",\"values\":[{\"id\":\"[100,500)\",\"[500-1000)\",\"[1000,)\"}]

## Developer Guide

This sections covers instructions for developers contributing to this FreeBEE project. 
For developers using the library in downstream projects, see the [Usage Guide](#Usage-Guide).

Requirements: 

- JDK 8 or later
- Maven 3 or later (tested on 3.5)

To build from source:

    mvn clean install
    
Build options:

    -skipStyle    # disable checkstyle checks
    -skipTests    # disable compiling and running tests

## Performance Benchmarking

FreeBEE includes a benchmarking utility and test harness capable of characterizing the performance of the library.
For example:

    Evaluated 5000 requests against 1000 expressions
    
    Total number of expressions:      1000
      CNF-like (bitset evaluation):   577 (57%)
      Freeform (interval evaluation): 423 (42%)
      Total number of intervals:      9624
    
    Total time: 35738 ms
      Setup benchmark:        4982 ms
      Build evaluator index:  387 ms
      Evaluate requests:      30369 ms
    
    Average and Max Metrics
      Average evaluation time:  6.073800 ms (wall clock)
      Average evaluation time:  6.072168 ms (instrument)
      Standard deviation:       2.819298 ms
      50th percentile:          6.029045 ms
      75th percentile:          8.070637 ms
      95th percentile:          10.070885 ms
      97th percentile:          10.540424 ms
      98th percentile:          10.896495 ms
      99th percentile:          11.468921 ms
      Max evaluation time:      69.791384 ms
    
    Request Input Stats
      Matched:      5000 requests
      Not matched:  0 requests

For more information, see the [Benchmarking Guide](docs/benchmark.md).


## Acknowledgments

Michael Bond wrote the initial code for the Boolean Expression Evaluator as part of a larger software library at LucidMedia and then Videology.

Bond was inspired by a white paper, [Efficiently Evaluating Complex Boolean Expressions](http://theory.stanford.edu/~sergei/papers/sigmod10-index.pdf) by Fontoura, Sadanandan, et al.

Many people contributed to the project building upon Bond's work. Their names are found in the initial commit of this repository as co-authors.

## Copyright & License

Copyright (c) 2015-2021 FreeBEE contributors

FreeBEE is open source software released under the [MIT License](LICENSE).