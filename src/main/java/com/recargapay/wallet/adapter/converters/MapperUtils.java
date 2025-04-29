package com.recargapay.wallet.adapter.converters;

public class MapperUtils {
    private MapperUtils() {}

    public static <S, T> T mapIfNotNull(S source, java.util.function.Function<S, T> mappingFunction) {
        return source == null ? null : mappingFunction.apply(source);
    }
}
