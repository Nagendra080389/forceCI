package com.commonsRabbitMQ;

import com.backgroundworker.quartzJob.*;
import com.codecoverage.SFDCCodeCoverage;
import com.codecoverage.SFDCCodeCoverageDetails;
import com.codecoverage.SFDCCodeCoverageOrg;
import com.model.LinkedServices;
import com.model.SFDCConnectionDetails;
import com.sforce.soap.tooling.*;
import com.sforce.soap.tooling.fault.ExceptionCode;
import com.sforce.soap.tooling.fault.UnexpectedErrorFault;
import com.sforce.soap.tooling.sobject.ApexCodeCoverageAggregate;
import com.sforce.soap.tooling.sobject.ApexOrgWideCoverage;
import com.sforce.soap.tooling.sobject.Name;
import com.sforce.soap.tooling.sobject.SObject;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;
import com.utils.SFDCUtilsCommons;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GHRef;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.net.URL;
import java.util.*;

import static com.backgroundworker.quartzJob.SchedulerConfig.*;

@Component
public class ScheduledRabbitMQHandler {

    private static final Logger logger = LoggerFactory.getLogger(ScheduledRabbitMQHandler.class);
    public static final String GIT_HUB = "GitHub";
    public static final String GIT_HUB_ENTERPRISE = "GitHub Enterprise";

    @Autowired
    private SFDCScheduledConnectionDetailsMongoRepository sfdcConnectionDetailsMongoRepository;
    @Autowired
    private ScheduledDeploymentMongoRepository scheduledDeploymentMongoRepository;
    @Autowired
    private ScheduledLinkedServicesMongoRepository scheduledLinkedServicesMongoRepository;
    @Autowired
    private SFDCCodeCoverageOrgMongoRepository sfdcCodeCoverageOrgMongoRepository;

    @Value("${salesforce.toolingEndpoint}")
    private String toolingAPIEndpoint;

