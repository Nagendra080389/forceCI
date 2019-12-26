package com.controller;

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
import com.utils.AntExecutor;
import com.utils.ApiSecurity;
import com.utils.BuildUtils;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.json.JSONException;
import org.kohsuke.github.*;
import org.springframework.amqp.core.*;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.kohsuke.github.GHDeploymentState.PENDING;

@RestController
public class ForceCIController {

    public static final int HTTP_STATUS_OK = 200;
    public static final int HTTP_STATUS_FAILED = 401;
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

    private final static List<Integer> LIST_VALID_RESPONSE_CODES = Arrays.asList(200, 201, 204, 207);

    @Autowired
    private RepositoryWrapperMongoRepository repositoryWrapperMongoRepository;

    @Autowired
    private UserWrapperMongoRepository userWrapperMongoRepository;

    @Autowired
    private SFDCConnectionDetailsMongoRepository sfdcConnectionDetailsMongoRepository;

    @Autowired
    private RabbitMqSenderConfig rabbitMqSenderConfig;

    @Autowired
    private AmqpTemplate rabbitTemplate;

    private static final String GITHUB_API = "https://api.github.com";

    @RequestMapping(value = "/**/{[path:[^\\.]*}")
    public String redirect(ServletResponse response, ServletRequest
            request) throws IOException {
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        // Forward to home page so that route is preserved.
        httpResponse.sendRedirect("/");
        return null;
    }

    @RequestMapping(value = "/sendMessageToQueuesDevelop", method = RequestMethod.GET)
    public void sendMessageToQueuesDevelop(ServletResponse response, ServletRequest
            request) throws Exception {




    }

