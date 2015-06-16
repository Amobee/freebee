package com.amobee.freebee.bench;

import com.amobee.freebee.evaluator.evaluator.BEInput;
import com.amobee.freebee.expression.BENode;
import com.amobee.freebee.bench.random.RandomBenchmarkConfiguration;
import com.amobee.freebee.bench.random.RandomBenchmarkConfigurationProperties;
import com.amobee.freebee.bench.shim.ComparisonEvaluatorBuilder;
import com.amobee.freebee.bench.shim.Evaluator;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({"checkstyle:Regexp", "Duplicates"})
public class ReferenceComparisonRunner
{
    private static final Logger logger = LoggerFactory.getLogger(ReferenceComparisonRunner.class);

    // TODO externalize this configuration. For now, change this values in order to configure the benchmark
    private static final int INPUT_COUNT = 100;
    private static final int MAX_VALUES_PER_INPUT = 100;
    private static final int EXPRESSION_COUNT = 1000;
    private static final int MAX_EXPRESSION_WIDTH = 100;
    private static final int MAX_EXPRESSION_DEPTH = 5;
    private static final long RANDOM_SEED = 14888790436548L;

    private ReferenceComparisonRunner() {}

    public static void main(final String[] args)
    {
        /* * * *  CONFIGURE * * * */

        final RandomBenchmarkConfigurationProperties expressionGeneratorProperties =
                new RandomBenchmarkConfigurationProperties()
                        .withRandomSeed(RANDOM_SEED)
                        .withMaxDepth(MAX_EXPRESSION_DEPTH)
                        .withMaxWidth(MAX_EXPRESSION_WIDTH)
                        .withMaxInputValues(MAX_VALUES_PER_INPUT);

        final BenchmarkConfiguration configuration = new RandomBenchmarkConfiguration(expressionGeneratorProperties);

        /* * * *  SETUP  * * * */

        final long startTime = System.currentTimeMillis();

        final DataTypeConfigurer dataTypeConfigurer = configuration.dataTypeConfigurer();
        final ExpressionGenerator expressionGenerator = configuration.expressionGenerator();
        final InputGenerator inputGenerator = configuration.inputGenerator();

        final List<IdentifiableBooleanExpression> expressions =
                expressionGenerator
                        .generateStream()
                        .map(IdentifiableBooleanExpression::new)
                        .limit(EXPRESSION_COUNT)
                        .collect(Collectors.toList());

        final List<BEInput> inputRequests = inputGenerator.generateList(INPUT_COUNT);

        final long benchmarkSetupComplete = System.currentTimeMillis();


        /* * * *  BUILD EVALUATOR  * * * */

        final ComparisonEvaluatorBuilder evaluatorBuilder = new ComparisonEvaluatorBuilder();
        evaluatorBuilder.addDataTypeConfigs(dataTypeConfigurer.getDataTypeConfigs());
        expressions.forEach(e -> {
            evaluatorBuilder.addExpression(e.getUuid().toString(), e.getExpression());
        });
        final Evaluator fastEvaluator = evaluatorBuilder.buildFastEvaluator();
        final Evaluator referenceEvaluator = evaluatorBuilder.buildReferenceEvaluator();

        final long evaluatorBuilt = System.currentTimeMillis();


        /* * * *  EVALUATE MANY INPUT REQUESTS  * * * */

        final long tenPercent = INPUT_COUNT / 10;
        final long[] fastEvalTimes = new long[INPUT_COUNT];
        final long[] referenceEvalTimes = new long[INPUT_COUNT];
        final Set<String> totalMismatchedExpressions = new HashSet<>();
        for (int i = 0; i < inputRequests.size(); i++)
        {
            final BEInput input = inputRequests.get(i);

            final long preFastEvalTime = System.nanoTime();
            final Set<String> fastResults = fastEvaluator.evaluate(input);
            fastEvalTimes[i] = System.nanoTime() - preFastEvalTime;

            final long preRefEvalTime = System.nanoTime();
            final Set<String> referenceResults = referenceEvaluator.evaluate(input);
            referenceEvalTimes[i] = System.nanoTime() - preRefEvalTime;

            if (!fastResults.equals(referenceResults))
            {
                logger.warn("RESULTS MISMATCH\n");
                logger.warn("Fast Results:      {} total", fastResults.size());
                logger.warn("Reference Results: {} total", referenceResults.size());

                final Set<String> matchedByFastOnly = new HashSet<>(fastResults);
                matchedByFastOnly.removeAll(referenceResults);
                final Set<String> matchedByRefOnly = new HashSet<>(referenceResults);
                matchedByRefOnly.removeAll(fastResults);
                logger.warn("Matched by fast but not by reference: {} total", matchedByFastOnly.size());
                matchedByFastOnly.forEach(id -> logger.warn("  {}", id));
                logger.warn("Matched by reference but not by fast: {} total", matchedByRefOnly.size());
                matchedByRefOnly.forEach(id -> logger.warn("  {}", id));

                final Set<String> mismatchedExpressions = new HashSet<>();
                mismatchedExpressions.addAll(matchedByFastOnly);
                mismatchedExpressions.addAll(matchedByRefOnly);

                mismatchedExpressions.forEach(id -> {
                    totalMismatchedExpressions.add(id);
                    try
                    {
                        final Optional<IdentifiableBooleanExpression> expr = expressions.stream()
                                .filter(e -> id.equals(e.getUuid().toString())).findFirst();
                        if (expr.isPresent())
                        {
                            final ObjectMapper mapper = new ObjectMapper();
                            final String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(expr.get().getExpression());
                            logger.warn("\nExpression {}:\n{}", id, json);
                        }

                    }
                    catch (final Throwable t)
                    {
                        logger.warn("Could not print expression due to " + t.getLocalizedMessage(), t);
                    }
                });
                logger.warn("END MISMATCH REPORT\n\n");

            }

            if (i > 0 && i % tenPercent == 0)
            {
                logger.info(" {}% of evaluator comparision complete", (int) ((double) i / (double) INPUT_COUNT * 100.0));
            }
        }
        logger.info("100% of evaluator comparision complete");

        final long evaluationsDone = System.currentTimeMillis();

        /* * * *  COMPUTE METRICS * * * */

        // TODO
        Arrays.sort(referenceEvalTimes);
        long maxEvalTime = referenceEvalTimes[referenceEvalTimes.length - 1];
        final long percentile50 = referenceEvalTimes[Math.round((float) Math.ceil((float) referenceEvalTimes.length * (50.0 / 100.0)))];
        final long percentile75 = referenceEvalTimes[Math.round((float) Math.ceil((float) referenceEvalTimes.length * (75.0 / 100.0)))];
        final long percentile95 = referenceEvalTimes[Math.round((float) Math.ceil((float) referenceEvalTimes.length * (95.0 / 100.0)))];
        final long percentile97 = referenceEvalTimes[Math.round((float) Math.ceil((float) referenceEvalTimes.length * (97.0 / 100.0)))];
        final long percentile98 = referenceEvalTimes[Math.round((float) Math.ceil((float) referenceEvalTimes.length * (98.0 / 100.0)))];
        final long percentile99 = referenceEvalTimes[Math.round((float) Math.ceil((float) referenceEvalTimes.length * (99.0 / 100.0)))];
        long totalEvalTime = 0;
        for (int i = 0; i < referenceEvalTimes.length; i++)
        {
            final long evalTime = referenceEvalTimes[i];
            maxEvalTime = evalTime > maxEvalTime ? evalTime : maxEvalTime;
            totalEvalTime += evalTime;
        }
        final double averageEvalTime = (double) totalEvalTime / (double) INPUT_COUNT;
        double totalSquaredMeanDiffs = 0.0;
        for (int i = 0; i < referenceEvalTimes.length; i++)
        {
            final long evalTime = referenceEvalTimes[i];
            final double squaredMeanDiff = Math.pow((double) evalTime - averageEvalTime, 2);
            totalSquaredMeanDiffs += squaredMeanDiff;
        }
        final double stdDeviation = Math.sqrt(totalSquaredMeanDiffs / (double) INPUT_COUNT);


        /* * * *  OUTPUT REPORT  * * * */

        final long totalTime = evaluationsDone - startTime;
        final long setupBenchmarkTime = benchmarkSetupComplete - startTime;
        final long buildEvaluatorTime = evaluatorBuilt - benchmarkSetupComplete;
        final long evaluationTime = evaluationsDone - evaluatorBuilt;

        System.out.println(String.format("\nEvaluated %d requests against %d expressions\n", INPUT_COUNT, EXPRESSION_COUNT));

        System.out.println(String.format("Total time: %d ms", totalTime));
        System.out.println(String.format("  Setup benchmark:        %d ms", setupBenchmarkTime));
        System.out.println(String.format("  Build evaluator index:  %d ms", buildEvaluatorTime));
        System.out.println(String.format("  Evaluate requests:      %d ms", evaluationTime));

        System.out.println("\nExpression evaluation results");
        System.out.println(String.format("  Total failed expressions:  %s (%2f)%%", totalMismatchedExpressions.size(), (double) totalMismatchedExpressions.size() / EXPRESSION_COUNT));

        System.out.println("\nAverage and Max Metrics");
        System.out.println(String.format("  Average evaluation time:  %f ms (wall clock)", (double) evaluationTime / (double) INPUT_COUNT));
        System.out.println(String.format("  Average evaluation time:  %f ms (instrument)", averageEvalTime / 1_000_000.0));
        System.out.println(String.format("  Standard deviation:       %f ms", stdDeviation / 1_000_000.0));
        System.out.println(String.format("  50th percentile:          %f ms", percentile50 / 1_000_000.0));
        System.out.println(String.format("  75th percentile:          %f ms", percentile75 / 1_000_000.0));
        System.out.println(String.format("  95th percentile:          %f ms", percentile95 / 1_000_000.0));
        System.out.println(String.format("  97th percentile:          %f ms", percentile97 / 1_000_000.0));
        System.out.println(String.format("  98th percentile:          %f ms", percentile98 / 1_000_000.0));
        System.out.println(String.format("  99th percentile:          %f ms", percentile99 / 1_000_000.0));
        System.out.println(String.format("  Max evaluation time:      %f ms", (double) maxEvalTime / 1_000_000.0));

    }

    @SuppressWarnings({"WeakerAccess", "checkstyle:RedundantModifier"})
    private static class IdentifiableBooleanExpression
    {

        private final UUID uuid;
        private final BENode expression;

        public IdentifiableBooleanExpression(@Nonnull final BENode expression)
        {
            this.uuid = UUID.randomUUID();
            this.expression = expression;
        }

        public UUID getUuid()
        {
            return this.uuid;
        }

        public BENode getExpression()
        {
            return this.expression;
        }

    }

}
