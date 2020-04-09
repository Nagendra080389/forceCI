package com.controller;

import com.google.common.collect.Lists;
import com.rabbitMQ.ConsumerHandler;
import com.utils.AntExecutor;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.kohsuke.github.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class TestMain {

    public static void main(String[] args) throws Exception {

        GitHub gitHub = GitHub.connectUsingOAuth("5eb02a3f21add0175375c4d1f7748c5acec88584");
        GHRepository repositoryById = gitHub.getRepositoryById("182022017");
        List<String> lstASd= new ArrayList<>();
        for (GHCommit c : repositoryById.queryCommits().from("develop").list()){
            lstASd.add(c.getSHA1());
        }

        System.out.println(String.join(" ", lstASd));

        Map<String, String> propertiesMap = new HashMap<>();
        String uuid = String.valueOf(UUID.randomUUID());
        Path tempDirectory = Files.createTempDirectory(uuid);
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream buildXml = classLoader.getResourceAsStream("build/build.xml");
        InputStream gitCherryPick = classLoader.getResourceAsStream("build/git-multi-cherry-pick.sh");
        File buildFile = ConsumerHandler.stream2file(buildXml, "build", ".xml");
        File cherryPick = ConsumerHandler.stream2file(gitCherryPick, "git-multi-cherry-pick", ".sh");
        propertiesMap.put("gitMultiCherryPick", cherryPick.getName());
        propertiesMap.put("gitCloneURL", "https://nagendra080389:862c16dfb8a309ddd12197750bf513f8c8755cf0@github.com/Nagendra080389/trailheadOrgCommitLevel.git");
        propertiesMap.put("gitBranchName", "master");
        propertiesMap.put("gitNewBranchName", "CEAS-1234");
        propertiesMap.put("cherryPickIds", "903f59d668e62e9950cb0616d39defde10a4730c 67faf443bf96e4b51f1d5eb0f93559d8c022e482");
        propertiesMap.put("gitDirectory", tempDirectory.toFile().getPath());
        propertiesMap.put("userEmail", "nagendra080389@gmail.com");
        propertiesMap.put("userName", "nagendra080389");
        propertiesMap.put("originUserName", "nagendra080389@gmail.com");
        propertiesMap.put("originToken", "862c16dfb8a309ddd12197750bf513f8c8755cf0");
        List<String> sf_build = AntExecutor.executeAntTask(buildFile.getPath(), "git_multi_cherry_pick", propertiesMap);

        List<String> newListToBeReturned = new ArrayList<>();
        if(!sf_build.isEmpty()) {
            for (String eachString : Lists.reverse(sf_build)) {
                if(eachString.contains("**** SUCCESS ****")){
                    newListToBeReturned.add(eachString);
                    for (String eachStringAgain : Lists.reverse(sf_build)) {
                        if(eachStringAgain.contains("remote:")){
                            if(eachStringAgain.split("remote:").length > 1){
                                eachStringAgain = eachStringAgain.split("remote:")[1].trim();
                                if(StringUtils.isNotEmpty(eachStringAgain) && StringUtils.isNotBlank(eachStringAgain)) {
                                    newListToBeReturned.add(eachStringAgain);
                                }
                            }
                        }
                    }
                } else if(eachString.contains("****")){
                    newListToBeReturned.add(eachString);
                }
            }

        }
        System.out.println(newListToBeReturned);
        FileUtils.deleteDirectory(tempDirectory.toFile());
    }

    public void execute() throws FileNotFoundException {

    }
}
