package com.password.manager.service;

import com.password.manager.request.DashboardDetailsRequest;
import com.password.manager.request.LoginRequest;
import com.password.manager.request.UserCreation;
import com.password.manager.request.UserCredsRequest;
import com.password.manager.response.BaseResponse;
import jakarta.servlet.http.HttpServletRequest;

public interface HomeManager {
    BaseResponse saveUserData(UserCredsRequest userCredsRequest, HttpServletRequest httpServletRequest);
//
    BaseResponse updateUserData(UserCredsRequest userCredsRequest, HttpServletRequest httpServletRequest);
//
    BaseResponse deleteUserData(UserCredsRequest userCredsRequest, HttpServletRequest httpServletRequest);
//
//    BaseResponse getUserData(UserCredsRequest userCredsRequest);

    BaseResponse getDashboardDetails(DashboardDetailsRequest dashboardDetailsRequest);

    BaseResponse createUser(UserCreation userCreation);
}
