package com.password.manager.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.password.manager.response.BaseResponse;
import com.password.manager.response.Error;
import com.password.manager.utility.Utility;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

@Component
public class TokenValidationInterceptor implements HandlerInterceptor {

    private final Logger logger = LoggerFactory.getLogger(TokenValidationInterceptor.class);
    @Autowired
    private TokenValidationService tokenValidationService;

    private final ObjectMapper objectMapper = new ObjectMapper();


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Skip validation for public endpoints
        if (request.getRequestURI().startsWith("/public/")) {
            return true;
        }
        if (request.getMethod().equals(HttpMethod.OPTIONS.toString())) {
            return true;
        }
        String apiName = Utility.getApiName(request);
        logger.warn("apiName is: {}", apiName);
        if (apiName.equalsIgnoreCase("create-user")) {
            return true;
        }

        String token = extractToken(request);
        String userName = request.getHeader("userName");
        if (token == null) {
            setResponse(response);
            return false;
        }

        try {
            if (tokenValidationService.validateToken(token, userName)) {
                logger.warn("Token is valid");
                return true;
            } else {
                logger.error("Token is not valid");
                setResponse(response);
                return false;
            }
        } catch (Exception e) {
            setResponse(response);
            return false;
        }
    }

    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private void setResponse(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");

        // Construct the JSON response
        BaseResponse baseResponse = new BaseResponse();
        com.password.manager.response.Error error = new com.password.manager.response.Error();
        error.setErrorCode(String.valueOf(HttpServletResponse.SC_UNAUTHORIZED));
        error.setMessage("Access Denied !! ");
        error.setErrorType("SYSTEM");
        Collection<Error> errors = new ArrayList<>();
        errors.add(error);
        baseResponse = Utility.getBaseResponse(HttpStatus.UNAUTHORIZED, errors);
        // Write the response
        response.getWriter().write(objectMapper.writeValueAsString(baseResponse));
    }
}
