package com.amobee.freebee.evaluator.index;

import com.amobee.freebee.config.BEDataTypeConfig;

import java.io.Serializable;

public interface BEDataTypeConfigSupplier extends Serializable
{

    BEDataTypeConfigSupplier CASE_SENSITIVE_STRING_SUPPLIER =
            dataTypeName -> new BEDataTypeConfig(dataTypeName, "string", false, null, null, null);

    BEDataTypeConfigSupplier CASE_INSENSITIVE_STRING_SUPPLIER =
            dataTypeName -> new BEDataTypeConfig(dataTypeName, "string", true, null, null, null);

    /**
     * Given a data type name, supplies a data type config.
     *
     * @param dataTypeName - the data type name for which a data type configuration is needed
     * @return a {@link BEDataTypeConfig}
     */
    BEDataTypeConfig get(String dataTypeName);

}
