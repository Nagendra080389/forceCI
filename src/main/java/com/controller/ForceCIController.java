package com.controller;

import com.dao.RepositoryWrapperMongoRepository;
import com.google.gson.reflect.TypeToken;
import com.model.*;
import com.google.gson.*;
import com.utils.ApiSecurity;
import org.apache.commons.codec.digest.HmacUtils;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpHost;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.io.IOUtils;
import org.apache.http.conn.params.ConnRoutePNames;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.kohsuke.github.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import spark.Request;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.*;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.kohsuke.github.GHDeploymentState.PENDING;
import static org.kohsuke.github.GHDeploymentState.SUCCESS;
import static spark.Spark.get;
import static spark.Spark.post;

@RestController
public class ForceCIController {

    public static final int HTTP_STATUS_OK = 200;
    @Value("${github.clientId}")
    String githubClientId;

    @Value("${github.clientSecret}")
    String githubClientSecret;

    @Value("${application.redirectURI}")
    String redirectURI;

    @Value("${application.hmacSecretKet}")
    String hmacSecretKet;

    public final static List<Integer> LIST_VALID_RESPONSE_CODES =  Arrays.asList(200, 201, 204, 207);

    @Autowired
    private RepositoryWrapperMongoRepository repositoryWrapperMongoRepository;

    private static final String GITHUB_API = "https://api.github.com";

    @RequestMapping(value = "/auth", method = RequestMethod.GET, params = {"code", "state"})
    public void auth(@RequestParam String code, @RequestParam String state, ServletResponse response, ServletRequest
            request) throws Exception {

        String environment = "https://github.com/login/oauth/access_token";
        HttpClient httpClient = new HttpClient();

        PostMethod post = new PostMethod(environment);
        post.setRequestHeader("Accept", MediaType.APPLICATION_JSON);
        post.addParameter("code", code);
        post.addParameter("redirect_uri", redirectURI);
        post.addParameter("client_id", githubClientId);
        post.addParameter("client_secret", githubClientSecret);
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

    @RequestMapping(value = "/hooks/github", method = RequestMethod.POST)
    public String webhooks(@RequestHeader("X-Hub-Signature") String signature, @RequestHeader("X-GitHub-Event") String githubEvent, @RequestBody String payload, HttpServletResponse response, HttpServletRequest request) {
        Gson gson = new Gson();
        // if signature is empty return 401
        if (!StringUtils.hasText(signature)) {
            return gson.toJson(HttpStatus.FORBIDDEN);
        }

        JsonObject jsonObject = gson.fromJson(payload, JsonElement.class).getAsJsonObject();

        switch (githubEvent){
            case "pull_request" :
                System.out.println(jsonObject);
                start_deployment(jsonObject.get("pull_request").getAsJsonObject());
                break;
            case "push":
                System.out.println(jsonObject);
                break;
        }

        return gson.toJson("");
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

    @RequestMapping(value = "/fetchRepositoryInDB", method = RequestMethod.GET)
    public String getRepositoryList(@RequestParam String gitHubUser, HttpServletResponse response, HttpServletRequest request) throws IOException, JSONException {
        Gson gson = new Gson();
        String reposOnDB = "";
        List<RepositoryWrapper> lstRepositoryWrapper = repositoryWrapperMongoRepository.findByOwnerId(gitHubUser);
        if(lstRepositoryWrapper != null && !lstRepositoryWrapper.isEmpty()){
            reposOnDB = gson.toJson(lstRepositoryWrapper);
        }
        return reposOnDB;
    }

    @RequestMapping(value = "/fetchUserName", method = RequestMethod.GET)
    public String getUserName(HttpServletResponse response, HttpServletRequest request) throws IOException, JSONException {
        Cookie[] cookies = request.getCookies();
        Gson gson = new Gson();
        String loginNameAndAvatar = "";
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals("ACCESS_TOKEN")) {
                String accessToken = cookie.getValue();
                GetMethod getUserMethod = new GetMethod(GITHUB_API + "/user");
                getUserMethod.setRequestHeader("Authorization", "token " + accessToken);
                HttpClient httpClient = new HttpClient();
                int intStatusOk = httpClient.executeMethod(getUserMethod);
                if(intStatusOk == HTTP_STATUS_OK) {
                    GitRepositoryUser gitRepositoryUser = gson.fromJson(IOUtils.toString(getUserMethod.getResponseBodyAsStream(), "UTF-8"), GitRepositoryUser.class);
                    loginNameAndAvatar = gson.toJson(gitRepositoryUser);
                }
            }
        }
        return loginNameAndAvatar;
    }

