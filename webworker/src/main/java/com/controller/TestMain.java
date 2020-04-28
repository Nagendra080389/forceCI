package com.controller;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.UploadObjectRequest;
import com.amazonaws.services.s3.transfer.MultipleFileUpload;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.google.common.collect.Lists;
import com.model.ObjectFactory;
import com.model.PackageType;
import com.model.TypesType;
import com.rabbitMQ.ConsumerHandler;
import com.sforce.soap.metadata.*;
import com.sforce.ws.ConnectorConfig;
import com.utils.AntExecutor;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.joda.time.DateTime;
import org.kohsuke.github.*;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.lang.Package;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static com.rabbitMQ.ConsumerHandler.stream2file;
import static org.quartz.SimpleScheduleBuilder.repeatSecondlyForever;
import static org.quartz.TriggerBuilder.newTrigger;

public class TestMain {

    // manifest file that controls which components get retrieved
    private static final String MANIFEST_FILE = "package.xml";

    private static final double API_VERSION = 31.0;

    public static void main(String[] args) throws Exception {

        /*Map<String, String> propertiesMap = new HashMap<>();
        Path tempDirectory = null;
        tempDirectory = Files.createTempDirectory("CEAS-4343");

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try {
            InputStream buildXml = classLoader.getResourceAsStream("build/build.xml");
            InputStream antSalesforce = classLoader.getResourceAsStream("build/ant-salesforce.jar");
            InputStream createChanges = classLoader.getResourceAsStream("build/create_changes.sh");
            InputStream generatePackageUnix = classLoader.getResourceAsStream("build/generate_package_unix.sh");
            InputStream generatePackageDes = classLoader.getResourceAsStream("build/generate_package_des.sh");
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
            File generate_package_des = stream2file(generatePackageDes, "generate_package_des", ".sh");
            File get_clone = stream2file(gitClone, "get_clone", ".sh");
            File get_diff_branches = stream2file(gitDiffBeforeMerge, "get_diff_branches", ".sh");
            File get_diff_commits = stream2file(gitDiffAfterMerge, "get_diff_commits", ".sh");
            File properties_helper = stream2file(propertiesHelper, "properties_helper", ".sh");
            propertiesMap.put("diffDir", tempDirectory.toFile().getPath() + "/deploy");
            propertiesMap.put("diffDirUpLevel", tempDirectory.toFile().getPath());

            propertiesMap.put("generatePackage", tempDirectory.toFile().getPath() + "/final.txt");
            propertiesMap.put("destructiveGeneratePackage", tempDirectory.toFile().getPath() + "/destructive.txt");
            propertiesMap.put("gitClone", get_clone.getName());
            propertiesMap.put("create_changes", create_changes.getName());
            propertiesMap.put("generate_package", generate_package.getName());
            propertiesMap.put("generate_package_des", generate_package_des.getName());
            propertiesMap.put("originURL", "https://github.com/Nagendra080389/trailheadOrgCommitLevel.git");
            propertiesMap.put("antPath", antJar.getPath());
            propertiesMap.put("scriptName", get_diff_branches.getName());
            propertiesMap.put("generate_package_unix", generate_package_unix.getName());
            propertiesMap.put("userEmail", "nagendra080389@gmail.com");
            propertiesMap.put("userName", "nagendra080389");
            propertiesMap.put("sf.deploy.serverurl", "https://nagesingh-dev-ed.my.salesforce.com");
            propertiesMap.put("sf.checkOnly", "false");
            propertiesMap.put("sf.pollWaitMillis", "100000");
            propertiesMap.put("sf.runAllTests", "false");
            propertiesMap.put("target", "develop");
            propertiesMap.put("sourceBranch", "CEAS-4343");
            propertiesMap.put("sf.maxPoll", "100");
            propertiesMap.put("sf.deploy.sessionId", "00D7F00000027wN!AQ4AQN8JYQoM.Q8.UQplzdP6nXXWWHtFdzOWSlBzXjWLzteenJp3j1Is52EHz5IMVkbqKM.9uXEXaefiNr1MPKwgtR1oqv1h");
            propertiesMap.put("sf.logType", "None");
            propertiesMap.put("sf.testRun", "NoTestRun");
            propertiesMap.put("targetName", "develop");

            List<String> sf_build = AntExecutor.executeAntTask(buildFile.getPath(), "sf_build", propertiesMap);
            System.out.println(sf_build);
        }catch (Exception e){

        }*/

        /*System.out.println(DigestUtils.sha1Hex("00D7F00000027wNUAQ"));*/

        /*ConnectorConfig connectorConfig = new ConnectorConfig();
        connectorConfig.setUsername("nagendra@deloitte.com");
        connectorConfig.setSessionId("00D7F00000027wN!AQ4AQARtqNtazXGcGDkQ91Q9P7BbSYQ_0hNzOTSdEc5Xnh0of2E.pn_p5X4_w3DG2v4Ze_Q3bktUVbEPdZvkinoUUZPRR_3d");
        connectorConfig.setServiceEndpoint("https://nagesingh-dev-ed.my.salesforce.com/services/Soap/m/46.0");
        MetadataConnection metadataConnection = new MetadataConnection(connectorConfig);
        DescribeMetadataResult describeMetadataResult = metadataConnection.describeMetadata(48.0);
        ObjectFactory objectFactory = new ObjectFactory();
        PackageType packageType1 = new ObjectFactory().createPackageType();
        packageType1.setVersion("48.0");
        for (DescribeMetadataObject metadataObject : describeMetadataResult.getMetadataObjects()) {
            System.out.println(metadataObject.getXmlName());
            TypesType typesType = objectFactory.createTypesType();
            typesType.setName(metadataObject.getXmlName());
            typesType.getMembers().add("*");
            packageType1.getTypes().add(typesType);
        }
        JAXBElement<PackageType> aPackage = objectFactory.createPackage(packageType1);
        String generatedPackageXML = jaxbObjectToXML(packageType1, aPackage);*/


        /*TransferManagerBuilder standard = TransferManagerBuilder.standard();
        standard.setS3Client(s3client);
        TransferManager build = standard.build();
        MultipleFileUpload upload = build.uploadDirectory(bucketName,"BuildNumber#1", "FilePathYouWant", true)*/;
        //Bucket bucketOnS3 = s3client.createBucket(bucketName);
        //System.out.println(bucketOnS3);





        /*Path tempDirectory  = Files.createTempDirectory(jobId);
        Path fileToCreatePath = tempDirectory.resolve("package.xml");
        Path folderForUnmanagedPackage = tempDirectory.resolve("trailhead");
        File packageTempFile = Files.createFile(fileToCreatePath).toFile();
        File trailHeadDirectory = Files.createDirectory(folderForUnmanagedPackage).toFile();

        packageTempFile.deleteOnExit();
        //write it
        BufferedWriter bw = new BufferedWriter(new FileWriter(packageTempFile));
        bw.write(generatedPackageXML);
        bw.close();*/

        /*ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream buildXml = classLoader.getResourceAsStream("build/build.xml");
        InputStream antSalesforce = classLoader.getResourceAsStream("build/ant-salesforce.jar");
        File antJar = ConsumerHandler.stream2file(antSalesforce, "ant-salesforce", ".jar");
        File buildFile = ConsumerHandler.stream2file(buildXml, "build", ".xml");
        Map<String, String> propertiesMap = new HashMap<>();
        propertiesMap.put("sf.username", "nagendra@deloitte.com");
        propertiesMap.put("sf.sessionId", "00D7F00000027wN!AQ4AQARtqNtazXGcGDkQ91Q9P7BbSYQ_0hNzOTSdEc5Xnh0of2E.pn_p5X4_w3DG2v4Ze_Q3bktUVbEPdZvkinoUUZPRR_3d");
        propertiesMap.put("sf.serverurl", "https://nagesingh-dev-ed.my.salesforce.com");
        propertiesMap.put("sf.retrieveTarget", trailHeadDirectory.getPath());
        propertiesMap.put("sf.unpackaged", packageTempFile.getPath());
        propertiesMap.put("sf.maxPoll", "100");
        propertiesMap.put("antPath", antJar.getPath());
        List<String> sf_build = AntExecutor.executeAntTask(buildFile.getPath(), "retrievePkg", propertiesMap);*/
        /*Path tempDirectory = Files.createTempDirectory("master");
        Map<String, String> propertiesMap = new HashMap<>();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream buildXml = classLoader.getResourceAsStream("build/build.xml");
        InputStream gitCherryPick = classLoader.getResourceAsStream("build/git-multi-cherry-pick.sh");
        File buildFile = ConsumerHandler.stream2file(buildXml, "build", ".xml");
        File cherryPick = ConsumerHandler.stream2file(gitCherryPick, "git-multi-cherry-pick", ".sh");
        propertiesMap.put("gitMultiCherryPick", cherryPick.getName());
        propertiesMap.put("gitCloneURL", "https://" + "Nagendra080389" + ":" + "e538b55842d3970b93aae6ddb478c03dfdc0bceb" + "@github.com/" +
                "Nagendra080389"+"/"+"trailHeadGitRepo"+".git");
        propertiesMap.put("gitBranchName", "master");
        propertiesMap.put("gitNewBranchName", "US-003345");
        propertiesMap.put("cherryPickIds", "c1760ce3984feb8242a511a685a66ff025252055");
        propertiesMap.put("gitDirectory", tempDirectory.toFile().getPath());
        propertiesMap.put("userEmail", "nagendra080389@gmail.com");
        propertiesMap.put("userName", "Nagendra080389");
        AntExecutor.executeAntTask(buildFile.getPath(), "git_multi_cherry_pick", propertiesMap);
        FileUtils.deleteDirectory(tempDirectory.toFile());*/

        String strBranch = "Test_Uni123";
        GitHub gitHub = GitHub.connectUsingOAuth("");
        GHRepository trailHeadGitRepo = gitHub.getRepositoryById("256012073");
        String develop = trailHeadGitRepo.getBranch("develop").getSHA1();
        GHRef ref = trailHeadGitRepo.createRef("refs/heads/"+strBranch, develop);
        GHPullRequest pullRequest = trailHeadGitRepo.createPullRequest("test_Uni", strBranch, "master", "Test123");
        System.out.println(pullRequest);
    }

