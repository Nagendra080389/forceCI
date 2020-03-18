package com.rabbitMQ;

import com.controller.ForceCIController;
import com.controller.PmdReviewService;
import com.dao.DeploymentJobMongoRepository;
import com.dao.SFDCConnectionDetailsMongoRepository;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.model.GithubStatusObject;
import com.model.SFDCConnectionDetails;
import com.pmd.PMDStructure;
import com.utils.AntExecutor;
import net.sourceforge.pmd.*;
import net.sourceforge.pmd.util.ResourceLoader;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class ConsumerHandler {

    private static final Logger logger = LoggerFactory.getLogger(ConsumerHandler.class);

    private DeploymentJobMongoRepository deploymentJobMongoRepository;

    private SFDCConnectionDetailsMongoRepository sfdcConnectionDetailsMongoRepository;


    public ConsumerHandler(DeploymentJobMongoRepository deploymentJobMongoRepository,
                           SFDCConnectionDetailsMongoRepository sfdcConnectionDetailsMongoRepository) {
        this.deploymentJobMongoRepository = deploymentJobMongoRepository;
        this.sfdcConnectionDetailsMongoRepository = sfdcConnectionDetailsMongoRepository;
    }

    public void handleMessage(DeploymentJob deploymentJob) {
        System.out.println("Deployment Job Started inside Container thread -> "+deploymentJob.getId());
        Optional<DeploymentJob> optionalDeploymentJob = deploymentJobMongoRepository.findById(deploymentJob.getId());
        if (optionalDeploymentJob.isPresent()) {
            deploymentJob = optionalDeploymentJob.get();
            if(!deploymentJob.isBoolIsJobCancelled()) {
                createTempDirectoryForDeployment(deploymentJob);
            }
        }
    }

    private void createTempDirectoryForDeployment(DeploymentJob deploymentJob) {
        boolean sfdcPass = false;
        List<String> lstFileLines = new ArrayList<>();
        try {
            SFDCConnectionDetails sfdcConnectionDetail = deploymentJob.getSfdcConnectionDetail();
            String emailId = deploymentJob.getEmailId();
            String userName = deploymentJob.getUserName();
            String gitCloneURL = deploymentJob.getGitCloneURL();
            String sourceBranch = deploymentJob.getSourceBranch();
            String targetBranch = deploymentJob.getTargetBranch();
            boolean merge = deploymentJob.isBoolMerge();
            boolean jobCancelled = deploymentJob.isBoolIsJobCancelled();

            Map<String, String> propertiesMap = new HashMap<>();
            Path tempDirectory = null;
            if (merge) {
                tempDirectory = Files.createTempDirectory(targetBranch);
            } else {
                tempDirectory = Files.createTempDirectory(sourceBranch);
            }

            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            try {
                InputStream buildXml = classLoader.getResourceAsStream("build/build.xml");
                InputStream antSalesforce = classLoader.getResourceAsStream("build/ant-salesforce.jar");
                InputStream createChanges = classLoader.getResourceAsStream("build/create_changes.sh");
                InputStream generatePackageUnix = classLoader.getResourceAsStream("build/generate_package_unix.sh");
                InputStream generatePackage = classLoader.getResourceAsStream("build/generate_package.sh");
                InputStream gitClone = classLoader.getResourceAsStream("build/get_clone.sh");
                InputStream gitDiffBeforeMerge = classLoader.getResourceAsStream("build/get_diff_branches.sh");
                InputStream gitDiffAfterMerge = classLoader.getResourceAsStream("build/get_diff_commits.sh");
                InputStream propertiesHelper = classLoader.getResourceAsStream("build/properties_helper.sh");
                InputStream ruleSetsInputStream = classLoader.getResourceAsStream("xml/ruleSet.xml");
                File buildFile = stream2file(buildXml, "build", ".xml");
                File antJar = stream2file(antSalesforce, "ant-salesforce", ".jar");
                File create_changes = stream2file(createChanges, "create_changes", ".sh");
                File generate_package_unix = stream2file(generatePackageUnix, "generate_package_unix", ".sh");
                File generate_package = stream2file(generatePackage, "generate_package", ".sh");
                File get_clone = stream2file(gitClone, "get_clone", ".sh");
                File get_diff_branches = stream2file(gitDiffBeforeMerge, "get_diff_branches", ".sh");
                File get_diff_commits = stream2file(gitDiffAfterMerge, "get_diff_commits", ".sh");
                File properties_helper = stream2file(propertiesHelper, "properties_helper", ".sh");
                File ruleSet = ConsumerHandler.stream2file(ruleSetsInputStream, "ruleSet", ".xml");

                propertiesMap.put("diffDir", tempDirectory.toFile().getPath() + "/deploy");
                propertiesMap.put("diffDirUpLevel", tempDirectory.toFile().getPath());

                propertiesMap.put("generatePackage", tempDirectory.toFile().getPath() + "/final.txt");
                propertiesMap.put("gitClone", get_clone.getName());
                propertiesMap.put("create_changes", create_changes.getName());
                propertiesMap.put("generate_package", generate_package.getName());
                propertiesMap.put("originURL", gitCloneURL);
                propertiesMap.put("antPath", antJar.getPath());
                // Only run on Merge
                if (merge) {
                    propertiesMap.put("scriptName", get_diff_commits.getName());
                } else {
                    propertiesMap.put("scriptName", get_diff_branches.getName());
                }
                propertiesMap.put("generate_package_unix", generate_package_unix.getName());
                propertiesMap.put("userEmail", emailId);
                propertiesMap.put("userName", userName);
                propertiesMap.put("sf.deploy.serverurl", sfdcConnectionDetail.getInstanceURL());
                if (merge) {
                    propertiesMap.put("sf.checkOnly", "false");
                } else {
                    propertiesMap.put("sf.checkOnly", "true");
                }
                propertiesMap.put("sf.pollWaitMillis", "100000");
                propertiesMap.put("sf.runAllTests", "false");
                propertiesMap.put("target", targetBranch);
                propertiesMap.put("sourceBranch", sourceBranch);
                propertiesMap.put("sf.maxPoll", "100");
                propertiesMap.put("sf.deploy.sessionId", sfdcConnectionDetail.getOauthToken());
                propertiesMap.put("sf.logType", "None");
                propertiesMap.put("sf.testRun", sfdcConnectionDetail.getTestLevel());
                propertiesMap.put("targetName", targetBranch);

                List<String> sf_build = AntExecutor.executeAntTask(buildFile.getPath(), "sf_build", propertiesMap, deploymentJob, deploymentJobMongoRepository);
                for (String eachLine : sf_build) {
                    if (!(eachLine.startsWith("Finding class") || eachLine.startsWith("Loaded from") || eachLine.startsWith("Class ") ||
                            eachLine.startsWith("+Datatype ") || eachLine.startsWith("Note: ") || eachLine.startsWith(" +Datatype ") ||
                            eachLine.startsWith(" +Target: ") || eachLine.startsWith("Setting project") || eachLine.startsWith("Adding reference") ||
                            eachLine.startsWith("Detected ") || eachLine.startsWith("Setting ro project ") || eachLine.startsWith("Condition "))) {
                        lstFileLines.add(eachLine);
                    }
                }
                if (merge) {
                    deploymentJob.setLstDeploymentBuildLines(lstFileLines);
                } else {
                    deploymentJob.setLstBuildLines(lstFileLines);
                }
                deploymentJob = deploymentJobMongoRepository.save(deploymentJob);
                Optional<DeploymentJob> deploymentJobMongoRepositoryById = deploymentJobMongoRepository.findById(deploymentJob.getId());
                if(deploymentJobMongoRepositoryById.isPresent()){
                    System.out.println("deploymentJob isPresent -> "+deploymentJob.getId());
                    deploymentJob = deploymentJobMongoRepositoryById.get();
                }

                StringBuilder stringBuilder = new StringBuilder();
                // Iterate and extract package xml formed.
                for (int i = 0; i < sf_build.size(); i++) {
                    if (sf_build.get(i).startsWith("====FINAL PACKAGE.XML=====")) {
                        for (int j = i; j < sf_build.size(); j++) {
                            if (sf_build.get(j).startsWith("Package generated.")) {
                                break;
                            } else {
                                stringBuilder.append(sf_build.get(j)).append("\n");
                            }
                        }
                    }
                }
                deploymentJob.setPackageXML(stringBuilder.toString());
                deploymentJobMongoRepository.save(deploymentJob);

                for (String eachBuildLine : Lists.reverse(lstFileLines)) {
                    System.out.println("eachBuildLine -> "+eachBuildLine);
                    if (eachBuildLine.contains("Failed to login:")) {
                        // try to get proper access token again
                        String refreshToken = sfdcConnectionDetail.getRefreshToken();
                        String environment = sfdcConnectionDetail.getEnvironment();
                        String instanceURL = sfdcConnectionDetail.getInstanceURL();
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
                        httpClient.executeMethod(post);
                        String responseBody = IOUtils.toString(post.getResponseBodyAsStream(), StandardCharsets.UTF_8);
                        JsonParser parser = new JsonParser();
                        JsonObject jsonObject = parser.parse(responseBody).getAsJsonObject();
                        String accessToken = jsonObject.get("access_token").getAsString();
                        sfdcConnectionDetail.setOauthToken(accessToken);
                        SFDCConnectionDetails newSfdcConnection = sfdcConnectionDetailsMongoRepository.save(sfdcConnectionDetail);
                        deploymentJob.setSfdcConnectionDetail(newSfdcConnection);
                        DeploymentJob savedDeploymentJob = deploymentJobMongoRepository.save(deploymentJob);
                        createTempDirectoryForDeployment(savedDeploymentJob);
                        break;
                    } else if (eachBuildLine.contains("*********** DEPLOYMENT SUCCEEDED ***********")) {
                        System.out.println("deploymentJob Id inside deployment succeeded -> "+deploymentJob.getId());
                        System.out.println("deploymentJob status url -> "+deploymentJob.getStatusesUrl());
                        Gson gson = new Gson();
                        GithubStatusObject githubStatusObject = new GithubStatusObject(ForceCIController.SUCCESS,
                                ForceCIController.BUILD_IS_SUCCESSFUL, targetBranch + ForceCIController.VALIDATION,
                                ForceCIController.CONNECT2DEPLOY_URL + "/" +
                                        sfdcConnectionDetail.getRepoName() + "/" + sfdcConnectionDetail.getGitRepoId() + "/" + targetBranch);
                        int status = ForceCIController.createStatusAndReturnCode(gson,
                                deploymentJob.getAccess_token(), deploymentJob.getStatusesUrl(), targetBranch, githubStatusObject);
                        if (merge) {
                            deploymentJob.setBoolSfdcDeploymentRunning(false);
                            deploymentJob.setBoolSfdcDeploymentPass(true);
                        } else {
                            deploymentJob.setBoolSfdcCompleted(true);
                            deploymentJob.setBoolSfdcRunning(false);
                            deploymentJob.setBoolSfdcPass(true);
                            deploymentJob.setBoolSfdcFail(false);
                            deploymentJob.setBoolCodeReviewCompleted(false);
                        }
                        sfdcPass = true;
                        break;
                    } else if (eachBuildLine.contains("*********** DEPLOYMENT FAILED ***********")) {
                        jobCancelled = deploymentJob.isBoolIsJobCancelled();
                        if(!jobCancelled) {
                            setFailedDeploymentDetails(deploymentJob, sfdcConnectionDetail, targetBranch, merge);
                        } else {
                            deploymentJob.setBoolSfdcCompleted(true);
                            deploymentJob.setBoolSfdcRunning(false);
                            deploymentJob.setBoolSfdcFail(false);
                            deploymentJob.setBoolSfdcPass(false);
                            deploymentJob.setBoolCodeReviewCompleted(false);
                            deploymentJob.setBoolIsJobCancelled(true);
                        }
                        break;
                    }
                }

                deploymentJob.setLastModifiedDate(new Date());
                deploymentJob = deploymentJobMongoRepository.save(deploymentJob);
                if (sfdcPass && !merge && !jobCancelled) {
                    Gson gson = new Gson();
                    GithubStatusObject githubStatusObject = null;
                    int status = 0;
                    deploymentJob.setBoolCodeReviewRunning(true);
                    deploymentJob.setBoolCodeReviewNotStarted(false);
                    deploymentJob = deploymentJobMongoRepository.save(deploymentJob);
                    PMDConfiguration pmdConfiguration = new PMDConfiguration();
                    pmdConfiguration.setReportFormat("text");
                    pmdConfiguration.setRuleSets(ruleSet.getPath());
                    pmdConfiguration.setThreads(4);

                    SourceCodeProcessor sourceCodeProcessor = new SourceCodeProcessor(pmdConfiguration);
                    RuleSetFactory ruleSetFactory = RulesetsFactoryUtils.getRulesetFactory(pmdConfiguration, new ResourceLoader());
                    RuleSets ruleSets = RulesetsFactoryUtils.getRuleSetsWithBenchmark(pmdConfiguration.getRuleSets(), ruleSetFactory);

                    PmdReviewService pmdReviewService = new PmdReviewService(sourceCodeProcessor, ruleSets);

                    Iterator<File> fileIterator = FileUtils.iterateFiles(new File(tempDirectory.toFile().getPath() + "/deploy"), null, true);
                    List<PMDStructure> pmdStructures = new ArrayList<>();
                    FileInputStream fileInputStream = null;
                    while (fileIterator.hasNext()) {
                        File next = fileIterator.next();
                        fileInputStream = new FileInputStream(next);
                        try {
                            List<RuleViolation> review = pmdReviewService.review(fileInputStream, next);
                            for (RuleViolation ruleViolation : review) {
                                PMDStructure pmdStructure = new PMDStructure();
                                pmdStructure.setReviewFeedback(ruleViolation.getDescription());
                                pmdStructure.setLineNumber(ruleViolation.getBeginLine());
                                pmdStructure.setName(next.getName());
                                pmdStructure.setRuleName(ruleViolation.getRule().getName());
                                pmdStructure.setRuleUrl(ruleViolation.getRule().getExternalInfoUrl());
                                pmdStructure.setRulePriority(ruleViolation.getRule().getPriority().getPriority());
                                pmdStructures.add(pmdStructure);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            fileInputStream.close();
                        }
                    }
                    if (!pmdStructures.isEmpty()) {

                        githubStatusObject = new GithubStatusObject(ForceCIController.ERROR,
                                ForceCIController.BUILD_IS_ERROR, targetBranch + ForceCIController.CODE_REVIEW_VALIDATION,
                                ForceCIController.CONNECT2DEPLOY_URL + "/" +
                                        sfdcConnectionDetail.getRepoName() + "/" + sfdcConnectionDetail.getGitRepoId() + "/" + targetBranch);
                        status = ForceCIController.createStatusAndReturnCode(gson,
                                deploymentJob.getAccess_token(), deploymentJob.getStatusesUrl(), targetBranch, githubStatusObject);
                        System.out.println("Code Validation Pass -> " + status);


                        deploymentJob.setBoolCodeReviewRunning(false);
                        deploymentJob.setBoolCodeReviewFail(true);
                        deploymentJob.setBoolCodeReviewCompleted(true);
                        deploymentJob.setLstPmdStructures(pmdStructures);
                    } else {
                        githubStatusObject = new GithubStatusObject(ForceCIController.SUCCESS,
                                ForceCIController.BUILD_IS_SUCCESSFUL, targetBranch + ForceCIController.CODE_REVIEW_VALIDATION,
                                ForceCIController.CONNECT2DEPLOY_URL + "/" +
                                        sfdcConnectionDetail.getRepoName() + "/" + sfdcConnectionDetail.getGitRepoId() + "/" + targetBranch);
                        status = ForceCIController.createStatusAndReturnCode(gson,
                                deploymentJob.getAccess_token(), deploymentJob.getStatusesUrl(), targetBranch, githubStatusObject);
                        System.out.println("Code Validation Failed -> " + status);


                        deploymentJob.setBoolCodeReviewRunning(false);
                        deploymentJob.setBoolCodeReviewFail(false);
                        deploymentJob.setBoolCodeReviewPass(true);
                        deploymentJob.setBoolCodeReviewCompleted(true);
                    }
                    deploymentJobMongoRepository.save(deploymentJob);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                FileUtils.deleteDirectory(tempDirectory.toFile());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setFailedDeploymentDetails(DeploymentJob deploymentJob, SFDCConnectionDetails sfdcConnectionDetail, String targetBranch, boolean merge) throws IOException {
        Gson gson = new Gson();
        GithubStatusObject githubStatusObject = new GithubStatusObject(ForceCIController.ERROR,
                ForceCIController.BUILD_IS_ERROR, targetBranch + ForceCIController.VALIDATION,
                ForceCIController.CONNECT2DEPLOY_URL + "/" +
                        sfdcConnectionDetail.getRepoName() + "/" + sfdcConnectionDetail.getGitRepoId() + "/" + targetBranch);
        int status = ForceCIController.createStatusAndReturnCode(gson,
                deploymentJob.getAccess_token(), deploymentJob.getStatusesUrl(), targetBranch, githubStatusObject);
        if (merge) {
            deploymentJob.setBoolSfdcDeploymentRunning(false);
            deploymentJob.setBoolSfdcDeploymentFail(true);
        } else {
            deploymentJob.setBoolSfdcCompleted(true);
            deploymentJob.setBoolSfdcRunning(false);
            deploymentJob.setBoolSfdcFail(true);
            deploymentJob.setBoolSfdcPass(false);
            deploymentJob.setBoolCodeReviewCompleted(false);
        }
    }

    public static File stream2file(InputStream in) throws IOException {
        final File tempFile = File.createTempFile("build", ".xml");
        tempFile.deleteOnExit();
        try (OutputStream out = Files.newOutputStream(Paths.get(tempFile.toURI()))) {
            IOUtils.copy(in, out);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tempFile;
    }

    public static File stream2file(InputStream in, String prefix, String suffix) throws IOException {
        final File tempFile = File.createTempFile(prefix, suffix);
        tempFile.deleteOnExit();
        try (OutputStream out = Files.newOutputStream(Paths.get(tempFile.toURI()))) {
            IOUtils.copy(in, out);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tempFile;
    }
}
