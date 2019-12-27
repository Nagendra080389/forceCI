package com.rabbitMQ;

import com.model.SFDCConnectionDetails;
import com.utils.AntExecutor;
import com.utils.BuildUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.socket.TextMessage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class ConsumerHandler {
    public void handleMessage(DeploymentJob deploymentJob) {
        System.out.println("deploymentJob -> "+deploymentJob);
        createTempDirectoryForDeployment(deploymentJob.getAccess_token(), deploymentJob.getSfdcConnectionDetail(),
                deploymentJob.getEmailId(), deploymentJob.getUserName(), deploymentJob.getGitCloneURL(), deploymentJob.getSourceBranch(), deploymentJob.getTargetBranch());
        try {
            System.out.println("deploymentJob.getSocketHandler().getSessions() -> "+deploymentJob.getSocketHandler().getSessions());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void createTempDirectoryForDeployment(String access_token, SFDCConnectionDetails sfdcConnectionDetail, String emailId, String userName, String gitCloneURL, String sourceBranch, String targetBranch) {
        try {

            System.out.println("inside start_deployment -> " + access_token);
            System.out.println("inside start_deployment -> " + sfdcConnectionDetail);
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
                System.out.println(buildFile.getPath());
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
                propertiesMap.put("targetName", targetBranch);

                AntExecutor.executeAntTask(buildFile.getPath(), "sf_build", propertiesMap);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                FileUtils.deleteDirectory(tempDirectory.toFile());
            }
        } catch (IOException e) {
            e.printStackTrace();
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
