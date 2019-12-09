package com.utils;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class ApiSecurity {
    public static String getHashValue(String secret, String message) {
        String hash = "";
        try {

            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
            sha256_HMAC.init(secret_key);

            hash = Base64.encodeBase64String(sha256_HMAC.doFinal(message.getBytes()));
        }
        catch (Exception e){
            System.out.println(e.getMessage());
        }

        return hash;
    }

}
