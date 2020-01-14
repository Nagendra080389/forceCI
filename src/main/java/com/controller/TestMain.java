package com.controller;

import com.rabbitMQ.ConsumerHandler;
import com.utils.AntExecutor;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class TestMain {

    public static void main(String[] args) throws IOException, GitAPIException {

        Map<String, String> propertiesMap = new HashMap<>();
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
            File buildFile = ConsumerHandler.stream2file(buildXml, "build", ".xml");
            System.out.println(buildFile.getPath());
            File antJar = ConsumerHandler.stream2file(antSalesforce, "ant-salesforce", ".jar");
            File create_changes = ConsumerHandler.stream2file(createChanges, "create_changes", ".sh");
            File generate_package_unix = ConsumerHandler.stream2file(generatePackageUnix, "generate_package_unix", ".sh");
            File generate_package = ConsumerHandler.stream2file(generatePackage, "generate_package", ".sh");
            File get_clone = ConsumerHandler.stream2file(gitClone, "get_clone", ".sh");
            File get_diff_branches = ConsumerHandler.stream2file(gitDiffBeforeMerge, "get_diff_branches", ".sh");
            File get_diff_commits = ConsumerHandler.stream2file(gitDiffAfterMerge, "get_diff_commits", ".sh");
            File properties_helper = ConsumerHandler.stream2file(propertiesHelper, "properties_helper", ".sh");


            propertiesMap.put("diffDir", tempDirectory.toFile().getPath() + "/deploy");
            propertiesMap.put("diffDirUpLevel", tempDirectory.toFile().getPath());
            propertiesMap.put("originURL", "https://github.com/Nagendra080389/trailheadOrgCommitLevel.git");
            propertiesMap.put("generatePackage", tempDirectory.toFile().getPath() + "/final.txt");
            propertiesMap.put("scriptName", get_diff_branches.getName());
            propertiesMap.put("gitClone", get_clone.getName());
            propertiesMap.put("create_changes", create_changes.getName());
            propertiesMap.put("generate_package", generate_package.getName());
            propertiesMap.put("antPath", antJar.getPath());

            // Only run on Merge
            propertiesMap.put("get_diff_commits", get_diff_commits.getName());

            propertiesMap.put("generate_package_unix", generate_package_unix.getName());
            propertiesMap.put("sf.deploy.serverurl", "https://nagesingh-dev-ed.my.salesforce.com");
            propertiesMap.put("sf.checkOnly", "true");
            propertiesMap.put("sf.pollWaitMillis", "100000");
            propertiesMap.put("sf.runAllTests", "false");
            propertiesMap.put("target", "master");
            propertiesMap.put("sourceBranch", "Nagendra080389-patch-9");
            propertiesMap.put("sf.maxPoll", "100");
            propertiesMap.put("sf.deploy.sessionId", "00D7F00000027wN!AQ4AQIN4FCRiy4zEj1T5KMGCUutsjUNpHibiChvTLoOKqFyJq.Ndk.FRtlQv.14gQsocZJJLY2DGItguDI8YuHhgi6l3Hg3r");
            propertiesMap.put("sf.logType", "None");
            propertiesMap.put("targetName", "master");

            List<String> allReadLines = new ArrayList<String>();
            try {


                BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

                AntExecutor.executeAntTask(buildFile.getPath(), "sf_build", propertiesMap);
                String s;
                while ((s = in.readLine()) != null && s.length() != 0) {
                    allReadLines.add(s);
                }
            } catch (Exception e) {

            }


            Stream<String> stream = allReadLines.stream();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            FileUtils.deleteDirectory(tempDirectory.toFile());
        }

        FileUtils.deleteDirectory(tempDirectory.toFile());


    }

    public void execute() throws FileNotFoundException {

    }
}
