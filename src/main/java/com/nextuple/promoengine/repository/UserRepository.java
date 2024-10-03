package com.nextuple.promoengine.repository;

import com.nextuple.promoengine.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<User,String> {
    User findByUsername(String username);
    boolean existsByUsername(String username);

}
