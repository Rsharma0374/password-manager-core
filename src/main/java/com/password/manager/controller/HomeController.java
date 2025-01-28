package com.password.manager.controller;

import com.password.manager.configuration.ActionConfiguration;
import com.password.manager.request.DashboardDetailsRequest;
import com.password.manager.request.LoginRequest;
import com.password.manager.request.UserCreation;
import com.password.manager.request.UserCredsRequest;
import com.password.manager.response.BaseResponse;
import com.password.manager.service.ConfigService;
import com.password.manager.service.FileUploadService;
import com.password.manager.service.HomeManager;
import com.password.manager.utility.FileUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;
import java.io.File;
import org.apache.commons.io.FileUtils;


@RestController
@RequestMapping("/password-manager")
public class HomeController {

    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

    @Autowired
    HomeManager homeManager;

    @Autowired
    FileUploadService fileUploadService;

    @Autowired
    private ConfigService configService;

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

    @PostMapping(EndPointReferrer.GET_DASHBOARD_DETAILS)
    public ResponseEntity<BaseResponse> getDashboardDetails(@RequestBody DashboardDetailsRequest dashboardDetailsRequest) {
        logger.info("getDashboardDetails");
        return new ResponseEntity<>(homeManager.getDashboardDetails(dashboardDetailsRequest), HttpStatus.OK);
    }

    @PostMapping(EndPointReferrer.CREATE_USER)
    public ResponseEntity<BaseResponse> createUser(@RequestBody UserCreation userCreation) {
        logger.info(EndPointReferrer.CREATE_USER);
        return new ResponseEntity<>(homeManager.createUser(userCreation), HttpStatus.OK);
    }
//
//    @PostMapping("/get-data")
//    public ResponseEntity<BaseResponse> getUserData(@RequestBody @NotNull UserCredsRequest userCredsRequest) {
//
//        logger.debug("get-data endpoint started.");
//
//        return new ResponseEntity<>(homeManager.getUserData(userCredsRequest), HttpStatus.OK);
//    }
//
    @PostMapping("/save-data")
    public ResponseEntity<BaseResponse> saveUserData(@RequestBody @NotNull UserCredsRequest userCredsRequest, HttpServletRequest request) {

        logger.debug("save-data endpoint started.");

        return new ResponseEntity<>(homeManager.saveUserData(userCredsRequest, request), HttpStatus.OK);
    }

    @PostMapping("/update-data")
    public ResponseEntity<BaseResponse> updateUserData(@RequestBody @NotNull UserCredsRequest userCredsRequest, HttpServletRequest request) {

        logger.debug("update-data endpoint started.");

        return new ResponseEntity<>(homeManager.updateUserData(userCredsRequest, request), HttpStatus.OK);
    }

    @PostMapping("/delete-data")
    public ResponseEntity<BaseResponse> deleteUserData(@RequestBody @NotNull UserCredsRequest userCredsRequest, HttpServletRequest request) {

        logger.debug("delete-data endpoint started.");

        return new ResponseEntity<>(homeManager.deleteUserData(userCredsRequest, request), HttpStatus.OK);
    }

    @PostMapping(EndPointReferrer.API_AUTHENTICATION_MASTER)
    public ResponseEntity<BaseResponse> uploadApiAuthenticationMaster(
            @RequestParam(value = "file", required = true) @NotNull final MultipartFile file,
            @RequestParam @NotNull String product) {
        File uploadedFile = FileUtil.saveFileToStagingDirectory(file);
        BaseResponse baseResponse = fileUploadService.uploadApiAuthenticationMaster(uploadedFile, product);
        FileUtils.deleteQuietly(uploadedFile);
        return new ResponseEntity<>(baseResponse, HttpStatus.OK);
    }

    @PostMapping("/add-action-config")
    public ResponseEntity<BaseResponse> addActionConfig(
            @RequestBody @NotNull ActionConfiguration actionConfiguration) {
        logger.info("Inside add action config controller");
        return new ResponseEntity<>(configService.addActionConfig(actionConfiguration), HttpStatus.OK);
    }

    @PostMapping("/update-action-config")
    public ResponseEntity<BaseResponse> updateActionConfig(
            @RequestBody @NotNull ActionConfiguration actionConfiguration) {
        logger.info("Inside add action config controller");
        return new ResponseEntity<>(configService.updateActionConfig(actionConfiguration), HttpStatus.OK);
    }
}
