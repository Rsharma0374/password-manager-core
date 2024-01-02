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

import java.util.*;

@Service
public class HomeManagerImpl implements HomeManager {
    private static final Logger logger = LoggerFactory.getLogger(HomeManagerImpl.class);

    @Autowired
    MongoService mongoService;

    @Override
    public BaseResponse saveUserData(UserCredsRequest userCredsRequest) {

        boolean dataSaved;
        try {
            if (null == userCredsRequest || StringUtils.isEmpty(userCredsRequest.getPassword()) || StringUtils.isEmpty(userCredsRequest.getLoginUser())) {
                return ResponseUtility.getBaseResponse(HttpStatus.BAD_REQUEST, "Request is invalid.");
            }
            UserCredsCollection userCredsCollection = mongoService.getUserData(userCredsRequest);

            if (null != userCredsCollection) {

                UserCredsCollection.CredList credList = new UserCredsCollection.CredList();
                credList.setEmail(userCredsRequest.getEmail());
                credList.setPassword(userCredsRequest.getPassword());
                credList.setUsername(userCredsRequest.getUsername());
                userCredsCollection.getCredLists().add(credList);

                dataSaved = mongoService.saveCredsCollection(userCredsCollection);

            } else {
                UserCredsCollection userCredsCollectionNew = new UserCredsCollection();
                userCredsCollectionNew.setLoginUsername(userCredsRequest.getLoginUser());

                UserCredsCollection.CredList credList = new UserCredsCollection.CredList();
                List<UserCredsCollection.CredList> credLists = new ArrayList<>();

                credList.setEmail(userCredsRequest.getEmail());
                credList.setPassword(userCredsRequest.getPassword());
                credList.setUsername(userCredsRequest.getUsername());

                credLists.add(credList);
                userCredsCollectionNew.setCredLists(credLists);

                userCredsCollectionNew.setLastUpdatedDate(new Date());

                dataSaved = mongoService.saveCredsCollection(userCredsCollectionNew);

            }

            if (dataSaved) {
                return ResponseUtility.getBaseResponse(HttpStatus.OK, "Data saved Successfully.");
            } else {
                return ResponseUtility.getBaseResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong, Please contact Administrator.");
            }

        } catch (Exception e) {
            logger.error("Exception occurred while saving user creds with probable cause - ", e);
            Error error = new Error();
            error.setMessage(e.getMessage());
            return ResponseUtility.getBaseResponse(HttpStatus.INTERNAL_SERVER_ERROR, Collections.singleton(error));
        }
    }
}
