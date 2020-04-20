package com.controller;

import com.amazonaws.services.s3.transfer.MultipleFileDownload;
import com.amazonaws.services.s3.transfer.MultipleFileUpload;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.dao.*;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.model.*;
import com.pmd.PMDStructure;
import com.rabbitMQ.ConsumerHandler;
import com.rabbitMQ.DeploymentJob;
import com.rabbitMQ.RabbitMqConsumer;
import com.rabbitMQ.RabbitMqSenderConfig;
import com.security.CryptoPassword;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import com.service.AmazonS3Client;
import com.service.CaptchaServiceV3;
import com.service.ICaptchaService;
import com.sforce.soap.metadata.MetadataConnection;
import com.sforce.ws.ConnectorConfig;
import com.utils.*;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.kohsuke.github.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.*;
import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@RestController
public class ForceCIController {

    public static final int HTTP_STATUS_OK = 200;
    public static final int HTTP_STATUS_CREATED = 201;
    public static final int HTTP_STATUS_FAILED = 401;
    public static final List<SseEmitter> emitters = Collections.synchronizedList(new ArrayList<>());
    public static final String GITHUB_GHE_API = "/api/v3";
    public static final String PENDING = "pending";
    public static final String ERROR = "error";
    public static final String SUCCESS = "success";
    public static final String BUILD_IS_PENDING = "Build is pending.";
    public static final String BUILD_IS_SUCCESSFUL = "Build is successful.";
    public static final String BUILD_IS_ERROR = "Build error.";
    public static final String VALIDATION = "_SfdcValidation";
    public static final String CODE_REVIEW_VALIDATION = "_CodeReviewValidation";
    public static final String CONNECT2DEPLOY_URL = "https://forceci.herokuapp.com/#!/apps/dashboard/app";
    private static final Logger logger = LoggerFactory.getLogger(ForceCIController.class);
    private final static List<Integer> LIST_VALID_RESPONSE_CODES = Arrays.asList(200, 201, 204, 207);
    private static final String GITHUB_API = "https://api.github.com";
    @Value("${github.clientId}")
    String githubClientId;
    @Value("${github.clientSecret}")
    String githubClientSecret;
    @Value("${application.redirectURI}")
    String gitHubRedirectURI;
    @Value("${application.gitHubEnterpriseRedirectURI}")
    String gitHubEnterpriseRedirectURI;
    @Value("${sfdc.clientId}")
    String sfdcClientId;
    @Value("${sfdc.clientSecret}")
    String sfdcClientSecret;
    @Value("${sfdc.redirectURI}")
    String sfdcRedirectURI;
    @Value("${application.hmacSecretKet}")
    String hmacSecretKet;
    @Value("${salesforce.metadataEndpoint}")
    String salesforceMetaDataEndpoint;
/*    @Value("${amazons3.bucketname}")
    private String bucketName;*/

    private Map<String, Map<String, RabbitMqConsumer>> consumerMap = new ConcurrentHashMap<>();
    @Autowired
    private RepositoryWrapperMongoRepository repositoryWrapperMongoRepository;
    @Autowired
    private SFDCConnectionDetailsMongoRepository sfdcConnectionDetailsMongoRepository;
    @Autowired
    private RabbitMqSenderConfig rabbitMqSenderConfig;
    @Autowired
    private AmqpTemplate rabbitTemplateCustomAdmin;
    @Autowired
    private DeploymentJobMongoRepository deploymentJobMongoRepository;
    @Autowired
    private Connect2DeployUserMongoRepository connect2DeployUserMongoRepository;
    @Autowired
    private Connect2DeployTokenMongoRepository connect2DeployTokenMongoRepository;
    @Autowired
    private ConnectionDetailsMongoRepository connectionDetailsMongoRepository;
    @Autowired
    private LinkedServicesMongoRepository linkedServicesMongoRepository;
    @Autowired
    private ICaptchaService captchaServiceV3;
    /*@Autowired
    private AmazonS3Client amazonS3Client;*/

    private static void update_deployment_status(JsonObject jsonObject) {
        System.out.println("Deployment status for " + jsonObject.get("deployment").getAsJsonObject().get("id").getAsString() +
                " is " + jsonObject.get("deployment_status").getAsJsonObject().get("state").getAsString());
    }

    private static String fetchCookies(HttpServletRequest request, Connect2DeployUserMongoRepository connect2DeployUserMongoRepository, LinkedServicesMongoRepository linkedServicesMongoRepository) {
        Gson gson = new Gson();
        String accessToken = org.apache.commons.lang3.StringUtils.isNotEmpty(request.getHeader(AUTHORIZATION)) ? request.getHeader(AUTHORIZATION) : "";
        accessToken = org.apache.commons.lang3.StringUtils.removeStart(accessToken, "Bearer").trim();
        Optional<Connect2DeployUser> byToken = connect2DeployUserMongoRepository.findByToken(accessToken);
        Map<String, String> mapLinkedServiceAndAccessToken = new HashMap<>();
        if (byToken.isPresent()) {
            Connect2DeployUser connect2DeployUser = byToken.get();
            Iterable<LinkedServices> allById = linkedServicesMongoRepository.findAllById(connect2DeployUser.getLinkedServices());
            for (LinkedServices linkedService : allById) {
                mapLinkedServiceAndAccessToken.put(linkedService.getName(), linkedService.getAccessToken());
            }

        }
        return gson.toJson(mapLinkedServiceAndAccessToken);
    }

    public static int createStatusAndReturnCode(Gson gson, String access_token, String statuseUrl, String targetBranch,
                                                GithubStatusObject githubStatusObject) throws IOException {
        PostMethod createStatus = new PostMethod(statuseUrl);
        createStatus.setRequestHeader("Authorization", "token " + access_token);
        createStatus.setRequestHeader("Content-Type", MediaType.APPLICATION_JSON);
        createStatus.setRequestBody(gson.toJson(githubStatusObject));
        HttpClient httpClient = new HttpClient();
        return httpClient.executeMethod(createStatus);
    }

    public static String encodeURIComponent(String s) {
        String result = null;

        try {
            result = URLEncoder.encode(s, StandardCharsets.UTF_8.name())
                    .replaceAll("\\+", "%20")
                    .replaceAll("\\%21", "!")
                    .replaceAll("\\%27", "'")
                    .replaceAll("\\%28", "(")
                    .replaceAll("\\%29", ")")
                    .replaceAll("\\%7E", "~");
        }

        // This exception should never occur.
        catch (UnsupportedEncodingException e) {
            result = s;
        }

        return result;
    }

    private static String getClientIp(HttpServletRequest request) {

        String remoteAddr = "";
        if (request != null) {
            remoteAddr = request.getHeader("X-FORWARDED-FOR");
            if (remoteAddr == null || "".equals(remoteAddr)) {
                remoteAddr = request.getRemoteAddr();
            }
        }

        return remoteAddr;
    }

    @GetMapping("/api/cancelDeployment")
    public String cancelDeployment(@RequestParam String deploymentJobId) throws Exception {
        Gson gson = new Gson();
        String result = "Success";
        Optional<DeploymentJob> jobMongoRepositoryById = deploymentJobMongoRepository.findById(deploymentJobId);
        boolean boolDeploymentCancelled = false;
        DeploymentJob deploymentJob = null;
        if (jobMongoRepositoryById.isPresent()) {
            deploymentJob = jobMongoRepositoryById.get();
            if (StringUtils.hasText(deploymentJob.getSfdcAsyncJobId())) {
                String instanceURL = deploymentJob.getSfdcConnectionDetail().getInstanceURL();
                String oauthToken = deploymentJob.getSfdcConnectionDetail().getOauthToken().trim();
                ConnectorConfig connectorConfig = new ConnectorConfig();
                connectorConfig.setServiceEndpoint(instanceURL + salesforceMetaDataEndpoint);
                connectorConfig.setSessionId(oauthToken);
                MetadataConnection metadataConnection = new MetadataConnection(connectorConfig);
                try {
                    boolDeploymentCancelled = SFDCUtils.cancelDeploy(metadataConnection, deploymentJob).equalsIgnoreCase("Done");
                } catch (Exception objException) {
                    result = objException.getMessage();
                }
            } else {
                boolDeploymentCancelled = true;
            }
        }

        System.out.println("boolDeploymentCancelled -> " + boolDeploymentCancelled);
        if (boolDeploymentCancelled) {
            if (!ObjectUtils.isEmpty(deploymentJob)) {
                deploymentJob.setBoolIsJobCancelled(true);
                DeploymentJob cancelledJob = deploymentJobMongoRepository.save(deploymentJob);
                System.out.println("cancelledJob -> " + cancelledJob.getId());
                GithubStatusObject githubStatusObject = new GithubStatusObject(ERROR, BUILD_IS_ERROR, deploymentJob.getTargetBranch() + VALIDATION,
                        CONNECT2DEPLOY_URL + "/" + deploymentJob.getRepoName() + "/" + deploymentJob.getRepoId() + "/" + deploymentJob.getTargetBranch());
                createStatusAndReturnCode(gson, deploymentJob.getAccess_token(), deploymentJob.getStatusesUrl(), deploymentJob.getTargetBranch(), githubStatusObject);
            }
            return gson.toJson(result);
        } else {
            return gson.toJson(result);
        }
    }

