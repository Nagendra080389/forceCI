package com.forceCI.forceCI.controller;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

@RestController
public class ForceCIController {
    @RequestMapping(value = "/auth", method = RequestMethod.GET, params = {"code", "state"})
    public void auth(@RequestParam String code, @RequestParam String state, ServletResponse response, ServletRequest
            request) throws Exception {

        String environment = "https://github.com/login/oauth/access_token";

        System.out.println("environment -> " + environment);
        HttpClient httpClient = new HttpClient();

        PostMethod post = new PostMethod(environment);
        post.addParameter("code", code);
        post.addParameter("grant_type", "authorization_code");
        post.addParameter("redirect_uri", "https://forceci.herokuapp.com/auth");
        post.addParameter("client_id", "0b5a2cb25fa55a0d2b76");
        post.addParameter("client_secret", "27e2145693b538a466e8264735259dafdaf783e7");

        httpClient.executeMethod(post);
        String responseBody = post.getResponseBodyAsString();

        String accessToken = null;
        String issuedAt = null;
        String signature = null;
        String id_token = null;
        String instance_url = null;
        String useridURL = null;
        String username = null;
        String display_name = null;
        String email = null;
        JsonParser parser = new JsonParser();

        JsonObject jsonObject = parser.parse(responseBody).getAsJsonObject();
        System.out.println("jsonObject is now "+jsonObject);

        try {

            accessToken = jsonObject.get("access_token").getAsString();
            issuedAt = jsonObject.get("issued_at").getAsString();
            signature = jsonObject.get("signature").getAsString();
            id_token = jsonObject.get("id_token").getAsString();
            instance_url = jsonObject.get("instance_url").getAsString();
            useridURL = jsonObject.get("id").getAsString();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            post.releaseConnection();
        }

        HttpServletResponse httpResponse = (HttpServletResponse) response;
        Cookie session1 = new Cookie("ACCESS_TOKEN", accessToken);
        Cookie session2 = new Cookie("INSTANCE_URL", instance_url);
        Cookie session3 = new Cookie("ID_TOKEN", id_token);
        Cookie session4 = new Cookie("USERIDURL", useridURL);
        session1.setMaxAge(-1); //cookie not persistent, destroyed on browser exit
        session2.setMaxAge(-1); //cookie not persistent, destroyed on browser exit
        session3.setMaxAge(-1); //cookie not persistent, destroyed on browser exit
        session4.setMaxAge(-1); //cookie not persistent, destroyed on browser exit
        httpResponse.addCookie(session1);
        httpResponse.addCookie(session2);
        httpResponse.addCookie(session3);
        httpResponse.addCookie(session4);
        httpResponse.sendRedirect(null);

    }

    @RequestMapping(value = "/webhook", method = RequestMethod.GET, params = {"code", "state"})
    public void webhookReceiver(@RequestParam String code, @RequestParam String state, ServletResponse response, ServletRequest
            request) throws Exception {

        String environment = "https://github.com/login/oauth/access_token";

        System.out.println("environment -> " + environment);
        HttpClient httpClient = new HttpClient();

        PostMethod post = new PostMethod(environment);
        post.addParameter("code", code);
        post.addParameter("grant_type", "authorization_code");
        post.addParameter("redirect_uri", "https://forceci.herokuapp.com/auth");
        post.addParameter("client_id", "Iv1.cf5c48ba09a79e4a");
        post.addParameter("client_secret", "be7c5020e0ce4090edfbaec20bc27a9a6d72fc77");

        httpClient.executeMethod(post);
        String responseBody = post.getResponseBodyAsString();

        String accessToken = null;
        String issuedAt = null;
        String signature = null;
        String id_token = null;
        String instance_url = null;
        String useridURL = null;
        String username = null;
        String display_name = null;
        String email = null;
        JsonParser parser = new JsonParser();

        JsonObject jsonObject = parser.parse(responseBody).getAsJsonObject();
        System.out.println("jsonObject is now "+jsonObject);

        try {

            accessToken = jsonObject.get("access_token").getAsString();
            issuedAt = jsonObject.get("issued_at").getAsString();
            signature = jsonObject.get("signature").getAsString();
            id_token = jsonObject.get("id_token").getAsString();
            instance_url = jsonObject.get("instance_url").getAsString();
            useridURL = jsonObject.get("id").getAsString();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            post.releaseConnection();
        }

        HttpServletResponse httpResponse = (HttpServletResponse) response;
        Cookie session1 = new Cookie("ACCESS_TOKEN", accessToken);
        Cookie session2 = new Cookie("INSTANCE_URL", instance_url);
        Cookie session3 = new Cookie("ID_TOKEN", id_token);
        Cookie session4 = new Cookie("USERIDURL", useridURL);
        session1.setMaxAge(-1); //cookie not persistent, destroyed on browser exit
        session2.setMaxAge(-1); //cookie not persistent, destroyed on browser exit
        session3.setMaxAge(-1); //cookie not persistent, destroyed on browser exit
        session4.setMaxAge(-1); //cookie not persistent, destroyed on browser exit
        httpResponse.addCookie(session1);
        httpResponse.addCookie(session2);
        httpResponse.addCookie(session3);
        httpResponse.addCookie(session4);
        httpResponse.sendRedirect(null);

    }
}
