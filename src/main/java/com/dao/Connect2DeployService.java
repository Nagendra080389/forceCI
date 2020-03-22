package com.dao;

import com.model.Connect2DeployUser;
import org.springframework.security.core.userdetails.User;

import java.util.Optional;

public interface Connect2DeployService {

    Optional<User> findByToken(String token);
}
