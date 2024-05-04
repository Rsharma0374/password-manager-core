package com.password.manager.service.cache.impl;

import com.password.manager.configuration.ActionConfiguration;
import com.password.manager.dao.MongoService;
import com.password.manager.model.master.ApiRoleAuthorisationMaster;
import com.password.manager.service.cache.CacheService;
import com.password.manager.service.redis.RedisService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class CacheServiceImpl implements CacheService {
    private static final Logger LOGGER = LoggerFactory.getLogger(CacheServiceImpl.class);
    private static final String ACTION = "ACTION";

    @Autowired
    RedisService redisService;

    @Autowired
    MongoService mongoService;

    @Override
    public ActionConfiguration getActionConfigurations(String product, String apiSkipAuthentication) {
        ActionConfiguration actionConfiguration = null;
        String lookupKey = generateKey(ACTION, product, apiSkipAuthentication);
        LOGGER.info("Inside getActionConfigurations, lookupKey {}", lookupKey);

        Object obj = redisService.getValueFromRedis(lookupKey);
        if (obj != null) {
            actionConfiguration = (ActionConfiguration) obj;
        } else {
            actionConfiguration = mongoService.getActionConfigByProductAndActionName(product, apiSkipAuthentication);
            if (null != actionConfiguration) {
                redisService.setValueInRedisWithExpiration(lookupKey, actionConfiguration, 24, TimeUnit.HOURS);
            }
        }
        return actionConfiguration;

    }

    @Override
    public boolean checkApiRoleAccess(String product, String apiName, String httpMethod) {
        String lookupKey = generateKey(apiName,product,httpMethod);
        LOGGER.info("Inside checkApiRoleAccess, lookupKey {}", lookupKey);
        Object obj = redisService.getValueFromRedis(lookupKey);
        boolean isApiAccessApplicable;
        if (null != obj) {
            isApiAccessApplicable =  (boolean) obj;
        } else {
            ApiRoleAuthorisationMaster apiRoleAuthorisationMaster = mongoService.getApiAuthMasterByApiName(product, apiName, httpMethod);
            if (null != apiRoleAuthorisationMaster) {
                redisService.setValueInRedisWithExpiration(lookupKey, true, 24, TimeUnit.HOURS);
                isApiAccessApplicable = true;
            } else {
                redisService.setValueInRedisWithExpiration(lookupKey, false, 24, TimeUnit.HOURS);
                isApiAccessApplicable = false;
            }
        }
        return isApiAccessApplicable;
    }

    private String generateKey(String... args) {
        return StringUtils.join(args);
    }
}
