package com.amobee.freebee.bench;

import com.amobee.freebee.config.BEDataTypeConfig;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public interface DataTypeConfigurer
{
    List<BEDataTypeConfig> getDataTypeConfigs();

    default List<String> getDataTypes()
    {
        return getDataTypeConfigs().stream().map(BEDataTypeConfig::getType).collect(Collectors.toList());
    }

    Optional<BEDataTypeConfig> getDataType(String typeName);
}
