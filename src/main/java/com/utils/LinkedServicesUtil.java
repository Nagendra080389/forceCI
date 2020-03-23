package com.utils;

import com.model.LinkedServices;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LinkedServicesUtil {
    private static List<String> lstLinkedServicesName = new ArrayList<String>( Arrays.asList("GitHub", "GitHub Enterprise"));
    public static List<LinkedServices> createLinkedServices(){
        List<LinkedServices> linkedServices = new ArrayList<>();
        for (String eachLinkedService : lstLinkedServicesName) {
            LinkedServices objServices = new LinkedServices();
            objServices.setActions("+ Connect to "+eachLinkedService);
            objServices.setConnected(false);
            objServices.setName(eachLinkedService);
            objServices.setUserName("Not Connected");
            linkedServices.add(objServices);
        }

        return linkedServices;
    }
}
