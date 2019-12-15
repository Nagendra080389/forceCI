package com.controller;

import com.utils.AntExecutor;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class TestMain {

    public void execute() throws FileNotFoundException {

    }
    public static void main(String[] args) throws IOException, GitAPIException {

        Map<String, String> propertiesMap  = new HashMap<>();
        propertiesMap.put("diffDir", "C:/tempOrg/deploy");
        propertiesMap.put("diffDirUpLevel", "C:/tempOrg");
        propertiesMap.put("originURL", "https://github.com/Nagendra080389/trailheadOrgCommitLevel.git");
        propertiesMap.put("generatePackage", "C:/tempOrg/final.txt");
        propertiesMap.put("scriptName", "get_diff_branches");
        propertiesMap.put("sf.deploy.serverurl", "https://login.salesforce.com");
        propertiesMap.put("sf.deploy.username", "nagendra@deloitte.com");
        propertiesMap.put("sf.checkOnly", "true");
        propertiesMap.put("sf.pollWaitMillis", "100000");
        propertiesMap.put("sf.runAllTests", "false");
        propertiesMap.put("target", "master");
        propertiesMap.put("ghprbTargetBranch", "CEAS-12702");
        propertiesMap.put("sf.maxPoll", "100");
        propertiesMap.put("sf.deploy.sessionId", "00D7F00000027wN!AQ4AQIN4FCRiy4zEj1T5KMGCUutsjUNpHibiChvTLoOKqFyJq.Ndk.FRtlQv.14gQsocZJJLY2DGItguDI8YuHhgi6l3Hg3r");
        propertiesMap.put("sf.logType", "None");
        propertiesMap.put("targetName", "master");
        File file = ResourceUtils.getFile("classpath:build/build.xml");
        AntExecutor.executeAntTask(file.getPath(), "sf_prepare_deployment", propertiesMap);


    }
}
