package com.rabbitMQ;

import com.dao.DeploymentJobMongoRepository;
import com.dao.SFDCConnectionDetailsMongoRepository;
import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.model.SFDCConnectionDetails;
import com.utils.AntExecutor;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class ConsumerHandler {

    private DeploymentJobMongoRepository deploymentJobMongoRepository;

    private SFDCConnectionDetailsMongoRepository sfdcConnectionDetailsMongoRepository;

    public ConsumerHandler(DeploymentJobMongoRepository deploymentJobMongoRepository, SFDCConnectionDetailsMongoRepository sfdcConnectionDetailsMongoRepository) {
        this.deploymentJobMongoRepository = deploymentJobMongoRepository;
        this.sfdcConnectionDetailsMongoRepository = sfdcConnectionDetailsMongoRepository;
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

    public void handleMessage(DeploymentJob deploymentJob) {
        Optional<DeploymentJob> optionalDeploymentJob = deploymentJobMongoRepository.findById(deploymentJob.getId());
        if (optionalDeploymentJob.isPresent()) {
            deploymentJob = optionalDeploymentJob.get();
            createTempDirectoryForDeployment(deploymentJob);
            try {
                DeploymentJob deploymentJobWithoutLogs = optionalDeploymentJob.get();
                deploymentJobWithoutLogs.setLstBuildLines(new ArrayList<>());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private List<String> createTempDirectoryForDeployment(DeploymentJob deploymentJob) {
        List<String> lstFileLines = new ArrayList<>();
        try {
            SFDCConnectionDetails sfdcConnectionDetail = deploymentJob.getSfdcConnectionDetail();
            String emailId = deploymentJob.getEmailId();
            String userName = deploymentJob.getUserName();
            String gitCloneURL = deploymentJob.getGitCloneURL();
            String sourceBranch = deploymentJob.getSourceBranch();
            String targetBranch = deploymentJob.getTargetBranch();

            Map<String, String> propertiesMap = new HashMap<>();
            Path tempDirectory = Files.createTempDirectory(sourceBranch);

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
                File buildFile = stream2file(buildXml, "build", ".xml");
                File antJar = stream2file(antSalesforce, "ant-salesforce", ".jar");
                File create_changes = stream2file(createChanges, "create_changes", ".sh");
                File generate_package_unix = stream2file(generatePackageUnix, "generate_package_unix", ".sh");
                File generate_package = stream2file(generatePackage, "generate_package", ".sh");
                File get_clone = stream2file(gitClone, "get_clone", ".sh");
                File get_diff_branches = stream2file(gitDiffBeforeMerge, "get_diff_branches", ".sh");
                File get_diff_commits = stream2file(gitDiffAfterMerge, "get_diff_commits", ".sh");
                File properties_helper = stream2file(propertiesHelper, "properties_helper", ".sh");

                propertiesMap.put("diffDir", tempDirectory.toFile().getPath() + "/deploy");
                propertiesMap.put("diffDirUpLevel", tempDirectory.toFile().getPath());

                propertiesMap.put("generatePackage", tempDirectory.toFile().getPath() + "/final.txt");
                propertiesMap.put("scriptName", get_diff_branches.getName());
                propertiesMap.put("gitClone", get_clone.getName());
                propertiesMap.put("create_changes", create_changes.getName());
                propertiesMap.put("generate_package", generate_package.getName());
                propertiesMap.put("originURL", gitCloneURL);
                propertiesMap.put("antPath", antJar.getPath());
                // Only run on Merge
                propertiesMap.put("get_diff_commits", get_diff_commits.getName());
                propertiesMap.put("generate_package_unix", generate_package_unix.getName());
                propertiesMap.put("userEmail", emailId);
                propertiesMap.put("userName", userName);
                propertiesMap.put("sf.deploy.serverurl", sfdcConnectionDetail.getInstanceURL());
                propertiesMap.put("sf.checkOnly", "true");
                propertiesMap.put("sf.pollWaitMillis", "100000");
                propertiesMap.put("sf.runAllTests", "false");
                propertiesMap.put("target", targetBranch);
                propertiesMap.put("sourceBranch", sourceBranch);
                propertiesMap.put("sf.maxPoll", "100");
                propertiesMap.put("sf.deploy.sessionId", sfdcConnectionDetail.getOauthToken());
                propertiesMap.put("sf.logType", "None");
                propertiesMap.put("sf.testRun", "NoTestRun");
                propertiesMap.put("targetName", targetBranch);

                List<String> sf_build = AntExecutor.executeAntTask(buildFile.getPath(), "sf_build", propertiesMap);
                for (String eachLine : sf_build) {
                    if (!(eachLine.startsWith("Finding class") || eachLine.startsWith("Loaded from") || eachLine.startsWith("Class ") ||
                            eachLine.startsWith("+Datatype ") || eachLine.startsWith("Note: ") || eachLine.startsWith(" +Datatype ") ||
                            eachLine.startsWith(" +Target: ") || eachLine.startsWith("Setting project") || eachLine.startsWith("Adding reference") ||
                            eachLine.startsWith("Detected ") || eachLine.startsWith("Setting ro project ") || eachLine.startsWith("Condition "))) {
                        lstFileLines.add(eachLine);
                    }
                }
                deploymentJob.setLstBuildLines(lstFileLines);
                for (String eachBuildLine : Lists.reverse(lstFileLines)) {
                    System.out.println("eachBuildLine -> " + eachBuildLine);
                    if (eachBuildLine.contains("Failed to login: INVALID_SESSION_ID")) {
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
                        deploymentJob.setBoolSfdcCompleted(true);
                        deploymentJob.setBoolSfdcPass(true);
                        deploymentJob.setBoolCodeReviewCompleted(false);
                        break;
                    } else if (eachBuildLine.contains("*********** DEPLOYMENT FAILED ***********")) {
                        deploymentJob.setBoolSfdcCompleted(true);
                        deploymentJob.setBoolSfdcFail(false);
                        deploymentJob.setBoolCodeReviewCompleted(false);
                        break;
                    }
                }

                deploymentJob.setLastModifiedDate(new Date());
                deploymentJobMongoRepository.save(deploymentJob);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                FileUtils.deleteDirectory(tempDirectory.toFile());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lstFileLines;
    }
}
