package com.controller;

import com.model.Repository;
import com.google.gson.*;
import com.model.RepositoryWrapper;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.kohsuke.github.*;
import org.springframework.web.bind.annotation.*;
import spark.Request;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

import static org.kohsuke.github.GHDeploymentState.PENDING;
import static org.kohsuke.github.GHDeploymentState.SUCCESS;
import static spark.Spark.get;
import static spark.Spark.post;

@RestController
public class ForceCIController {

    private static final String GITHUB_API = "https://api.github.com";

    @RequestMapping(value = "/auth", method = RequestMethod.GET, params = {"code", "state"})
    public void auth(@RequestParam String code, @RequestParam String state, ServletResponse response, ServletRequest
            request) throws Exception {

        String environment = "https://github.com/login/oauth/access_token";
        HttpClient httpClient = new HttpClient();

        PostMethod post = new PostMethod(environment);
        post.setRequestHeader("Accept", MediaType.APPLICATION_JSON);
        post.addParameter("code", code);
        post.addParameter("redirect_uri", "https://forceci.herokuapp.com/auth");
        post.addParameter("client_id", "0b5a2cb25fa55a0d2b76");
        post.addParameter("client_secret", "27e2145693b538a466e8264735259dafdaf783e7");
        post.addParameter("state", state);

        httpClient.executeMethod(post);
        String responseBody = post.getResponseBodyAsString();

        String accessToken = null;
        String token_type = null;
        JsonParser parser = new JsonParser();

        JsonObject jsonObject = parser.parse(responseBody).getAsJsonObject();
        System.out.println("jsonObject is now " + jsonObject);

        try {

            accessToken = jsonObject.get("access_token").getAsString();
            token_type = jsonObject.get("token_type").getAsString();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            post.releaseConnection();
        }

        HttpServletResponse httpResponse = (HttpServletResponse) response;
        Cookie session1 = new Cookie("ACCESS_TOKEN", accessToken);
        Cookie session2 = new Cookie("TOKEN_TYPE", token_type);
        session1.setMaxAge(-1); //cookie not persistent, destroyed on browser exit
        session2.setMaxAge(-1); //cookie not persistent, destroyed on browser exit
        httpResponse.addCookie(session1);
        httpResponse.addCookie(session2);
        httpResponse.sendRedirect("/html/dashboard.html");

    }

    @RequestMapping(value = "/event_handler", method = RequestMethod.GET)
    public void webhookReceiver(@RequestParam String code, @RequestParam String state, ServletResponse response, ServletRequest
            request) throws Exception {

        Request req = (Request) request;
        String payload = req.body();
        String x_github_event = req.headers("X-GITHUB-EVENT");

        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(payload, JsonElement.class).getAsJsonObject();

        switch (x_github_event) {
            case "pull_request":
                if ("closed".equalsIgnoreCase(jsonObject.get("action").getAsString()) &&
                        jsonObject.get("pull_request").getAsJsonObject().get("merged").getAsBoolean()) {

                    System.out.println("A pull request was merged! A deployment should start now...");

                    start_deployment(jsonObject.get("pull_request").getAsJsonObject());
                }
                break;
            case "deployment":
                process_deployment(jsonObject);
                break;
            case "deployment_status":
                update_deployment_status(jsonObject);
                break;
        }

    }

