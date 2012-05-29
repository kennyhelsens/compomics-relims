package com.compomics.relims.model.guava.functions;

import com.google.common.base.Function;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * This class is a
 */
public class DoubleRounderFunction implements Function<Double, Double> {

    private int iDecimals = 2;

    public DoubleRounderFunction(int aDecimals) {
        iDecimals = aDecimals;
    }

    public Double apply(@Nullable Double aDouble) {
        if (aDouble != null) {
            BigDecimal lBigDecimal = new BigDecimal(aDouble);
            lBigDecimal.setScale(iDecimals, RoundingMode.DOWN);
            return lBigDecimal.doubleValue();
        }else{
            return null;
        }
    }
}
