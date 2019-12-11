package com.utils;

import org.apache.commons.io.IOUtils;

import javax.xml.bind.DatatypeConverter;
import java.security.SecureRandom;
import java.util.Base64;

public class ApiSecurity {

    public static String generateSafeToken() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[20];
        random.nextBytes(bytes);
        Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
        return encoder.encodeToString(bytes);
    }
}
