package com.amobee.freebee.evaluator.index;

import com.amobee.freebee.config.BEDataTypeConfig;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * An implementation of {@link BEDataTypeConfigSupplier} that is backed by a collection of explicity data types configs.
 *
 * @author Kevin Doran
 */
public class BEDataTypeConfigLookupSupplier implements BEDataTypeConfigSupplier
{
    private static final long serialVersionUID = -706162797183363056L;

    private final Map<String, BEDataTypeConfig> categoryToTypeConfig;
    private final BEDataTypeConfigSupplier defaultDataTypeConfigSupplier;
    private final boolean caseSensitive;

    private BEDataTypeConfigLookupSupplier(final Builder builder)
    {
        this.caseSensitive = builder.caseSensitive;
        this.defaultDataTypeConfigSupplier = builder.defaultDataTypeConfigSupplier != null
                ? builder.defaultDataTypeConfigSupplier
                : dataTypeName -> null;
        this.categoryToTypeConfig = builder.dataTypeConfigs == null
                ? new HashMap<>()
                : builder.dataTypeConfigs.stream()
                .collect(Collectors.toMap(dtConfig ->
                   normalizeDataTypeName(dtConfig.getType()), Function.identity()));
    }

    @Override
    public BEDataTypeConfig get(final String dataTypeName)
    {
        return this.categoryToTypeConfig.getOrDefault(
                normalizeDataTypeName(dataTypeName), this.defaultDataTypeConfigSupplier.get(dataTypeName));
    }

    private String normalizeDataTypeName(final String dataTypeName)
    {
        return this.caseSensitive ? dataTypeName : dataTypeName.toUpperCase();
    }

    public static Builder builder()
    {
        return new Builder();
    }

    public static class Builder
    {

        private final Collection<BEDataTypeConfig> dataTypeConfigs = new ArrayList<>();
        private BEDataTypeConfigSupplier defaultDataTypeConfigSupplier;
        private boolean caseSensitive;

        private Builder() {}

        public Builder defaultDataTypeConfig(final BEDataTypeConfigSupplier defaultDataTypeConfigSupplier)
        {
            this.defaultDataTypeConfigSupplier = defaultDataTypeConfigSupplier;
            return this;
        }

        public Builder caseSensitive(final boolean caseSensitive)
        {
            this.caseSensitive = caseSensitive;
            return this;
        }

        public Builder addAllDataTypeConfigs(final Collection<BEDataTypeConfig> dataTypeConfigs)
        {
            this.dataTypeConfigs.addAll(dataTypeConfigs);
            return this;
        }

        public Builder addDataTypeConfig(final BEDataTypeConfig dataTypeConfig)
        {
            this.dataTypeConfigs.add(dataTypeConfig);
            return this;
        }

        public BEDataTypeConfigLookupSupplier build()
        {
            return new BEDataTypeConfigLookupSupplier(this);
        }

    }

}
