package com.dao;

import com.model.Connect2DeployUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class Connect2DeployServiceImpl implements Connect2DeployService {

    @Autowired
    Connect2DeployUserMongoRepository connect2DeployUserMongoRepository;

    @Override
    public Optional<User> findByToken(String token) {
        Optional customer= connect2DeployUserMongoRepository.findByToken(token);
        if(customer.isPresent()){
            Connect2DeployUser customer1 = (Connect2DeployUser) customer.get();
            User user= new User(customer1.getEmailId(), customer1.getPassword(), true, true, true, true,
                    AuthorityUtils.createAuthorityList("USER"));
            return Optional.of(user);
        }
        return  Optional.empty();
    }
}
