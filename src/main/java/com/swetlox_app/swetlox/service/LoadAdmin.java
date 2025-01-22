package com.swetlox_app.swetlox.service;

import com.swetlox_app.swetlox.allenum.UserType;
import com.swetlox_app.swetlox.entity.Role;
import com.swetlox_app.swetlox.entity.User;
import com.swetlox_app.swetlox.repository.UserRepo;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LoadAdmin {

    private final PasswordEncoder passwordEncoder;
    private final UserRepo userRepository;
    private final MongoTemplate mongoTemplate;

    @PostConstruct
    public void init(){
        System.out.println(mongoTemplate.getDb().getName());
    }


//    @PostConstruct
//    public void loadAdmin(){
//        Role role=new Role();
//        role.setRole("ADMIN");
//        User adminUser = User.builder()
//                .userName("admin")
//                .email("admin@gmail.com")
//                .password(passwordEncoder.encode("parit2003"))
//                .isVerified(true)
//                .createdAt(LocalDateTime.now())
//                .roleList(List.of(role))
//                .userType(UserType.EMAIL)
//                .build();
//        userRepository.save(adminUser);
//    }
}
