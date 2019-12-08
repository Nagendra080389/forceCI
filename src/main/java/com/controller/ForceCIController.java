package com.controller;

import com.dao.RepositoryWrapperMongoRepository;
import com.google.gson.reflect.TypeToken;
import com.model.*;
import com.google.gson.*;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.kohsuke.github.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import spark.Request;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
        Gson gson = new Gson();
        RepositoryWrapper repositoryWrapper = null;
        Cookie[] cookies = request.getCookies();
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals("ACCESS_TOKEN")) {
                String accessToken = cookie.getValue();
                GetMethod getUserMethod = new GetMethod(GITHUB_API + "/user");
                getUserMethod.setRequestHeader("Authorization", "token " + accessToken);
                HttpClient httpClient = new HttpClient();
                int intStatusOk = httpClient.executeMethod(getUserMethod);
                if(intStatusOk == HTTP_STATUS_OK) {
                    JSONObject jsonResponse = new JSONObject(new JSONTokener(new InputStreamReader(getUserMethod.getResponseBodyAsStream())));
                    if (jsonResponse.has("login")) {
                        List<Repository> repositoryList = new ArrayList<>();
                        String loginId = (String) jsonResponse.get("login");
                        GetMethod getUserRepository = new GetMethod(GITHUB_API + "/users/" + loginId + "/repos");
                        repositoryWrapper = repositoryWrapperMongoRepository.findByOwnerId(loginId);
                        List<String> listOfRepositoryNameInDb = new ArrayList<>();
                        if (repositoryWrapper != null) {
                            for (Repository eachRepository : repositoryWrapper.getLstRepositories()) {
                                listOfRepositoryNameInDb.add(eachRepository.getRepositoryName());
                            }
                        } else {
                            repositoryWrapper = new RepositoryWrapper();
                        }

                        getUserRepository.setRequestHeader("Authorization", "token " + accessToken);
                        httpClient = new HttpClient();
                        httpClient.executeMethod(getUserRepository);
                        for (Header requestHeader : getUserRepository.getResponseHeaders("Link")) {
                            System.out.println("getUserRepository.getResponseHeaders() -> "+requestHeader.getName()+" - " + requestHeader.getValue());
                        }
                        Type listOfGitRepository = new TypeToken<ArrayList<GitRepository>>() {
                        }.getType();
                        List<GitRepository> lstGitRepos = gson.fromJson(IOUtils.toString(getUserRepository.getResponseBodyAsStream(), "UTF-8"), listOfGitRepository);
                        for (GitRepository eachGitRepository : lstGitRepos) {
                            String name = eachGitRepository.getName();
                            if (!listOfRepositoryNameInDb.contains(name)) {
                                Repository repository = new Repository();
                                String htmlUrl = eachGitRepository.getHtml_url();
                                GetMethod getWebHook = new GetMethod(GITHUB_API + "/repos/" + loginId + "/" + name + "/hooks");
                                getWebHook.setRequestHeader("Authorization", "token " + accessToken);
                                httpClient = new HttpClient();
                                httpClient.executeMethod(getWebHook);

                                Type listOfWebHooks = new TypeToken<ArrayList<WebHook>>() {
                                }.getType();
                                List<WebHook> webHookList = gson.fromJson(getWebHook.getResponseBodyAsString(), listOfWebHooks);
                                for (WebHook webHook : webHookList) {
                                    if (webHook.getName() != null && webHook.getName().contains("/forceCI/")) {
                                        repository.setWebHook(webHook);
                                    }
                                }
                                repository.setRepositoryName(name);
                                repository.setRepositoryUrl(htmlUrl);
                                repository.setActive(Boolean.FALSE);
                                repositoryList.add(repository);
                            }

                        }
                        repositoryWrapper.setLstRepositories(repositoryList);
                        repositoryWrapper.setOwnerId(loginId);
                        repositoryWrapperMongoRepository.save(repositoryWrapper);
                    }
                }
            }
        }
        return repositoryWrapper;
    }

    @RequestMapping(value = "/fetchUserName", method = RequestMethod.GET)
    public String getUserName(HttpServletResponse response, HttpServletRequest request) throws IOException, JSONException {
        RepositoryWrapper repositoryWrapper = null;
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


    @RequestMapping(value = "/modifyRepository", method = RequestMethod.POST)
    public Repository createFile(@RequestBody Repository repository, HttpServletResponse response, HttpServletRequest
            request) {
        String accessToken = fetchCookies(request);

        System.out.println("enabled ---> " + repository.getActive());
        System.out.println("repositoryName ---> " + repository.getRepositoryName());
        return repository;
    }

    @RequestMapping(value = "/createWebHook", method = RequestMethod.POST)
    public String createWebHook(@RequestBody Repository repository, HttpServletResponse response, HttpServletRequest
            request) throws IOException {

        Gson gson = new Gson();
        String returnResponse = null;

        String accessToken = fetchCookies(request);

        System.out.println("accessToken --> " + accessToken);
        if (accessToken != null) {
            PostMethod createWebHook = new PostMethod(GITHUB_API + "/repos/" + repository.getOwner() + "/" + repository.getRepositoryName() + "/hooks");
            createWebHook.setRequestHeader("Authorization", "token " + accessToken);
            createWebHook.setRequestHeader("Content-Type", MediaType.APPLICATION_JSON);
            CreateWebhookPayload createWebhookPayload = new CreateWebhookPayload();
            createWebhookPayload.setActive(repository.getActive());
            List<String> events = new ArrayList<>();
            events.add("push");
            events.add("pull_request");
            createWebhookPayload.setEvents(events);
            Config config = new Config();
            config.setContentType("json");
            config.setUrl("https://forceci.herokuapp.com/" + repository.getRepositoryName());
            createWebhookPayload.setConfig(config);
            createWebhookPayload.setName("web");
            System.out.println("gson.toJson(createWebhookPayload) -> " + gson.toJson(createWebhookPayload));
            createWebHook.setRequestBody(gson.toJson(createWebhookPayload));
            HttpClient httpClient = new HttpClient();
            httpClient.executeMethod(createWebHook);
            JsonParser jsonParser = new JsonParser();
            JsonElement parse = jsonParser.parse(new InputStreamReader(createWebHook.getResponseBodyAsStream()));
            System.out.println("createWebHook -> " + createWebHook.getRequestEntity().getContentType());
            System.out.println(" parse---> " + parse);
            returnResponse = gson.toJson(parse);
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
