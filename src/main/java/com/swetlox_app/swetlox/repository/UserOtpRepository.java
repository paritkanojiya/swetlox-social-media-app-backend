package com.swetlox_app.swetlox.repository;

import com.swetlox_app.swetlox.entity.UserOtp;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserOtpRepository extends MongoRepository<UserOtp,String> {
    UserOtp findByEmail(String email);
}