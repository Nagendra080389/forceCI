package com.controller;

import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.PagedSearchIterable;

import java.io.FileNotFoundException;

public class TestMain {

    public static void main(String[] args) throws Exception {

        String queryParam = "fork:true user:" + "Nagendra080389" + " " + "trail";
        GitHub gitHub = GitHub.connectUsingOAuth("768b256dc3bcab692cfdfac2604f02e3bd019563");
        PagedSearchIterable<GHRepository> list = gitHub.searchRepositories().q(queryParam).list();
        for (GHRepository ghRepository : list) {
            System.out.println(ghRepository);
        }
    }

    public void execute() throws FileNotFoundException {

    }
}
