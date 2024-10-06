package com.password.manager.service;

import com.password.manager.configuration.ActionConfiguration;
import com.password.manager.response.BaseResponse;

public interface ConfigService {
    BaseResponse addActionConfig(ActionConfiguration actionConfiguration);

    BaseResponse updateActionConfig(ActionConfiguration actionConfiguration);
}
