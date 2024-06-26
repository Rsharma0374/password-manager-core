package com.password.manager.controller;

import com.password.manager.request.UserCredsRequest;
import com.password.manager.response.BaseResponse;
import com.password.manager.service.HomeManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;

@RestController
@RequestMapping("/password-manager")
public class HomeController {

    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

    @Autowired
    HomeManager homeManager;

    @GetMapping("/welcome")
    public String getResult(){
        return "+-------------------------------------+\n" +
                "|  Welcome to Password Manager!       |\n" +
                "|                                     |\n" +
                "|  Store and manage your passwords    |\n" +
                "|  securely with ease.                |\n" +
                "|                                     |\n" +
                "+-------------------------------------+\n";
    }

    @PostMapping("/get-data")
    public ResponseEntity<BaseResponse> getUserData(@RequestBody @NotNull UserCredsRequest userCredsRequest) {

        logger.debug("get-data endpoint started.");

        return new ResponseEntity<>(homeManager.getUserData(userCredsRequest), HttpStatus.OK);
    }

    @PostMapping("/save-data")
    public ResponseEntity<BaseResponse> saveUserData(@RequestBody @NotNull UserCredsRequest userCredsRequest) {

        logger.debug("save-data endpoint started.");

        return new ResponseEntity<>(homeManager.saveUserData(userCredsRequest), HttpStatus.OK);
    }

    @PostMapping("/update-data")
    public ResponseEntity<BaseResponse> updateUserData(@RequestBody @NotNull UserCredsRequest userCredsRequest) {

        logger.debug("update-data endpoint started.");

        return new ResponseEntity<>(homeManager.updateUserData(userCredsRequest), HttpStatus.OK);
    }

    @PostMapping("/delete-data")
    public ResponseEntity<BaseResponse> deleteUserData(@RequestBody @NotNull UserCredsRequest userCredsRequest) {

        logger.debug("delete-data endpoint started.");

        return new ResponseEntity<>(homeManager.deleteUserData(userCredsRequest), HttpStatus.OK);
    }
}
