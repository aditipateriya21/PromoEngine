package com.nextuple.promoengine.repository;


import com.nextuple.promoengine.model.Rule;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RuleRepository extends MongoRepository<Rule, String> {
    Rule findByName(String name);
}

