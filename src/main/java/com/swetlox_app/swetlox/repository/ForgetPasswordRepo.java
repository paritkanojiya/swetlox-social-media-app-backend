package com.swetlox_app.swetlox.repository;

import com.swetlox_app.swetlox.entity.ForgetPassword;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ForgetPasswordRepo extends MongoRepository<ForgetPassword,String> {
    Optional<ForgetPassword> findByUserId(String authId);
}
