package com.controller;

import com.dao.DeploymentJobMongoRepository;
import com.dao.RepositoryWrapperMongoRepository;
import com.dao.SFDCConnectionDetailsMongoRepository;
import com.dao.UserWrapperMongoRepository;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.model.*;
import com.rabbitMQ.ConsumerHandler;
import com.rabbitMQ.DeploymentJob;
import com.rabbitMQ.RabbitMqConsumer;
import com.rabbitMQ.RabbitMqSenderConfig;
import com.utils.ApiSecurity;
import com.utils.ValidationStatus;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.io.IOUtils;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.json.JSONException;
import org.kohsuke.github.*;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.kohsuke.github.GHDeploymentState.PENDING;

@RestController
public class ForceCIController {

    public static final int HTTP_STATUS_OK = 200;
    public static final int HTTP_STATUS_FAILED = 401;
    public static final List<SseEmitter> emitters = Collections.synchronizedList(new ArrayList<>());
    private final static List<Integer> LIST_VALID_RESPONSE_CODES = Arrays.asList(200, 201, 204, 207);
    private static final String GITHUB_API = "https://api.github.com";
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
    private Map<String, Map<String, RabbitMqConsumer>> consumerMap = new ConcurrentHashMap<>();
    @Autowired
    private RepositoryWrapperMongoRepository repositoryWrapperMongoRepository;
    @Autowired
    private UserWrapperMongoRepository userWrapperMongoRepository;
    @Autowired
    private SFDCConnectionDetailsMongoRepository sfdcConnectionDetailsMongoRepository;
    @Autowired
    private RabbitMqSenderConfig rabbitMqSenderConfig;
    @Autowired
    private AmqpTemplate rabbitTemplateCustomAdmin;
    @Autowired
    private DeploymentJobMongoRepository deploymentJobMongoRepository;

    //@RequestMapping(value = "/**/{[path:[^\\.]*}")
    /*public String redirect(ServletResponse response, ServletRequest
            request) throws IOException {
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        // Forward to home page so that route is preserved.
        httpResponse.sendRedirect("/");
        return null;
    }*/

    private static void update_deployment_status(JsonObject jsonObject) {
        System.out.println("Deployment status for " + jsonObject.get("deployment").getAsJsonObject().get("id").getAsString() +
                " is " + jsonObject.get("deployment_status").getAsJsonObject().get("state").getAsString());
    }

