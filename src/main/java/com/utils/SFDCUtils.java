package com.utils;

import com.google.gson.Gson;
import com.model.DeployResultAPI;
import com.model.DeployResultWrapper;
import com.model.SHAObject;
import com.rabbitMQ.DeploymentJob;
import com.sforce.soap.metadata.CancelDeployResult;
import com.sforce.soap.metadata.DeployResult;
import com.sforce.soap.metadata.DeployStatus;
import com.sforce.soap.metadata.MetadataConnection;
import com.sforce.ws.ConnectorConfig;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.io.IOUtils;
import org.springframework.http.MediaType;

import java.nio.charset.StandardCharsets;

public class SFDCUtils {

    public static boolean cancelDeploy(MetadataConnection metadataConnection, DeploymentJob deploymentJob) throws Exception {

        String asyncId = deploymentJob.getSfdcAsyncJobId().trim();
        // Issue the deployment cancellation request
        CancelDeployResult result = metadataConnection.cancelDeploy(asyncId);

        // If the deployment cancellation completed, write a message to the output.
        if (result.isDone()) {
            System.out.println("Your deployment was canceled successfully!");
            return true;
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
            return true;
        }
    }

}


