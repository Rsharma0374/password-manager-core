package com.password.manager.service.impl;

import com.password.manager.constant.ErrorCode;
import com.password.manager.dao.MongoService;
import com.password.manager.model.UserCredsCollection;
import com.password.manager.request.DashboardDetailsRequest;
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


    @Override
    public BaseResponse saveUserData(UserCredsRequest userCredsRequest, HttpServletRequest httpServletRequest) {

        boolean dataSaved = false;
        Collection<Error> errors = new ArrayList<>();
        try {
            if (null == userCredsRequest || StringUtils.isEmpty(userCredsRequest.getPassword())) {
                return Utility.getBaseResponse(HttpStatus.BAD_REQUEST, Utility.getBadRequestErrorList("Request is invalid."));

            }
            String userName = httpServletRequest.getHeader("userName");
            UserCredsCollection userCredsCollection = mongoService.getUserDataByIdentifier(userName);

            if (null != userCredsCollection) {
                List<UserCredsCollection.CredList> credLists = userCredsCollection.getCredLists();
                if (CollectionUtils.isEmpty(credLists)) {
                    UserCredsCollection.CredList credList = new UserCredsCollection.CredList();
                    List<UserCredsCollection.CredList> credListsArray = new ArrayList<>();
                    credList.setEmail(userCredsRequest.getEmail());
                    credList.setPassword(userCredsRequest.getPassword());
                    credList.setUsername(userCredsRequest.getUsername());
                    credList.setService(userCredsRequest.getService());
                    credListsArray.add(credList);
                    userCredsCollection.setCredLists(credListsArray);

                    dataSaved = mongoService.saveUser(userCredsCollection);
                } else {
                    List<UserCredsCollection.CredList> filteredCredList = credLists.stream()
                            .filter(f -> f.getService().equalsIgnoreCase(userCredsRequest.getService()))
                            .toList();
                    for (UserCredsCollection.CredList credList : filteredCredList) {
                        if (StringUtils.equalsIgnoreCase(credList.getEmail(), userCredsRequest.getEmail())
                                || StringUtils.equalsIgnoreCase(credList.getUsername(), userCredsRequest.getUsername())) {

                            errors.add(Error.builder()
                                    .message("Request is invalid.")
                                    .errorCode(String.valueOf(Error.ERROR_TYPE.BAD_REQUEST.toCode()))
                                    .errorType(Error.ERROR_TYPE.BAD_REQUEST.toValue())
                                    .level(Error.SEVERITY.LOW.name())
                                    .build());
                            return Utility.getBaseResponse(HttpStatus.BAD_REQUEST, errors);
                        }
                    }
                    UserCredsCollection.CredList credList = new UserCredsCollection.CredList();
                    credList.setEmail(userCredsRequest.getEmail());
                    credList.setPassword(userCredsRequest.getPassword());
                    credList.setUsername(userCredsRequest.getUsername());
                    credList.setService(userCredsRequest.getService());
                    userCredsCollection.getCredLists().add(credList);

                    dataSaved = mongoService.saveUser(userCredsCollection);

                }

            } else {
                logger.error("User does not exist.");
                errors.add(Error.builder()
                        .message("User does not exist.")
                        .errorCode(String.valueOf(Error.ERROR_TYPE.SYSTEM.toCode()))
                        .errorType(Error.ERROR_TYPE.SYSTEM.toValue())
                        .level(Error.SEVERITY.HIGH.name())
                        .build());
                return Utility.getBaseResponse(HttpStatus.INTERNAL_SERVER_ERROR, errors);

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
//
//    @Override
//    public BaseResponse updateUserData(UserCredsRequest userCredsRequest) {
//        try {
//            if (null == userCredsRequest || StringUtils.isEmpty(userCredsRequest.getPassword())
//                    || StringUtils.isEmpty(userCredsRequest.getLoginUser())
//                    || StringUtils.isEmpty(userCredsRequest.getPlatformName())) {
//                return Utility.getBaseResponse(HttpStatus.BAD_REQUEST, Utility.getBadRequestErrorList("Request is invalid."));
//            }
//            UserCredsCollection userCredsCollection = mongoService.getUserData(userCredsRequest);
//            if (null == userCredsCollection) {
//                return Utility.getBaseResponse(HttpStatus.NO_CONTENT, Utility.getNoContentErrorList());
//
//            }
//            List<UserCredsCollection.CredList> credList = userCredsCollection.getCredLists();
//            if (CollectionUtils.isEmpty(credList)) {
//                return Utility.getBaseResponse(HttpStatus.BAD_REQUEST, Utility.getBadRequestErrorList("Request is invalid."));
//            }
//            List<UserCredsCollection.CredList> filteredCredList = credList.stream()
//                    .filter(f-> Objects.equals(f.getPlatformName(), userCredsRequest.getPlatformName()))
//                    .collect(Collectors.toList());
//
//            for (UserCredsCollection.CredList credList1 : filteredCredList) {
//                if (StringUtils.equalsIgnoreCase(credList1.getEmail(), userCredsRequest.getEmail())
//                        || StringUtils.equalsIgnoreCase(credList1.getUsername(), userCredsRequest.getUsername())) {
//                    credList1.setUsername(userCredsRequest.getUsername());
//                    credList1.setEmail(userCredsRequest.getEmail());
//                    credList1.setPlatformName(userCredsRequest.getPlatformName());
//                    credList1.setPassword(userCredsRequest.getPassword());
//                    break;
//                }
//            }
//
//            if (mongoService.saveCredsCollection(userCredsCollection)) {
//                return Utility.getBaseResponse(HttpStatus.OK, userCredsCollection);
//            } else {
//                return Utility.getBaseResponse(HttpStatus.INTERNAL_SERVER_ERROR, Utility.getInterServerErrorList("Something went wrong, Please contact Administrator."));
//
//            }
//
//
//        } catch (Exception e) {
//            logger.error("Exception occurred while updating user creds with probable cause - ", e);
//            Error error = new Error();
//            error.setMessage(e.getMessage());
//            return Utility.getBaseResponse(HttpStatus.INTERNAL_SERVER_ERROR, Collections.singleton(error));
//        }
//    }
//
//    @Override
//    public BaseResponse deleteUserData(UserCredsRequest userCredsRequest) {
//
//        try {
//            if (null == userCredsRequest || StringUtils.isEmpty(userCredsRequest.getLoginUser()) || StringUtils.isEmpty(userCredsRequest.getPlatformName())) {
//                return Utility.getBaseResponse(HttpStatus.BAD_REQUEST, Utility.getBadRequestErrorList("Request is invalid."));
//            }
//            UserCredsCollection userCredsCollection = mongoService.getUserData(userCredsRequest);
//            if (null == userCredsCollection) {
//                return Utility.getBaseResponse(HttpStatus.NO_CONTENT, Utility.getNoContentErrorList());
//
//            }
//
//            List<UserCredsCollection.CredList> credList = userCredsCollection.getCredLists();
//            if (CollectionUtils.isEmpty(credList)) {
//                return Utility.getBaseResponse(HttpStatus.BAD_REQUEST, Utility.getBadRequestErrorList("Request is invalid."));
//            }
//            boolean removed = credList.removeIf(f -> (null != f.getPlatformName()
//                    && StringUtils.equalsIgnoreCase(f.getPlatformName(), userCredsRequest.getPlatformName())
//                    && StringUtils.equalsIgnoreCase(f.getEmail(), userCredsRequest.getEmail())));
//            logger.debug("Removed success");
//
//            if (mongoService.saveCredsCollection(userCredsCollection)) {
//                return Utility.getBaseResponse(HttpStatus.OK, userCredsCollection);
//            } else {
//                return Utility.getBaseResponse(HttpStatus.INTERNAL_SERVER_ERROR, Utility.getInterServerErrorList("Something went wrong, Please contact Administrator."));
//            }
//
//        } catch (Exception e) {
//            logger.error("Exception occurred while deleteUserData with probable cause - ", e);
//            Error error = new Error();
//            error.setMessage(e.getMessage());
//            return Utility.getBaseResponse(HttpStatus.INTERNAL_SERVER_ERROR, Collections.singleton(error));
//        }
//    }

//    @Override
//    public BaseResponse getUserData(UserCredsRequest userCredsRequest) {
//        try {
//            if (null == userCredsRequest || StringUtils.isEmpty(userCredsRequest.getLoginUser())) {
//                return Utility.getBaseResponse(HttpStatus.BAD_REQUEST, Utility.getBadRequestErrorList("Request is invalid."));
//            }
//            UserCredsCollection userCredsCollection = mongoService.getUserData(userCredsRequest);
//            if (null == userCredsCollection) {
//                return Utility.getBaseResponse(HttpStatus.NO_CONTENT, Utility.getNoContentErrorList());
//            }
//
//            return Utility.getBaseResponse(HttpStatus.OK, userCredsCollection);
//
//        } catch (Exception e) {
//            logger.error("Exception occurred while getUserData with probable cause - ", e);
//            Error error = new Error();
//            error.setMessage(e.getMessage());
//            return Utility.getBaseResponse(HttpStatus.INTERNAL_SERVER_ERROR, Collections.singleton(error));
//        }
//    }

    @Override
    public BaseResponse getDashboardDetails(DashboardDetailsRequest dashboardDetailsRequest) {
        logger.info("getDashboardDetails called");
        BaseResponse baseResponse = null;
        Collection<Error> errors = new ArrayList<>();

        try {
            UserCredsCollection userCredsCollection = mongoService.getUserDataByIdentifier(dashboardDetailsRequest);
            if (null == userCredsCollection) {
                errors.add(Error.builder()
                        .message(ErrorCode.USER_NOT_FOUND_ERROR)
                        .errorCode(String.valueOf(HttpStatus.BAD_REQUEST.value()))
                        .errorType(Error.ERROR_TYPE.BUSINESS.toValue())
                        .level(Error.SEVERITY.LOW.name())
                        .build());
                return Utility.getBaseResponse(HttpStatus.BAD_REQUEST, errors);
            }
            baseResponse = Utility.getBaseResponse(HttpStatus.OK, userCredsCollection);

        } catch (Exception e) {
            logger.error("Exception occurred while getDashboardDetails with probable cause - ", e);
            Error error = new Error();
            error.setMessage(e.getMessage());
            baseResponse = Utility.getBaseResponse(HttpStatus.INTERNAL_SERVER_ERROR, Collections.singleton(error));
        }
        return baseResponse;
    }

    @Override
    public BaseResponse createUser(UserCreation userCreation) {
        BaseResponse baseResponse = null;
        try {
            UserCredsCollection userCredsCollection = new UserCredsCollection();
            userCredsCollection.setUserName(userCreation.getUserName());
            userCredsCollection.setEmail(userCreation.getEmail());
            userCredsCollection.setAccountActive(true);
            userCredsCollection.setLastUpdatedDate(new Date());

            mongoService.saveUser(userCredsCollection);

            baseResponse = Utility.getBaseResponse(HttpStatus.CREATED, userCredsCollection);

        } catch (Exception e) {
            logger.error("Exception occurred while creating user - ", e);
            baseResponse = Utility.getBaseResponse(HttpStatus.INTERNAL_SERVER_ERROR, Collections.singleton(e.getMessage()));
        }
        return baseResponse;
    }
}
