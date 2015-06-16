package com.amobee.freebee.bench;

import com.amobee.freebee.config.BEDataTypeConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class DefaultDataTypeConfigurer implements DataTypeConfigurer
{

    private static final BEDataTypeConfig AGE_TYPE_CONFIG =
            new BEDataTypeConfig(
                    "age",
                    "INT",
                    false,
                    false,
                    true,
                    false);

    private static final BEDataTypeConfig COUNTRY_TYPE_CONFIG =
            new BEDataTypeConfig(
                    "country",
                    "STRING",
                    true,
                    false,
                    false,
                    false);

    private static final BEDataTypeConfig DAYPART_TYPE_CONFIG =
            new BEDataTypeConfig(
                    "daypart",
                    "INT",
                    false,
                    false,
                    false,
                    false);

    private static final BEDataTypeConfig DMA_TYPE_CONFIG =
            new BEDataTypeConfig(
                    "dma",
                    "INT",
                    false,
                    false,
                    false,
                    false);

    private static final BEDataTypeConfig DOMAIN_TYPE_CONFIG =
            new BEDataTypeConfig(
                    "domain",
                    "STRING",
                    true,
                    true,
                    false,
                    true);


    private static final BEDataTypeConfig GENDER_TYPE_CONFIG =
            new BEDataTypeConfig(
                    "gender",
                    "STRING",
                    true,
                    false,
                    false,
                    false);

    private static final BEDataTypeConfig GENRE_TYPE_CONFIG =
            new BEDataTypeConfig("genre",
                    "STRING",
                    true,
                    false,
                    false,
                    false);

    private static final Map<String, BEDataTypeConfig> dataTypesByName = new HashMap<>();

    static
    {
        dataTypesByName.put(AGE_TYPE_CONFIG.getType(), AGE_TYPE_CONFIG);
        dataTypesByName.put(COUNTRY_TYPE_CONFIG.getType(), COUNTRY_TYPE_CONFIG);
        dataTypesByName.put(DAYPART_TYPE_CONFIG.getType(), DAYPART_TYPE_CONFIG);
        dataTypesByName.put(DMA_TYPE_CONFIG.getType(), DMA_TYPE_CONFIG);
        dataTypesByName.put(DOMAIN_TYPE_CONFIG.getType(), DOMAIN_TYPE_CONFIG);
        dataTypesByName.put(GENDER_TYPE_CONFIG.getType(), GENDER_TYPE_CONFIG);
        dataTypesByName.put(GENRE_TYPE_CONFIG.getType(), GENRE_TYPE_CONFIG);
    }

    @Override
    public List<BEDataTypeConfig> getDataTypeConfigs()
    {
        return new ArrayList<>(dataTypesByName.values());
    }

    @Override
    public Optional<BEDataTypeConfig> getDataType(final String typeName)
    {
        return Optional.ofNullable(dataTypesByName.get(typeName));
    }
}
