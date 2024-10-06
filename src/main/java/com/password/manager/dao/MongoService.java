package com.password.manager.dao;

import com.password.manager.configuration.ActionConfiguration;
import com.password.manager.configuration.MasterMappingConfiguration;
import com.password.manager.model.UserCredsCollection;
import com.password.manager.model.master.ApiRoleAuthorisationMaster;
import com.password.manager.request.UserCredsRequest;

import java.util.List;

public interface MongoService {
    UserCredsCollection getUserData(UserCredsRequest userCredsRequest);

    boolean saveCredsCollection(UserCredsCollection userCredsCollection);

    ActionConfiguration getActionConfigByProductAndActionName(String product, String apiSkipAuthentication);

    ApiRoleAuthorisationMaster getApiAuthMasterByApiName(String product, String apiName, String httpMethod);

    MasterMappingConfiguration getMasterMappingConfiguration(String product, String masterName);

    boolean insertApiRoleAuthorisationMaster(List<ApiRoleAuthorisationMaster> apiRoleAuthorisationMasters, String product);

}
