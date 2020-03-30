package com.utils;

import com.dao.LinkedServicesMongoRepository;
import com.model.Connect2DeployUser;
import com.model.LinkedServices;

import java.util.*;

public class LinkedServicesUtil {
    public static final String GIT_HUB = "GitHub";
    public static final String GIT_HUB_ENTERPRISE = "GitHub Enterprise";
    private static List<String> lstLinkedServicesName = new ArrayList<String>(Arrays.asList(GIT_HUB, GIT_HUB_ENTERPRISE));

    public static Set<String> createLinkedServices(LinkedServicesMongoRepository linkedServicesMongoRepository, Connect2DeployUser connect2DeployUser) {
        Set<String> linkedServices = new HashSet<>();
        for (String eachLinkedService : lstLinkedServicesName) {
            LinkedServices objServices = new LinkedServices();
            objServices.setActions("+ Connect to " + eachLinkedService);
            objServices.setConnected(false);
            objServices.setName(eachLinkedService);
            objServices.setUserName("Not Connected");
            objServices.setConnect2DeployUser(connect2DeployUser.getEmailId());
            LinkedServices save = linkedServicesMongoRepository.save(objServices);
            linkedServices.add(save.getId());
        }

        return linkedServices;
    }
}
