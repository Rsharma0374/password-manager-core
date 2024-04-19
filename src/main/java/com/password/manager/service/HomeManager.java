package com.password.manager.service;

import com.password.manager.request.UserCredsRequest;
import com.password.manager.response.BaseResponse;

public interface HomeManager {
    BaseResponse saveUserData(UserCredsRequest userCredsRequest);

    BaseResponse updateUserData(UserCredsRequest userCredsRequest);

    BaseResponse deleteUserData(UserCredsRequest userCredsRequest);

    BaseResponse getUserData(UserCredsRequest userCredsRequest);
}
