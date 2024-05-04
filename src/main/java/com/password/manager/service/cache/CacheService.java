package com.password.manager.service.cache;

import com.password.manager.configuration.ActionConfiguration;

public interface CacheService {
    ActionConfiguration getActionConfigurations(String product, String apiSkipAuthentication);

    boolean checkApiRoleAccess(String product, String apiName, String httpMethod);
}