    private static String fetchCookies(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        String accessToken = null;
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("ACCESS_TOKEN")) {
                    accessToken = cookie.getValue();
                    break;
                }
            }
        }
        return accessToken;
    }

    @GetMapping("/asyncDeployments")
    public SseEmitter fetchData2(@RequestParam String userName, @RequestParam String repoId, @RequestParam String branchName) {
        final SseEmitter emitter = new SseEmitter();
        ExecutorService executor = Executors.newSingleThreadExecutor();

        executor.execute(() -> {
            try {
                if (StringUtils.hasText(userName) && StringUtils.hasText(repoId) && StringUtils.hasText(branchName)) {
                    List<DeploymentJob> byTargetBranch = deploymentJobMongoRepository.findByRepoId(repoId);
                    List<DeploymentJobWrapper> jobWrapperList = new ArrayList<>();
                    if(byTargetBranch != null && !byTargetBranch.isEmpty()){
                        for (DeploymentJob targetBranch : byTargetBranch) {
                            DeploymentJobWrapper deploymentJobWrapper = new DeploymentJobWrapper();
                            deploymentJobWrapper.setId(targetBranch.getId());
                            deploymentJobWrapper.setJobNo(targetBranch.getJobId());
                            deploymentJobWrapper.setPrNumber(targetBranch.getPullRequestNumber());
                            deploymentJobWrapper.setPrHtml(targetBranch.getPullRequestHtmlUrl());

                            if(targetBranch.isBoolSfdcDeploymentNotStarted()) {
                                deploymentJobWrapper.setBoolSFDCDeploymentNotStarted(true);
                                deploymentJobWrapper.setSfdcDeploymentNotStarted(ValidationStatus.VALIDATION_NOTSTARTED.getText());
                            }
                            if(targetBranch.isBoolSfdcDeploymentRunning()) {
                                deploymentJobWrapper.setBoolSFDCDeploymentRunning(true);
                                deploymentJobWrapper.setSfdcDeploymentRunning(ValidationStatus.VALIDATION_RUNNING.getText());
                            }

                            if(targetBranch.isBoolSfdcDeploymentFail()) {
                                deploymentJobWrapper.setBoolSFDCDeploymentFail(true);
                                deploymentJobWrapper.setSfdcDeploymentFail(ValidationStatus.VALIDATION_FAIL.getText());
                            }

                            if(targetBranch.isBoolSfdcDeploymentPass()) {
                                deploymentJobWrapper.setBoolSFDCDeploymentPass(true);
                                deploymentJobWrapper.setSfdcDeploymentPass(ValidationStatus.VALIDATION_PASS.getText());
                            }

                            if(targetBranch.isBoolCodeReviewNotStarted()){
                                deploymentJobWrapper.setBoolCodeReviewNotStarted(true);
                                deploymentJobWrapper.setCodeReviewValidationNotStarted(ValidationStatus.VALIDATION_NOTSTARTED.getText());
                            }
                            if(targetBranch.isBoolCodeReviewPass()){
                                deploymentJobWrapper.setBoolCodeReviewValidationPass(true);
                                deploymentJobWrapper.setCodeReviewValidationPass(ValidationStatus.VALIDATION_PASS.getText());
                            }
                            if(targetBranch.isBoolCodeReviewRunning()){
                                deploymentJobWrapper.setBoolCodeReviewValidationRunning(true);
                                deploymentJobWrapper.setCodeReviewValidationRunning(ValidationStatus.VALIDATION_RUNNING.getText());
                            }
                            if(targetBranch.isBoolCodeReviewFail()){
                                deploymentJobWrapper.setBoolCodeReviewValidationFail(true);
                                deploymentJobWrapper.setCodeReviewValidationFail(ValidationStatus.VALIDATION_FAIL.getText());
                            }
                            if(targetBranch.isBoolSfdcRunning()){
                                deploymentJobWrapper.setBoolSfdcValidationRunning(true);
                                deploymentJobWrapper.setSfdcValidationRunning(ValidationStatus.VALIDATION_RUNNING.getText());
                            }
                            if(targetBranch.isBoolSfdcPass()) {
                                deploymentJobWrapper.setBoolSfdcValidationPass(true);
                                deploymentJobWrapper.setSfdcValidationPass(ValidationStatus.VALIDATION_PASS.getText());
                            }
                            if(targetBranch.isBoolSfdcFail()) {
                                deploymentJobWrapper.setBoolSfdcValidationFail(true);
                                deploymentJobWrapper.setSfdcValidationFail(ValidationStatus.VALIDATION_FAIL.getText());
                            }
                            jobWrapperList.add(deploymentJobWrapper);
                        }
                    }
                    Collections.sort(jobWrapperList);
                    emitter.send(jobWrapperList);
                }
                emitter.complete();

            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        });
        executor.shutdown();

        return emitter;
    }

    @RequestMapping(value = "/gitAuth", method = RequestMethod.GET, params = {"code", "state"})
    public void gitAuth(@RequestParam String code, @RequestParam String state, ServletResponse response, ServletRequest
            request) throws Exception {

        Gson gson = new Gson();
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
        String responseBody = IOUtils.toString(post.getResponseBodyAsStream(), StandardCharsets.UTF_8);

        String accessToken = null;
        String token_type = null;
        JsonParser parser = new JsonParser();

        JsonObject jsonObject = parser.parse(responseBody).getAsJsonObject();
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        try {

            accessToken = jsonObject.get("access_token").getAsString();
            token_type = jsonObject.get("token_type").getAsString();

            Cookie session1 = new Cookie("ACCESS_TOKEN", accessToken);
            Cookie session2 = new Cookie("TOKEN_TYPE", token_type);
            session1.setMaxAge(-1); //cookie not persistent, destroyed on browser exit
            session2.setMaxAge(-1); //cookie not persistent, destroyed on browser exit
            httpResponse.addCookie(session1);
            httpResponse.addCookie(session2);
        } catch (Exception e) {
            e.printStackTrace();
        }
        httpResponse.sendRedirect("/index.html");
    }

    @RequestMapping(value = "/sfdcAuth", method = RequestMethod.GET, params = {"code", "state"})
    public void sfdcAuth(@RequestParam String code, @RequestParam String state, ServletResponse response, ServletRequest request) throws Exception {

        String environment = null;
        if (state.equals("0")) {
            environment = "https://login.salesforce.com/services/oauth2/token";
        } else if (state.contains("2")) {
            environment = state + "/services/oauth2/token";
        } else {
            environment = "https://test.salesforce.com/services/oauth2/token";
        }

        HttpClient httpClient = new HttpClient();

        PostMethod post = new PostMethod(environment);
        post.addParameter("code", code);
        post.addParameter("grant_type", "authorization_code");
        post.addParameter("redirect_uri", sfdcRedirectURI);
        post.addParameter("client_id", sfdcClientId);
        post.addParameter("client_secret", sfdcClientSecret);

        httpClient.executeMethod(post);
        String responseBody = IOUtils.toString(post.getResponseBodyAsStream(), StandardCharsets.UTF_8);
        String accessToken = null;
        String refresh_token = null;
        String instance_url = null;
        String useridURL = null;
        String username = null;
        JsonParser parser = new JsonParser();

        JsonObject jsonObject = parser.parse(responseBody).getAsJsonObject();

        try {

            accessToken = jsonObject.get("access_token").getAsString();
            instance_url = jsonObject.get("instance_url").getAsString();
            useridURL = jsonObject.get("id").getAsString();
            refresh_token = jsonObject.get("refresh_token").getAsString();

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

        if (StringUtils.hasText(accessToken)) {
            Cookie accessTokenCookie = new Cookie("SFDC_ACCESS_TOKEN", accessToken);
            Cookie userNameCookie = new Cookie("SFDC_USER_NAME", username);
            Cookie instanceURLCookie = new Cookie("SFDC_INSTANCE_URL", instance_url);
            Cookie refreshTokenCookie = new Cookie("SFDC_REFRESH_TOKEN", refresh_token);
            httpResponse.addCookie(accessTokenCookie);
            httpResponse.addCookie(userNameCookie);
            httpResponse.addCookie(instanceURLCookie);
            httpResponse.addCookie(refreshTokenCookie);
            accessTokenCookie.setMaxAge(-1); //cookie not persistent, destroyed on browser exit
            userNameCookie.setMaxAge(-1); //cookie not persistent, destroyed on browser exit
            instanceURLCookie.setMaxAge(-1); //cookie not persistent, destroyed on browser exit
            refreshTokenCookie.setMaxAge(-1); //cookie not persistent, destroyed on browser exit
            httpResponse.sendRedirect("/html/success.html");
        } else {
            httpResponse.sendRedirect("/html/error.html");
        }
    }

    @RequestMapping(value = "/getAllBranches", method = RequestMethod.GET)
    public String getAllBranches(@RequestParam String strRepoId, HttpServletResponse response, HttpServletRequest request) throws IOException, GitAPIException {
        String access_token = fetchCookies(request);
        GitHub gitHub = GitHubBuilder.fromEnvironment().withOAuthToken(access_token).build();
        GHRepository repository = gitHub.getRepositoryById(strRepoId);
        Map<String, GHBranch> branches = repository.getBranches();
        List<String> lstBranchesToBeReturned = new ArrayList<>();
        for (Map.Entry<String, GHBranch> stringGHBranchEntry : branches.entrySet()) {
            lstBranchesToBeReturned.add(stringGHBranchEntry.getValue().getName());
        }

        Gson gson = new Gson();
        return gson.toJson(lstBranchesToBeReturned);
    }

    @RequestMapping(value = "/hooks/github", method = RequestMethod.POST)
    public String webhooks(@RequestHeader("X-Hub-Signature") String signature, @RequestHeader("X-GitHub-Event") String githubEvent,
                           @RequestBody String payload, HttpServletResponse response, HttpServletRequest request) throws Exception {
        Gson gson = new Gson();
        // if signature is empty return 401
        if (!StringUtils.hasText(signature)) {
            return gson.toJson(HttpStatus.FORBIDDEN);
        }

        JsonObject jsonObject = gson.fromJson(payload, JsonElement.class).getAsJsonObject();
        String access_token = fetchCookies(request);
        String emailId = null;
        SFDCConnectionDetails sfdcConnectionDetails = null;
        switch (githubEvent) {
            case "pull_request":
                String user = jsonObject.get("pull_request").getAsJsonObject().get("user").getAsJsonObject().get("login").getAsString();
                UserWrapper byOwnerId = userWrapperMongoRepository.findByOwnerId(user);
                if (byOwnerId != null) {
                    access_token = byOwnerId.getAccess_token();
                    emailId = byOwnerId.getEmail_Id();
                }
                if (("opened".equalsIgnoreCase(jsonObject.get("action").getAsString()) || "synchronize".equalsIgnoreCase(jsonObject.get("action").getAsString())) &&
                        !jsonObject.get("pull_request").getAsJsonObject().get("merged").getAsBoolean()) {
                    System.out.println("A pull request was created! A validation should start now...");
                    start_deployment(jsonObject.get("pull_request").getAsJsonObject(), jsonObject.get("repository").getAsJsonObject(), access_token,
                            sfdcConnectionDetailsMongoRepository, sfdcConnectionDetails, emailId, rabbitMqSenderConfig, rabbitTemplateCustomAdmin, false);
                }
                break;
            case "push":
                String pushUser = jsonObject.get("repository").getAsJsonObject().get("owner").getAsJsonObject().get("login").getAsString();
                UserWrapper userWrapper = userWrapperMongoRepository.findByOwnerId(pushUser);
                if (userWrapper != null) {
                    access_token = userWrapper.getAccess_token();
                    emailId = userWrapper.getEmail_Id();
                }
                System.out.println("A pull request was merged! Deployment start now...");
                start_deployment(jsonObject, jsonObject.get("repository").getAsJsonObject(), access_token,
                        sfdcConnectionDetailsMongoRepository, sfdcConnectionDetails, emailId, rabbitMqSenderConfig, rabbitTemplateCustomAdmin, true);
                break;
            case "deployment":
                if (!StringUtils.hasText(access_token)) {
                    String userId = jsonObject.get("repository").getAsJsonObject().get("owner").getAsJsonObject().get("login").getAsString();
                    access_token = userWrapperMongoRepository.findByOwnerId(userId).getAccess_token();
                }
                process_deployment(jsonObject, access_token);
                break;
        }
        return gson.toJson("");
    }

    @RequestMapping(value = "/fetchRepositoryInDB", method = RequestMethod.GET)
    public String getRepositoryList(@RequestParam String gitHubUser, HttpServletResponse response, HttpServletRequest request) throws Exception {
        Gson gson = new Gson();
        String reposOnDB = "";
        List<RepositoryWrapper> lstRepositoryWrapper = repositoryWrapperMongoRepository.findByOwnerId(gitHubUser);
        List<RepositoryWrapper> newLstRepositoryWrapper = new ArrayList<>();
        if (lstRepositoryWrapper != null && !lstRepositoryWrapper.isEmpty()) {
            for (RepositoryWrapper repositoryWrapper : lstRepositoryWrapper) {
                List<SFDCConnectionDetails> byGitRepoId = sfdcConnectionDetailsMongoRepository.findByGitRepoId(repositoryWrapper.getRepository().getRepositoryId());
                repositoryWrapper.getRepository().setSfdcConnectionDetails(byGitRepoId);
                newLstRepositoryWrapper.add(repositoryWrapper);
                if (consumerMap != null && !consumerMap.containsKey(repositoryWrapper.getRepository().getRepositoryId())) {
                    Map<String, RabbitMqConsumer> rabbitMqConsumerMap = new ConcurrentHashMap<>();
                    for (SFDCConnectionDetails sfdcConnectionDetails : byGitRepoId) {
                        RabbitMqConsumer container = new RabbitMqConsumer();
                        container.setConnectionFactory(rabbitMqSenderConfig.connectionFactory());
                        container.setQueueNames(sfdcConnectionDetails.getBranchConnectedTo());
                        container.setConcurrentConsumers(1);
                        container.setMessageListener(new MessageListenerAdapter(new ConsumerHandler(deploymentJobMongoRepository, sfdcConnectionDetailsMongoRepository), new Jackson2JsonMessageConverter()));
                        container.startConsumers();
                        rabbitMqConsumerMap.put(sfdcConnectionDetails.getBranchConnectedTo(), container);
                    }
                    consumerMap.put(repositoryWrapper.getRepository().getRepositoryId(), rabbitMqConsumerMap);
                }
            }
            reposOnDB = gson.toJson(newLstRepositoryWrapper);
        }
        return reposOnDB;
    }

    @RequestMapping(value = "/fetchUserName", method = RequestMethod.GET)
    public String getUserName(HttpServletResponse response, HttpServletRequest request) throws IOException, JSONException {
        Gson gson = new Gson();
        String loginNameAndAvatar = "";
        String accessToken = fetchCookies(request);
        if (StringUtils.hasText(accessToken)) {
            GetMethod getUserMethod = new GetMethod(GITHUB_API + "/user");
            getUserMethod.setRequestHeader("Authorization", "token " + accessToken);
            HttpClient httpClient = new HttpClient();
            int intStatusOk = httpClient.executeMethod(getUserMethod);
            if (intStatusOk == HTTP_STATUS_OK) {
                GitRepositoryUser gitRepositoryUser = gson.fromJson(IOUtils.toString(getUserMethod.getResponseBodyAsStream(), StandardCharsets.UTF_8), GitRepositoryUser.class);
                UserWrapper userWrapper = userWrapperMongoRepository.findByOwnerId(gitRepositoryUser.getLogin());
                if (userWrapper != null) {
                    userWrapper.setAccess_token(accessToken);
                } else {
                    userWrapper = new UserWrapper();
                    userWrapper.setAccess_token(accessToken);
                    userWrapper.setOwnerId(gitRepositoryUser.getLogin());
                    userWrapper.setEmail_Id(gitRepositoryUser.getEmail());
                }
                userWrapperMongoRepository.save(userWrapper);
                loginNameAndAvatar = gson.toJson(gitRepositoryUser);
            } else {
                throw new JSONException("Bad Credentials");
            }
        }
        return loginNameAndAvatar;
    }

    @RequestMapping(value = "/fetchRepository", method = RequestMethod.GET)
    public String getRepositoryByName(@RequestParam String repoName, @RequestParam String repoUser, HttpServletResponse response, HttpServletRequest request) throws IOException, JSONException {
        Cookie[] cookies = request.getCookies();
        Gson gson = new Gson();
        String lstRepo = "";

        FinalResult finalResult = new FinalResult();
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals("ACCESS_TOKEN")) {
                String accessToken = cookie.getValue();
                String queryParam = repoName + "%20in:name+user:" + repoUser + "+fork:true";
                GetMethod getRepoByName = new GetMethod(GITHUB_API + "/search/repositories?q=" + queryParam);
                getRepoByName.setRequestHeader("Authorization", "token " + accessToken);
                HttpClient httpClient = new HttpClient();
                int intStatusOk = httpClient.executeMethod(getRepoByName);
                if (intStatusOk == HTTP_STATUS_OK) {
                    GitRepositoryFromQuery gitRepositoryFromQuery = gson.fromJson(IOUtils.toString(getRepoByName.getResponseBodyAsStream(), StandardCharsets.UTF_8), GitRepositoryFromQuery.class);
                    lstRepo = gson.toJson(gitRepositoryFromQuery);
                }
            }
        }

        List<RepositoryWrapper> lstRepositoryWrapper = repositoryWrapperMongoRepository.findByOwnerId(repoUser);
        finalResult.setGitRepositoryFromQuery(lstRepo);
        finalResult.setRepositoryWrappers(lstRepositoryWrapper);

        return gson.toJson(finalResult);
    }

    @RequestMapping(value = "/deleteWebHook", method = RequestMethod.DELETE)
    public String deleteWebHook(@RequestParam String repositoryName, @RequestParam String repositoryId, @RequestParam String repositoryOwner,
                                @RequestParam String webHookId, HttpServletResponse response, HttpServletRequest
                                        request) throws IOException, URISyntaxException {

        Gson gson = new Gson();
        int status = 0;

        String accessToken = fetchCookies(request);
        if (accessToken != null) {
            DeleteMethod deleteWebHook = new DeleteMethod(GITHUB_API + "/repos/" + repositoryOwner + "/" + repositoryName + "/hooks/" + webHookId);
            deleteWebHook.setRequestHeader("Authorization", "token " + accessToken);
            deleteWebHook.setRequestHeader("Content-Type", MediaType.APPLICATION_JSON);
            HttpClient httpClient = new HttpClient();
            status = httpClient.executeMethod(deleteWebHook);
            if (status == 204) {
                try {
                    consumerMap.remove(repositoryId);
                    rabbitMqSenderConfig.amqpAdmin().deleteExchange(repositoryName);
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
                RepositoryWrapper byRepositoryRepositoryName = repositoryWrapperMongoRepository.findByOwnerIdAndRepositoryRepositoryId(repositoryOwner, repositoryId);
                repositoryWrapperMongoRepository.delete(byRepositoryRepositoryName);
                List<SFDCConnectionDetails> byGitRepoId = sfdcConnectionDetailsMongoRepository.findByGitRepoId(repositoryId);
                sfdcConnectionDetailsMongoRepository.deleteAll(byGitRepoId);
                for (SFDCConnectionDetails sfdcConnectionDetails : byGitRepoId) {
                    rabbitMqSenderConfig.amqpAdmin().deleteQueue(sfdcConnectionDetails.getBranchConnectedTo());
                }

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
            if (LIST_VALID_RESPONSE_CODES.contains(status)) {
                WebHook webHookResponse = gson.fromJson(IOUtils.toString(createWebHook.getResponseBodyAsStream(), StandardCharsets.UTF_8), WebHook.class);
                GetMethod fetchBranches = new GetMethod(GITHUB_API + "/repos/" + repository.getOwner() + "/" + repository.getRepositoryName() + "/branches");
                GitHub gitHub = GitHubBuilder.fromEnvironment().withOAuthToken(accessToken).build();
                GHRepository repositoryFromLib = gitHub.getRepositoryById(repository.getRepositoryId());
                Map<String, GHBranch> branches = repositoryFromLib.getBranches();
                List<String> lstBranchesInRepo = new ArrayList<>();
                if (!branches.isEmpty()) {
                    for (String eachKey : branches.keySet()) {
                        lstBranchesInRepo.add(branches.get(eachKey).getName());
                    }
                }
                repository.setLstBranches(lstBranchesInRepo);
                repository.setWebHook(webHookResponse);
                repository.setHmacSecret(randomString);
                RepositoryWrapper repositoryWrapper = new RepositoryWrapper();
                repositoryWrapper.setOwnerId(repository.getOwner());
                repositoryWrapper.setRepository(repository);
                repositoryWrapperMongoRepository.save(repositoryWrapper);
                try {
                    rabbitMqSenderConfig.amqpAdmin().declareExchange(new DirectExchange(repository.getRepositoryName()));
                    Map<String, RabbitMqConsumer> rabbitMqConsumerMap = new ConcurrentHashMap<>();
                    consumerMap.put(repository.getRepositoryId(), rabbitMqConsumerMap);
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
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
        if (sfdcConnectionDetails.getId() == null) {
            SFDCConnectionDetails byBranchConnectedToAndBoolActive = sfdcConnectionDetailsMongoRepository.findByBranchConnectedToAndBoolActive(sfdcConnectionDetails.getBranchConnectedTo(), true);
            if (byBranchConnectedToAndBoolActive != null) {
                throw new Exception("User already connected to ForceCI");
            }
        }
        Properties develop = rabbitMqSenderConfig.amqpAdmin().getQueueProperties(sfdcConnectionDetails.getBranchConnectedTo());

        if (develop != null && develop.stringPropertyNames() != null && !develop.stringPropertyNames().isEmpty()) {
            // Do nothing
        } else {
            RepositoryWrapper byRepositoryRepositoryId = repositoryWrapperMongoRepository.findByRepositoryRepositoryId(sfdcConnectionDetails.getGitRepoId());
            Queue queue = new Queue(sfdcConnectionDetails.getBranchConnectedTo(), true);
            rabbitMqSenderConfig.amqpAdmin().declareQueue(queue);
            rabbitMqSenderConfig.amqpAdmin().declareBinding(BindingBuilder.bind(queue).to(new DirectExchange(byRepositoryRepositoryId.getRepository().getRepositoryName())).withQueueName());
            RabbitMqConsumer container = new RabbitMqConsumer();
            container.setConnectionFactory(rabbitMqSenderConfig.connectionFactory());
            container.setQueueNames(sfdcConnectionDetails.getBranchConnectedTo());
            container.setConcurrentConsumers(1);
            container.setMessageListener(new MessageListenerAdapter(new ConsumerHandler(deploymentJobMongoRepository, sfdcConnectionDetailsMongoRepository), new Jackson2JsonMessageConverter()));
            container.startConsumers();
            Map<String, RabbitMqConsumer> rabbitMqConsumerMap = consumerMap.get(byRepositoryRepositoryId.getRepository().getRepositoryId());
            if (rabbitMqConsumerMap != null && !rabbitMqConsumerMap.isEmpty()) {

            } else {
                rabbitMqConsumerMap = new ConcurrentHashMap<>();
                rabbitMqConsumerMap.put(sfdcConnectionDetails.getBranchConnectedTo(), container);
            }
        }
        sfdcConnectionDetails.setOauthSaved("true");
        SFDCConnectionDetails connectionSaved = sfdcConnectionDetailsMongoRepository.save(sfdcConnectionDetails);
        returnResponse = gson.toJson(connectionSaved);

        return returnResponse;
    }

    @RequestMapping(value = "/showSfdcConnectionDetails", method = RequestMethod.GET)
    public String showSfdcConnectionDetails(@RequestParam String gitRepoId, HttpServletResponse response, HttpServletRequest
            request) throws IOException {

        Gson gson = new Gson();
        String returnResponse = null;
        List<SFDCConnectionDetails> gitReposById = sfdcConnectionDetailsMongoRepository.findByGitRepoId(gitRepoId);
        if (gitReposById != null && !gitReposById.isEmpty()) {
            returnResponse = gson.toJson(gitReposById);
        }
        return returnResponse;
    }

    @RequestMapping(value = "/deleteSfdcConnectionDetails", method = RequestMethod.DELETE)
    public String deleteSfdcConnectionDetails(@RequestParam String sfdcDetailsId, HttpServletResponse response, HttpServletRequest
            request) throws IOException {
        Gson gson = new Gson();
        try {
            Optional<SFDCConnectionDetails> byId = sfdcConnectionDetailsMongoRepository.findById(sfdcDetailsId);
            rabbitMqSenderConfig.amqpAdmin().deleteQueue(byId.get().getBranchConnectedTo());
            Map<String, RabbitMqConsumer> rabbitMqConsumerMap = consumerMap.get(byId.get().getGitRepoId());
            if (rabbitMqConsumerMap != null && !rabbitMqConsumerMap.isEmpty() && rabbitMqConsumerMap.get(byId.get().getBranchConnectedTo()) != null) {
                rabbitMqConsumerMap.remove(byId.get().getBranchConnectedTo());
            }
            sfdcConnectionDetailsMongoRepository.deleteById(sfdcDetailsId);
            return gson.toJson("Success");
        } catch (Exception e) {
            return gson.toJson("Error");
        }

    }

    private void start_deployment(JsonObject pullRequestJsonObject, JsonObject repositoryJsonObject, String access_token,
                                  SFDCConnectionDetailsMongoRepository sfdcConnectionDetailsMongoRepository,
                                  SFDCConnectionDetails sfdcConnectionDetail, String emailId,
                                  RabbitMqSenderConfig rabbitMqSenderConfig, AmqpTemplate rabbitTemplate, boolean merge) throws Exception {

        String userName = pullRequestJsonObject.has("user") ? pullRequestJsonObject.get("user").getAsJsonObject().get("login").getAsString() : "";
        String gitCloneURL = repositoryJsonObject.has("clone_url") ? repositoryJsonObject.get("clone_url").getAsString() : "";
        String prHtmlURL = pullRequestJsonObject.has("html_url") ? pullRequestJsonObject.get("html_url").getAsString() : "";
        String prNumber = pullRequestJsonObject.has("number") ? pullRequestJsonObject.get("number").getAsString() : "";
        String prTitle = pullRequestJsonObject.has("title") ? pullRequestJsonObject.get("title").getAsString() : "";
        String gitRepoId = repositoryJsonObject.has("id") ? repositoryJsonObject.get("id").getAsString() : "";
        String sourceBranch = pullRequestJsonObject.has("head") ? pullRequestJsonObject.get("head").getAsJsonObject().get("ref").getAsString() : "";
        String repoName = pullRequestJsonObject.has("base") ? pullRequestJsonObject.get("base").getAsJsonObject().get("repo").getAsJsonObject().get("name").getAsString() : "";
        String targetBranch = pullRequestJsonObject.has("base") ? pullRequestJsonObject.get("base").getAsJsonObject().get("ref").getAsString() : "";
        String baseSHA = pullRequestJsonObject.has("base") ? pullRequestJsonObject.get("base").getAsJsonObject().get("sha").getAsString() : "";

        if(merge) {
            baseSHA = pullRequestJsonObject.has("before") ? pullRequestJsonObject.get("before").getAsString() : "";
            userName = repositoryJsonObject.has("owner") ? repositoryJsonObject.get("owner").getAsJsonObject().get("login").getAsString() : "";
            gitCloneURL = repositoryJsonObject.has("clone_url") ? repositoryJsonObject.get("clone_url").getAsString() : "";
            gitRepoId = repositoryJsonObject.has("id") ? repositoryJsonObject.get("id").getAsString() : "";
            repoName = repositoryJsonObject.has("name") ? repositoryJsonObject.get("name").getAsString() : "";
            targetBranch = pullRequestJsonObject.has("ref") ? pullRequestJsonObject.get("ref").getAsString().split("/")[2] : "";
        }
        List<SFDCConnectionDetails> byGitRepoId = sfdcConnectionDetailsMongoRepository.findByGitRepoId(gitRepoId);

        if (byGitRepoId != null && !byGitRepoId.isEmpty()) {
            for (SFDCConnectionDetails sfdcConnectionDetails : byGitRepoId) {
                if (sfdcConnectionDetails.getBranchConnectedTo() != null) {
                    if (sfdcConnectionDetails.getBranchConnectedTo().equals(targetBranch)) {
                        sfdcConnectionDetail = sfdcConnectionDetails;
                        break;
                    }
                }
            }
        }

        if (sfdcConnectionDetail == null) {
            return;
        }

        Properties develop = rabbitMqSenderConfig.amqpAdmin().getQueueProperties(targetBranch);
        String queue_name = develop.getProperty("QUEUE_NAME");

        Long aLong = deploymentJobMongoRepository.countByRepoId(gitRepoId);
        List<DeploymentJob> byRepoIdAndBaseSHA = deploymentJobMongoRepository.findByRepoIdAndBaseSHAOrderByPullRequestNumberDesc(gitRepoId, baseSHA);
        System.out.println("aLong -> "+aLong);
        // Create the object detail to be passed to RabbitMQ
        DeploymentJob deploymentJob = new DeploymentJob();
        if(byRepoIdAndBaseSHA != null && !byRepoIdAndBaseSHA.isEmpty()) {
            System.out.println("byRepoIdAndBaseSHA.get(0).getBaseSHA() -> "+byRepoIdAndBaseSHA.get(0).getBaseSHA());
            deploymentJob = byRepoIdAndBaseSHA.get(0);
        }
        if(aLong != null && !merge) {
            deploymentJob.setJobId(String.valueOf(aLong.intValue() + 1));
        }
        deploymentJob.setRepoId(gitRepoId);
        if(StringUtils.hasText(prNumber)) {
            deploymentJob.setPullRequestNumber(prNumber);
        }
        if(StringUtils.hasText(prHtmlURL)) {
            deploymentJob.setPullRequestHtmlUrl(prHtmlURL);
        }
        if(StringUtils.hasText(prTitle)) {
            deploymentJob.setPullRequestTitle(prTitle);
        }
        deploymentJob.setAccess_token(access_token);
        deploymentJob.setSfdcConnectionDetail(sfdcConnectionDetail);
        deploymentJob.setEmailId(emailId);
        deploymentJob.setUserName(userName);
        deploymentJob.setGitCloneURL(gitCloneURL);
        if(StringUtils.hasText(sourceBranch)) {
            deploymentJob.setSourceBranch(sourceBranch);
        }
        deploymentJob.setTargetBranch(targetBranch);
        deploymentJob.setQueueName(queue_name);
        deploymentJob.setBoolSfdcCompleted(false);
        deploymentJob.setBaseSHA(baseSHA);
        if(merge) {
            deploymentJob.setBoolMerge(true);
            deploymentJob.setBoolSfdcDeploymentNotStarted(false);
            deploymentJob.setBoolSfdcDeploymentRunning(true);
        } else {
            deploymentJob.setBoolSfdcRunning(true);
            deploymentJob.setBoolMerge(false);
            deploymentJob.setBoolSfdcDeploymentNotStarted(true);
            deploymentJob.setBoolCodeReviewNotStarted(true);

        }
        deploymentJob.setCreatedDate(new Date());
        DeploymentJob savedDeploymentJob = deploymentJobMongoRepository.save(deploymentJob);
        rabbitTemplate.convertAndSend(repoName, queue_name, savedDeploymentJob);
        if (consumerMap != null && !consumerMap.isEmpty() && !consumerMap.containsKey(sfdcConnectionDetail.getGitRepoId())) {
            Map<String, RabbitMqConsumer> rabbitMqConsumerMap = consumerMap.get(sfdcConnectionDetail.getGitRepoId());
            if ((rabbitMqConsumerMap != null && !rabbitMqConsumerMap.containsKey(queue_name))) {
                RabbitMqConsumer container = new RabbitMqConsumer();
                container.setConnectionFactory(rabbitMqSenderConfig.connectionFactory());
                container.setQueueNames(queue_name);
                container.setConcurrentConsumers(1);
                container.setMessageListener(new MessageListenerAdapter(new ConsumerHandler(deploymentJobMongoRepository, sfdcConnectionDetailsMongoRepository), new Jackson2JsonMessageConverter()));
                container.startConsumers();
                rabbitMqConsumerMap.put(queue_name, container);
                consumerMap.put(sfdcConnectionDetail.getGitRepoId(), rabbitMqConsumerMap);
            }
        }
    }

    private void process_deployment(JsonObject jsonObject, String access_token) {
        String payload_str = jsonObject.get("deployment").getAsJsonObject().get("payload").getAsString();
        Map payload = new Gson().fromJson(payload_str, Map.class);

        try {
            Thread.sleep(2000L);
            GitHub gitHub = GitHubBuilder.fromEnvironment().withOAuthToken(access_token).build();
            GHRepository repository = gitHub.getRepository(
                    jsonObject.get("repository").getAsJsonObject()
                            .get("full_name").getAsString());
            GHDeploymentStatus deploymentStatus = new GHDeploymentStatusBuilder(repository,
                    jsonObject.get("deployment").getAsJsonObject().get("id").getAsInt(), PENDING).create();

            List<SFDCConnectionDetails> byGitRepoId = sfdcConnectionDetailsMongoRepository.findByGitRepoId(String.valueOf(repository.getId()));

            Thread.sleep(20000L);

            // This will happen only after validation is success

           /* GHDeploymentStatus deploymentStatus2 = new GHDeploymentStatusBuilder(repository,
                    jsonObject.get("deployment").getAsJsonObject().get("id").getAsInt(), SUCCESS).create();*/
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

    }

    private class FinalResult {

        private String gitRepositoryFromQuery;
        private List<RepositoryWrapper> repositoryWrappers;

        public String getGitRepositoryFromQuery() {
            return gitRepositoryFromQuery;
        }

        public void setGitRepositoryFromQuery(String gitRepositoryFromQuery) {
            this.gitRepositoryFromQuery = gitRepositoryFromQuery;
        }

        public List<RepositoryWrapper> getRepositoryWrappers() {
            return repositoryWrappers;
        }

        public void setRepositoryWrappers(List<RepositoryWrapper> repositoryWrappers) {
            this.repositoryWrappers = repositoryWrappers;
        }
    }

}
