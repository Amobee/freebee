package com.amobee.freebee.bench;

import com.amobee.freebee.evaluator.evaluator.BEEvaluator;
import com.amobee.freebee.evaluator.evaluator.BEEvaluatorBuilder;
import com.amobee.freebee.evaluator.evaluator.BEHybridEvaluator;
import com.amobee.freebee.evaluator.evaluator.BEInput;
import com.amobee.freebee.evaluator.index.BEIndexMetrics;
import com.amobee.freebee.expression.BENode;
import com.amobee.freebee.bench.random.RandomBenchmarkConfiguration;
import com.amobee.freebee.bench.random.RandomBenchmarkConfigurationProperties;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({"checkstyle:Regexp", "Duplicates"})
public class BenchmarkRunner
{
    private static final Logger logger = LoggerFactory.getLogger(BenchmarkRunner.class);

    // TODO externalize this configuration. For now, change this values in order to configure the benchmark
    private static final int INPUT_COUNT = 5_000;
    private static final int MAX_VALUES_PER_INPUT = 100;
    private static final int EXPRESSION_COUNT = 1_000;
    private static final int MAX_EXPRESSION_WIDTH = 100;
    private static final int MAX_EXPRESSION_DEPTH = 5;
    private static final long RANDOM_SEED = 14888790436548L;

    private BenchmarkRunner() {}

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

        final BEEvaluatorBuilder<UUID> evaluatorBuilder = new BEEvaluatorBuilder<>();
        evaluatorBuilder.addDataTypeConfigs(dataTypeConfigurer.getDataTypeConfigs());
        expressions.forEach(e -> {
            if (logger.isTraceEnabled())
            {
                try
                {
                    final ObjectMapper mapper = new ObjectMapper();
                    final String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(e);
                    logger.trace("Expression:\n{}", json);
                }
                catch (final Throwable t)
                {
                    logger.warn("Could not print expression due to " + t.getLocalizedMessage(), t);
                }
            }
            evaluatorBuilder.addExpression(e.getUuid(), e.getExpression());
        });
        final BEEvaluator evaluator = evaluatorBuilder.build();

        final long evaluatorBuilt = System.currentTimeMillis();


        /* * * *  EVALUATE MANY INPUT REQUESTS  * * * */

        final long tenPercent = INPUT_COUNT / 10;
        long matchedRequestCount = 0;
        final long[] evalTimes = new long[INPUT_COUNT];
        for (int i = 0; i < inputRequests.size(); i++)
        {
            final BEInput input = inputRequests.get(i);
            final long preEvalTime = System.nanoTime();
            matchedRequestCount += evaluator.evaluate(input).isEmpty() ? 0 : 1;
            final long postEvalTime = System.nanoTime();
            evalTimes[i] = postEvalTime - preEvalTime;
            if (i > 0 && i % tenPercent == 0)
            {
                logger.info(" {}% of benchmark complete", (int) ((double) i / (double) INPUT_COUNT * 100.0));
            }
        }
        logger.info("100% of benchmark complete");

        final long evaluationsDone = System.currentTimeMillis();

        /* * * *  COMPUTE METRICS * * * */

        Arrays.sort(evalTimes);
        long maxEvalTime = evalTimes[evalTimes.length - 1];
        final long percentile50 = evalTimes[Math.round((float) Math.ceil((float) evalTimes.length * (50.0 / 100.0)))];
        final long percentile75 = evalTimes[Math.round((float) Math.ceil((float) evalTimes.length * (75.0 / 100.0)))];
        final long percentile95 = evalTimes[Math.round((float) Math.ceil((float) evalTimes.length * (95.0 / 100.0)))];
        final long percentile97 = evalTimes[Math.round((float) Math.ceil((float) evalTimes.length * (97.0 / 100.0)))];
        final long percentile98 = evalTimes[Math.round((float) Math.ceil((float) evalTimes.length * (98.0 / 100.0)))];
        final long percentile99 = evalTimes[Math.round((float) Math.ceil((float) evalTimes.length * (99.0 / 100.0)))];
        long totalEvalTime = 0;
        for (int i = 0; i < evalTimes.length; i++)
        {
            final long evalTime = evalTimes[i];
            maxEvalTime = evalTime > maxEvalTime ? evalTime : maxEvalTime;
            totalEvalTime += evalTime;
        }
        final double averageEvalTime = (double) totalEvalTime / (double) INPUT_COUNT;
        double totalSquaredMeanDiffs = 0.0;
        for (int i = 0; i < evalTimes.length; i++)
        {
            final long evalTime = evalTimes[i];
            final double squaredMeanDiff = Math.pow((double) evalTime - averageEvalTime, 2);
            totalSquaredMeanDiffs += squaredMeanDiff;
        }
        final double stdDeviation = Math.sqrt(totalSquaredMeanDiffs / (double) INPUT_COUNT);


        /* * * *  OUTPUT REPORT  * * * */

        final long totalTime = evaluationsDone - startTime;
        final long setupBenchmarkTime = benchmarkSetupComplete - startTime;
        final long buildEvaluatorTime = evaluatorBuilt - benchmarkSetupComplete;
        final long evaluationTime = evaluationsDone - evaluatorBuilt;

        final BEIndexMetrics metrics = ((BEHybridEvaluator) evaluator).getMetrics();
        final long expressionCount = metrics.getExpressionCount();
        final long intervalCount = metrics.getIntervalCount();
        final long bitSetExpressionCount = metrics.getExpressionCountWithBitSetEvaluation();
        final long intervalExpressionCount = metrics.getExpressionCountWithIntervalEvaluation();

        System.out.println(String.format("\nEvaluated %d requests against %d expressions\n", INPUT_COUNT, EXPRESSION_COUNT));

        System.out.println(String.format("Total number of expressions:      %d", expressionCount));
        System.out.println(String.format("  CNF-like (bitset evaluation):   %d (%d%%)", bitSetExpressionCount, (int) ((double) bitSetExpressionCount / (double) expressionCount * 100)));
        System.out.println(String.format("  Freeform (interval evaluation): %d (%d%%)", intervalExpressionCount, (int) ((double) intervalExpressionCount / (double) expressionCount * 100)));
        System.out.println(String.format("  Total number of intervals:      %d", intervalCount));

        System.out.println(String.format("\nTotal time: %d ms", totalTime));
        System.out.println(String.format("  Setup benchmark:        %d ms", setupBenchmarkTime));
        System.out.println(String.format("  Build evaluator index:  %d ms", buildEvaluatorTime));
        System.out.println(String.format("  Evaluate requests:      %d ms", evaluationTime));

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

        System.out.println("\nRequest Input Stats");
        System.out.println(String.format("  Matched:      %d requests", matchedRequestCount));
        System.out.println(String.format("  Not matched:  %d requests", INPUT_COUNT - matchedRequestCount));

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
