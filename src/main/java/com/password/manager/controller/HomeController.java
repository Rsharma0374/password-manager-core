package com.password.manager.controller;

import com.password.manager.configuration.ActionConfiguration;
import com.password.manager.request.LoginRequest;
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

    /**
     * This Java function handles a POST request for user login and returns a response entity with the login information.
     *
     * @param loginRequest The `loginRequest` parameter is an object of type `LoginRequest` which is being passed as the
     * request body in the POST request. It is annotated with `@RequestBody` to indicate that the data for this parameter
     * should be taken from the request body. Additionally, it is annotated with `@
     * @param httpRequest The `httpRequest` parameter in the `getLogin` method is of type `HttpServletRequest`. It
     * represents the HTTP request that was made to the server and contains information such as request headers,
     * parameters, and body. In this method, it is being used to access information from the incoming HTTP request.
     * @return A ResponseEntity object containing a BaseResponse is being returned.
     */
    @PostMapping(EndPointReferrer.LOGIN)
    public ResponseEntity<BaseResponse> getLogin(
            @Validated(value = {LoginRequest.FetchGrp.class})
            @RequestBody @NotNull LoginRequest loginRequest,
            HttpServletRequest httpRequest) {

        logger.debug("{} controller started",EndPointReferrer.LOGIN);

        return new ResponseEntity<>(homeManager.login(loginRequest, httpRequest), HttpStatus.OK);

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
