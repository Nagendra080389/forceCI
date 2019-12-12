package com.utils;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.IOUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class ApiSecurity {

    public static final String HMAC_SHA_1 = "HmacSHA1";

    public static String generateSafeToken() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[20];
        random.nextBytes(bytes);
        Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
        return encoder.encodeToString(bytes);
    }

    public static boolean verifySignature(String payLoad, String hmacSecret, String signatureFromGit) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac hmac = Mac.getInstance(HMAC_SHA_1);
        hmac.init(new SecretKeySpec(hmacSecret.getBytes(StandardCharsets.UTF_8), HMAC_SHA_1));
        String calculatedSignature = Hex.encodeHexString(hmac.doFinal(payLoad.getBytes(StandardCharsets.UTF_8)));
        return MessageDigest.isEqual(calculatedSignature.getBytes(StandardCharsets.UTF_8), signatureFromGit.substring("sha1=".length()).getBytes(StandardCharsets.UTF_8));
    }
}
