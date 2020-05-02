package com.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.model.SFDCConnectionDetails;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class SFDCUtilsCommons {

    private static final Logger logger = LoggerFactory.getLogger(SFDCUtilsCommons.class);

    public static String refreshSFDCToken(SFDCConnectionDetails sfdcConnectionDetails) throws IOException {
        String refreshToken = sfdcConnectionDetails.getRefreshToken();
        String environment = sfdcConnectionDetails.getEnvironment();
        String instanceURL = sfdcConnectionDetails.getInstanceURL();
        String clientId = System.getenv("SFDC_CLIENTID");
        String clientSecret = System.getenv("SFDC_CLIENTSECRET");
        String url = "";
        if (environment.equals("0")) {
            url = "https://login.salesforce.com/services/oauth2/token?" +
                    "grant_type=refresh_token&client_id=" + clientId + "&client_secret=" + clientSecret +
                    "&refresh_token=" + refreshToken;
        } else if (environment.equals("1")) {
            url = "https://test.salesforce.com/services/oauth2/token?" +
                    "grant_type=refresh_token&client_id=" + clientId + "&client_secret=" + clientSecret +
                    "&refresh_token=" + refreshToken;
        } else {
            url = instanceURL + "/services/oauth2/token?" +
                    "grant_type=refresh_token&client_id=" + clientId + "&client_secret=" + clientSecret +
                    "&refresh_token=" + refreshToken;
        }
        HttpClient httpClient = new HttpClient();
        PostMethod post = new PostMethod(url);
        int intStatus = httpClient.executeMethod(post);
        logger.info("Refresh Token return status = "+intStatus);
        String responseBody = IOUtils.toString(post.getResponseBodyAsStream(), StandardCharsets.UTF_8);
        JsonParser parser = new JsonParser();
        JsonObject jsonObject = parser.parse(responseBody).getAsJsonObject();
        return jsonObject.get("access_token").getAsString();
    }

}


