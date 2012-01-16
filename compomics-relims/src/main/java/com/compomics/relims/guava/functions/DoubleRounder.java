package com.compomics.relims.guava.functions;

import com.google.common.base.Function;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * This class is a
 */
public class DoubleRounder implements Function<Double, Double> {

    private static DoubleRounder instance = new DoubleRounder();
    private int iDecimals = 2;

    public DoubleRounder() {

    }

    public DoubleRounder(int aDecimals) {
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
