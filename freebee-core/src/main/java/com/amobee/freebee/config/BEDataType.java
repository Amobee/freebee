package com.amobee.freebee.config;

import java.util.function.Function;
import javax.annotation.Nonnull;

import com.amobee.freebee.evaluator.index.BEByteIndexAttributeCategory;
import com.amobee.freebee.evaluator.index.BEDoubleIndexAttributeCategory;
import com.amobee.freebee.evaluator.index.BEIndexAttributeCategory;
import com.amobee.freebee.evaluator.index.BEIntIndexAttributeCategory;
import com.amobee.freebee.evaluator.index.BELongIndexAttributeCategory;
import com.amobee.freebee.evaluator.index.BEStringIndexAttributeCategory;

/**
 * @author Michael Bond
 */
public enum BEDataType
{
    BYTE(BEByteIndexAttributeCategory::newInstance),
    DOUBLE(BEDoubleIndexAttributeCategory::newInstance),
    INT(BEIntIndexAttributeCategory::newInstance),
    LONG(BELongIndexAttributeCategory::newInstance),
    STRING(BEStringIndexAttributeCategory::newInstance);

    private final Function<BEDataTypeConfig, BEIndexAttributeCategory> supplier;

    BEDataType(@Nonnull final Function<BEDataTypeConfig, BEIndexAttributeCategory> supplier)
    {
        this.supplier = supplier;
    }

    public BEIndexAttributeCategory newIndexAttributeCategory(@Nonnull final BEDataTypeConfig dataTypeConfig)
    {
        return this.supplier.apply(dataTypeConfig);
    }
}
