package com.swetlox_app.swetlox.controller;

import com.swetlox_app.swetlox.dto.admin.StatusDto;
import com.swetlox_app.swetlox.dto.admin.UserDto;
import com.swetlox_app.swetlox.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/v1/api")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

//    @GetMapping("/get-num-of-user")
//    public ResponseEntity<Long> getNumberOfUserApi(){
//        long numOfUser = userService.getNumOfUser();
//        return ResponseEntity.ok(numOfUser);
//    }

    @GetMapping("/get-status")
    public ResponseEntity<StatusDto> getActiveUserCount(){
        StatusDto status = adminService.getStatus();
        return ResponseEntity.ok(status);
    }

    @DeleteMapping("/delete-user/{userId}")
    public void deleteUser(@PathVariable("userId") String userId){
        adminService.deleteUser(userId);
    }

    @GetMapping("/suspense-user/{userId}")
    public void suspenseUser(@PathVariable("userId") String userId){
        adminService.suspenseAccount(userId);
    }

    @GetMapping("/unsuspense-user/{userId}")
    public void unSpenseUser(@PathVariable("userId") String userId){
        adminService.unsuspenseAccount(userId);
    }
    @GetMapping("/get-all-user/{pageNum}")
    public ResponseEntity<Page<UserDto>> getAllUserApi(@PathVariable("pageNum") Integer pageNum){
        Page<UserDto> allUser = adminService.getUserData(pageNum);
        return ResponseEntity.ok(allUser);
    }
//
//    @GetMapping("/delete-user/{id}")
//    public void deleteUser(@PathVariable("id") String id){
//        userService.deleteUser(id);
//    }
}
