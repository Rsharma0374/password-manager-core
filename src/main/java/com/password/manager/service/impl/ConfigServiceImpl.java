package com.password.manager.service.impl;

import com.password.manager.configuration.ActionConfiguration;
import com.password.manager.dao.DynamoDbService;
import com.password.manager.response.BaseResponse;
import com.password.manager.response.Error;
import com.password.manager.service.ConfigService;
import com.password.manager.utility.Utility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class ConfigServiceImpl implements ConfigService {
    private static final Logger logger = LoggerFactory.getLogger(ConfigServiceImpl.class);

    @Autowired
    private DynamoDbService dynamoDbService;

    @Override
    public BaseResponse addActionConfig(ActionConfiguration actionConfiguration) {
        logger.info("Inside add action config for action {}", actionConfiguration.getActionName());
        BaseResponse baseResponse;
        boolean success;
        try {
            success = dynamoDbService.addActionConfiguration(actionConfiguration);

            if (success) {
                baseResponse = Utility.getBaseResponse(HttpStatus.OK, "Record inserted successfully.");
            } else {
                baseResponse = Utility.getBaseResponse(HttpStatus.OK, "Record inserted failed.");
            }

            return baseResponse;

        } catch (Exception e) {
            logger.error("Exception occurred while adding action configuration for action {} with probable cause - ", actionConfiguration.getActionName(), e);
            Error error = new Error();
            error.setMessage(e.getMessage());
            return Utility.getBaseResponse(HttpStatus.INTERNAL_SERVER_ERROR, Collections.singleton(error));
        }
    }

    @Override
    public BaseResponse updateActionConfig(ActionConfiguration actionConfiguration) {
        logger.info("Inside update action config for action {}", actionConfiguration.getActionName());
        BaseResponse baseResponse;
        boolean success;
        try {
            success = dynamoDbService.updateActionConfiguration(actionConfiguration);

            if (success) {
                baseResponse = Utility.getBaseResponse(HttpStatus.OK, "Record updated successfully.");
            } else {
                baseResponse = Utility.getBaseResponse(HttpStatus.OK, "Record updating failed.");
            }

            return baseResponse;

        } catch (Exception e) {
            logger.error("Exception occurred while updating action configuration for action {} with probable cause - ", actionConfiguration.getActionName(), e);
            Error error = new Error();
            error.setMessage(e.getMessage());
            return Utility.getBaseResponse(HttpStatus.INTERNAL_SERVER_ERROR, Collections.singleton(error));
        }    }
}
