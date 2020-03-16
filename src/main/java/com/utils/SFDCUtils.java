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

        boolean jobCancelled = false;
        String asyncId = deploymentJob.getSfdcAsyncJobId();
        String instanceURL = deploymentJob.getSfdcConnectionDetail().getInstanceURL();
        String oauthToken = deploymentJob.getSfdcConnectionDetail().getOauthToken();
        HttpClient client = new HttpClient();

        // Issue the deployment cancellation request
        PostMethod patch = createPost(instanceURL + "/services/data/v44.0" + "/metadata/deployRequest/" + asyncId.trim() + "?_HttpMethod=PATCH", oauthToken);
        String stringRequestBody = "{\"deployResult\":{\"status\" : \"Canceling\"}}";
        patch.setRequestEntity(new StringRequestEntity(stringRequestBody, MediaType.APPLICATION_JSON_VALUE, StandardCharsets.UTF_8.name()));
        int jobCancelledStatus = client.executeMethod(patch);
        if (jobCancelledStatus == 202) {
            DeployResultAPI deployResult = gson.fromJson(IOUtils.toString(patch.getResponseBodyAsStream(), StandardCharsets.UTF_8), DeployResultAPI.class);
            if (deployResult != null && deployResult.getDeployResult() != null && deployResult.getDeployResult().getStatus() != null) {
                String deploymentStatusFromAPI = deployResult.getDeployResult().getStatus().toString();
                if (deploymentStatusFromAPI.equalsIgnoreCase(DeployStatus.Canceling.toString()) || deploymentStatusFromAPI.equalsIgnoreCase(DeployStatus.Canceled.toString())) {
                    jobCancelled = true;
                }
            }
        }
        return jobCancelled;
    }

    private static PostMethod createPost(String uri, String oauthToken) {
        PostMethod post = new PostMethod(uri);
        post.setRequestHeader("Authorization", "Bearer " + oauthToken.trim());
        return post;
    }

}


