package com.compomics.relims.model.guava.functions;

import com.google.common.base.Function;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * This class is a
 */
public class DoubleRounderFunction implements Function<Double, Double> {

    private static DoubleRounderFunction instance = new DoubleRounderFunction();
    private int iDecimals = 2;

    public DoubleRounderFunction() {

    }

    public DoubleRounderFunction(int aDecimals) {
        iDecimals = aDecimals;
    }

    public Double apply(@Nullable Double aDouble) {
        BigDecimal lBigDecimal = new BigDecimal(aDouble);
        lBigDecimal.setScale(iDecimals, RoundingMode.DOWN);
        return lBigDecimal.doubleValue();
    }


    public int getDecimals() {
        return iDecimals;
    }

    public void setDecimals(int aDecimals) {
        iDecimals = aDecimals;
    }
}
