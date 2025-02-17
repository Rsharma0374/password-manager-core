package com.password.manager.service.impl;

import com.password.manager.constant.ErrorCode;
import com.password.manager.dao.MongoService;
import com.password.manager.model.UserCredsCollection;
import com.password.manager.request.DashboardDetailsRequest;
import com.password.manager.request.UserCreation;
import com.password.manager.request.UserCredsRequest;
import com.password.manager.response.BaseResponse;
import com.password.manager.response.Error;
import com.password.manager.security.EncryptDecryptService;
import com.password.manager.service.HomeManager;
import com.password.manager.utility.Utility;
import jakarta.servlet.http.HttpServletRequest;
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
                List<UserCredsCollection.EncryptedCred> encryptedCredList = userCredsCollection.getEncryptedCredLists();
                if (CollectionUtils.isEmpty(encryptedCredList)) {
                    UserCredsCollection.CredList credList = new UserCredsCollection.CredList();
                    List<UserCredsCollection.CredList> credListsArray = new ArrayList<>();
                    List<UserCredsCollection.EncryptedCred> encryptedCredLists = new ArrayList<>();
                    UserCredsCollection.EncryptedCred encryptedCred = new UserCredsCollection.EncryptedCred();

                    credList.setEmail(userCredsRequest.getEmail());
                    credList.setPassword(userCredsRequest.getPassword());
                    credList.setUsername(userCredsRequest.getUsername());
                    credList.setService(userCredsRequest.getService());
                    credList.setUrl(userCredsRequest.getUrl());
                    String encryptedValue = EncryptDecryptService.encryptText(credList, userCredsCollection.getUserName());
                    encryptedCred.setValue(encryptedValue);
                    encryptedCredLists.add(encryptedCred);
                    userCredsCollection.setCredLists(credListsArray);
                    userCredsCollection.setEncryptedCredLists(encryptedCredLists);

                    dataSaved = mongoService.saveUser(userCredsCollection);
                    credListsArray.add(credList);
                    userCredsCollection.setCredLists(credListsArray);
                } else {
                    List<UserCredsCollection.CredList> credLists = new ArrayList<>(encryptedCredList.stream()
                            .map(encryptedCred -> {
                                try {
                                    // Decrypt the encrypted value using the username as the key
                                    String decryptedJson = EncryptDecryptService.decryptText(encryptedCred.getValue(), userCredsCollection.getUserName());

                                    // Convert JSON string back into a CredList object
                                    return EncryptDecryptService.parseJson(decryptedJson, UserCredsCollection.CredList.class);
                                } catch (Exception e) {
                                    logger.error("Error decrypting credentials", e);
                                    return null; // Handle errors gracefully
                                }
                            })
                            .filter(Objects::nonNull) // Remove any null results from failed decryptions
                            .toList());

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
                    UserCredsCollection.EncryptedCred encryptedCred = new UserCredsCollection.EncryptedCred();
                    credList.setEmail(userCredsRequest.getEmail());
                    credList.setPassword(userCredsRequest.getPassword());
                    credList.setUsername(userCredsRequest.getUsername());
                    credList.setService(userCredsRequest.getService());
                    credList.setUrl(userCredsRequest.getUrl());

                    encryptedCred.setValue(EncryptDecryptService.encryptText(credList, userCredsCollection.getUserName()));
                    userCredsCollection.getEncryptedCredLists().add(encryptedCred);
                    dataSaved = mongoService.saveUser(userCredsCollection);

                    credLists.add(credList);
                    userCredsCollection.setCredLists(credLists);

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

    @Override
    public BaseResponse updateUserData(UserCredsRequest userCredsRequest, HttpServletRequest httpServletRequest) {
        try {
            if (null == userCredsRequest) {
                return Utility.getBaseResponse(HttpStatus.BAD_REQUEST, Utility.getBadRequestErrorList("Request is invalid."));
            }
            String userName = httpServletRequest.getHeader("userName");

            UserCredsCollection userCredsCollection = mongoService.getUserDataByIdentifier(userName);
            if (null == userCredsCollection) {
                return Utility.getBaseResponse(HttpStatus.NO_CONTENT, Utility.getNoContentErrorList());
            }

            // Decrypt the encrypted credentials
            List<UserCredsCollection.CredList> credLists = userCredsCollection.getEncryptedCredLists().stream()
                    .map(encryptedCred -> {
                        try {
                            // Decrypt the encrypted value using the username as the key
                            String decryptedJson = EncryptDecryptService.decryptText(encryptedCred.getValue(), userCredsCollection.getUserName());

                            // Convert JSON string back into a CredList object
                            return EncryptDecryptService.parseJson(decryptedJson, UserCredsCollection.CredList.class);
                        } catch (Exception e) {
                            logger.error("Error decrypting credentials", e);
                            return null; // Handle errors gracefully
                        }
                    })
                    .filter(Objects::nonNull) // Remove any null results from failed decryptions
                    .toList();

            if (CollectionUtils.isEmpty(credLists)) {
                return Utility.getBaseResponse(HttpStatus.BAD_REQUEST, Utility.getBadRequestErrorList("Request is invalid."));
            }

            // Find the CredList to update
            List<UserCredsCollection.CredList> filteredCredList = credLists.stream()
                    .filter(f -> Objects.equals(f.getService(), userCredsRequest.getService()))
                    .toList();

            for (UserCredsCollection.CredList cred : filteredCredList) {
                if (StringUtils.equalsIgnoreCase(cred.getEmail(), userCredsRequest.getEmail())) {
                    // Update the matching CredList
                    cred.setUsername(userCredsRequest.getUsername());
                    cred.setEmail(userCredsRequest.getEmail());
                    cred.setService(userCredsRequest.getService());
                    cred.setPassword(userCredsRequest.getPassword());
                    cred.setUrl(userCredsRequest.getUrl());

                    // Re-encrypt the updated CredList and replace the old encrypted value
                    String encryptedValue = EncryptDecryptService.encryptText(cred, userCredsCollection.getUserName()); // Encrypt the updated JSON

                    // Find and update the corresponding encryptedCred in encryptedCredLists
                    userCredsCollection.getEncryptedCredLists().forEach(encryptedCred -> {
                        try {
                            // Decrypt to check if this encrypted entry matches the one being updated
                            String decryptedJson = EncryptDecryptService.decryptText(encryptedCred.getValue(), userCredsCollection.getUserName());
                            UserCredsCollection.CredList decryptedCred = EncryptDecryptService.parseJson(decryptedJson, UserCredsCollection.CredList.class);

                            // If the service and email match, update this entry with the new encrypted value
                            if (decryptedCred != null && StringUtils.equalsIgnoreCase(decryptedCred.getEmail(), userCredsRequest.getEmail())
                                    && StringUtils.equalsIgnoreCase(decryptedCred.getService(), userCredsRequest.getService())) {
                                encryptedCred.setValue(encryptedValue); // Set the updated encrypted value
                            }
                        } catch (Exception e) {
                            logger.error("Error updating encrypted credential", e);
                        }
                    });
                    break; // Exit loop after updating the first match
                }
            }

            // Save the updated UserCredsCollection
            if (mongoService.saveUser(userCredsCollection)) {
                userCredsCollection.setCredLists(credLists); // Set the updated credLists
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
    public BaseResponse deleteUserData(UserCredsRequest userCredsRequest, HttpServletRequest httpServletRequest) {

        try {
            if (null == userCredsRequest) {
                return Utility.getBaseResponse(HttpStatus.BAD_REQUEST, Utility.getBadRequestErrorList("Request is invalid."));
            }
            String userName = httpServletRequest.getHeader("userName");

            UserCredsCollection userCredsCollection = mongoService.getUserDataByIdentifier(userName);
            if (null == userCredsCollection) {
                return Utility.getBaseResponse(HttpStatus.NO_CONTENT, Utility.getNoContentErrorList());

            }

            List<UserCredsCollection.CredList> credLists = new ArrayList<>(userCredsCollection.getEncryptedCredLists().stream()
                    .map(encryptedCred -> {
                        try {
                            // Decrypt the encrypted value using the username as the key
                            String decryptedJson = EncryptDecryptService.decryptText(encryptedCred.getValue(), userCredsCollection.getUserName());

                            // Convert JSON string back into a CredList object
                            return EncryptDecryptService.parseJson(decryptedJson, UserCredsCollection.CredList.class);
                        } catch (Exception e) {
                            logger.error("Error decrypting credentials", e);
                            return null; // Handle errors gracefully
                        }
                    })
                    .filter(Objects::nonNull) // Remove any null results from failed decryptions
                    .toList());

            if (CollectionUtils.isEmpty(credLists)) {
                return Utility.getBaseResponse(HttpStatus.BAD_REQUEST, Utility.getBadRequestErrorList("Request is invalid."));
            }
            boolean removed = credLists.removeIf(f -> (null != f.getService()
                    && StringUtils.equalsIgnoreCase(f.getService(), userCredsRequest.getService())
                    && StringUtils.equalsIgnoreCase(f.getEmail(), userCredsRequest.getEmail())));
            logger.debug("Removed success");

            // If credentials were removed, also remove from encryptedCredLists
            if (removed) {
                // Find and remove corresponding encrypted credentials from encryptedCredLists
                userCredsCollection.getEncryptedCredLists().removeIf(encryptedCred -> {
                    try {
                        // Decrypt the value to find the matching service and email
                        String decryptedJson = EncryptDecryptService.decryptText(encryptedCred.getValue(), userCredsCollection.getUserName());
                        UserCredsCollection.CredList decryptedCred = EncryptDecryptService.parseJson(decryptedJson, UserCredsCollection.CredList.class);

                        // Check if the decrypted data matches the request
                        return decryptedCred != null && StringUtils.equalsIgnoreCase(decryptedCred.getService(), userCredsRequest.getService())
                                && StringUtils.equalsIgnoreCase(decryptedCred.getEmail(), userCredsRequest.getEmail());
                    } catch (Exception e) {
                        logger.error("Error decrypting encrypted credentials for removal", e);
                        return false; // Do not remove on decryption error
                    }
                });
                logger.debug("Removed encrypted credential successfully");
            }

            if (mongoService.saveUser(userCredsCollection)) {
                userCredsCollection.setCredLists(credLists);
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
            List<UserCredsCollection.CredList> credLists = new ArrayList<>(userCredsCollection.getEncryptedCredLists().stream()
                    .map(encryptedCred -> {
                        try {
                            // Decrypt the encrypted value using the username as the key
                            String decryptedJson = EncryptDecryptService.decryptText(encryptedCred.getValue(), userCredsCollection.getUserName());

                            // Convert JSON string back into a CredList object
                            return EncryptDecryptService.parseJson(decryptedJson, UserCredsCollection.CredList.class);
                        } catch (Exception e) {
                            logger.error("Error decrypting credentials", e);
                            return null; // Handle errors gracefully
                        }
                    })
                    .filter(Objects::nonNull) // Remove any null results from failed decryptions
                    .toList());

            userCredsCollection.setCredLists(credLists);

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
