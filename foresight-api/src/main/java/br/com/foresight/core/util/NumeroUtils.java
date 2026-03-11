package br.com.foresight.core.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class NumeroUtils {
    public static BigDecimal formatarMoeda(BigDecimal valor) {
        if (valor == null) return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        return valor.setScale(2, RoundingMode.HALF_UP);
    }
}