    @GetMapping("/asyncDeployments")
    public SseEmitter fetchData2(@RequestParam String userName, @RequestParam String repoId, @RequestParam String branchName) {
        final SseEmitter emitter = new SseEmitter();
        ExecutorService executor = Executors.newSingleThreadExecutor();

        executor.execute(() -> {
            try {
                if (StringUtils.hasText(userName) && StringUtils.hasText(repoId) && StringUtils.hasText(branchName)) {
                    List<DeploymentJob> byTargetBranch = deploymentJobMongoRepository.findByRepoIdAndTargetBranch(repoId, branchName);
                    List<DeploymentJobWrapper> jobWrapperList = new ArrayList<>();
                    if (byTargetBranch != null && !byTargetBranch.isEmpty()) {
                        for (DeploymentJob deploymentJob : byTargetBranch) {
                            DeploymentJobWrapper deploymentJobWrapper = new DeploymentJobWrapper();
                            deploymentJobWrapper.setSourceBranch(deploymentJob.getSourceBranch());
                            if (StringUtils.hasText(deploymentJob.getPackageXML())) {
                                deploymentJobWrapper.setPackageXML(encodeURIComponent(deploymentJob.getPackageXML()));
                            }
                            deploymentJobWrapper.setSfdcAsyncJobId(deploymentJob.getSfdcAsyncJobId());
                            deploymentJobWrapper.setId(deploymentJob.getId());
                            deploymentJobWrapper.setJobNo(deploymentJob.getJobId());
                            deploymentJobWrapper.setPrNumber(deploymentJob.getPullRequestNumber());
                            deploymentJobWrapper.setPrHtml(deploymentJob.getPullRequestHtmlUrl());

                            if (deploymentJob.isBoolIsJobCancelled()) {
                                deploymentJobWrapper.setBoolJobCancelled(true);
                                deploymentJobWrapper.setBoolCodeReviewNotStarted(false);
                                deploymentJobWrapper.setBoolCodeReviewValidationFail(false);
                                deploymentJobWrapper.setBoolCodeReviewValidationPass(false);
                                deploymentJobWrapper.setBoolCodeReviewValidationRunning(false);
                                deploymentJobWrapper.setBoolSFDCDeploymentFail(false);
                                deploymentJobWrapper.setBoolSFDCDeploymentNotStarted(false);
                                deploymentJobWrapper.setBoolSFDCDeploymentPass(false);
                                deploymentJobWrapper.setBoolSFDCDeploymentRunning(false);
                                deploymentJobWrapper.setBoolSfdcValidationFail(false);
                                deploymentJobWrapper.setBoolSfdcValidationPass(false);
                                deploymentJobWrapper.setBoolSfdcValidationRunning(false);
                                deploymentJobWrapper.setJobCancelled(ValidationStatus.VALIDATION_CANCELLED.getText());
                            } else {

                                if (deploymentJob.isBoolSfdcDeploymentNotStarted()) {
                                    deploymentJobWrapper.setBoolSFDCDeploymentNotStarted(true);
                                    deploymentJobWrapper.setSfdcDeploymentNotStarted(ValidationStatus.VALIDATION_NOTSTARTED.getText());
                                }
                                if (deploymentJob.isBoolSfdcDeploymentRunning()) {
                                    deploymentJobWrapper.setBoolSFDCDeploymentRunning(true);
                                    deploymentJobWrapper.setSfdcDeploymentRunning(ValidationStatus.VALIDATION_RUNNING.getText());
                                }

                                if (deploymentJob.isBoolSfdcDeploymentFail()) {
                                    deploymentJobWrapper.setBoolSFDCDeploymentFail(true);
                                    deploymentJobWrapper.setSfdcDeploymentFail(ValidationStatus.VALIDATION_FAIL.getText());
                                }

                                if (deploymentJob.isBoolSfdcDeploymentPass()) {
                                    deploymentJobWrapper.setBoolSFDCDeploymentPass(true);
                                    deploymentJobWrapper.setSfdcDeploymentPass(ValidationStatus.VALIDATION_PASS.getText());
                                }

                                if (deploymentJob.isBoolCodeReviewNotStarted()) {
                                    deploymentJobWrapper.setBoolCodeReviewNotStarted(true);
                                    deploymentJobWrapper.setCodeReviewValidationNotStarted(ValidationStatus.VALIDATION_NOTSTARTED.getText());
                                }
                                if (deploymentJob.isBoolCodeReviewPass()) {
                                    deploymentJobWrapper.setBoolCodeReviewValidationPass(true);
                                    deploymentJobWrapper.setCodeReviewValidationPass(ValidationStatus.VALIDATION_PASS.getText());
                                }
                                if (deploymentJob.isBoolCodeReviewRunning()) {
                                    deploymentJobWrapper.setBoolCodeReviewValidationRunning(true);
                                    deploymentJobWrapper.setCodeReviewValidationRunning(ValidationStatus.VALIDATION_RUNNING.getText());
                                }
                                if (deploymentJob.isBoolCodeReviewFail()) {
                                    deploymentJobWrapper.setBoolCodeReviewValidationFail(true);
                                    deploymentJobWrapper.setCodeReviewValidationFail(ValidationStatus.VALIDATION_FAIL.getText());
                                }
                                if (deploymentJob.isBoolSfdcRunning()) {
                                    deploymentJobWrapper.setBoolSfdcValidationRunning(true);
                                    deploymentJobWrapper.setSfdcValidationRunning(ValidationStatus.VALIDATION_RUNNING.getText());
                                }
                                if (deploymentJob.isBoolSfdcPass()) {
                                    deploymentJobWrapper.setBoolSfdcValidationPass(true);
                                    deploymentJobWrapper.setSfdcValidationPass(ValidationStatus.VALIDATION_PASS.getText());
                                }
                                if (deploymentJob.isBoolSfdcFail()) {
                                    deploymentJobWrapper.setBoolSfdcValidationFail(true);
                                    deploymentJobWrapper.setSfdcValidationFail(ValidationStatus.VALIDATION_FAIL.getText());
                                }
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

    @RequestMapping(value = "/gitAuth", method = RequestMethod.GET, params = {"code", "state", "connectionId"})
    public void gitAuth(@RequestParam String code, @RequestParam String state, @RequestParam String connectionId, ServletResponse response, ServletRequest request) throws Exception {

        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String environment = "https://github.com/login/oauth/access_token";
        HttpClient httpClient = new HttpClient();

        Optional<ConnectionDetails> byUui = connectionDetailsMongoRepository.findByUui(connectionId);
        if (byUui.isPresent()) {
            ConnectionDetails connectionDetails = byUui.get();
            SwingUtilities.invokeLater(() -> connectionDetailsMongoRepository.delete(connectionDetails));
            Connect2DeployUser byEmailId = connect2DeployUserMongoRepository.findByEmailId(connectionDetails.getUserName());
            logger.info("byEmailId -> Github -> " + byEmailId.getEmailId());
            if (!ObjectUtils.isEmpty(byEmailId)) {
                PostMethod post = new PostMethod(environment);
                post.setRequestHeader("Accept", MediaType.APPLICATION_JSON);
                post.addParameter("code", code);
                post.addParameter("redirect_uri", gitHubRedirectURI);
                post.addParameter("client_id", githubClientId);
                post.addParameter("client_secret", githubClientSecret);
                post.addParameter("state", state);

                int i = httpClient.executeMethod(post);
                logger.info("byEmailId -> Github status -> " + i);
                String responseBody = IOUtils.toString(post.getResponseBodyAsStream(), StandardCharsets.UTF_8);

                String accessToken = null;
                JsonParser parser = new JsonParser();

                JsonObject jsonObject = parser.parse(responseBody).getAsJsonObject();
                try {
                    accessToken = jsonObject.get("access_token").getAsString();
                    for (String linkedServiceId : byEmailId.getLinkedServices()) {
                        Optional<LinkedServices> linkedServicesById = linkedServicesMongoRepository.findById(linkedServiceId);
                        if(linkedServicesById.isPresent()) {
                            LinkedServices linkedService =  linkedServicesById.get();
                            logger.info("byEmailId -> Github status linkedService -> " + linkedService.getName());
                            if (linkedService.getName().equalsIgnoreCase(LinkedServicesUtil.GIT_HUB)) {
                                linkedService.setConnected(true);
                                linkedService.setServerURL(connectionDetails.getServerURL());
                                linkedService.setAccessToken(accessToken);
                                fetchUserName(linkedService);
                                break;
                            }
                        }
                    }


                    SwingUtilities.invokeLater(() -> connect2DeployUserMongoRepository.save(byEmailId));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        httpResponse.sendRedirect("/?redirect_git=true");
    }

    @RequestMapping(value = "/api/initiateGitHubEnterpriseFlow", method = RequestMethod.POST)
    public String initiateGitHubEnterpriseFlow(@RequestBody ConnectionDetails connectionDetails) {
        Gson gson = new Gson();
        String oauthUrl;
        String randomString = String.valueOf(UUID.randomUUID());
        connectionDetails.setUui(randomString);
        connectionDetails.setRequestFrom("/apps/dashboard/createApp");
        connectionDetailsMongoRepository.save(connectionDetails);
        oauthUrl = connectionDetails.getServerURL() + "/login/oauth/authorize?scope=repo,user:email&client_id=" + connectionDetails.getClientId() +
                "&redirect_uri=" + gitHubEnterpriseRedirectURI + "?connectionId=" + connectionDetails.getUui() + "&state=" + randomString;

        logger.info("randomString -> " + randomString);
        return gson.toJson(oauthUrl);
    }

    @RequestMapping(value = "/api/fetchAllCommits", method = RequestMethod.POST)
    public String fetchAllCommits(@RequestBody CommitRequest gitCommitSearch) throws Exception {
        Gson gson = new Gson();
        List<CommitResponse> lstCommits = new ArrayList<>();
        if(!ObjectUtils.isEmpty(gitCommitSearch)){
            String userConnect2DeployToken = gitCommitSearch.getUserConnect2DeployToken();
            Optional<Connect2DeployUser> userByToken = connect2DeployUserMongoRepository.findByToken(userConnect2DeployToken);
            if(userByToken.isPresent()){
                Connect2DeployUser connect2DeployUser = userByToken.get();
                for (String linkedService : connect2DeployUser.getLinkedServices()) {
                    Optional<LinkedServices> linkedServiceById = linkedServicesMongoRepository.findById(linkedService);
                    if(linkedServiceById.isPresent() && linkedServiceById.get().getName().equalsIgnoreCase(gitCommitSearch.getLinkedServiceName())){
                        GitHub gitHub = null;
                        if(gitCommitSearch.getLinkedServiceName().equalsIgnoreCase(LinkedServicesUtil.GIT_HUB)) {
                            gitHub = GitHub.connectUsingOAuth(linkedServiceById.get().getAccessToken());
                        }else if(gitCommitSearch.getLinkedServiceName().equalsIgnoreCase(LinkedServicesUtil.GIT_HUB_ENTERPRISE)) {
                            gitHub = GitHub.connectToEnterpriseWithOAuth(linkedServiceById.get().getServerURL(),linkedServiceById.get().getUserName(), linkedServiceById.get().getAccessToken());
                        }
                        GHRepository repositoryById = gitHub.getRepositoryById(gitCommitSearch.getRepoId());
                        for (GHCommit eachCommit : repositoryById.queryCommits()
                                .from(gitCommitSearch.getBranchFromGit())
                                .since(gitCommitSearch.getFromDate())
                                .until(gitCommitSearch.getToDate()).list()) {
                            CommitResponse commitResponse = new CommitResponse();
                            if(eachCommit.getAuthor() != null) {
                                commitResponse.setAuthorName(eachCommit.getAuthor().getLogin());
                                commitResponse.setAuthorUrl(eachCommit.getAuthor().getHtmlUrl().toExternalForm());
                            } else {
                                if(eachCommit.getCommitShortInfo() != null && eachCommit.getCommitShortInfo().getAuthor() != null) {
                                    commitResponse.setAuthorName(eachCommit.getCommitShortInfo().getAuthor().getName());
                                }
                            }
                            commitResponse.setRepoToken(linkedServiceById.get().getAccessToken());
                            commitResponse.setRepoUserName(linkedServiceById.get().getUserName());
                            commitResponse.setGhEnterpriseServerURL(linkedServiceById.get().getServerURL());
                            if(eachCommit.getOwner() != null){
                                commitResponse.setGitCloneURL(eachCommit.getOwner().getHttpTransportUrl());
                            }
                            commitResponse.setCommitDate(eachCommit.getCommitDate());
                            commitResponse.setCommitId(eachCommit.getSHA1());
                            if(eachCommit.getCommitShortInfo() != null) {
                                commitResponse.setCommitMessage(eachCommit.getCommitShortInfo().getMessage());
                            }
                            if(eachCommit.getCommitter() != null) {
                                commitResponse.setCommitterName(eachCommit.getCommitter().getLogin());
                                commitResponse.setCommitterURL(eachCommit.getCommitter().getHtmlUrl().toExternalForm());
                            } else {
                                if(eachCommit.getCommitShortInfo() != null && eachCommit.getCommitShortInfo().getCommitter() != null) {
                                    commitResponse.setCommitterName(eachCommit.getCommitShortInfo().getCommitter().getName());
                                }
                            }
                            commitResponse.setCommitURL(eachCommit.getHtmlUrl().toExternalForm());
                            lstCommits.add(commitResponse);
                        }
                    }
                }
            }
        }
        return gson.toJson(lstCommits);
    }

    @RequestMapping(value = "/api/cherryPick", method = RequestMethod.POST)
    public String cherryPick(@RequestBody CherryPickRequest cherryPickRequest) throws Exception {
        Gson gson = new Gson();
        List<String> newListToBeReturned = new ArrayList<>();
        List<String> sf_build = new ArrayList<>();
        GitHub gitHub = null;
        if(cherryPickRequest.getLinkedServiceName().equalsIgnoreCase(LinkedServicesUtil.GIT_HUB)) {
            gitHub = GitHub.connectUsingOAuth(cherryPickRequest.getRepoToken());
        }else if(cherryPickRequest.getLinkedServiceName().equalsIgnoreCase(LinkedServicesUtil.GIT_HUB_ENTERPRISE)) {
            gitHub = GitHub.connectToEnterpriseWithOAuth(cherryPickRequest.getGhEnterpriseServerURL(),cherryPickRequest.getRepoUserName(), cherryPickRequest.getRepoToken());
        }
        GHRepository repositoryById = gitHub.getRepositoryById(cherryPickRequest.getRepoId());
        GHBranch branch = null;
        try {
            branch = repositoryById.getBranch(cherryPickRequest.getNewBranch());
        } catch (Exception objException){
            logger.error(objException.getMessage());
        }
        if(ObjectUtils.isEmpty(branch)) {

            Path tempDirectory = Files.createTempDirectory(cherryPickRequest.getDestinationBranch());
            try {
                Map<String, String> propertiesMap = new HashMap<>();
                ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
                InputStream buildXml = classLoader.getResourceAsStream("build/build.xml");
                InputStream gitCherryPick = classLoader.getResourceAsStream("build/git-multi-cherry-pick.sh");
                File buildFile = ConsumerHandler.stream2file(buildXml, "build", ".xml");
                File cherryPick = ConsumerHandler.stream2file(gitCherryPick, "git-multi-cherry-pick", ".sh");
                propertiesMap.put("gitMultiCherryPick", cherryPick.getName());
                if(cherryPickRequest.getGhEnterpriseServerURL() != null){
                    propertiesMap.put("gitCloneURL", "https://" + cherryPickRequest.getRepoUserName() + ":" + cherryPickRequest.getRepoToken() + "@"+cherryPickRequest.getGhEnterpriseServerURL()+"/" +
                            cherryPickRequest.getRepoUserName()+"/"+repositoryById.getName()+".git");
                }else {
                    propertiesMap.put("gitCloneURL", "https://" + cherryPickRequest.getRepoUserName() + ":" + cherryPickRequest.getRepoToken() + "@github.com/" +
                            cherryPickRequest.getRepoUserName()+"/"+repositoryById.getName()+".git");
                }
                propertiesMap.put("gitBranchName", cherryPickRequest.getDestinationBranch());
                propertiesMap.put("gitNewBranchName", cherryPickRequest.getNewBranch());
                propertiesMap.put("cherryPickIds", String.join(" ", cherryPickRequest.getLstCommitIdsSelected()));
                propertiesMap.put("gitDirectory", tempDirectory.toFile().getPath());
                propertiesMap.put("userEmail", gitHub.getUser(cherryPickRequest.getRepoUserName()).getEmail());
                propertiesMap.put("userName", gitHub.getUser(cherryPickRequest.getRepoUserName()).getLogin());
                logger.info("propertiesMap -> "+gson.toJson(propertiesMap));
                sf_build = AntExecutor.executeAntTask(buildFile.getPath(), "git_multi_cherry_pick", propertiesMap);
            } catch (Exception objException) {
                logger.error(objException.getMessage());
            } finally {
                FileUtils.deleteDirectory(tempDirectory.toFile());
            }

            if (!sf_build.isEmpty()) {
                for (String eachString : Lists.reverse(sf_build)) {
                    if (eachString.contains("**** SUCCESS ****")) {
                        newListToBeReturned.add(eachString);
                        for (String eachStringAgain : Lists.reverse(sf_build)) {
                            if (eachStringAgain.contains("remote:")) {
                                if (eachStringAgain.split("remote:").length > 1) {
                                    eachStringAgain = eachStringAgain.split("remote:")[1].trim();
                                    if (org.apache.commons.lang3.StringUtils.isNotEmpty(eachStringAgain) && org.apache.commons.lang3.StringUtils.isNotBlank(eachStringAgain)) {
                                        newListToBeReturned.add(eachStringAgain);
                                    }
                                }
                            }
                        }
                    } else if (eachString.contains("****")) {
                        newListToBeReturned.add(eachString);
                    }
                }

            }
        } else {
            newListToBeReturned.add("Branch " +cherryPickRequest.getNewBranch()+ " Already Exists ! Please try creating different branch.");
        }
        return gson.toJson(newListToBeReturned);
    }

    @RequestMapping(value = "/api/deleteLinkedService", method = RequestMethod.GET)
    public String deleteLinkedService(@RequestParam String linkedServiceName, @RequestParam String linkedServiceId) {
        Gson gson = new Gson();
        String strSuccess = "Success";
        Optional<LinkedServices> linkedServiceFromDB = linkedServicesMongoRepository.findById(linkedServiceId);
        if (linkedServiceFromDB.isPresent()) {
            LinkedServices linkedServices = linkedServiceFromDB.get();
            linkedServices.setActions("+ Connect to " + linkedServiceName);
            linkedServices.setConnected(false);
            linkedServices.setName(linkedServiceName);
            linkedServices.setUserName("Not Connected");
            linkedServices.setAccessToken(org.apache.commons.lang3.StringUtils.EMPTY);
            linkedServices.setUserEmail(org.apache.commons.lang3.StringUtils.EMPTY);
            linkedServicesMongoRepository.save(linkedServices);
        } else {
            strSuccess = "No Linked Services Found.";
        }
        return gson.toJson(strSuccess);
    }

    @RequestMapping(value = "/api/fetchAccessTokens", method = RequestMethod.GET)
    public String fetchAccessTokens(@RequestParam String userEmail) {
        Gson gson = new Gson();
        Connect2DeployUser byEmailId = connect2DeployUserMongoRepository.findByEmailId(userEmail);
        Map<String, String> mapAppAndAccessToken = new HashMap<>();
        if (!ObjectUtils.isEmpty(byEmailId)) {
            for (String linkedServiceId : byEmailId.getLinkedServices()) {
                Optional<LinkedServices> linkedServices = linkedServicesMongoRepository.findById(linkedServiceId);
                if(linkedServices.isPresent()){
                    LinkedServices eachLinkedServices = linkedServices.get();
                    mapAppAndAccessToken.put(eachLinkedServices.getName(), eachLinkedServices.getAccessToken());
                }
            }
        }
        return gson.toJson(mapAppAndAccessToken);
    }

    @RequestMapping(value = "/api/initiateGitHubFlow", method = RequestMethod.GET)
    public String initiateGitHubFlow(@RequestParam String userEmail) {
        Gson gson = new Gson();
        String oauthUrl;
        String randomString = String.valueOf(UUID.randomUUID());
        ConnectionDetails connectionDetails = new ConnectionDetails();
        connectionDetails.setUui(randomString);
        connectionDetails.setUserName(userEmail);
        connectionDetails.setRequestFrom("/apps/dashboard/createApp");
        connectionDetailsMongoRepository.save(connectionDetails);
        oauthUrl = "https://github.com/login/oauth/authorize?scope=repo,user:email&client_id=" + githubClientId +
                "&redirect_uri=" + gitHubRedirectURI + "?connectionId=" + randomString + "&state=" + randomString;

        logger.info("randomString -> " + randomString);
        return gson.toJson(oauthUrl);
    }

    @RequestMapping(value = "/github-enterprise/callback", method = RequestMethod.GET, params = {"code", "state", "connectionId"})
    public void gitHubEnterprise(@RequestParam String code, @RequestParam String state, @RequestParam String connectionId, ServletResponse response, ServletRequest
            request) throws Exception {
        Optional<ConnectionDetails> byUui = connectionDetailsMongoRepository.findByUui(connectionId);
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        if (byUui.isPresent()) {
            ConnectionDetails connectionDetails = byUui.get();
            connectionDetailsMongoRepository.delete(connectionDetails);
            String environment = connectionDetails.getServerURL() + "/login/oauth/access_token";
            HttpClient httpClient = new HttpClient();

            PostMethod post = new PostMethod(environment);
            post.setRequestHeader("Accept", MediaType.APPLICATION_JSON);
            post.addParameter("code", code);
            post.addParameter("redirect_uri", gitHubEnterpriseRedirectURI);
            post.addParameter("client_id", connectionDetails.getClientId());
            post.addParameter("client_secret", connectionDetails.getClientSecret());
            post.addParameter("state", state);

            httpClient.executeMethod(post);
            String responseBody = IOUtils.toString(post.getResponseBodyAsStream(), StandardCharsets.UTF_8);

            String accessToken = null;
            String token_type = null;
            JsonParser parser = new JsonParser();

            JsonObject jsonObject = parser.parse(responseBody).getAsJsonObject();
            try {
                accessToken = jsonObject.get("access_token").getAsString();
                Connect2DeployUser byEmailId = connect2DeployUserMongoRepository.findByEmailId(connectionDetails.getUserName());
                if (!ObjectUtils.isEmpty(byEmailId)) {
                    for (String linkedServiceId : byEmailId.getLinkedServices()) {
                        Optional<LinkedServices> linkedServices = linkedServicesMongoRepository.findById(linkedServiceId);
                        if(linkedServices.isPresent()) {
                            LinkedServices linkedService = linkedServices.get();
                            logger.info("Enterprise -> " + linkedService.getName());
                            if (linkedService.getName().equalsIgnoreCase(LinkedServicesUtil.GIT_HUB_ENTERPRISE)) {
                                linkedService.setConnected(true);
                                linkedService.setServerURL(connectionDetails.getServerURL());
                                linkedService.setAccessToken(accessToken);
                                fetchUserName(linkedService);
                                break;
                            }
                        }
                    }
                    SwingUtilities.invokeLater(() -> connect2DeployUserMongoRepository.save(byEmailId));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        httpResponse.sendRedirect("/index.html?redirect_git=true");
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

    @RequestMapping(value = "/api/getAllBranches", method = RequestMethod.GET)
    public String getAllBranches(@RequestParam String strRepoId, HttpServletResponse response,
                                 HttpServletRequest request) throws IOException {
        Gson gson = new Gson();
        String fromCookies = fetchCookies(request, connect2DeployUserMongoRepository, linkedServicesMongoRepository);
        Type type = new TypeToken<Map<String, String>>() {
        }.getType();
        Map<String, String> linkedServiceAndAccessTokenMap = gson.fromJson(fromCookies, type);
        GitHub gitHub = GitHub.connectUsingOAuth(linkedServiceAndAccessTokenMap.get(LinkedServicesUtil.GIT_HUB));
        GHRepository repository = gitHub.getRepositoryById(strRepoId);
        Map<String, GHBranch> branches = repository.getBranches();
        List<String> lstBranchesToBeReturned = new ArrayList<>();
        for (Map.Entry<String, GHBranch> stringGHBranchEntry : branches.entrySet()) {
            lstBranchesToBeReturned.add(stringGHBranchEntry.getValue().getName());
        }
        return gson.toJson(lstBranchesToBeReturned);
    }

    @RequestMapping(value = "/api/createBranch", method = RequestMethod.GET)
    public String createBranch(@RequestParam String repoId, @RequestParam String targetBranch,
                               @RequestParam String userName, @RequestParam String newBranchName,
                               HttpServletResponse response,
                               HttpServletRequest request) throws IOException {
        Gson gson = new Gson();
        String fromCookies = fetchCookies(request, connect2DeployUserMongoRepository, linkedServicesMongoRepository);
        Type type = new TypeToken<Map<String, String>>() {
        }.getType();
        Map<String, String> linkedServiceAndAccessTokenMap = gson.fromJson(fromCookies, type);
        String access_token = linkedServiceAndAccessTokenMap.get(LinkedServicesUtil.GIT_HUB);
        GitHub gitHub = GitHub.connectUsingOAuth(access_token);
        GHRepository repository = gitHub.getRepositoryById(repoId);
        GetMethod fetchSHA = new GetMethod(GITHUB_API + "/repos/" + userName + "/" + repository.getName() + "/" + "git/ref/heads/" + targetBranch);
        fetchSHA.setRequestHeader("Authorization", "token " + access_token);
        fetchSHA.setRequestHeader("Content-Type", MediaType.APPLICATION_JSON);
        HttpClient httpClient = new HttpClient();
        int fetchSHACode = httpClient.executeMethod(fetchSHA);
        if (fetchSHACode == HTTP_STATUS_OK) {
            SHAObject shaObject = gson.fromJson(IOUtils.toString(fetchSHA.getResponseBodyAsStream(), StandardCharsets.UTF_8), SHAObject.class);
            String targetSHA = shaObject.getObject().getSha();
            PostMethod createBranch = new PostMethod(GITHUB_API + "/repos/" + userName + "/" + repository.getName() + "/" + "git/refs");
            createBranch.setRequestHeader("Authorization", "token " + access_token);
            createBranch.setRequestHeader("Content-Type", MediaType.APPLICATION_JSON);
            CreateBranch objCreateBranch = new CreateBranch("refs/heads/" + newBranchName, targetSHA);
            StringRequestEntity requestEntity = new StringRequestEntity(
                    gson.toJson(objCreateBranch),
                    "application/json",
                    "UTF-8");
            createBranch.setRequestEntity(requestEntity);
            httpClient = new HttpClient();
            int createBranchCode = httpClient.executeMethod(createBranch);
            if (createBranchCode == HTTP_STATUS_CREATED) {
                return gson.toJson("Success");
            } else {
                return gson.toJson("Error");
            }
        }
        return gson.toJson("Error");
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
        String fromCookies = fetchCookies(request, connect2DeployUserMongoRepository, linkedServicesMongoRepository);
        Type type = new TypeToken<Map<String, String>>() {
        }.getType();
        Map<String, String> linkedServiceAndAccessTokenMap = gson.fromJson(fromCookies, type);
        String access_token = linkedServiceAndAccessTokenMap.get(LinkedServicesUtil.GIT_HUB);
        String emailId = null;
        SFDCConnectionDetails sfdcConnectionDetails = null;
        logger.info("githubEvent -> " + githubEvent);
        switch (githubEvent) {
            case "pull_request":
                String user = jsonObject.get("pull_request").getAsJsonObject().get("user").getAsJsonObject().get("login").getAsString();
                LinkedServices byUserName = linkedServicesMongoRepository.findByUserName(user);
                if (byUserName != null) {
                    access_token = byUserName.getAccessToken();
                    emailId = byUserName.getUserEmail();
                }
                if (("opened".equalsIgnoreCase(jsonObject.get("action").getAsString()) || "synchronize".equalsIgnoreCase(jsonObject.get("action").getAsString())) &&
                        !jsonObject.get("pull_request").getAsJsonObject().get("merged").getAsBoolean()) {
                    System.out.println("A pull request was created! A validation should start now...");
                    start_deployment(jsonObject.get("pull_request").getAsJsonObject(), jsonObject.get("repository").getAsJsonObject(), access_token,
                            sfdcConnectionDetailsMongoRepository, sfdcConnectionDetails, emailId, rabbitMqSenderConfig,
                            rabbitTemplateCustomAdmin, false, jsonObject.get("sender").getAsJsonObject());
                }
                break;
            case "push":
                String pushUser = jsonObject.get("repository").getAsJsonObject().get("owner").getAsJsonObject().get("login").getAsString();
                LinkedServices byUserName1 = linkedServicesMongoRepository.findByUserName(pushUser);
                if (byUserName1 != null) {
                    access_token = byUserName1.getAccessToken();
                    emailId = byUserName1.getUserEmail();
                }
                System.out.println("A pull request was merged! Deployment start now...");
                start_deployment(jsonObject, jsonObject.get("repository").getAsJsonObject(), access_token,
                        sfdcConnectionDetailsMongoRepository, sfdcConnectionDetails, emailId, rabbitMqSenderConfig,
                        rabbitTemplateCustomAdmin, true, jsonObject.get("sender").getAsJsonObject());
                break;
            case "deployment":
                if (!StringUtils.hasText(access_token)) {
                    String userId = jsonObject.get("repository").getAsJsonObject().get("owner").getAsJsonObject().get("login").getAsString();
                    access_token = linkedServicesMongoRepository.findByUserName(userId).getAccessToken();
                }
                process_deployment(jsonObject, access_token);
                break;
        }
        return gson.toJson("");
    }

    @RequestMapping(value = "/api/fetchRepositoryInDB", method = RequestMethod.GET)
    public String getRepositoryList(HttpServletResponse response, HttpServletRequest request) throws Exception {
        Gson gson = new Gson();
        String reposOnDB = "";
        String accessToken = org.apache.commons.lang3.StringUtils.isNotEmpty(request.getHeader(AUTHORIZATION)) ? request.getHeader(AUTHORIZATION) : "";
        accessToken = org.apache.commons.lang3.StringUtils.removeStart(accessToken, "Bearer").trim();
        Optional<Connect2DeployUser> byToken = connect2DeployUserMongoRepository.findByToken(accessToken);
        LinkedServices linkedServiceFromDB = null;
        List<RepositoryWrapper> lstRepositoryWrapper = null;
        if (byToken.isPresent()) {
            Connect2DeployUser connect2DeployUser = byToken.get();
            List<RepositoryWrapper> newLstRepositoryWrapper = new ArrayList<>();
            for (String linkedServiceId : connect2DeployUser.getLinkedServices()) {
                Optional<LinkedServices> linkedServices = linkedServicesMongoRepository.findById(linkedServiceId);
                if(linkedServices.isPresent()){
                    linkedServiceFromDB = linkedServices.get();
                    lstRepositoryWrapper = repositoryWrapperMongoRepository.findByOwnerId(linkedServiceFromDB.getUserName());
                }
                if (lstRepositoryWrapper != null && !lstRepositoryWrapper.isEmpty()) {
                    for (RepositoryWrapper repositoryWrapper : lstRepositoryWrapper) {
                        List<SFDCConnectionDetails> byGitRepoId = sfdcConnectionDetailsMongoRepository.findByGitRepoId(repositoryWrapper.getRepository().getRepositoryId());
                        repositoryWrapper.getRepository().setSfdcConnectionDetails(byGitRepoId);
                        newLstRepositoryWrapper.add(repositoryWrapper);
                        logger.info("consumerMap from /api/fetchRepositoryInDB -> "+consumerMap);
                        if(byGitRepoId != null && !byGitRepoId.isEmpty()) {
                            if (consumerMap != null && consumerMap.containsKey(repositoryWrapper.getRepository().getRepositoryId())
                                    && consumerMap.get(repositoryWrapper.getRepository().getRepositoryId()).isEmpty()) {
                                logger.info("sfdcConnectionDetails if Key check -> " + consumerMap);
                                Map<String, RabbitMqConsumer> stringRabbitMqConsumerMap = consumerMap.get(repositoryWrapper.getRepository().getRepositoryId());
                                for (SFDCConnectionDetails sfdcConnectionDetails : byGitRepoId) {
                                    logger.info("sfdcConnectionDetails if -> " + sfdcConnectionDetails.getGitRepoId());
                                    RabbitMqConsumer container = new RabbitMqConsumer();
                                    container.setConnectionFactory(rabbitMqSenderConfig.connectionFactory());
                                    container.setQueueNames(sfdcConnectionDetails.getGitRepoId() + "_" + sfdcConnectionDetails.getBranchConnectedTo());
                                    container.setConcurrentConsumers(1);
                                    container.setMessageListener(new MessageListenerAdapter(new ConsumerHandler(deploymentJobMongoRepository, sfdcConnectionDetailsMongoRepository), new Jackson2JsonMessageConverter()));
                                    logger.info("Started Consumer called from getRepository List");
                                    container.startConsumers();
                                    stringRabbitMqConsumerMap.put(sfdcConnectionDetails.getGitRepoId() + "_" + sfdcConnectionDetails.getBranchConnectedTo(), container);
                                }
                            } else {
                                if (consumerMap != null && !consumerMap.containsKey(repositoryWrapper.getRepository().getRepositoryId())) {
                                    logger.info("sfdcConnectionDetails else Key check -> " + consumerMap);
                                    Map<String, RabbitMqConsumer> rabbitMqConsumerMap = new ConcurrentHashMap<>();
                                    for (SFDCConnectionDetails sfdcConnectionDetails : byGitRepoId) {
                                        logger.info("sfdcConnectionDetails else -> " + sfdcConnectionDetails.getGitRepoId());
                                        RabbitMqConsumer container = new RabbitMqConsumer();
                                        container.setConnectionFactory(rabbitMqSenderConfig.connectionFactory());
                                        container.setQueueNames(sfdcConnectionDetails.getGitRepoId() + "_" + sfdcConnectionDetails.getBranchConnectedTo());
                                        container.setConcurrentConsumers(1);
                                        container.setMessageListener(new MessageListenerAdapter(new ConsumerHandler(deploymentJobMongoRepository, sfdcConnectionDetailsMongoRepository), new Jackson2JsonMessageConverter()));
                                        logger.info("Started Consumer called from getRepository List");
                                        container.startConsumers();
                                        rabbitMqConsumerMap.put(sfdcConnectionDetails.getGitRepoId() + "_" + sfdcConnectionDetails.getBranchConnectedTo(), container);
                                    }
                                    consumerMap.put(repositoryWrapper.getRepository().getRepositoryId(), rabbitMqConsumerMap);
                                }
                            }
                        }
                    }
                }
            }
            logger.info("consumerMap end -> "+consumerMap);
            reposOnDB = gson.toJson(newLstRepositoryWrapper);
        }
        return reposOnDB;
    }

    public void fetchUserName(LinkedServices linkedService) throws Exception {
        Gson gson = new Gson();
        String accessToken = null;
        String serverURL = null;
        accessToken = linkedService.getAccessToken();
        serverURL = linkedService.getServerURL();
        GetMethod getUserMethod = null;
        if (StringUtils.hasText(serverURL)) {
            getUserMethod = new GetMethod(serverURL + GITHUB_GHE_API + "/user");
        } else {
            getUserMethod = new GetMethod(GITHUB_API + "/user");
        }
        getUserMethod.setRequestHeader("Authorization", "token " + accessToken);
        HttpClient httpClient = new HttpClient();
        int intStatusOk = httpClient.executeMethod(getUserMethod);
        logger.info("intStatusOk for -> " + serverURL + " -> " + intStatusOk);
        if (intStatusOk == HTTP_STATUS_OK) {
            GitRepositoryUser gitRepositoryUser = gson.fromJson(IOUtils.toString(getUserMethod.getResponseBodyAsStream(), StandardCharsets.UTF_8), GitRepositoryUser.class);
            linkedService.setUserName(gitRepositoryUser.getLogin());
            linkedService.setUserEmail(gitRepositoryUser.getEmail());
            linkedServicesMongoRepository.save(linkedService);
        }
    }

    @RequestMapping(value = "/api/fetchRepository", method = RequestMethod.GET)
    public String getRepositoryByName(@RequestParam String repoName, @RequestParam String appName,
                                      HttpServletResponse response, HttpServletRequest request) throws IOException {
        Gson gson = new Gson();
        String repoUser = org.apache.commons.lang3.StringUtils.EMPTY;
        String gitHubURL = org.apache.commons.lang3.StringUtils.EMPTY;
        String accessToken = org.apache.commons.lang3.StringUtils.isNotEmpty(request.getHeader(AUTHORIZATION)) ? request.getHeader(AUTHORIZATION) : "";
        accessToken = org.apache.commons.lang3.StringUtils.removeStart(accessToken, "Bearer").trim();
        Optional<Connect2DeployUser> byToken = connect2DeployUserMongoRepository.findByToken(accessToken);
        if (byToken.isPresent()) {
            Connect2DeployUser connect2DeployUser = byToken.get();
            for (String linkedServiceId : connect2DeployUser.getLinkedServices()) {
                Optional<LinkedServices> linkedServices = linkedServicesMongoRepository.findById(linkedServiceId);
                if (linkedServices.isPresent() && appName.equalsIgnoreCase(linkedServices.get().getName())) {
                    accessToken = linkedServices.get().getAccessToken();
                    repoUser = linkedServices.get().getUserName();
                    gitHubURL = linkedServices.get().getServerURL();
                    break;
                }
            }
        }
        FinalResult finalResult = new FinalResult();
        String queryParam = "fork:true user:" + repoUser + " " + repoName;
        if (appName.equalsIgnoreCase(LinkedServicesUtil.GIT_HUB_ENTERPRISE)) {
            GitHub gitHub = GitHub.connectToEnterpriseWithOAuth(gitHubURL + GITHUB_GHE_API, repoUser, accessToken);
            extractItemAndConvertToFinalResult(gson, repoUser, finalResult, queryParam, gitHub);
        } else if (appName.equalsIgnoreCase(LinkedServicesUtil.GIT_HUB)) {
            GitHub gitHub = GitHub.connectUsingOAuth(accessToken);
            extractItemAndConvertToFinalResult(gson, repoUser, finalResult, queryParam, gitHub);
        }

        return gson.toJson(finalResult);
    }

    private void extractItemAndConvertToFinalResult(Gson gson, String repoUser, FinalResult finalResult, String queryParam, GitHub gitHub) throws IOException {
        String lstRepo;
        PagedSearchIterable<GHRepository> ghRepositories = gitHub.searchRepositories().q(queryParam).list();
        GitRepositoryFromQuery gitRepositoryFromQuery = new GitRepositoryFromQuery();
        gitRepositoryFromQuery.setIncomplete_results(String.valueOf(ghRepositories.isIncomplete()));
        gitRepositoryFromQuery.setTotal_count(String.valueOf(ghRepositories.getTotalCount()));
        List<Items> itemsList = new ArrayList<>();
        for (GHRepository ghRepository : ghRepositories) {
            Items items = new Items();
            items.setName(ghRepository.getName());
            items.setFull_name(ghRepository.getFullName());
            items.setHtml_url(ghRepository.getHtmlUrl().toExternalForm());
            items.setGit_url(ghRepository.getGitTransportUrl());
            items.setClone_url(ghRepository.getHttpTransportUrl());
            items.setLanguage(ghRepository.getLanguage());
            items.setDefault_branch(ghRepository.getDefaultBranch());
            GHUser ghRepositoryOwner = ghRepository.getOwner();
            Owner owner = new Owner();
            owner.setAvatar_url(ghRepositoryOwner.getAvatarUrl());
            owner.setHtml_url(ghRepositoryOwner.getHtmlUrl().toExternalForm());
            owner.setId(String.valueOf(ghRepositoryOwner.getId()));
            owner.setLogin(ghRepositoryOwner.getLogin());
            owner.setUrl(ghRepositoryOwner.getUrl().toExternalForm());
            items.setOwner(owner);
            Permissions permissions = new Permissions();
            permissions.setAdmin(String.valueOf(ghRepository.hasAdminAccess()));
            permissions.setPull(String.valueOf(ghRepository.hasPullAccess()));
            permissions.setPush(String.valueOf(ghRepository.hasPushAccess()));
            items.setPermissions(permissions);
            DateFormat dateFormat = new SimpleDateFormat();
            items.setCreated_at(dateFormat.format((ghRepository.getCreatedAt())));
            items.setUpdated_at(dateFormat.format((ghRepository.getUpdatedAt())));
            items.setOpen_issues_count(String.valueOf(ghRepository.getOpenIssueCount()));
            items.setId(String.valueOf(ghRepository.getId()));
            itemsList.add(items);
        }
        gitRepositoryFromQuery.setItems(Iterables.toArray(itemsList, Items.class));
        List<RepositoryWrapper> lstRepositoryWrapper = repositoryWrapperMongoRepository.findByOwnerId(repoUser);
        lstRepo = gson.toJson(gitRepositoryFromQuery);
        finalResult.setGitRepositoryFromQuery(lstRepo);
        finalResult.setRepositoryWrappers(lstRepositoryWrapper);
    }

    @RequestMapping(value = "/api/deleteWebHook", method = RequestMethod.DELETE)
    public String deleteWebHook(@RequestParam String repositoryName, @RequestParam String repositoryId, @RequestParam String repositoryOwner,
                                @RequestParam String webHookId, HttpServletResponse response, HttpServletRequest
                                        request) throws IOException, URISyntaxException {

        Gson gson = new Gson();
        int status = 0;

        String fromCookies = fetchCookies(request, connect2DeployUserMongoRepository, linkedServicesMongoRepository);
        Type type = new TypeToken<Map<String, String>>() {
        }.getType();
        Map<String, String> linkedServiceAndAccessTokenMap = gson.fromJson(fromCookies, type);
        String accessToken = linkedServiceAndAccessTokenMap.get(LinkedServicesUtil.GIT_HUB);
        if (accessToken != null) {
            DeleteMethod deleteWebHook = new DeleteMethod(GITHUB_API + "/repos/" + repositoryOwner + "/" + repositoryName + "/hooks/" + webHookId);
            deleteWebHook.setRequestHeader("Authorization", "token " + accessToken);
            deleteWebHook.setRequestHeader("Content-Type", MediaType.APPLICATION_JSON);
            HttpClient httpClient = new HttpClient();
            status = httpClient.executeMethod(deleteWebHook);
            if (status == 204) {
                try {
                    if(consumerMap != null && consumerMap.containsKey(repositoryId)) {
                        Map<String, RabbitMqConsumer> stringRabbitMqConsumerMap = consumerMap.get(repositoryId);
                        if(stringRabbitMqConsumerMap != null && !stringRabbitMqConsumerMap.isEmpty()) {
                            for (Map.Entry<String, RabbitMqConsumer> stringRabbitMqConsumerEntry : stringRabbitMqConsumerMap.entrySet()) {
                                stringRabbitMqConsumerEntry.getValue().stopConsumers();
                                stringRabbitMqConsumerEntry.getValue().shutDownConsumers();
                                logger.info("Consumer Stopped");
                            }
                        }
                        consumerMap.remove(repositoryId);
                    }
                    List<SFDCConnectionDetails> byGitRepoId = sfdcConnectionDetailsMongoRepository.findByGitRepoId(repositoryId);
                    deploymentJobMongoRepository.deleteAllByRepoId(repositoryId);
                    for (SFDCConnectionDetails sfdcConnectionDetails : byGitRepoId) {
                        logger.info("Delete Queue -> "+sfdcConnectionDetails.getGitRepoId());
                        boolean b = rabbitMqSenderConfig.amqpAdmin().deleteQueue(sfdcConnectionDetails.getGitRepoId() + '_' + sfdcConnectionDetails.getBranchConnectedTo());
                        logger.info("Queue Deleted -> "+b);
                    }
                    boolean b = rabbitMqSenderConfig.amqpAdmin().deleteExchange(repositoryId);
                    logger.info("Exchange Deleted -> "+b);
                    sfdcConnectionDetailsMongoRepository.deleteAll(byGitRepoId);
                } catch (Exception e) {
                    logger.error("Error from Delete webHook -> "+e.getMessage());
                }
                RepositoryWrapper byRepositoryRepositoryName = repositoryWrapperMongoRepository.findByOwnerIdAndRepositoryRepositoryId(repositoryOwner, repositoryId);
                repositoryWrapperMongoRepository.delete(byRepositoryRepositoryName);
            }
        }

        return gson.toJson(status);
    }

    @RequestMapping(value = "/api/createWebHook", method = RequestMethod.POST)
    public String createWebHook(@RequestBody Repository repository, HttpServletResponse response, HttpServletRequest
            request) throws IOException {

        Gson gson = new Gson();
        String returnResponse = null;

        String fromCookies = fetchCookies(request, connect2DeployUserMongoRepository, linkedServicesMongoRepository);
        Type type = new TypeToken<Map<String, String>>() {
        }.getType();
        Map<String, String> linkedServiceAndAccessTokenMap = gson.fromJson(fromCookies, type);
        String accessToken = linkedServiceAndAccessTokenMap.get(LinkedServicesUtil.GIT_HUB);
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
            logger.info("status -> Create webhook -> " + status);
            logger.info("status -> Create webhook response -> " + createWebHook.getResponseBodyAsString());
            if (LIST_VALID_RESPONSE_CODES.contains(status)) {
                WebHook webHookResponse = gson.fromJson(IOUtils.toString(createWebHook.getResponseBodyAsStream(), StandardCharsets.UTF_8), WebHook.class);
                GitHub gitHub = GitHub.connectUsingOAuth(accessToken);
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
                    rabbitMqSenderConfig.amqpAdmin().declareExchange(new DirectExchange(repository.getRepositoryId()));
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


    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public String registerUser(@RequestBody Connect2DeployUser userEntity, HttpServletResponse response, HttpServletRequest
            request) throws Exception {

        captchaServiceV3.processResponse(userEntity.getGoogleReCaptchaV3(), CaptchaServiceV3.REGISTER_ACTION);
        logger.info("recaptchaResponse -> "+userEntity.getGoogleReCaptchaV3());
        Gson gson = new Gson();
        String returnResponse = null;
        Connect2DeployUser existingUser = connect2DeployUserMongoRepository.findByEmailId(userEntity.getEmailId());
        if (existingUser != null && existingUser.isBoolEmailVerified()) {
            throw new Exception("User Already Exists");
        } else if (existingUser != null && !existingUser.isBoolEmailVerified()) {
            connect2DeployUserMongoRepository.delete(existingUser);
            returnResponse = createNewUserAndReturnMessage(userEntity);
        } else {
            returnResponse = createNewUserAndReturnMessage(userEntity);
        }


        return gson.toJson(returnResponse);
    }

    private String createNewUserAndReturnMessage(@RequestBody Connect2DeployUser userEntity) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
        String returnResponse;
        userEntity.setEnabled(true);
        userEntity.setBoolEmailVerified(false);
        userEntity.setPassword(CryptoPassword.generateStrongPasswordHash(userEntity.getPassword()));
        Set<String> linkedServices = LinkedServicesUtil.createLinkedServices(linkedServicesMongoRepository, userEntity);
        userEntity.setLinkedServices(linkedServices);
        userEntity = connect2DeployUserMongoRepository.save(userEntity);
        Connect2DeployToken confirmationToken = new Connect2DeployToken(userEntity.getId());
        confirmationToken = connect2DeployTokenMongoRepository.save(confirmationToken);
        Email from = new Email("no-reply@connect2deploy.com", "Connect2Deploy");
        String subject = "Hello " + userEntity.getFirstName() + " !";
        Email to = new Email(userEntity.getEmailId());
        Content content = new Content("text/plain", "To confirm your account, please click here : "
                + "https://forceci.herokuapp.com/#!/apps/dashboard/token/" + confirmationToken.getConfirmationToken());
        int statusCodeEmail = SendEmailsUtil.sendEmail(subject, from, to, content);
        logger.info("Email Sent -> " + statusCodeEmail);
        returnResponse = "Success";
        return returnResponse;
    }

    @RequestMapping(value = "/api/saveSfdcConnectionDetails", method = RequestMethod.POST)
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
        Properties develop = rabbitMqSenderConfig.amqpAdmin().getQueueProperties(sfdcConnectionDetails.getGitRepoId() + "_" + sfdcConnectionDetails.getBranchConnectedTo());

        if (develop != null && develop.stringPropertyNames() != null && !develop.stringPropertyNames().isEmpty()) {
            // Do nothing
        } else {
            RepositoryWrapper byRepositoryRepositoryId = repositoryWrapperMongoRepository.findByRepositoryRepositoryId(sfdcConnectionDetails.getGitRepoId());
            Queue queue = new Queue(sfdcConnectionDetails.getGitRepoId() + "_" + sfdcConnectionDetails.getBranchConnectedTo(), true);
            rabbitMqSenderConfig.amqpAdmin().declareQueue(queue);
            rabbitMqSenderConfig.amqpAdmin().declareBinding(BindingBuilder.bind(queue).to(new DirectExchange(byRepositoryRepositoryId.getRepository().getRepositoryId())).withQueueName());
            RabbitMqConsumer container = new RabbitMqConsumer();
            container.setConnectionFactory(rabbitMqSenderConfig.connectionFactory());
            container.setQueueNames(queue.getName());
            container.setConcurrentConsumers(1);
            container.setMessageListener(new MessageListenerAdapter(new ConsumerHandler(deploymentJobMongoRepository, sfdcConnectionDetailsMongoRepository), new Jackson2JsonMessageConverter()));
            logger.info("Started Consumer called from saveSfdcConnectionDetails");
            container.startConsumers();
            Map<String, RabbitMqConsumer> rabbitMqConsumerMap = consumerMap.get(byRepositoryRepositoryId.getRepository().getRepositoryId());
            if (rabbitMqConsumerMap != null && !rabbitMqConsumerMap.isEmpty()) {

            } else {
                rabbitMqConsumerMap = new ConcurrentHashMap<>();
                rabbitMqConsumerMap.put(sfdcConnectionDetails.getGitRepoId() + "_" + sfdcConnectionDetails.getBranchConnectedTo(), container);
            }
            logger.info("rabbitMqConsumerMap -> "+gson.toJson(rabbitMqConsumerMap.keySet()));
        }
        sfdcConnectionDetails.setOauthSaved("true");
        SFDCConnectionDetails connectionSaved = sfdcConnectionDetailsMongoRepository.save(sfdcConnectionDetails);
        returnResponse = gson.toJson(connectionSaved);
        return returnResponse;
    }

    @RequestMapping(value = "/api/showSfdcConnectionDetails", method = RequestMethod.GET)
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

    @RequestMapping(value = "/validateToken", method = RequestMethod.GET)
    public String validateToken(@RequestParam String token, HttpServletResponse response, HttpServletRequest
            request) throws IOException {

        Gson gson = new Gson();
        String returnResponse = null;
        Connect2DeployToken byConfirmationToken = connect2DeployTokenMongoRepository.findByConfirmationToken(token);
        if (byConfirmationToken != null) {
            Optional<Connect2DeployUser> userFromDB = connect2DeployUserMongoRepository.findById(byConfirmationToken.getUserId());
            if (userFromDB.isPresent()) {
                Connect2DeployUser connect2DeployUser = userFromDB.get();
                if (!connect2DeployUser.isBoolEmailVerified()) {
                    connect2DeployUser.setBoolEmailVerified(true);
                    connect2DeployUserMongoRepository.save(connect2DeployUser);
                    returnResponse = "Email Verified";
                } else {
                    returnResponse = "Email Already Verified";
                }
            }
        } else {
            returnResponse = "Error";
        }
        return gson.toJson(returnResponse);
    }

    @RequestMapping(value = "/loginConnect", method = RequestMethod.POST)
    public String loginController(@RequestBody Connect2DeployUser connect2DeployUser, HttpServletResponse response, HttpServletRequest
            request) throws Exception {

        Gson gson = new Gson();
        String returnResponse = null;
        captchaServiceV3.processResponse(connect2DeployUser.getGoogleReCaptchaV3(), CaptchaServiceV3.REGISTER_ACTION);
        Connect2DeployUser byEmailId = connect2DeployUserMongoRepository.findByEmailId(connect2DeployUser.getEmailId());
        if (byEmailId != null && CryptoPassword.validatePassword(connect2DeployUser.getPassword(), byEmailId.getPassword())) {
            if (!byEmailId.isBoolEmailVerified()) {
                throw new Exception("Email Not Verified");
            } else {
                // This means the token has not expired
                if (byEmailId.getTokenExpirationTime() != null && byEmailId.getTokenExpirationTime().after(new Date())) {
                    Cookie accessTokenCookie = new Cookie("CONNECT2DEPLOY_TOKEN", byEmailId.getToken());
                    response.addCookie(accessTokenCookie);
                    accessTokenCookie.setMaxAge(-1); //cookie not persistent, destroyed on browser exit
                    returnResponse = byEmailId.getEmailId();
                } else {
                    // Token has expired or this is a new User
                    String token = UUID.randomUUID().toString();
                    byEmailId.setToken(token);
                    Date currentDate = Calendar.getInstance().getTime();
                    currentDate = cvtToGmt(currentDate);
                    byEmailId.setTokenExpirationTime(DateUtils.addHours(currentDate, 24));
                    byEmailId = connect2DeployUserMongoRepository.save(byEmailId);
                    returnResponse = byEmailId.getEmailId();
                    Cookie accessTokenCookie = new Cookie("CONNECT2DEPLOY_TOKEN", token);
                    response.addCookie(accessTokenCookie);
                    accessTokenCookie.setMaxAge(-1); //cookie not persistent, destroyed on browser exit
                }
            }
        } else {
            throw new Exception("No User Found");
        }
        return gson.toJson(returnResponse);
    }

    @RequestMapping(value = "/validateConnect2DeployToken", method = RequestMethod.GET)
    public Boolean validateConnect2DeployToken(@RequestParam String strToken, HttpServletResponse response, HttpServletRequest
            request) {
        Optional<Connect2DeployUser> connect2DeployUser = connect2DeployUserMongoRepository.findByToken(strToken);
        return connect2DeployUser.isPresent();
    }

    @RequestMapping(value = "/api/fetchLogs", method = RequestMethod.GET)
    public String fetchLogs(@RequestParam String jobNo, @RequestParam String type, @RequestParam String repoId,
                            HttpServletResponse response, HttpServletRequest request) throws IOException {

        Gson gson = new Gson();
        String returnResponse = null;
        List<DeploymentJob> byJobIdAndRepoId = deploymentJobMongoRepository.findByJobIdAndRepoId(jobNo, repoId);
        List<String> lstFinalResult;
        if (byJobIdAndRepoId != null && !byJobIdAndRepoId.isEmpty()) {
            if (type.equals("sfdcValidation")) {
                lstFinalResult = byJobIdAndRepoId.get(0).getLstBuildLines();
            } else if (type.equals("codeValidation")) {
                List<PMDStructure> lstPmdStructures = byJobIdAndRepoId.get(0).getLstPmdStructures();
                List<String> stringList = new ArrayList<>();
                if (lstPmdStructures != null) {
                    for (PMDStructure pmdStructure : lstPmdStructures) {
                        stringList.add(pmdStructure.getName() + " \n " + "Line Number : " + pmdStructure.getLineNumber() + " \n "
                                + "Review Feedback : " + pmdStructure.getReviewFeedback() + "\n" + "Rule URL : " + pmdStructure.getRuleUrl());
                        stringList.add("\n ---------------------------------------------- \n");
                    }
                }
                lstFinalResult = stringList;
            } else {
                lstFinalResult = byJobIdAndRepoId.get(0).getLstDeploymentBuildLines();
            }
            returnResponse = gson.toJson(lstFinalResult);
        }
        return returnResponse;
    }

    @RequestMapping(value = "/api/deleteSfdcConnectionDetails", method = RequestMethod.DELETE)
    public String deleteSfdcConnectionDetails(@RequestParam String sfdcDetailsId, HttpServletResponse response, HttpServletRequest
            request) throws IOException {
        Gson gson = new Gson();
        try {
            Optional<SFDCConnectionDetails> byId = sfdcConnectionDetailsMongoRepository.findById(sfdcDetailsId);
            if(byId.isPresent()) {
                rabbitMqSenderConfig.amqpAdmin().deleteQueue(byId.get().getGitRepoId() + '_' + byId.get().getBranchConnectedTo());
                Map<String, RabbitMqConsumer> rabbitMqConsumerMap = consumerMap.get(byId.get().getGitRepoId());
                if (rabbitMqConsumerMap != null && !rabbitMqConsumerMap.isEmpty() && rabbitMqConsumerMap.get(byId.get().getBranchConnectedTo()) != null) {
                    rabbitMqConsumerMap.remove(byId.get().getBranchConnectedTo());
                }
                sfdcConnectionDetailsMongoRepository.deleteById(sfdcDetailsId);
                return gson.toJson("Success");
            } else {
                return gson.toJson("No Details Found");
            }
        } catch (Exception e) {
            return gson.toJson("Error");
        }

    }

    private void start_deployment(JsonObject pullRequestJsonObject, JsonObject repositoryJsonObject, String access_token,
                                  SFDCConnectionDetailsMongoRepository sfdcConnectionDetailsMongoRepository,
                                  SFDCConnectionDetails sfdcConnectionDetail, String emailId,
                                  RabbitMqSenderConfig rabbitMqSenderConfig, AmqpTemplate rabbitTemplate,
                                  boolean merge, JsonObject sender) throws Exception {

        String userName = pullRequestJsonObject.has("user") ? pullRequestJsonObject.get("user").getAsJsonObject().get("login").getAsString() : "";
        String gitCloneURL = repositoryJsonObject.has("clone_url") ? repositoryJsonObject.get("clone_url").getAsString() : "";
        String prHtmlURL = pullRequestJsonObject.has("html_url") ? pullRequestJsonObject.get("html_url").getAsString() : "";
        String statusesUrl = pullRequestJsonObject.has("statuses_url") ? pullRequestJsonObject.get("statuses_url").getAsString() : "";
        String prNumber = pullRequestJsonObject.has("number") ? pullRequestJsonObject.get("number").getAsString() : "";
        String prTitle = pullRequestJsonObject.has("title") ? pullRequestJsonObject.get("title").getAsString() : "";
        String gitRepoId = repositoryJsonObject.has("id") ? repositoryJsonObject.get("id").getAsString() : "";
        String sourceBranch = pullRequestJsonObject.has("head") ? pullRequestJsonObject.get("head").getAsJsonObject().get("ref").getAsString() : "";
        String repoName = pullRequestJsonObject.has("base") ? pullRequestJsonObject.get("base").getAsJsonObject().get("repo").getAsJsonObject().get("name").getAsString() : "";
        String targetBranch = pullRequestJsonObject.has("base") ? pullRequestJsonObject.get("base").getAsJsonObject().get("ref").getAsString() : "";
        String baseSHA = pullRequestJsonObject.has("base") ? pullRequestJsonObject.get("base").getAsJsonObject().get("sha").getAsString() : "";

        if (merge) {
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
                if (sfdcConnectionDetails.getBranchConnectedTo() != null &&
                        sfdcConnectionDetails.getBranchConnectedTo().equals(targetBranch)) {
                    sfdcConnectionDetail = sfdcConnectionDetails;
                    break;
                }
            }
        }

        if (sfdcConnectionDetail == null) {
            return;
        }

        Properties develop = rabbitMqSenderConfig.amqpAdmin().getQueueProperties(gitRepoId + "_" + targetBranch);
        String queue_name = develop.getProperty("QUEUE_NAME");

        Long aLong = deploymentJobMongoRepository.countByRepoIdAndTargetBranch(gitRepoId, targetBranch);

        // Create the object detail to be passed to RabbitMQ
        DeploymentJob deploymentJob = new DeploymentJob();
        if (merge) {
            List<DeploymentJob> byRepoIdAndBaseSHA = deploymentJobMongoRepository.findByRepoIdAndBaseSHAOrderByJobIdDesc(gitRepoId, baseSHA);
            if (byRepoIdAndBaseSHA != null && !byRepoIdAndBaseSHA.isEmpty()) {
                deploymentJob = byRepoIdAndBaseSHA.get(0);
                logger.info("deploymentJob getBaseSHA -> "+deploymentJob.getBaseSHA());
                logger.info("deploymentJob Id -> "+deploymentJob.getJobId());
                logger.info("deploymentJob RepoId -> "+deploymentJob.getRepoId());
            }
        }
        if (aLong != null && !merge) {
            deploymentJob.setJobId(String.valueOf(aLong.intValue() + 1));
        }
        deploymentJob.setRepoId(gitRepoId);
        deploymentJob.setRepoName(repoName);
        if (StringUtils.hasText(prNumber)) {
            deploymentJob.setPullRequestNumber(prNumber);
        }
        if (StringUtils.hasText(prHtmlURL)) {
            deploymentJob.setPullRequestHtmlUrl(prHtmlURL);
        }
        if (StringUtils.hasText(prTitle)) {
            deploymentJob.setPullRequestTitle(prTitle);
        }

        if (StringUtils.hasText(statusesUrl)) {
            deploymentJob.setStatusesUrl(statusesUrl);
        }
        deploymentJob.setAccess_token(access_token);
        deploymentJob.setSfdcConnectionDetail(sfdcConnectionDetail);
        deploymentJob.setEmailId(emailId);
        deploymentJob.setUserName(userName);
        deploymentJob.setGitCloneURL(gitCloneURL);
        if (StringUtils.hasText(sourceBranch)) {
            deploymentJob.setSourceBranch(sourceBranch);
        }
        deploymentJob.setTargetBranch(targetBranch);
        deploymentJob.setQueueName(queue_name);
        deploymentJob.setBoolSfdcCompleted(false);
        deploymentJob.setBaseSHA(baseSHA);
        if (merge) {
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
        deploymentJob.setBoolIsJobCancelled(false);
        DeploymentJob savedDeploymentJob = deploymentJobMongoRepository.save(deploymentJob);
        rabbitTemplate.convertAndSend(gitRepoId, queue_name, savedDeploymentJob);
        if (consumerMap != null && !consumerMap.isEmpty() &&
                !consumerMap.containsKey(sfdcConnectionDetail.getGitRepoId()) &&
                consumerMap.get(sfdcConnectionDetail.getGitRepoId()) != null &&
                !consumerMap.get(sfdcConnectionDetail.getGitRepoId()).isEmpty()) {
            Map<String, RabbitMqConsumer> rabbitMqConsumerMap = consumerMap.get(sfdcConnectionDetail.getGitRepoId());
            if ((rabbitMqConsumerMap != null && !rabbitMqConsumerMap.containsKey(queue_name))) {
                RabbitMqConsumer container = new RabbitMqConsumer();
                container.setConnectionFactory(rabbitMqSenderConfig.connectionFactory());
                container.setQueueNames(queue_name);
                container.setConcurrentConsumers(1);
                container.setMessageListener(new MessageListenerAdapter(new ConsumerHandler(deploymentJobMongoRepository, sfdcConnectionDetailsMongoRepository), new Jackson2JsonMessageConverter()));
                logger.info("Started Consumer called from start_deployment");
                container.startConsumers();
                rabbitMqConsumerMap.put(queue_name, container);
                consumerMap.put(sfdcConnectionDetail.getGitRepoId(), rabbitMqConsumerMap);
                logger.info("consumerMap start deployment-> "+consumerMap);
            }
        }
    }

    private void process_deployment(JsonObject jsonObject, String access_token) {
        String payload_str = jsonObject.get("deployment").getAsJsonObject().get("payload").getAsString();
        Map payload = new Gson().fromJson(payload_str, Map.class);

        try {
            Thread.sleep(2000L);
            GitHub gitHub = GitHub.connectUsingOAuth(access_token);
            GHRepository repository = gitHub.getRepository(
                    jsonObject.get("repository").getAsJsonObject()
                            .get("full_name").getAsString());
            GHDeploymentStatus deploymentStatus = new GHDeploymentStatusBuilder(repository,
                    jsonObject.get("deployment").getAsJsonObject().get("id").getAsInt(), GHDeploymentState.PENDING).create();

            List<SFDCConnectionDetails> byGitRepoId = sfdcConnectionDetailsMongoRepository.findByGitRepoId(String.valueOf(repository.getId()));

            Thread.sleep(20000L);

            // This will happen only after validation is success

           /* GHDeploymentStatus deploymentStatus2 = new GHDeploymentStatusBuilder(repository,
                    jsonObject.get("deployment").getAsJsonObject().get("id").getAsInt(), SUCCESS).create();*/
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

    }

    @RequestMapping(value = "/api/fetchAllLinkedServices", method = RequestMethod.GET)
    public String fetchAllLinkedServices(@RequestParam String userEmail, HttpServletResponse response, HttpServletRequest
            request) throws IOException {
        Gson gson = new Gson();
        Set<String> linkedServicesId = null;
        List<LinkedServices> lstLinkedServices = null;
        try {
            Connect2DeployUser byEmailId = connect2DeployUserMongoRepository.findByEmailId(userEmail);
            if (byEmailId != null) {
                linkedServicesId = byEmailId.getLinkedServices();
                lstLinkedServices = (List<LinkedServices>) linkedServicesMongoRepository.findAllById(linkedServicesId);
            }
            return gson.toJson(lstLinkedServices);
        } catch (Exception e) {
            return gson.toJson("Error");
        }

    }
/*
    @RequestMapping(value = "/api/connectAmazonS3/upload", method = RequestMethod.GET)
    public String uploadToAmazonS3(HttpServletResponse response, HttpServletRequest request) throws IOException, InterruptedException {
        String uniqueId = UUID.randomUUID().toString();
        logger.info("Uploaded With -> "+ uniqueId);
        TransferManager transferManager = TransferManagerBuilder.standard()
                .withS3Client(amazonS3Client.amazonClient()).build();
        Path directoryPath = Files.createTempDirectory(uniqueId);
        Path fileToCreatePath = directoryPath.resolve("fileName.txt");
        Path folderForUnmanagedPackage = directoryPath.resolve("testMe.txt");
        Files.createFile(fileToCreatePath).toFile();
        Files.createFile(folderForUnmanagedPackage).toFile();
        for (File eachFile : directoryPath.toFile().listFiles()) {
            logger.info("file name inside upload-> " + eachFile.getName());
        }
        MultipleFileUpload upload = transferManager.uploadDirectory(bucketName, uniqueId, directoryPath.toFile(), false);
        upload.waitForCompletion();
        logger.info("File Uploaded Successfully");
        transferManager.shutdownNow();
        return null;
    }

    @RequestMapping(value = "/api/connectAmazonS3/download", method = RequestMethod.GET)
    public String downloadFromAmazonS3(@RequestParam String prefixKey, HttpServletResponse response, HttpServletRequest request) throws IOException, InterruptedException {
        String uniqueId = UUID.randomUUID().toString();
        logger.info("Download With -> "+ prefixKey);
        TransferManager transferManager = TransferManagerBuilder.standard()
                .withS3Client(amazonS3Client.amazonClient()).build();
        File destinationDirectory = Files.createTempDirectory(uniqueId).toFile();
        logger.info("destinationDirectory With -> "+ destinationDirectory.getPath());
        MultipleFileDownload multipleFileDownload = transferManager.downloadDirectory(bucketName, prefixKey, destinationDirectory);
        multipleFileDownload.waitForCompletion();
        if(destinationDirectory.listFiles() != null && destinationDirectory.listFiles().length > 0) {
            for (File file : destinationDirectory.listFiles()) {
                if(file.isDirectory()){
                    for (File listFile : file.listFiles()) {
                        logger.info("file name inside download-> " + listFile.getName());
                    }

                }else{
                    logger.info("file name inside download-> " + file.getName());
                }

            }
        }

        logger.info("Download Successfully");
        transferManager.shutdownNow();
        return null;
    }*/

    public static Date cvtToGmt(Date date) {
        TimeZone tz = TimeZone.getDefault();
        Date ret = new Date(date.getTime() - tz.getRawOffset());

        // if we are now in DST, back off by the delta.  Note that we are checking the GMT date, this is the KEY.
        if (tz.inDaylightTime(ret)) {
            Date dstDate = new Date(ret.getTime() - tz.getDSTSavings());

            // check to make sure we have not crossed back into standard time
            // this happens when we are on the cusp of DST (7pm the day before the change for PDT)
            if (tz.inDaylightTime(dstDate)) {
                ret = dstDate;
            }
        }
        return ret;
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
