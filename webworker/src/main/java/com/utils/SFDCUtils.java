package com.utils;

import com.backgroundworker.quartzJob.ScheduledJobRepositoryCustomImpl;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.model.SFDCConnectionDetails;
import com.rabbitMQ.DeploymentJob;
import com.sforce.soap.metadata.CancelDeployResult;
import com.sforce.soap.metadata.DeployResult;
import com.sforce.soap.metadata.DeployStatus;
import com.sforce.soap.metadata.MetadataConnection;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class SFDCUtils {

    private static final Logger logger = LoggerFactory.getLogger(SFDCUtils.class);

    public static String cancelDeploy(MetadataConnection metadataConnection, DeploymentJob deploymentJob) throws Exception {

        String asyncId = deploymentJob.getSfdcAsyncJobId().trim();
        // Issue the deployment cancellation request
        CancelDeployResult result = metadataConnection.cancelDeploy(asyncId);

        // If the deployment cancellation completed, write a message to the output.
        if (result.isDone()) {
            System.out.println("Your deployment was canceled successfully!");
            return "Done";
        } else {
            // The deployment cancellation is still in progress, so get a new status
            DeployResult deployResult = metadataConnection.checkDeployStatus(asyncId, false);

            // Check whether the deployment is done. If not done, this means
            // that the cancellation is still in progress and the status is Canceling.
            while (!deployResult.isDone()) {
                // Assert that the deployment status is Canceling
                assert deployResult.getStatus() == DeployStatus.Canceling;
                // Wait 2 seconds
                Thread.sleep(2000);
                // Get the deployment status again
                deployResult = metadataConnection.checkDeployStatus(asyncId, false);
            }

            // The deployment is done. Write the status to the output.
            // (When the deployment is done, the cancellation should have completed
            // and the status should be Canceled. However, in very rare cases,
            // the deployment can complete before it is canceled.)
            System.out.println("Final deploy status = >" + deployResult.getStatus());
            return "Done";
        }
    }

    public static String refreshSFDCToken(SFDCConnectionDetails sfdcConnectionDetails) throws IOException {
        String refreshToken = sfdcConnectionDetails.getRefreshToken();
        String environment = sfdcConnectionDetails.getEnvironment();
        String instanceURL = sfdcConnectionDetails.getInstanceURL();
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
        int intStatus = httpClient.executeMethod(post);
        logger.info("Refresh Token return status = "+intStatus);
        String responseBody = IOUtils.toString(post.getResponseBodyAsStream(), StandardCharsets.UTF_8);
        JsonParser parser = new JsonParser();
        JsonObject jsonObject = parser.parse(responseBody).getAsJsonObject();
        return jsonObject.get("access_token").getAsString();
    }

}


