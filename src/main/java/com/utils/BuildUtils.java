package com.utils;

import com.offbytwo.jenkins.JenkinsServer;
import com.offbytwo.jenkins.model.Job;
import com.offbytwo.jenkins.model.JobWithDetails;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

public class BuildUtils {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(BuildUtils.class);

    public static void main(String[] args) throws IOException, URISyntaxException, JAXBException {

        JenkinsServer jenkins = new JenkinsServer(new URI("http://localhost:8080/"), "nagesingh", "Developer!8");
        Map<String, Job> jobs = jenkins.getJobs();


        ClassLoader classLoader1 = Thread.currentThread().getContextClassLoader();
        InputStream resourceAsStream1 = classLoader1.getResourceAsStream("config.xml");
        String xmlNew = IOUtils.toString(resourceAsStream1);
        ConvertXmlToObjects convertXmlToObjects = new ConvertXmlToObjects();
        convertXmlToObjects.convertToObjects(xmlNew);
        jenkins.createJob("JobFromJava", xmlNew, true);

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream resourceAsStream = classLoader.getResourceAsStream("build/build.xml");
        String ruleSetFilePath = "";
        if (resourceAsStream != null) {
            File file = null;
            try {
                file = stream2file(resourceAsStream);
            } catch (IOException e) {
                LOGGER.error("Exception while converting file: " + e.getMessage());
            } finally {
                resourceAsStream.close();
            }
            ruleSetFilePath = file != null ? file.getPath() : null;

        }

        File source = new File(ruleSetFilePath);
        File dest = new File("H:\\work-temp\\file2");
        try {
            FileUtils.copyDirectory(source, dest);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Running default target of ant script
        //executeAntTask(ruleSetFilePath);

        // Running specified target of ant script
        executeAntTask(ruleSetFilePath, "sf_build");
    }

    /**
     * To execute the default target specified in the Ant build.xml file
     *
     * @param buildXmlFileFullPath
     * @return boolean
     */
    public static boolean executeAntTask(String buildXmlFileFullPath) {
        return executeAntTask(buildXmlFileFullPath, null);
    }

    /**
     * To execute a target specified in the Ant build.xml file
     *
     * @param buildXmlFileFullPath
     * @param target
     * @return
     */
    public static boolean executeAntTask(String buildXmlFileFullPath, String target) {
        boolean success = false;
        DefaultLogger consoleLogger = getConsoleLogger();

        // Prepare Ant project
        Project project = new Project();
        File buildFile = new File(buildXmlFileFullPath);
        project.setUserProperty("ant.file", buildFile.getAbsolutePath());
        project.addBuildListener(consoleLogger);
        // Capture event for Ant script build start / stop / failure
        try {
            project.fireBuildStarted();
            project.init();
            ProjectHelper projectHelper = ProjectHelper.getProjectHelper();
            project.addReference("ant.projectHelper", projectHelper);
            projectHelper.parse(project, buildFile);
            // If no target specified then default target will be executed.
            String targetToExecute = (target != null && target.trim().length() > 0) ? target.trim() : project.getDefaultTarget();
            project.setProperty("diffDir", ".\\deploy");
            project.executeTarget(targetToExecute);
            project.fireBuildFinished(null);
            success = true;
        } catch (BuildException buildException) {
            project.fireBuildFinished(buildException);
            throw new RuntimeException("!!! Unable to restart the IEHS App !!!", buildException);
        }
        return success;
    }

    /**
     * Logger to log output generated while executing ant script in console
     *
     * @return
     */
    private static DefaultLogger getConsoleLogger() {
        DefaultLogger consoleLogger = new DefaultLogger();
        consoleLogger.setErrorPrintStream(System.err);
        consoleLogger.setOutputPrintStream(System.out);
        consoleLogger.setMessageOutputLevel(Project.MSG_INFO);
        return consoleLogger;
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
}