    public static class HelloJob implements Job {

        @Override
        public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
            JobDataMap jobDataMap = jobExecutionContext.getJobDetail().getJobDataMap();
            Object jobId = jobDataMap.get("JobId");
            System.out.println(jobId);
            System.out.println("HelloJob executed");
        }
    }


    /*
    private void setUnpackaged(RetrieveRequest request) throws Exception {
        // Edit the path, if necessary, if your package.xml file is located elsewhere
        File unpackedManifest = new File(MANIFEST_FILE);
        System.out.println("Manifest file: " + unpackedManifest.getAbsolutePath());

        if (!unpackedManifest.exists() || !unpackedManifest.isFile())
            throw new Exception("Should provide a valid retrieve manifest " +
                    "for unpackaged content. " +
                    "Looking for " + unpackedManifest.getAbsolutePath());

        // Note that we populate the _package object by parsing a manifest file here.
        // You could populate the _package based on any source for your
        // particular application.
        com.sforce.soap.metadata.Package p = parsePackage(unpackedManifest);
        request.setUnpackaged(p);
    }

    private com.sforce.soap.metadata.Package parsePackage(File file) throws Exception {
        try {
            InputStream is = new FileInputStream(file);
            List<PackageTypeMembers> pd = new ArrayList<PackageTypeMembers>();
            DocumentBuilder db =
                    DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Element d = db.parse(is).getDocumentElement();
            for (Node c = d.getFirstChild(); c != null; c = c.getNextSibling()) {
                if (c instanceof Element) {
                    Element ce = (Element) c;
                    //
                    NodeList namee = ce.getElementsByTagName("name");
                    if (namee.getLength() == 0) {
                        // not
                        continue;
                    }
                    String name = namee.item(0).getTextContent();
                    NodeList m = ce.getElementsByTagName("members");
                    List<String> members = new ArrayList<String>();
                    for (int i = 0; i < m.getLength(); i++) {
                        Node mm = m.item(i);
                        members.add(mm.getTextContent());
                    }
                    PackageTypeMembers pdi = new PackageTypeMembers();
                    pdi.setName(name);
                    pdi.setMembers(members.toArray(new String[members.size()]));
                    pd.add(pdi);
                }
            }
            com.sforce.soap.metadata.Package r = new com.sforce.soap.metadata.Package();
            r.setTypes(pd.toArray(new PackageTypeMembers[pd.size()]));
            r.setVersion(API_VERSION + "");
            return r;
        } catch (ParserConfigurationException pce) {
            throw new Exception("Cannot create XML parser", pce);
        } catch (IOException ioe) {
            throw new Exception(ioe);
        } catch (SAXException se) {
            throw new Exception(se);
        }
    }


    private static String jaxbObjectToXML(PackageType employee, JAXBElement<PackageType> packageTypeJAXBElement) {

        String xmlContent = "";
        try {
            //Create JAXB Context
            JAXBContext jaxbContext = JAXBContext.newInstance(ObjectFactory.class);

            //Create Marshaller
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

            //Required formatting??
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

            //Print XML String to Console
            StringWriter sw = new StringWriter();

            //Write XML to StringWriter
            jaxbMarshaller.marshal(packageTypeJAXBElement, sw);

            //Verify XML Content
            xmlContent = sw.toString();

        } catch (JAXBException e) {
            e.printStackTrace();
        }

        return xmlContent;
    }*/


}
