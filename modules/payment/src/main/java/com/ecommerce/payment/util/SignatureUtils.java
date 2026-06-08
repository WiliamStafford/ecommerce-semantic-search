package com.ecommerce.payment.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

public class SignatureUtils {

    public static String hmacSha256(String data, String key) throws Exception {
        byte[] byteKey = key.getBytes(StandardCharsets.UTF_8);
        byte[] byteData = data.getBytes(StandardCharsets.UTF_8);
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secret_key = new SecretKeySpec(byteKey, "HmacSHA256");
        sha256_HMAC.init(secret_key);
        byte[] hashBytes = sha256_HMAC.doFinal(byteData);
        return HexFormat.of().formatHex(hashBytes);
    }
    public static String hmacSha512(String data, String key) throws Exception {
        if (key == null || data == null) throw new NullPointerException();
        Mac hmac512 = Mac.getInstance("HmacSHA512");
        byte[] hmacKeyBytes = key.getBytes(StandardCharsets.UTF_8);
        SecretKeySpec secretKey = new SecretKeySpec(hmacKeyBytes, "HmacSHA512");
        hmac512.init(secretKey);
        byte[] result = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));

        StringBuilder sb = new StringBuilder(2 * result.length);
        for (byte b : result) {
            sb.append(String.format("%02x", b & 0xff));
        }
        return sb.toString();
    }
}