    @RabbitListener(queues = SCHEDULED_QUEUE_NAME)
    public void receivedMessage(ScheduledDeploymentJob scheduledDeploymentJob) {
        try {
            logger.info("scheduledDeploymentJob -> " + scheduledDeploymentJob.getGitRepoId());
            scheduledDeploymentJob.setExecuted(true);
            scheduledDeploymentJob.setStatus(Status.RUNNING.getText());
            scheduledDeploymentJob.setLastTimeRun(scheduledDeploymentJob.getStartTimeRun());
            ScheduledDeploymentJob savedScheduledJob = scheduledDeploymentMongoRepository.save(scheduledDeploymentJob);
            Optional<SFDCConnectionDetails> objSfdcConnectionDetails = sfdcConnectionDetailsMongoRepository.findById(savedScheduledJob.getSfdcConnection());
            if (savedScheduledJob.getType().equalsIgnoreCase(DEPLOYMENT_JOB)) {
                if (objSfdcConnectionDetails.isPresent()) {
                    SFDCConnectionDetails sfdcConnectionDetails = objSfdcConnectionDetails.get();
                    String gitRepoId = sfdcConnectionDetails.getGitRepoId();
                    String linkedService = sfdcConnectionDetails.getLinkedService();
                    String connect2DeployUser = sfdcConnectionDetails.getConnect2DeployUser();
                    List<LinkedServices> linkedServiceByUserName = scheduledLinkedServicesMongoRepository.findByConnect2DeployUser(connect2DeployUser);
                    GitHub gitHub = null;
                    if (linkedServiceByUserName != null && !linkedServiceByUserName.isEmpty()) {
                        for (LinkedServices eachService : linkedServiceByUserName) {
                            if (linkedService.equalsIgnoreCase(eachService.getName()) && linkedService.equalsIgnoreCase(GIT_HUB)) {
                                gitHub = GitHub.connectUsingOAuth(eachService.getAccessToken());
                            } else if (linkedService.equalsIgnoreCase(eachService.getName()) && linkedService.equalsIgnoreCase(GIT_HUB_ENTERPRISE)) {
                                gitHub = GitHub.connectToEnterpriseWithOAuth(eachService.getServerURL(), eachService.getUserEmail(), eachService.getAccessToken());
                            }
                        }
                    }

                    GHRepository trailHeadGitRepo = gitHub.getRepositoryById(gitRepoId);
                    String sourceBranch = trailHeadGitRepo.getBranch(savedScheduledJob.getSourceBranch()).getSHA1();
                    GHRef ref = trailHeadGitRepo.createRef("refs/heads/" + savedScheduledJob.getJobName(), sourceBranch);
                    trailHeadGitRepo.createPullRequest(savedScheduledJob.getJobName(), ref.getRef(), savedScheduledJob.getTargetBranch(), savedScheduledJob.getJobName());
                    savedScheduledJob.setStatus(Status.FINISHED.getText());
                    scheduledDeploymentMongoRepository.save(savedScheduledJob);
                    logger.info("Pull Request Successfully created");
                }
            } else if (savedScheduledJob.getType().equalsIgnoreCase(TESTING_JOB) && objSfdcConnectionDetails.isPresent()) {
                SFDCConnectionDetails sfdcConnectionDetails = objSfdcConnectionDetails.get();
                ConnectorConfig connectorConfig = new ConnectorConfig();
                connectorConfig.setServiceEndpoint(sfdcConnectionDetails.getInstanceURL() + toolingAPIEndpoint);
                connectorConfig.setSessionId(sfdcConnectionDetails.getOauthToken());
                ToolingConnection toolingConnection = new ToolingConnection(connectorConfig);
                RunTestsRequest runTestsRequest = new RunTestsRequest();
                runTestsRequest.setAllTests(true);
                SFDCCodeCoverageOrg sfdcCodeCoverageOrg = new SFDCCodeCoverageOrg();
                sfdcCodeCoverageOrg.setScheduledJobId(savedScheduledJob.getId());
                List<SFDCCodeCoverageDetails> sfdcCodeCoverageDetailsTests = new ArrayList<>();
                List<SFDCCodeCoverageDetails> sfdcCodeCoverageDetailsWithoutTests = new ArrayList<>();
                Map<String, List<SFDCCodeCoverage>> stringSFDCCodeCoverageMap = new HashMap<>();
                RunTestsResult runTestsResult = null;

                try {
                    runTestsResult = toolingConnection.runTests(runTestsRequest);
                    sfdcCodeCoverageOrg = fetchCodeCoverageResult(toolingConnection,
                            sfdcCodeCoverageOrg,
                            sfdcCodeCoverageDetailsTests,
                            sfdcCodeCoverageDetailsWithoutTests,
                            stringSFDCCodeCoverageMap,
                            runTestsResult);
                    sfdcCodeCoverageOrg.setCreatedDate(new Date());
                    sfdcCodeCoverageOrg = sfdcCodeCoverageOrgMongoRepository.save(sfdcCodeCoverageOrg);
                } catch (Exception exception) {
                    if (exception instanceof UnexpectedErrorFault && ((UnexpectedErrorFault) exception).getExceptionCode().equals(ExceptionCode.INVALID_SESSION_ID)) {
                        String sfdcToken = SFDCUtilsCommons.refreshSFDCToken(sfdcConnectionDetails);
                        connectorConfig.setSessionId(sfdcToken);
                        toolingConnection = new ToolingConnection(connectorConfig);
                        runTestsResult = toolingConnection.runTests(runTestsRequest);
                        sfdcCodeCoverageOrg = fetchCodeCoverageResult(toolingConnection,
                                sfdcCodeCoverageOrg,
                                sfdcCodeCoverageDetailsTests,
                                sfdcCodeCoverageDetailsWithoutTests,
                                stringSFDCCodeCoverageMap,
                                runTestsResult);
                        sfdcCodeCoverageOrg.setCreatedDate(new Date());
                        sfdcCodeCoverageOrg = sfdcCodeCoverageOrgMongoRepository.save(sfdcCodeCoverageOrg);
                    } else {
                        if (exception instanceof UnexpectedErrorFault) {
                            sfdcCodeCoverageOrg.setBoolFail(true);
                            sfdcCodeCoverageOrg.setErrorMessage(((UnexpectedErrorFault) exception).getExceptionMessage());
                            sfdcCodeCoverageOrg.setCreatedDate(new Date());
                            sfdcCodeCoverageOrg = sfdcCodeCoverageOrgMongoRepository.save(sfdcCodeCoverageOrg);
                        } else {
                            logger.error(exception.getMessage());
                            exception.printStackTrace();
                        }
                    }
                }
                savedScheduledJob.setStatus(Status.FINISHED.getText());
                if(sfdcCodeCoverageOrg.getOrgCoverage() < scheduledDeploymentJob.getThreshold()){
                    savedScheduledJob.setStatus(Status.FAILED.getText());
                }
                scheduledDeploymentMongoRepository.save(scheduledDeploymentJob);
            }
        } catch (Exception exception) {
            logger.error(exception.getMessage());
            exception.printStackTrace();
        }
    }

