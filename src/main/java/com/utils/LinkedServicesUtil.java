package com.utils;

import com.dao.LinkedServicesMongoRepository;
import com.model.LinkedServices;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LinkedServicesUtil {
    public static final String GIT_HUB = "GitHub";
    public static final String GIT_HUB_ENTERPRISE = "GitHub Enterprise";
    private static List<String> lstLinkedServicesName = new ArrayList<String>(Arrays.asList(GIT_HUB, GIT_HUB_ENTERPRISE));

    public static List<LinkedServices> createLinkedServices(LinkedServicesMongoRepository linkedServicesMongoRepository) {
        List<LinkedServices> linkedServices = new ArrayList<>();
        for (String eachLinkedService : lstLinkedServicesName) {
            LinkedServices objServices = new LinkedServices();
            objServices.setActions("+ Connect to " + eachLinkedService);
            objServices.setConnected(false);
            objServices.setName(eachLinkedService);
            objServices.setUserName("Not Connected");
            LinkedServices save = linkedServicesMongoRepository.save(objServices);
            linkedServices.add(save);
        }

        return linkedServices;
    }
}
