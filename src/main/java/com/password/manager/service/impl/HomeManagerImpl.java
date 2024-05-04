package com.password.manager.service.impl;

import com.password.manager.dao.MongoService;
import com.password.manager.model.UserCredsCollection;
import com.password.manager.request.LoginRequest;
import com.password.manager.request.UserCreation;
import com.password.manager.request.UserCredsRequest;
import com.password.manager.response.BaseResponse;
import com.password.manager.response.Error;
import com.password.manager.response.LoginResponse;
import com.password.manager.service.HomeManager;
import com.password.manager.service.transport.TransportUtils;
import com.password.manager.utility.Utility;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class HomeManagerImpl implements HomeManager {
    private static final Logger logger = LoggerFactory.getLogger(HomeManagerImpl.class);

    @Autowired
    MongoService mongoService;

    @Value("${userAuthentication.login.api}")
    private String loginApi;

    /**
     * The `login` function in Java handles user authentication by checking the password with SHA encryption and generating
     * an OTP for verification.
     *
     * @param loginRequest The `loginRequest` parameter in the `login` method contains information required for user
     * authentication, such as the username, password, and SHA-encrypted password. It is used to validate the user's
     * credentials during the login process. The method checks if the provided username exists in the system and then
     * compares
     * @param httpRequest The `httpRequest` parameter in the `login` method is of type `HttpServletRequest`. This parameter
     * is used to access information about the HTTP request that triggered the login operation. It can provide details such
     * as request headers, parameters, and other information related to the incoming HTTP request. This information can be
     * @return The method `login` returns a `BaseResponse` object.
     */
    @Override
    public BaseResponse login(LoginRequest loginRequest, HttpServletRequest httpRequest) {
        logger.info("Inside login request");
        LoginResponse loginResponse = new LoginResponse();
        try {

            loginResponse = (LoginResponse) TransportUtils.postJsonRequest(loginRequest, loginApi, LoginResponse.class);
            if (null != loginResponse) {
                return Utility.getBaseResponse(HttpStatus.OK, loginResponse);
            }

            return Utility.getBaseResponse(HttpStatus.BAD_REQUEST, "Something went wrong. Please contact system administrator");
        } catch (Exception ex) {
            Error error = new Error();
            error.setMessage(ex.getMessage());
            logger.error("Exception occurred while login due to - ", ex);
            return Utility.getBaseResponse(HttpStatus.INTERNAL_SERVER_ERROR, Collections.singleton(error));
        }
    }

    @Override
    public BaseResponse saveUserData(UserCredsRequest userCredsRequest) {

        boolean dataSaved = false;
        try {
            if (null == userCredsRequest || StringUtils.isEmpty(userCredsRequest.getPassword()) || StringUtils.isEmpty(userCredsRequest.getLoginUser())) {
                return Utility.getBaseResponse(HttpStatus.BAD_REQUEST, Utility.getBadRequestErrorList("Request is invalid."));

            }
            UserCredsCollection userCredsCollection = mongoService.getUserData(userCredsRequest);

            if (null != userCredsCollection) {
                List<UserCredsCollection.CredList> credLists = userCredsCollection.getCredLists();
                if (CollectionUtils.isEmpty(credLists)) {
                    UserCredsCollection.CredList credList = new UserCredsCollection.CredList();
                    credList.setEmail(userCredsRequest.getEmail());
                    credList.setPassword(userCredsRequest.getPassword());
                    credList.setUsername(userCredsRequest.getUsername());
                    credList.setPlatformName(userCredsRequest.getPlatformName());
                    userCredsCollection.getCredLists().add(credList);

                    dataSaved = mongoService.saveCredsCollection(userCredsCollection);
                } else {
                    List<UserCredsCollection.CredList> filteredCredList = credLists.stream()
                            .filter(f -> Objects.equals(f.getPlatformName(), userCredsRequest.getPlatformName()))
                            .collect(Collectors.toList());
                    for (UserCredsCollection.CredList credList : filteredCredList) {
                        if (StringUtils.equalsIgnoreCase(credList.getEmail(), userCredsRequest.getEmail())
                                || StringUtils.equalsIgnoreCase(credList.getUsername(), userCredsRequest.getUsername())) {
                            Error error = new Error();
                            error.setErrorCode(String.valueOf(HttpStatus.BAD_REQUEST.value()));
                            error.setMessage("Request is invalid.");
                            return Utility.getBaseResponse(HttpStatus.BAD_REQUEST, error);
                        }
                    }
                    UserCredsCollection.CredList credList = new UserCredsCollection.CredList();
                    credList.setEmail(userCredsRequest.getEmail());
                    credList.setPassword(userCredsRequest.getPassword());
                    credList.setUsername(userCredsRequest.getUsername());
                    credList.setPlatformName(userCredsRequest.getPlatformName());
                    userCredsCollection.getCredLists().add(credList);

                    dataSaved = mongoService.saveCredsCollection(userCredsCollection);

                }

            } else {
                userCredsCollection = new UserCredsCollection();
                userCredsCollection.setLoginUsername(userCredsRequest.getLoginUser());

                UserCredsCollection.CredList credList = new UserCredsCollection.CredList();
                List<UserCredsCollection.CredList> credLists = new ArrayList<>();

                credList.setEmail(userCredsRequest.getEmail());
                credList.setPassword(userCredsRequest.getPassword());
                credList.setUsername(userCredsRequest.getUsername());
                credList.setPlatformName(userCredsRequest.getPlatformName());

                credLists.add(credList);
                userCredsCollection.setCredLists(credLists);

                userCredsCollection.setLastUpdatedDate(new Date());

                dataSaved = mongoService.saveCredsCollection(userCredsCollection);

            }

            if (dataSaved) {
                return Utility.getBaseResponse(HttpStatus.OK, userCredsCollection);
            } else {
                return Utility.getBaseResponse(HttpStatus.INTERNAL_SERVER_ERROR, Utility.getInterServerErrorList("Something went wrong, Please contact Administrator."));

            }

        } catch (Exception e) {
            logger.error("Exception occurred while saving user creds with probable cause - ", e);
            Error error = new Error();
            error.setMessage(e.getMessage());
            return Utility.getBaseResponse(HttpStatus.INTERNAL_SERVER_ERROR, Collections.singleton(error));
        }
    }

    @Override
    public BaseResponse updateUserData(UserCredsRequest userCredsRequest) {
        try {
            if (null == userCredsRequest || StringUtils.isEmpty(userCredsRequest.getPassword())
                    || StringUtils.isEmpty(userCredsRequest.getLoginUser())
                    || StringUtils.isEmpty(userCredsRequest.getPlatformName())) {
                return Utility.getBaseResponse(HttpStatus.BAD_REQUEST, Utility.getBadRequestErrorList("Request is invalid."));
            }
            UserCredsCollection userCredsCollection = mongoService.getUserData(userCredsRequest);
            if (null == userCredsCollection) {
                return Utility.getBaseResponse(HttpStatus.NO_CONTENT, Utility.getNoContentErrorList());

            }
            List<UserCredsCollection.CredList> credList = userCredsCollection.getCredLists();
            if (CollectionUtils.isEmpty(credList)) {
                return Utility.getBaseResponse(HttpStatus.BAD_REQUEST, Utility.getBadRequestErrorList("Request is invalid."));
            }
            List<UserCredsCollection.CredList> filteredCredList = credList.stream()
                    .filter(f-> Objects.equals(f.getPlatformName(), userCredsRequest.getPlatformName()))
                    .collect(Collectors.toList());

            for (UserCredsCollection.CredList credList1 : filteredCredList) {
                if (StringUtils.equalsIgnoreCase(credList1.getEmail(), userCredsRequest.getEmail())
                        || StringUtils.equalsIgnoreCase(credList1.getUsername(), userCredsRequest.getUsername())) {
                    credList1.setUsername(userCredsRequest.getUsername());
                    credList1.setEmail(userCredsRequest.getEmail());
                    credList1.setPlatformName(userCredsRequest.getPlatformName());
                    credList1.setPassword(userCredsRequest.getPassword());
                    break;
                }
            }

            if (mongoService.saveCredsCollection(userCredsCollection)) {
                return Utility.getBaseResponse(HttpStatus.OK, userCredsCollection);
            } else {
                return Utility.getBaseResponse(HttpStatus.INTERNAL_SERVER_ERROR, Utility.getInterServerErrorList("Something went wrong, Please contact Administrator."));

            }


        } catch (Exception e) {
            logger.error("Exception occurred while updating user creds with probable cause - ", e);
            Error error = new Error();
            error.setMessage(e.getMessage());
            return Utility.getBaseResponse(HttpStatus.INTERNAL_SERVER_ERROR, Collections.singleton(error));
        }
    }

    @Override
    public BaseResponse deleteUserData(UserCredsRequest userCredsRequest) {

        try {
            if (null == userCredsRequest || StringUtils.isEmpty(userCredsRequest.getLoginUser()) || StringUtils.isEmpty(userCredsRequest.getPlatformName())) {
                return Utility.getBaseResponse(HttpStatus.BAD_REQUEST, Utility.getBadRequestErrorList("Request is invalid."));
            }
            UserCredsCollection userCredsCollection = mongoService.getUserData(userCredsRequest);
            if (null == userCredsCollection) {
                return Utility.getBaseResponse(HttpStatus.NO_CONTENT, Utility.getNoContentErrorList());

            }

            List<UserCredsCollection.CredList> credList = userCredsCollection.getCredLists();
            if (CollectionUtils.isEmpty(credList)) {
                return Utility.getBaseResponse(HttpStatus.BAD_REQUEST, Utility.getBadRequestErrorList("Request is invalid."));
            }
            boolean removed = credList.removeIf(f -> (null != f.getPlatformName()
                    && StringUtils.equalsIgnoreCase(f.getPlatformName(), userCredsRequest.getPlatformName())
                    && StringUtils.equalsIgnoreCase(f.getEmail(), userCredsRequest.getEmail())));
            logger.debug("Removed success");

            if (mongoService.saveCredsCollection(userCredsCollection)) {
                return Utility.getBaseResponse(HttpStatus.OK, userCredsCollection);
            } else {
                return Utility.getBaseResponse(HttpStatus.INTERNAL_SERVER_ERROR, Utility.getInterServerErrorList("Something went wrong, Please contact Administrator."));
            }

        } catch (Exception e) {
            logger.error("Exception occurred while deleteUserData with probable cause - ", e);
            Error error = new Error();
            error.setMessage(e.getMessage());
            return Utility.getBaseResponse(HttpStatus.INTERNAL_SERVER_ERROR, Collections.singleton(error));
        }
    }

    @Override
    public BaseResponse getUserData(UserCredsRequest userCredsRequest) {
        try {
            if (null == userCredsRequest || StringUtils.isEmpty(userCredsRequest.getLoginUser())) {
                return Utility.getBaseResponse(HttpStatus.BAD_REQUEST, Utility.getBadRequestErrorList("Request is invalid."));
            }
            UserCredsCollection userCredsCollection = mongoService.getUserData(userCredsRequest);
            if (null == userCredsCollection) {
                return Utility.getBaseResponse(HttpStatus.NO_CONTENT, Utility.getNoContentErrorList());
            }

            return Utility.getBaseResponse(HttpStatus.OK, userCredsCollection);

        } catch (Exception e) {
            logger.error("Exception occurred while getUserData with probable cause - ", e);
            Error error = new Error();
            error.setMessage(e.getMessage());
            return Utility.getBaseResponse(HttpStatus.INTERNAL_SERVER_ERROR, Collections.singleton(error));
        }
    }
}
