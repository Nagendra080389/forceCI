package com.utils;

import com.google.gson.Gson;
import com.model.DeployResult;
import com.model.DeployResultAPI;
import com.model.DeployResultWrapper;
import com.model.SHAObject;
import com.rabbitMQ.DeploymentJob;
import com.sforce.soap.metadata.CancelDeployResult;
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

    public static boolean cancelDeploy(Gson gson, DeploymentJob deploymentJob) throws Exception {

        String asyncId = deploymentJob.getSfdcAsyncJobId();
        // Issue the deployment cancellation request
        String instanceURL = deploymentJob.getSfdcConnectionDetail().getInstanceURL();
        String oauthToken = deploymentJob.getSfdcConnectionDetail().getOauthToken();
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

        PostMethod patch = createPost(instanceURL + "/services/data/v44.0" + "/metadata/deployRequest/" + asyncId.trim() + "?_HttpMethod=PATCH", oauthToken);
        DeployResult canceling = new DeployResult("Canceling");
        String stringRequestBody = gson.toJson(new DeployResultWrapper(canceling));
        System.out.println("stringRequestBody -> "+stringRequestBody);
        patch.setRequestEntity(new StringRequestEntity(stringRequestBody, MediaType.APPLICATION_JSON_VALUE, StandardCharsets.UTF_8.name()));
        System.out.println("Patch -> "+patch.getQueryString());
        System.out.println("Patch request Entity -> "+patch.getRequestEntity().toString());
        int i = client.executeMethod(patch);
        System.out.println("Patch response code -> "+i);
        DeployResultAPI deployResult = gson.fromJson(IOUtils.toString(patch.getResponseBodyAsStream(), StandardCharsets.UTF_8), DeployResultAPI.class);
        if(deployResult != null && deployResult.getDeployResult() != null && deployResult.getDeployResult().getStatus() != null) {
            System.out.println(deployResult.getDeployResult().getStatus().toString());
        }
        return true;
    }

    private static PostMethod createPost(String uri, String oauthToken) {
        PostMethod post = new PostMethod(uri);
        post.setRequestHeader("Authorization", "Bearer " + oauthToken.trim());
        return post;
    }

}


