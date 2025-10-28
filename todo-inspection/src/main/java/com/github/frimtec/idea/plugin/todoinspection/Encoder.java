package com.github.frimtec.idea.plugin.todoinspection;

import com.intellij.openapi.diagnostic.Logger;
import org.apache.commons.lang3.StringUtils;

import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

record Encoder(String encodedValue) {

    private final static Logger LOGGER = Logger.getInstance(MethodHandles.lookup().lookupClass());

    private static final Base64.Decoder DECODER = Base64.getDecoder();
    private static final Base64.Encoder ENCODER = Base64.getEncoder();

    String plain() {
        try {
            return new String(DECODER.decode(StringUtils.reverse(encodedValue)), StandardCharsets.UTF_8);
        } catch (Exception e) {
            LOGGER.warn("Failed to decode secret, will be reset to empty");
            return "";
        }
    }

    static Encoder fromPlain(String value) {
        return new Encoder(StringUtils.reverse(ENCODER.encodeToString(value.getBytes(StandardCharsets.UTF_8))));
    }
}
