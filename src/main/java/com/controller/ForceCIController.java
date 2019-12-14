package com.controller;

import com.dao.RepositoryWrapperMongoRepository;
import com.dao.SFDCConnectionDetailsMongoRepository;
import com.dao.UserWrapperMongoRepository;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.model.*;
import com.utils.ApiSecurity;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.kohsuke.github.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.kohsuke.github.GHDeploymentState.PENDING;

@RestController
public class ForceCIController {

    public static final int HTTP_STATUS_OK = 200;
    @Value("${github.clientId}")
    String githubClientId;

    @Value("${github.clientSecret}")
    String githubClientSecret;

    @Value("${application.redirectURI}")
    String redirectURI;

    @Value("${sfdc.clientId}")
    String sfdcClientId;

    @Value("${sfdc.clientSecret}")
    String sfdcClientSecret;

    @Value("${sfdc.redirectURI}")
    String sfdcRedirectURI;

    @Value("${application.hmacSecretKet}")
    String hmacSecretKet;

    private final static List<Integer> LIST_VALID_RESPONSE_CODES =  Arrays.asList(200, 201, 204, 207);

    @Autowired
    private RepositoryWrapperMongoRepository repositoryWrapperMongoRepository;

    @Autowired
    private UserWrapperMongoRepository userWrapperMongoRepository;

    @Autowired
    private SFDCConnectionDetailsMongoRepository sfdcConnectionDetailsMongoRepository;

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


