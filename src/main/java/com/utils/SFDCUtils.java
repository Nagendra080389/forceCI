package com.utils;

import com.rabbitMQ.DeploymentJob;
import com.sforce.soap.metadata.CancelDeployResult;
import com.sforce.soap.metadata.DeployResult;
import com.sforce.soap.metadata.DeployStatus;
import com.sforce.soap.metadata.MetadataConnection;
import com.sforce.ws.ConnectorConfig;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;

public class SFDCUtils {

    public static boolean cancelDeploy(String salesforceMetaDataEndpoint, DeploymentJob deploymentJob) throws Exception {

        String asyncId = deploymentJob.getSfdcAsyncJobId();
        // Issue the deployment cancellation request
        String instanceURL = deploymentJob.getSfdcConnectionDetail().getInstanceURL();
        String oauthToken = deploymentJob.getSfdcConnectionDetail().getOauthToken();
        ConnectorConfig connectorConfig = new ConnectorConfig();
        System.out.println("instanceURL -> "+instanceURL);
        System.out.println("oauthToken -> "+oauthToken);
        System.out.println("asyncId -> "+asyncId);
        /*connectorConfig.setServiceEndpoint(instanceURL);
        connectorConfig.setSessionId(oauthToken);
        MetadataConnection metadataConnection = new MetadataConnection(connectorConfig);

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
            return true;*/
        HttpClient client = new HttpClient();
        PostMethod patch = createPost(instanceURL + "/services/data/v44.0" + "/metadata/deployRequest/" + asyncId + "?_HttpMethod=PATCH", oauthToken
        );
        patch.setRequestEntity(new StringRequestEntity("{\"deployResult\":{\"status\" : \"Canceling\"}}","application/json", "UTF-8"));
        System.out.println("Patch -> "+patch.getQueryString());
        int i = client.executeMethod(patch);
        System.out.println("Patch response code -> "+i);
        System.out.println("Patch response -> "+patch.getResponseBodyAsString());

        return true;
    }

    private static PostMethod createPost(String uri, String oauthToken) {
        PostMethod post = new PostMethod(uri);
        post.setRequestHeader("Authorization", "OAuth " + oauthToken.trim());
        return post;
    }
}