    @RequestMapping(value = "/listRepository", method = RequestMethod.GET)
    public RepositoryWrapper getRepositoryList(HttpServletResponse response, HttpServletRequest request) throws IOException, JSONException {
        List<Repository> repositoryList = new ArrayList<>();
        RepositoryWrapper repositoryWrapper = null;
        Cookie[] cookies = request.getCookies();
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals("ACCESS_TOKEN")) {
                String accessToken = cookie.getValue();
                GetMethod getUserMethod = new GetMethod(GITHUB_API + "/user");
                getUserMethod.setRequestHeader("Authorization", "token " + accessToken);
                HttpClient httpClient = new HttpClient();
                httpClient.executeMethod(getUserMethod);
                List<JSONObject> jsonObjects = new ArrayList<>();
                JSONObject jsonResponse = new JSONObject(new JSONTokener(new InputStreamReader(getUserMethod.getResponseBodyAsStream())));
                if (jsonResponse.has("login")) {
                    String loginId = (String) jsonResponse.get("login");
                    GetMethod getUserRepository = new GetMethod(GITHUB_API + "/users/" + loginId + "/repos");
                    getUserRepository.setRequestHeader("Authorization", "token " + accessToken);
                    httpClient = new HttpClient();
                    httpClient.executeMethod(getUserRepository);
                    JsonParser jsonParser = new JsonParser();
                    JsonElement parse = jsonParser.parse(new InputStreamReader(getUserRepository.getResponseBodyAsStream()));
                    if (parse.isJsonArray()) {
                        repositoryWrapper = new RepositoryWrapper();
                        JsonArray asJsonArray = parse.getAsJsonArray();
                        for (JsonElement jsonElement : asJsonArray) {
                            if (jsonElement.isJsonObject()) {
                                String name = jsonElement.getAsJsonObject().get("name").getAsString();
                                String htmlUrl = jsonElement.getAsJsonObject().get("html_url").getAsString();
                                Repository repository = new Repository();
                                repository.setRepositoryName(name);
                                repository.setRepositoryUrl(htmlUrl);
                                repository.setActive(Boolean.FALSE);
                                repositoryList.add(repository);
                            }
                        }
                    }
                    if(repositoryWrapper != null) {
                        repositoryWrapper.setLstRepositories(repositoryList);
                        repositoryWrapper.setOwnerId(loginId);
                    }
                }
            }
        }
        return repositoryWrapper;
    }

    @RequestMapping(value = "/modifyRepository", method = RequestMethod.POST)
    public Repository createFile(@RequestBody Repository repository, HttpServletResponse response, HttpServletRequest
            request) {
        System.out.println("enabled ---> " + repository.getActive());
        System.out.println("repositoryName ---> " + repository.getRepositoryName());
        return repository;
    }

    @RequestMapping(value = "/createWebHook", method = RequestMethod.POST)
    public Boolean createWebHook(@RequestBody Repository repository, HttpServletResponse response, HttpServletRequest
            request) throws IOException {

        Cookie[] cookies = request.getCookies();
        String accessToken = null;
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals("ACCESS_TOKEN")) {
                accessToken = cookie.getValue();
                break;
            }
        }

        if(accessToken != null){
            PostMethod createWebHook = new PostMethod(GITHUB_API + "/repos/" + repository.getOwner()+"/"+repository.getRepositoryName()+ "/repos");
            createWebHook.setRequestHeader("Authorization", "token " + accessToken);
            List<NameValuePair> nameValuePairs = new ArrayList<>();
            NameValuePair nameValuePair = new NameValuePair();
            nameValuePair.setName("name");
            nameValuePair.setValue("Test1");
            nameValuePair.setName("active");
            nameValuePair.setValue("true");
            nameValuePair.setName("events");
            nameValuePair.setValue("[\"push\",\"pull_request\"]");
            nameValuePair.setName("config");
            NameValuePair configValue = new NameValuePair();
            configValue.setName("url");
            configValue.setValue("http://example.com/webhook");
            configValue.setName("content_type");
            configValue.setValue("json");
            nameValuePair.setValue(configValue.toString());
            nameValuePairs.add(nameValuePair);
            NameValuePair[] arrayNameValuePair = new NameValuePair[nameValuePairs.size()];
            arrayNameValuePair = nameValuePairs.toArray(arrayNameValuePair);
            createWebHook.setRequestBody(arrayNameValuePair);
            HttpClient httpClient = new HttpClient();
            httpClient.executeMethod(createWebHook);
            JsonParser jsonParser = new JsonParser();
            JsonElement parse = jsonParser.parse(new InputStreamReader(createWebHook.getResponseBodyAsStream()));
        }

        return true;
    }


    private static void start_deployment(JsonObject jsonObject) {
        String user = jsonObject.get("user").getAsJsonObject().get("login").getAsString();
        Map<String, String> map = new HashMap<>();
        map.put("environment", "QA");
        map.put("deploy_user", user);
        Gson gson = new Gson();
        String payload = gson.toJson(map);

        try {
            GitHub gitHub = GitHubBuilder.fromEnvironment().build();
            GHRepository repository = gitHub.getRepository(
                    jsonObject.get("head").getAsJsonObject()
                            .get("repo").getAsJsonObject()
                            .get("full_name").getAsString());
            GHDeployment deployment =
                    new GHDeploymentBuilder(
                            repository,
                            jsonObject.get("head").getAsJsonObject().get("sha").getAsString()
                    ).description("Auto Deploy after merge").payload(payload).autoMerge(false).create();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void process_deployment(JsonObject jsonObject) {
        String payload_str = jsonObject.get("deployment").getAsJsonObject().get("payload").getAsString();
        Map payload = new Gson().fromJson(payload_str, Map.class);

        System.out.println("Processing " + jsonObject.get("deployment").getAsJsonObject().get("description").getAsString() +
                " for " + payload.<String>get("deploy_user") + " to " + payload.<String>get("environment"));

        try {
            Thread.sleep(2000L);
            GitHub gitHub = GitHubBuilder.fromEnvironment().build();
            GHRepository repository = gitHub.getRepository(
                    jsonObject.get("repository").getAsJsonObject()
                            .get("full_name").getAsString());
            GHDeploymentStatus deploymentStatus = new GHDeploymentStatusBuilder(repository,
                    jsonObject.get("deployment").getAsJsonObject().get("id").getAsInt(), PENDING).create();
            Thread.sleep(5000L);

            GHDeploymentStatus deploymentStatus2 = new GHDeploymentStatusBuilder(repository,
                    jsonObject.get("deployment").getAsJsonObject().get("id").getAsInt(), SUCCESS).create();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }


    private static void update_deployment_status(JsonObject jsonObject) {
        System.out.println("Deployment status for " + jsonObject.get("deployment").getAsJsonObject().get("id").getAsString() +
                " is " + jsonObject.get("deployment_status").getAsJsonObject().get("state").getAsString());
    }
}