package com.utils;

import org.apache.commons.io.IOUtils;
import org.apache.tools.ant.*;
import org.springframework.util.StringUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AntExecutor {
    /**
     * To execute the default target specified in the Ant build.xml file
     *
     * @param buildXmlFileFullPath
     */
    public static List<String> executeAntTask(String buildXmlFileFullPath) throws IOException {
        return executeAntTask(buildXmlFileFullPath, null, null);
    }

    /**
     * To execute a target specified in the Ant build.xml file
     *
     * @param buildXmlFileFullPath
     * @param target
     */
    public static List<String> executeAntTask(String buildXmlFileFullPath, String target, Map<String, String> propertiesMap) throws IOException {
        boolean success = false;

        List<String> consoleLogs = new ArrayList<>();
        // Prepare Ant project
        Project project = new Project();
        File buildFile = new File(buildXmlFileFullPath);
        project.setUserProperty("ant.file", buildFile.getAbsolutePath());

        if(propertiesMap != null && !propertiesMap.isEmpty()) {
            for (Map.Entry<String, String> entrySet : propertiesMap.entrySet()) {
                project.setProperty(entrySet.getKey(), entrySet.getValue());
            }
        }

        /*project.addBuildListener(new BuildListener() {
            @Override
            public void buildStarted(BuildEvent buildEvent) {

            }

            @Override
            public void buildFinished(BuildEvent buildEvent) {
                if(buildEvent.getException()  != null && StringUtils.hasText(buildEvent.getException().getMessage())){
                    consoleLogs.add(buildEvent.getException().getMessage());
                }
            }

            @Override
            public void targetStarted(BuildEvent buildEvent) {

            }

            @Override
            public void targetFinished(BuildEvent buildEvent) {

            }

            @Override
            public void taskStarted(BuildEvent buildEvent) {

            }

            @Override
            public void taskFinished(BuildEvent buildEvent) {

            }

            @Override
            public void messageLogged(BuildEvent buildEvent) {
                if(StringUtils.hasText(buildEvent.getMessage())) {
                    consoleLogs.add(buildEvent.getMessage());
                }
            }
        });*/

        project.addBuildListener(getConsoleLogger());

        // Capture event for Ant script build start / stop / failure
        try {
            project.fireBuildStarted();
            project.init();
            ProjectHelper projectHelper = ProjectHelper.getProjectHelper();
            project.addReference("ant.projectHelper", projectHelper);
            projectHelper.parse(project, buildFile);

            // If no target specified then default target will be executed.
            String targetToExecute = (target != null && target.trim().length() > 0) ? target.trim() : project.getDefaultTarget();
            project.executeTarget(targetToExecute);
            project.fireBuildFinished(null);
        } catch (BuildException buildException) {
            project.fireBuildFinished(buildException);
        }

        return consoleLogs;
    }

    private static DefaultLogger getConsoleLogger() {
        DefaultLogger consoleLogger = new DefaultLogger();
        consoleLogger.setErrorPrintStream(System.err);
        consoleLogger.setOutputPrintStream(System.out);
        consoleLogger.setMessageOutputLevel(Project.MSG_INFO);

        return consoleLogger;
    }


}