    @RequestMapping(value = "/gitAuth", method = RequestMethod.GET, params = {"code", "state"})
    public void gitAuth(@RequestParam String code, @RequestParam String state, ServletResponse response, ServletRequest
            request) throws Exception {

        Gson gson = new Gson();
        String environment = "https://github.com/login/oauth/access_token";
        HttpClient httpClient = new HttpClient();

        System.out.println("code - > " + code);
        PostMethod post = new PostMethod(environment);
        post.setRequestHeader("Accept", MediaType.APPLICATION_JSON);
        post.addParameter("code", code);
        post.addParameter("redirect_uri", redirectURI);
        post.addParameter("client_id", githubClientId);
        post.addParameter("client_secret", githubClientSecret);
        post.addParameter("state", state);

        httpClient.executeMethod(post);
        String responseBody = IOUtils.toString(post.getResponseBodyAsStream(), StandardCharsets.UTF_8);

        System.out.println("responseBody - > " + responseBody);
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
        System.out.println(" state -> " + state);
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

        if (StringUtils.hasText(accessToken)) {
            Cookie accessTokenCookie = new Cookie("SFDC_ACCESS_TOKEN", accessToken);
            Cookie userNameCookie = new Cookie("SFDC_USER_NAME", username);
            Cookie instanceURLCookie = new Cookie("SFDC_INSTANCE_URL", instance_url);
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
        System.out.println("githubEvent -> " + githubEvent);
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
                            sfdcConnectionDetailsMongoRepository, sfdcConnectionDetails, emailId, rabbitMqSenderConfig, rabbitTemplate);
                }
                break;
            case "push":
                System.out.println(jsonObject);
                break;
            case "deployment":
                if (!StringUtils.hasText(access_token)) {
                    String userId = jsonObject.get("repository").getAsJsonObject().get("owner").getAsJsonObject().get("login").getAsString();
                    access_token = userWrapperMongoRepository.findByOwnerId(userId).getAccess_token();
                }
                process_deployment(jsonObject, access_token);
                break;
        }
        System.out.println("access_token -> " + access_token);

        return gson.toJson("");
    }


    @RequestMapping(value = "/fetchRepositoryInDB", method = RequestMethod.GET)
    public String getRepositoryList(@RequestParam String gitHubUser, HttpServletResponse response, HttpServletRequest request) throws IOException, JSONException {
        Gson gson = new Gson();
        String reposOnDB = "";
        List<RepositoryWrapper> lstRepositoryWrapper = repositoryWrapperMongoRepository.findByOwnerId(gitHubUser);
        List<RepositoryWrapper> newLstRepositoryWrapper = new ArrayList<>();
        if (lstRepositoryWrapper != null && !lstRepositoryWrapper.isEmpty()) {
            for (RepositoryWrapper repositoryWrapper : lstRepositoryWrapper) {
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
                if(!branches.isEmpty()) {
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

        if(develop != null && develop.stringPropertyNames() != null && !develop.stringPropertyNames().isEmpty()) {
            // Do nothing
        } else {
            RepositoryWrapper byRepositoryRepositoryId = repositoryWrapperMongoRepository.findByRepositoryRepositoryId(sfdcConnectionDetails.getGitRepoId());
            Queue queue = new Queue(sfdcConnectionDetails.getBranchConnectedTo(), true);
            rabbitMqSenderConfig.amqpAdmin().declareQueue(new Queue(sfdcConnectionDetails.getBranchConnectedTo(), true));
            System.out.println("byRepositoryRepositoryId.getRepository().getRepositoryName() -> "+byRepositoryRepositoryId.getRepository().getRepositoryName());
            rabbitMqSenderConfig.amqpAdmin().declareBinding(BindingBuilder.bind(queue).to(new DirectExchange(byRepositoryRepositoryId.getRepository().getRepositoryName())).withQueueName());
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
            sfdcConnectionDetailsMongoRepository.deleteById(sfdcDetailsId);
            return gson.toJson("Success");
        } catch (Exception e){
            return gson.toJson("Error");
        }

    }

    private static void start_deployment(JsonObject pullRequestJsonObject, JsonObject repositoryJsonObject, String access_token,
                                         SFDCConnectionDetailsMongoRepository sfdcConnectionDetailsMongoRepository, SFDCConnectionDetails sfdcConnectionDetail, String emailId,
                                         RabbitMqSenderConfig rabbitMqSenderConfig, AmqpTemplate rabbitTemplate) throws Exception {
        String userName = pullRequestJsonObject.get("user").getAsJsonObject().get("login").getAsString();
        String gitCloneURL = repositoryJsonObject.get("clone_url").getAsString();
        String gitRepoId = repositoryJsonObject.get("id").getAsString();
        String sourceBranch = pullRequestJsonObject.get("head").getAsJsonObject().get("ref").getAsString();
        String repoName = pullRequestJsonObject.get("base").getAsJsonObject().get("repo").getAsJsonObject().get("name").getAsString();
        String targetBranch = pullRequestJsonObject.get("base").getAsJsonObject().get("ref").getAsString();
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

        if(sfdcConnectionDetail == null){
            return;
        }

        Properties develop = rabbitMqSenderConfig.amqpAdmin().getQueueProperties(targetBranch);
        String queue_name = develop.getProperty("QUEUE_NAME");

        // Create the object detail to be passed to RabbitMQ
        DeploymentJob deploymentJob = new DeploymentJob();
        deploymentJob.setAccess_token(access_token);
        deploymentJob.setSfdcConnectionDetail(sfdcConnectionDetail);
        deploymentJob.setEmailId(emailId);
        deploymentJob.setUserName(userName);
        deploymentJob.setGitCloneURL(gitCloneURL);
        deploymentJob.setSourceBranch(sourceBranch);
        deploymentJob.setTargetBranch(targetBranch);
        rabbitTemplate.convertAndSend(repoName, queue_name, deploymentJob);
        RabbitMqConsumer container = new RabbitMqConsumer();
        container.setConnectionFactory(rabbitMqSenderConfig.connectionFactory());
        container.setQueueNames(queue_name);
        container.setConcurrentConsumers(1);
        container.setMessageListener(new MessageListenerAdapter(new ConsumerHandler(), new Jackson2JsonMessageConverter()));
        container.startConsumers();


    }

    private void process_deployment(JsonObject jsonObject, String access_token) {
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

            List<SFDCConnectionDetails> byGitRepoId = sfdcConnectionDetailsMongoRepository.findByGitRepoId(String.valueOf(repository.getId()));

            System.out.println(" byGitRepoId-> " + byGitRepoId);
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