    @RequestMapping(value = "/sfdcAuth", method = RequestMethod.GET, params = {"code", "state"})
    public void sfdcAuth(@RequestParam String code, @RequestParam String state, ServletResponse response, ServletRequest request) throws Exception {

        String environment = null;
        System.out.println(" state -> "+state);
        if (state.equals("0")) {
            environment = "https://login.salesforce.com/services/oauth2/token";
        } else if (state.contains("2")) {
            environment = state + "/services/oauth2/token";
        } else {
            environment = "https://test.salesforce.com/services/oauth2/token";
        }

        System.out.println("environment -> " + environment);
        HttpClient httpClient = new HttpClient();

        PostMethod post = new PostMethod(environment);
        post.addParameter("code", code);
        post.addParameter("grant_type", "authorization_code");
        post.addParameter("redirect_uri", sfdcRedirectURI);
        post.addParameter("client_id", sfdcClientId);
        post.addParameter("client_secret", sfdcClientSecret);

        httpClient.executeMethod(post);
        String responseBody = post.getResponseBodyAsString();

        String accessToken = null;
        String issuedAt = null;
        String signature = null;
        String id_token = null;
        String instance_url = null;
        String useridURL = null;
        String username = null;
        JsonParser parser = new JsonParser();

        JsonObject jsonObject = parser.parse(responseBody).getAsJsonObject();

        try {

            accessToken = jsonObject.get("access_token").getAsString();
            instance_url = jsonObject.get("instance_url").getAsString();
            useridURL = jsonObject.get("id").getAsString();

            GetMethod getMethod = new GetMethod(useridURL);
            getMethod.addRequestHeader("Authorization", "Bearer " + accessToken);
            httpClient.executeMethod(getMethod);
            String responseUserName = getMethod.getResponseBodyAsString();

            try {
                jsonObject = parser.parse(responseUserName).getAsJsonObject();
                username = jsonObject.get("username").getAsString();
            } catch (Exception ignored) {

            } finally {
                getMethod.releaseConnection();
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            post.releaseConnection();
        }
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        if(StringUtils.hasText(accessToken)){
            Cookie accessTokenCookie = new Cookie("SFDC_ACCESS_TOKEN", accessToken);
            Cookie userNameCookie = new Cookie("SFDC_USER_NAME", username);
            Cookie instanceURLCookie = new Cookie("SFDC_INSTANCE_URL", username);
            httpResponse.addCookie(accessTokenCookie);
            httpResponse.addCookie(userNameCookie);
            httpResponse.addCookie(instanceURLCookie);
            accessTokenCookie.setMaxAge(-1); //cookie not persistent, destroyed on browser exit
            userNameCookie.setMaxAge(-1); //cookie not persistent, destroyed on browser exit
            instanceURLCookie.setMaxAge(-1); //cookie not persistent, destroyed on browser exit
            httpResponse.sendRedirect("/html/success.html");
        } else {
            httpResponse.sendRedirect("/html/error.html");
        }
    }

    @RequestMapping(value = "/hooks/github", method = RequestMethod.POST)
    public String webhooks(@RequestHeader("X-Hub-Signature") String signature, @RequestHeader("X-GitHub-Event") String githubEvent,
                           @RequestBody String payload, HttpServletResponse response, HttpServletRequest request) {
        Gson gson = new Gson();
        // if signature is empty return 401
        if (!StringUtils.hasText(signature)) {
            return gson.toJson(HttpStatus.FORBIDDEN);
        }

        JsonObject jsonObject = gson.fromJson(payload, JsonElement.class).getAsJsonObject();
        String access_token = fetchCookies(request);
        System.out.println("githubEvent -> "+githubEvent);
        switch (githubEvent){
            case "pull_request" :
                if(!StringUtils.hasText(access_token)){
                    String user = jsonObject.get("pull_request").getAsJsonObject().get("user").getAsJsonObject().get("login").getAsString();
                    access_token = userWrapperMongoRepository.findByOwnerId(user).getAccess_token();
                }
                if ("opened".equalsIgnoreCase(jsonObject.get("action").getAsString()) &&
                        !jsonObject.get("pull_request").getAsJsonObject().get("merged").getAsBoolean()) {
                    System.out.println("A pull request was created! A validation should start now...");
                    start_deployment(jsonObject.get("pull_request").getAsJsonObject(), access_token);
                }
                break;
            case "push":
                System.out.println(jsonObject);
                break;
            case "deployment":
                if(!StringUtils.hasText(access_token)){
                    String user = jsonObject.get("repository").getAsJsonObject().get("owner").getAsJsonObject().get("login").getAsString();
                    access_token = userWrapperMongoRepository.findByOwnerId(user).getAccess_token();
                }
                process_deployment(jsonObject, access_token);
                break;
        }
        System.out.println("access_token -> "+access_token);

        return gson.toJson("");
    }

    /*@RequestMapping(value = "/event_handler", method = RequestMethod.GET)
    public void webhookReceiver(@RequestParam String code, @RequestParam String state, ServletResponse response, ServletRequest
            request, HttpServletRequest servletRequest) throws Exception {

        Request req = (Request) request;
        String payload = req.body();
        String x_github_event = req.headers("X-GITHUB-EVENT");

        String access_token = fetchCookies(servletRequest);

        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(payload, JsonElement.class).getAsJsonObject();

        switch (x_github_event) {
            case "pull_request":
                if ("opened".equalsIgnoreCase(jsonObject.get("action").getAsString()) &&
                        jsonObject.get("pull_request").getAsJsonObject().get("merged").getAsBoolean()) {

                    System.out.println("A pull request was merged! A deployment should start now...");

                    start_deployment(jsonObject.get("pull_request").getAsJsonObject(), access_token);
                }
                break;
            case "deployment":
                process_deployment(jsonObject, access_token);
                break;
            case "deployment_status":
                update_deployment_status(jsonObject);
                break;
        }

    }*/

    @RequestMapping(value = "/fetchRepositoryInDB", method = RequestMethod.GET)
    public String getRepositoryList(@RequestParam String gitHubUser, HttpServletResponse response, HttpServletRequest request) throws IOException, JSONException {
        Gson gson = new Gson();
        String reposOnDB = "";
        List<RepositoryWrapper> lstRepositoryWrapper = repositoryWrapperMongoRepository.findByOwnerId(gitHubUser);
        List<RepositoryWrapper> newLstRepositoryWrapper = new ArrayList<>();
        if(lstRepositoryWrapper != null && !lstRepositoryWrapper.isEmpty()){
            for (RepositoryWrapper repositoryWrapper : lstRepositoryWrapper){
                List<SFDCConnectionDetails> byGitRepoId = sfdcConnectionDetailsMongoRepository.findByGitRepoId(repositoryWrapper.getRepository().getRepositoryId());
                repositoryWrapper.getRepository().setSfdcConnectionDetails(byGitRepoId);
                newLstRepositoryWrapper.add(repositoryWrapper);
            }
            reposOnDB = gson.toJson(newLstRepositoryWrapper);
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
                    GitRepositoryUser gitRepositoryUser = gson.fromJson(IOUtils.toString(getUserMethod.getResponseBodyAsStream(), StandardCharsets.UTF_8), GitRepositoryUser.class);
                    UserWrapper userWrapper = userWrapperMongoRepository.findByOwnerId(gitRepositoryUser.getLogin());
                    if(userWrapper != null){
                        userWrapper.setAccess_token(accessToken);
                    } else {
                        userWrapper = new UserWrapper();
                        userWrapper.setAccess_token(accessToken);
                        userWrapper.setOwnerId(gitRepositoryUser.getLogin());
                    }
                    userWrapperMongoRepository.save(userWrapper);
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
                    GitRepositoryFromQuery gitRepositoryFromQuery = gson.fromJson(IOUtils.toString(getRepoByName.getResponseBodyAsStream(), StandardCharsets.UTF_8), GitRepositoryFromQuery.class);
                    lstRepo = gson.toJson(gitRepositoryFromQuery);
                }
            }
        }
        return lstRepo;
    }

    @RequestMapping(value = "/deleteWebHook", method = RequestMethod.DELETE)
    public String deleteWebHook(@RequestParam String repositoryName, @RequestParam String repositoryId, @RequestParam String repositoryOwner,
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
                RepositoryWrapper byRepositoryRepositoryName = repositoryWrapperMongoRepository.findByOwnerIdAndRepositoryRepositoryId(repositoryOwner, repositoryId);
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
                WebHook webHookResponse = gson.fromJson(IOUtils.toString(createWebHook.getResponseBodyAsStream(), StandardCharsets.UTF_8), WebHook.class);
                GetMethod fetchBranches = new GetMethod(GITHUB_API + "/repos/" + repository.getOwner() + "/" + repository.getRepositoryName() + "/branches");
                fetchBranches.setRequestHeader("Authorization", "token " + accessToken);
                fetchBranches.setRequestHeader("Content-Type", MediaType.APPLICATION_JSON);
                httpClient = new HttpClient();
                status = httpClient.executeMethod(fetchBranches);
                if(LIST_VALID_RESPONSE_CODES.contains(status)) {
                    Type listBranches = new TypeToken<ArrayList<GitBranches>>(){}.getType();
                    List<GitBranches> branchesListFromApi = new Gson().fromJson(IOUtils.toString(fetchBranches.getResponseBodyAsStream(), StandardCharsets.UTF_8), listBranches);
                    Map<String, GitBranches> stringGitBranchesMap = new HashMap<>();
                    for (GitBranches eachBranch : branchesListFromApi) {
                        stringGitBranchesMap.put(eachBranch.getName(), eachBranch);
                    }
                    repository.setMapBranches(stringGitBranchesMap);
                }

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


    @RequestMapping(value = "/saveSfdcConnectionDetails", method = RequestMethod.POST)
    public String saveSfdcConnectionDetails(@RequestBody SFDCConnectionDetails sfdcConnectionDetails, HttpServletResponse response, HttpServletRequest
            request) throws Exception {

        Gson gson = new Gson();
        String returnResponse = null;
        SFDCConnectionDetails byUserName = sfdcConnectionDetailsMongoRepository.findByUserName(sfdcConnectionDetails.getUserName());
        if(byUserName == null){
            sfdcConnectionDetails.setOauthSaved("true");
            SFDCConnectionDetails connectionSaved = sfdcConnectionDetailsMongoRepository.save(sfdcConnectionDetails);
            returnResponse = gson.toJson(connectionSaved);
        } else {
            throw new Exception("User already connected to ForceCI");
        }

        return returnResponse;
    }

    @RequestMapping(value = "/showSfdcConnectionDetails", method = RequestMethod.GET)
    public String showSfdcConnectionDetails(@RequestParam String gitRepoId, HttpServletResponse response, HttpServletRequest
            request) throws IOException {

        Gson gson = new Gson();
        String returnResponse = null;
        List<SFDCConnectionDetails> gitReposById = sfdcConnectionDetailsMongoRepository.findByGitRepoId(gitRepoId);
        if(gitReposById != null && !gitReposById.isEmpty()){
            returnResponse = gson.toJson(gitReposById);
        }
        return returnResponse;
    }

    private static void start_deployment(JsonObject jsonObject, String access_token) {
        String user = jsonObject.get("user").getAsJsonObject().get("login").getAsString();
        Map<String, String> map = new HashMap<>();
        map.put("environment", "QA");
        map.put("deploy_user", user);
        Gson gson = new Gson();
        String payload = gson.toJson(map);

        try {
            GitHub gitHub = GitHubBuilder.fromEnvironment().withOAuthToken(access_token).build();
            System.out.println("gitHub -> "+gitHub);
            GHRepository repository = gitHub.getRepository(jsonObject.get("head").getAsJsonObject().get("repo").getAsJsonObject().get("full_name").getAsString());
            GHDeploymentStatus deploymentStatus = new GHDeploymentStatusBuilder(repository,
                    jsonObject.get("deployment").getAsJsonObject().get("id").getAsInt(), PENDING).create();
            GHDeployment deployment = new GHDeploymentBuilder(repository,jsonObject.get("head").getAsJsonObject().get("sha").getAsString()).description("Auto Deploy after merge").payload(payload).autoMerge(false).create();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void process_deployment(JsonObject jsonObject, String access_token) {
        String payload_str = jsonObject.get("deployment").getAsJsonObject().get("payload").getAsString();
        Map payload = new Gson().fromJson(payload_str, Map.class);

        System.out.println("Processing " + jsonObject.get("deployment").getAsJsonObject().get("description").getAsString() +
                " for " + payload.<String>get("deploy_user") + " to " + payload.<String>get("environment"));

        try {
            Thread.sleep(2000L);
            GitHub gitHub = GitHubBuilder.fromEnvironment().withOAuthToken(access_token).build();
            GHRepository repository = gitHub.getRepository(
                    jsonObject.get("repository").getAsJsonObject()
                            .get("full_name").getAsString());
            GHDeploymentStatus deploymentStatus = new GHDeploymentStatusBuilder(repository,
                    jsonObject.get("deployment").getAsJsonObject().get("id").getAsInt(), PENDING).create();
            Thread.sleep(20000L);

            // This will happen only after validation is success

           /* GHDeploymentStatus deploymentStatus2 = new GHDeploymentStatusBuilder(repository,
                    jsonObject.get("deployment").getAsJsonObject().get("id").getAsInt(), SUCCESS).create();*/
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

    }


    private static void update_deployment_status(JsonObject jsonObject) {
        System.out.println("Deployment status for " + jsonObject.get("deployment").getAsJsonObject().get("id").getAsString() +
                " is " + jsonObject.get("deployment_status").getAsJsonObject().get("state").getAsString());
    }

    private static String fetchCookies(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        String accessToken = null;
        if( cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("ACCESS_TOKEN")) {
                    accessToken = cookie.getValue();
                    break;
                }
            }
        }
        return accessToken;
    }
}
