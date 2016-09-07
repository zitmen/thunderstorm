package cz.cuni.lf1.lge.ThunderSTORM.util;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class StringFormatting {
    public static DecimalFormat getDecimalFormat() {
        return getDecimalFormat(Integer.MAX_VALUE);
    }

    public static DecimalFormat getDecimalFormat(int floatPrecision) {
        DecimalFormat df = new DecimalFormat();
        DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(Locale.US);
        symbols.setInfinity("Infinity");
        symbols.setNaN("NaN");
        df.setDecimalFormatSymbols(symbols);
        df.setGroupingUsed(false);
        df.setRoundingMode(RoundingMode.HALF_EVEN);
        df.setMaximumFractionDigits(floatPrecision);
        return df;
    }
}
