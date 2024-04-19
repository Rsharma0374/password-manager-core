package com.password.manager.service.impl;

import com.password.manager.dao.MongoService;
import com.password.manager.model.UserCredsCollection;
import com.password.manager.request.UserCredsRequest;
import com.password.manager.response.BaseResponse;
import com.password.manager.response.Error;
import com.password.manager.service.HomeManager;
import com.password.manager.utility.ResponseUtility;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Override
    public BaseResponse saveUserData(UserCredsRequest userCredsRequest) {

        boolean dataSaved = false;
        try {
            if (null == userCredsRequest || StringUtils.isEmpty(userCredsRequest.getPassword()) || StringUtils.isEmpty(userCredsRequest.getLoginUser())) {
                Error error = new Error();
                error.setErrorCode(String.valueOf(HttpStatus.BAD_REQUEST.value()));
                error.setMessage("Request is invalid.");
                return ResponseUtility.getBaseResponse(HttpStatus.BAD_REQUEST, error);
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
                            return ResponseUtility.getBaseResponse(HttpStatus.BAD_REQUEST, error);
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
                return ResponseUtility.getBaseResponse(HttpStatus.OK, userCredsCollection);
            } else {
                Error error = new Error();
                error.setErrorCode(String.valueOf(HttpStatus.FAILED_DEPENDENCY.value()));
                error.setMessage("Something went wrong, Please contact Administrator.");
                return ResponseUtility.getBaseResponse(HttpStatus.INTERNAL_SERVER_ERROR, error);
            }

        } catch (Exception e) {
            logger.error("Exception occurred while saving user creds with probable cause - ", e);
            Error error = new Error();
            error.setMessage(e.getMessage());
            return ResponseUtility.getBaseResponse(HttpStatus.INTERNAL_SERVER_ERROR, Collections.singleton(error));
        }
    }

    @Override
    public BaseResponse updateUserData(UserCredsRequest userCredsRequest) {
        try {
            if (null == userCredsRequest || StringUtils.isEmpty(userCredsRequest.getPassword())
                    || StringUtils.isEmpty(userCredsRequest.getLoginUser())
                    || StringUtils.isEmpty(userCredsRequest.getPlatformName())) {
                Error error = new Error();
                error.setErrorCode(String.valueOf(HttpStatus.BAD_REQUEST.value()));
                error.setMessage("Request is invalid.");
                return ResponseUtility.getBaseResponse(HttpStatus.BAD_REQUEST, error);
            }
            UserCredsCollection userCredsCollection = mongoService.getUserData(userCredsRequest);
            if (null == userCredsCollection) {
                Error error = new Error();
                error.setErrorCode(String.valueOf(HttpStatus.BAD_REQUEST.value()));
                error.setMessage("No user is found with provided request.");
                return ResponseUtility.getBaseResponse(HttpStatus.BAD_REQUEST, error);
            }
            List<UserCredsCollection.CredList> credList = userCredsCollection.getCredLists();
            if (CollectionUtils.isEmpty(credList)) {
                Error error = new Error();
                error.setErrorCode(String.valueOf(HttpStatus.BAD_REQUEST.value()));
                error.setMessage("Request is invalid.");
                return ResponseUtility.getBaseResponse(HttpStatus.BAD_REQUEST, error);
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
                return ResponseUtility.getBaseResponse(HttpStatus.OK, userCredsCollection);
            } else {
                Error error = new Error();
                error.setErrorCode(String.valueOf(HttpStatus.FAILED_DEPENDENCY.value()));
                error.setMessage("Something went wrong, Please contact Administrator.");
                return ResponseUtility.getBaseResponse(HttpStatus.INTERNAL_SERVER_ERROR, error);
            }


        } catch (Exception e) {
            logger.error("Exception occurred while updating user creds with probable cause - ", e);
            Error error = new Error();
            error.setMessage(e.getMessage());
            return ResponseUtility.getBaseResponse(HttpStatus.INTERNAL_SERVER_ERROR, Collections.singleton(error));
        }
    }
}
