package com.utils;

import com.rabbitMQ.DeploymentJob;
import com.sforce.soap.metadata.CancelDeployResult;
import com.sforce.soap.metadata.DeployResult;
import com.sforce.soap.metadata.DeployStatus;
import com.sforce.soap.metadata.MetadataConnection;

public class SFDCUtils {

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

}


