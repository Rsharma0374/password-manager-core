package com.password.manager.dao;

import com.password.manager.model.UserCredsCollection;
import com.password.manager.request.UserCredsRequest;

public interface MongoService {
    UserCredsCollection getUserData(UserCredsRequest userCredsRequest);

    boolean saveCredsCollection(UserCredsCollection userCredsCollection);
}