    private SFDCCodeCoverageOrg fetchCodeCoverageResult(ToolingConnection toolingConnection, SFDCCodeCoverageOrg sfdcCodeCoverageOrg,
                                         List<SFDCCodeCoverageDetails> sfdcCodeCoverageDetailsTests,
                                         List<SFDCCodeCoverageDetails> sfdcCodeCoverageDetailsWithoutTests,
                                         Map<String, List<SFDCCodeCoverage>> stringSFDCCodeCoverageMap,
                                         RunTestsResult runTestsResult) throws ConnectionException {
        RunTestSuccess[] successes = runTestsResult.getSuccesses();
        RunTestFailure[] failures = runTestsResult.getFailures();

        for (RunTestSuccess success : successes) {

            if (stringSFDCCodeCoverageMap.containsKey(success.getName())) {
                List<SFDCCodeCoverage> sfdcCodeCoverages = stringSFDCCodeCoverageMap.get(success.getName());
                SFDCCodeCoverage sfdcCodeCoverage = new SFDCCodeCoverage();
                sfdcCodeCoverage.setBoolPassed(true);
                sfdcCodeCoverage.setMethodName(success.getMethodName());
                sfdcCodeCoverage.setRunTime(String.valueOf(success.getTime()));
                sfdcCodeCoverages.add(sfdcCodeCoverage);
            } else {
                List<SFDCCodeCoverage> sfdcCodeCoverageList = new ArrayList<>();
                SFDCCodeCoverage sfdcCodeCoverage = new SFDCCodeCoverage();
                sfdcCodeCoverage.setBoolPassed(true);
                sfdcCodeCoverage.setMethodName(success.getMethodName());
                sfdcCodeCoverage.setRunTime(String.valueOf(success.getTime()));
                sfdcCodeCoverageList.add(sfdcCodeCoverage);
                stringSFDCCodeCoverageMap.put(success.getName(), sfdcCodeCoverageList);
            }
        }


        for (RunTestFailure failure : failures) {

            if (stringSFDCCodeCoverageMap.containsKey(failure.getName())) {
                List<SFDCCodeCoverage> sfdcCodeCoverages = stringSFDCCodeCoverageMap.get(failure.getName());
                SFDCCodeCoverage sfdcCodeCoverage = new SFDCCodeCoverage();
                sfdcCodeCoverage.setBoolPassed(false);
                sfdcCodeCoverage.setErrorMessage(failure.getMessage());
                sfdcCodeCoverage.setStacktrace(failure.getStackTrace());
                sfdcCodeCoverage.setMethodName(failure.getMethodName());
                sfdcCodeCoverage.setRunTime(String.valueOf(failure.getTime()));
                sfdcCodeCoverages.add(sfdcCodeCoverage);
            } else {
                List<SFDCCodeCoverage> sfdcCodeCoverageList = new ArrayList<>();
                SFDCCodeCoverage sfdcCodeCoverage = new SFDCCodeCoverage();
                sfdcCodeCoverage.setBoolPassed(false);
                sfdcCodeCoverage.setErrorMessage(failure.getMessage());
                sfdcCodeCoverage.setStacktrace(failure.getStackTrace());
                sfdcCodeCoverage.setMethodName(failure.getMethodName());
                sfdcCodeCoverage.setRunTime(String.valueOf(failure.getTime()));
                sfdcCodeCoverageList.add(sfdcCodeCoverage);
                stringSFDCCodeCoverageMap.put(failure.getName(), sfdcCodeCoverageList);
            }
        }

        List<ApexOrgWideCoverage> apexCodeCoverageAggregates = new ArrayList<>();
        QueryResult qResult = toolingConnection.query("SELECT PercentCovered FROM ApexOrgWideCoverage");
        boolean done = false;
        if (qResult.getSize() > 0) {
            while (!done) {
                SObject[] records = qResult.getRecords();
                for (SObject record : records) {
                    apexCodeCoverageAggregates.add((ApexOrgWideCoverage) record);
                }
                if (qResult.isDone()) {
                    done = true;
                } else {
                    qResult = toolingConnection.queryMore(qResult.getQueryLocator());
                }
            }
        }
        for (ApexOrgWideCoverage apexCodeCoverageAggregate : apexCodeCoverageAggregates) {
            sfdcCodeCoverageOrg.setOrgCoverage(Double.valueOf(apexCodeCoverageAggregate.getPercentCovered()));
        }


        List<ApexCodeCoverageAggregate> lstApexCodeCoverageAggregates = new ArrayList<>();

        QueryResult apexCodeCoverageAggregate = toolingConnection.query("SELECT ApexClassOrTrigger.Name, NumLinesCovered, NumLinesUncovered FROM ApexCodeCoverageAggregate");
        boolean isCodeCoverageDone = false;
        if (qResult.getSize() > 0) {
            while (!isCodeCoverageDone) {
                SObject[] records = apexCodeCoverageAggregate.getRecords();
                for (SObject record : records) {
                    lstApexCodeCoverageAggregates.add((ApexCodeCoverageAggregate) record);
                }
                if (qResult.isDone()) {
                    isCodeCoverageDone = true;
                } else {
                    qResult = toolingConnection.queryMore(qResult.getQueryLocator());
                }
            }
        }

        for (Map.Entry<String, List<SFDCCodeCoverage>> stringListEntry : stringSFDCCodeCoverageMap.entrySet()) {
            SFDCCodeCoverageDetails eachCodeCoverageDetail = new SFDCCodeCoverageDetails();
            eachCodeCoverageDetail.setNameOfClassOrTrigger(stringListEntry.getKey());
            eachCodeCoverageDetail.setSfdcCodeCoverageList(stringListEntry.getValue());
            sfdcCodeCoverageDetailsTests.add(eachCodeCoverageDetail);
        }

        for (ApexCodeCoverageAggregate coverageAggregate : lstApexCodeCoverageAggregates) {
            String nameOfClassOrTrigger = ((Name) coverageAggregate.getApexClassOrTrigger()).getName();
            SFDCCodeCoverageDetails sfdcCodeCoverageDetailsWithoutTest = new SFDCCodeCoverageDetails();
            sfdcCodeCoverageDetailsWithoutTest.setLinesCovered(coverageAggregate.getNumLinesCovered());
            sfdcCodeCoverageDetailsWithoutTest.setLinesUncovered(coverageAggregate.getNumLinesUncovered());
            sfdcCodeCoverageDetailsWithoutTest.setNameOfClassOrTrigger(nameOfClassOrTrigger);
            sfdcCodeCoverageDetailsWithoutTests.add(sfdcCodeCoverageDetailsWithoutTest);
        }
        sfdcCodeCoverageOrg.setLstSfdcCodeCoverageDetails(sfdcCodeCoverageDetailsWithoutTests);
        sfdcCodeCoverageOrg.setLstSfdcCodeCoverageDetailsTests(sfdcCodeCoverageDetailsTests);
        sfdcCodeCoverageOrg.setBoolFail(false);
        return sfdcCodeCoverageOrg;
    }
}
