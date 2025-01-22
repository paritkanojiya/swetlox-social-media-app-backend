package com.swetlox_app.swetlox.controller;

import com.swetlox_app.swetlox.entity.User;
import com.swetlox_app.swetlox.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/v1/api")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;

    @GetMapping("/get-num-of-user")
    public ResponseEntity<Long> getNumberOfUserApi(){
        long numOfUser = userService.getNumOfUser();
        return ResponseEntity.ok(numOfUser);
    }

    @GetMapping("/get-all-user/{pageNum}")
    public ResponseEntity<Page<User>> getAllUserApi(@PathVariable("pageNum") Integer pageNum){
        Page<User> allUser = userService.getAllUser(pageNum);
        return ResponseEntity.ok(allUser);
    }

    @GetMapping("/delete-user/{id}")
    public void deleteUser(@PathVariable("id") String id){
        userService.deleteUser(id);
    }
}
