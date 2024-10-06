package com.password.manager.dao;

import com.password.manager.configuration.ActionConfiguration;

public interface DynamoDbService {
    boolean addActionConfiguration(ActionConfiguration actionConfiguration);

    boolean updateActionConfiguration(ActionConfiguration actionConfiguration);
}
