package com.controller;

import com.utils.AntExecutor;
import com.utils.BuildUtils;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class TestMain {

    public void execute() throws FileNotFoundException {

    }
    public static void main(String[] args) throws IOException, GitAPIException {

        Map<String, String> propertiesMap  = new HashMap<>();
        Path tempDirectory = Files.createTempDirectory("TestMe");
        System.out.println(tempDirectory.getParent());
        System.out.println(tempDirectory.toFile().getPath());

        //AntExecutor.executeAntTask(file.getPath(), "sf_prepare_deployment", propertiesMap);
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
            File buildFile = BuildUtils.stream2file(buildXml, "build", ".xml");
            System.out.println(buildFile.getPath());
            File antJar = BuildUtils.stream2file(antSalesforce, "ant-salesforce", ".jar");
            File create_changes = BuildUtils.stream2file(createChanges, "create_changes", ".sh");
            File generate_package_unix = BuildUtils.stream2file(generatePackageUnix, "generate_package_unix", ".sh");
            File generate_package = BuildUtils.stream2file(generatePackage, "generate_package", ".sh");
            File get_clone = BuildUtils.stream2file(gitClone, "get_clone", ".sh");
            File get_diff_branches = BuildUtils.stream2file(gitDiffBeforeMerge, "get_diff_branches", ".sh");
            File get_diff_commits = BuildUtils.stream2file(gitDiffAfterMerge, "get_diff_commits", ".sh");
            File properties_helper = BuildUtils.stream2file(propertiesHelper, "properties_helper", ".sh");


            propertiesMap.put("diffDir", tempDirectory.toFile().getPath()+"/deploy");
            propertiesMap.put("diffDirUpLevel", tempDirectory.toFile().getPath());
            propertiesMap.put("originURL", "https://github.com/Nagendra080389/trailheadOrgCommitLevel.git");
            propertiesMap.put("generatePackage", tempDirectory.toFile().getPath()+"/final.txt");
            propertiesMap.put("scriptName", get_diff_branches.getName());
            propertiesMap.put("gitClone", get_clone.getName());
            propertiesMap.put("create_changes", create_changes.getName());
            propertiesMap.put("generate_package", generate_package.getName());

            // Only run on Merge
            propertiesMap.put("get_diff_commits", get_diff_commits.getName());

            propertiesMap.put("generate_package_unix", generate_package_unix.getName());
            propertiesMap.put("sf.deploy.serverurl", "https://login.salesforce.com");
            propertiesMap.put("sf.deploy.username", "nagendra@deloitte.com");
            propertiesMap.put("sf.checkOnly", "true");
            propertiesMap.put("sf.pollWaitMillis", "100000");
            propertiesMap.put("sf.runAllTests", "false");
            propertiesMap.put("target", "master");
            propertiesMap.put("sourceBranch", "CEAS-12702");
            propertiesMap.put("sf.maxPoll", "100");
            propertiesMap.put("sf.deploy.sessionId", "00D7F00000027wN!******.Ndk.FRtlQv.14gQsocZJJLY2DGItguDI8YuHhgi6l3Hg3r");
            propertiesMap.put("sf.logType", "None");
            propertiesMap.put("targetName", "master");


            AntExecutor.executeAntTask(buildFile.getPath(), "sf_prepare_deployment", propertiesMap);
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            FileUtils.deleteDirectory(tempDirectory.toFile());
        }

        FileUtils.deleteDirectory(tempDirectory.toFile());


    }
}
