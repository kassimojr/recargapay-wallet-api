package com.recargapay.wallet.adapter.converters;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MapperUtilsTest {
    @Test
    void mapIfNotNull_shouldReturnMappedValue() {
        String result = MapperUtils.mapIfNotNull("abc", s -> ((String) s).toUpperCase());
        assertEquals("ABC", result);
    }

    @Test
    void mapIfNotNull_shouldReturnNullIfSourceNull() {
        String result = MapperUtils.mapIfNotNull(null, s -> ((String) s).toUpperCase());
        assertNull(result);
    }
}