    @RequestMapping(value = "/fetchRepository", method = RequestMethod.GET)
    public String getRepositoryByName(@RequestParam String repoName, @RequestParam String repoUser, HttpServletResponse response, HttpServletRequest request) throws IOException, JSONException {
        Cookie[] cookies = request.getCookies();
        Gson gson = new Gson();
        String lstRepo = "";
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals("ACCESS_TOKEN")) {
                String accessToken = cookie.getValue();
                String queryParam = repoName +"%20in:name+user:"+repoUser+"+fork:true";
                GetMethod getRepoByName = new GetMethod(GITHUB_API + "/search/repositories?q="+queryParam);
                getRepoByName.setRequestHeader("Authorization", "token " + accessToken);
                HttpClient httpClient = new HttpClient();
                int intStatusOk = httpClient.executeMethod(getRepoByName);
                if(intStatusOk == HTTP_STATUS_OK) {
                    GitRepositoryFromQuery gitRepositoryFromQuery = gson.fromJson(IOUtils.toString(getRepoByName.getResponseBodyAsStream(), "UTF-8"), GitRepositoryFromQuery.class);
                    lstRepo = gson.toJson(gitRepositoryFromQuery);
                }
            }
        }
        return lstRepo;
    }

    @RequestMapping(value = "/deleteWebHook", method = RequestMethod.DELETE)
    public String deleteWebHook(@RequestParam String repositoryName, @RequestParam String repositoryOwner,
                                @RequestParam String webHookId, HttpServletResponse response, HttpServletRequest
            request) throws IOException {

        Gson gson = new Gson();
        int status = 0;

        String accessToken = fetchCookies(request);
        if (accessToken != null) {
            DeleteMethod deleteWebHook = new DeleteMethod(GITHUB_API + "/repos/" + repositoryOwner + "/" + repositoryName + "/hooks/"+webHookId);
            deleteWebHook.setRequestHeader("Authorization", "token " + accessToken);
            deleteWebHook.setRequestHeader("Content-Type", MediaType.APPLICATION_JSON);
            HttpClient httpClient = new HttpClient();
            status = httpClient.executeMethod(deleteWebHook);
            if(status == 204){
                RepositoryWrapper byRepositoryRepositoryName = repositoryWrapperMongoRepository.findByOwnerIdAndRepositoryRepositoryName(repositoryOwner, repositoryName);
                repositoryWrapperMongoRepository.delete(byRepositoryRepositoryName);
            }
        }

        return gson.toJson(status);
    }

    @RequestMapping(value = "/createWebHook", method = RequestMethod.POST)
    public String createWebHook(@RequestBody Repository repository, HttpServletResponse response, HttpServletRequest
            request) throws IOException {

        Gson gson = new Gson();
        String returnResponse = null;

        String accessToken = fetchCookies(request);
        if (accessToken != null) {
            PostMethod createWebHook = new PostMethod(GITHUB_API + "/repos/" + repository.getOwner() + "/" + repository.getRepositoryName() + "/hooks");
            createWebHook.setRequestHeader("Authorization", "token " + accessToken);
            createWebHook.setRequestHeader("Content-Type", MediaType.APPLICATION_JSON);
            CreateWebhookPayload createWebhookPayload = new CreateWebhookPayload();
            createWebhookPayload.setActive(repository.getActive());
            List<String> events = new ArrayList<>();
            events.add("*");
            createWebhookPayload.setEvents(events);
            Config config = new Config();
            config.setContent_type("json");
            String randomString = ApiSecurity.generateSafeToken();
            config.setSecret(randomString);
            config.setUrl("https://forceci.herokuapp.com/hooks/github");
            createWebhookPayload.setConfig(config);
            createWebhookPayload.setName("web");
            createWebHook.setRequestBody(gson.toJson(createWebhookPayload));
            HttpClient httpClient = new HttpClient();
            int status = httpClient.executeMethod(createWebHook);
            if(LIST_VALID_RESPONSE_CODES.contains(status)) {
                WebHook webHookResponse = gson.fromJson(IOUtils.toString(createWebHook.getResponseBodyAsStream(), "UTF-8"), WebHook.class);
                repository.setWebHook(webHookResponse);
                repository.setHmacSecret(randomString);
                RepositoryWrapper repositoryWrapper = new RepositoryWrapper();
                repositoryWrapper.setOwnerId(repository.getOwner());
                repositoryWrapper.setRepository(repository);
                repositoryWrapperMongoRepository.save(repositoryWrapper);
                returnResponse = gson.toJson(repository);
            }

        }

        return returnResponse;
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

    private static String fetchCookies(HttpServletRequest request) {
        String returnResponse = null;

        Cookie[] cookies = request.getCookies();
        String accessToken = null;
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals("ACCESS_TOKEN")) {
                accessToken = cookie.getValue();
                break;
            }
        }
        return accessToken;
    }
}
