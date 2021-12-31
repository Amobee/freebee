package com.amobee.freebee.bench.random;

public class RandomBenchmarkConfigurationProperties
{
    private static final int DEFAULT_MAX_EXPRESSION_DEPTH = 3;
    private static final int DEFAULT_MAX_EXPRESSION_WIDTH = 1000;
    private static final int DEFAULT_MAX_INPUT_VALUES = 100;
    private static final boolean DEFAULT_TRACKING_ENABLED = true;

    private Long randomSeed;
    private int maxDepth = DEFAULT_MAX_EXPRESSION_DEPTH;
    private int maxWidth = DEFAULT_MAX_EXPRESSION_WIDTH;
    private int maxInputValues = DEFAULT_MAX_INPUT_VALUES;
    private boolean inputTrackingEnabled = DEFAULT_TRACKING_ENABLED;

    public Long getRandomSeed()
    {
        return this.randomSeed;
    }

    public void setRandomSeed(final Long randomSeed)
    {
        this.randomSeed = randomSeed;
    }

    public RandomBenchmarkConfigurationProperties withRandomSeed(final Long randomSeed)
    {
        this.randomSeed = randomSeed;
        return this;
    }

    public int getMaxDepth()
    {
        return this.maxDepth;
    }

    public void setMaxDepth(final int maxDepth)
    {
        this.maxDepth = maxDepth;
    }

    public RandomBenchmarkConfigurationProperties withMaxDepth(final int maxDepth)
    {
        this.maxDepth = maxDepth;
        return this;
    }

    public int getMaxWidth()
    {
        return this.maxWidth;
    }

    public void setMaxWidth(final int maxWidth)
    {
        this.maxWidth = maxWidth;
    }

    public RandomBenchmarkConfigurationProperties withMaxWidth(final int maxWidth)
    {
        this.maxWidth = maxWidth;
        return this;
    }

    public int getMaxInputValues()
    {
        return this.maxInputValues;
    }

    public void setMaxInputValues(final int maxInputValues)
    {
        this.maxInputValues = maxInputValues;
    }

    public RandomBenchmarkConfigurationProperties withMaxInputValues(final int maxInputValues)
    {
        this.maxInputValues = maxInputValues;
        return this;
    }

    public boolean isInputTrackingEnabled()
    {
        return this.inputTrackingEnabled;
    }

    public void setInputTrackingEnabled(final boolean inputTrackingEnabled)
    {
        this.inputTrackingEnabled = inputTrackingEnabled;
    }

    public RandomBenchmarkConfigurationProperties withInputTracingEnabled(final boolean tracingEnabled)
    {
        this.inputTrackingEnabled = tracingEnabled;
        return this;
    }

    @Override
    public String toString()
    {
        return "RandomBenchmarkConfigurationProperties{" +
                "randomSeed=" + this.randomSeed +
                ", maxDepth=" + this.maxDepth +
                ", maxWidth=" + this.maxWidth +
                ", maxInputValues=" + this.maxInputValues +
                ", inputTracingEnabled=" + this.inputTrackingEnabled +
                '}';
    }
}
