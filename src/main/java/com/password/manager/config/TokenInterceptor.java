package com.password.manager.config;

import com.password.manager.configuration.ActionConfiguration;
import com.password.manager.constant.FieldSeparator;
import com.password.manager.dao.MongoService;
import com.password.manager.response.BaseResponse;
import com.password.manager.service.cache.CacheService;
import com.password.manager.service.redis.RedisService;
import com.password.manager.utility.Utility;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.CharEncoding;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.entity.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.servlet.AsyncHandlerInterceptor;

import java.io.IOException;
import java.util.Map;

@Component
public class TokenInterceptor implements AsyncHandlerInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(TokenInterceptor.class);

    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String PRODUCT = "Product";
    public static final String USER_ID = "userId";
    public static final int SEVEN = 7;
    public static final String API_SKIP_AUTHENTICATION = "API_SKIP_AUTHENTICATION";
    public static final String API_ROLE_AUTHORISATION_ENABLE = "API_ROLE_AUTHORISATION_ENABLE";
    public static final String AUTH_SKIPPED_COMPLETELY_MESSAGE = "API {} authentication/authorization skipped for product: {}";
    public static final String PRODUCT_CANNOT_BE_EMPTY = "product cannot be empty";
    public static final String AUTH_FAILED_MESSAGE = "API auth failed with cause: {}";
    public static final String NON_AUTHORITATIVE_INFORMATION = "Not authorized to access this service.";
    public static final String EXCEPTION_WRITING_NEGATIVE_RESPONSE = "Exception writing negative response";
    public static final String AUTH_CONFIG_NOT_PRESENT = "Authenticate and Authorization config not present for product: %s";
    public static final String USER_TOKEN_AUTHENTICATION_SKIPPED = "User token authentication is skipped for product: {}";
    private static final String NO_LOGIN_RESPONSE_FOUND = "No login response found for user";
    private static final String DIFFERENT_AUTH_TOKEN_FOUND_FOR_USER = "Different authToken found during user authentication";
    private static final String USER_ROLE_AUTHORIZATION_SKIPPED = "User role authorization is skipped for product: {}, authorization mode: {}";
    private static final String API_ROLE_ACCESS_CHECK_FAILED = "API authorization failed for product: %s, apiName: %s, httpMethod: %s";
    public static final String AUTHORIZATION_HEADER_PREFIX = "Bearer ";


    @Autowired
    RedisService redisService;

    @Autowired
    MongoService mongoService;

    @Autowired
    CacheService cacheService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        try {
            String apiName = Utility.getApiName(request);
            String product = request.getHeader(PRODUCT);
//            validateTrueOrThrow(StringUtils.isNotBlank(product), PRODUCT_CANNOT_BE_EMPTY);
//
//
//            boolean apiPresentInSkipConfig = isApiPresentInSkipConfig(apiName, product);
//            if (apiPresentInSkipConfig) {
//                LOGGER.debug(AUTH_SKIPPED_COMPLETELY_MESSAGE, apiName, product);
//                return true;
//            }
//
//            authenticateAndAuthorizeUser(request, product, apiName);

            return true;

        } catch (ApplicationSecurityException e) {
            LOGGER.warn(AUTH_FAILED_MESSAGE, e.message);
            setNegativeResponse(response);
            return false;
        }

    }

    private boolean isApiPresentInSkipConfig(String apiName, String product) {
        ActionConfiguration skipConfiguration = cacheService.getActionConfigurations(product, API_SKIP_AUTHENTICATION);
        return skipConfiguration != null && !CollectionUtils.isEmpty(skipConfiguration.getSkipApiList()) && skipConfiguration.getSkipApiList().contains(apiName);
    }

    private void validateTrueOrThrow(boolean b, String message) {
        if (!b) {
            throw new ApplicationSecurityException(message);
        }
    }

    private static class ApplicationSecurityException extends RuntimeException {
        private final String message;

        public ApplicationSecurityException(String message) {
            this.message = message;
        }
    }

    private void setNegativeResponse(HttpServletResponse response) {
        response.setStatus(HttpStatus.OK.value());
        response.setContentType(ContentType.APPLICATION_JSON.getMimeType());
        response.setCharacterEncoding(CharEncoding.UTF_8);
        try {
            response.getWriter().write(Utility.ObjectToString(UNAUTHORIZED_BASE_RESPONSE));
        } catch (IOException e) {
            LOGGER.error(EXCEPTION_WRITING_NEGATIVE_RESPONSE, e);
        }
    }

    private void authenticateAndAuthorizeUser(HttpServletRequest request, String product, String apiName) {
        ActionConfiguration authConfig = cacheService.getActionConfigurations(product, API_ROLE_AUTHORISATION_ENABLE);
        validateAuthenticateAndAuthorizeConfigPresent(product, authConfig);
        validateUserIsAuthenticated(request, product, authConfig);
        validateUserIsAuthorized(request, product, apiName, authConfig);
    }

    private void validateAuthenticateAndAuthorizeConfigPresent(String product, ActionConfiguration authConfig) {
        if (authConfig == null) {
            throw new ApplicationSecurityException(String.format(AUTH_CONFIG_NOT_PRESENT, product));
        }
    }

    private void validateUserIsAuthenticated(HttpServletRequest request, String product, ActionConfiguration authConfig) {
         if (authConfig.getAuthenticationMode().equalsIgnoreCase("ON")) {
            authenticateUser(request, product);
        } else {
            LOGGER.warn(USER_TOKEN_AUTHENTICATION_SKIPPED, product, authConfig.getAuthenticationMode());
        }
    }

    private void authenticateUser(HttpServletRequest request, String product) {
        String userId = request.getHeader(USER_ID);
        String redisKey = StringUtils.join(userId, FieldSeparator.UNDER_SCORE_STR, product);
        Object obj = redisService.getValueFromRedis(redisKey);
        if (null == obj) {
            throw new ApplicationSecurityException(NO_LOGIN_RESPONSE_FOUND);
        }
        Map<String, Object> redisMap = Utility.convertToMap(obj);
        String token = (String) redisMap.get("token");
        String username = (String) redisMap.get("username");
        validateUserLoggedIn(username);
        validateTokenMatches(request, token);
    }

    private void validateUserLoggedIn(String username) {
        if (StringUtils.isBlank(username)) {
            throw new ApplicationSecurityException(NO_LOGIN_RESPONSE_FOUND);
        }
    }

    private void validateTokenMatches(HttpServletRequest request, String redisToken) {
        String authToken = getAuthTokenFromAuthorizationHeader(request.getHeader(AUTHORIZATION_HEADER));

        if (!StringUtils.equals(authToken, redisToken)) {
            throw new ApplicationSecurityException(DIFFERENT_AUTH_TOKEN_FOUND_FOR_USER);
        }
    }

    private String getAuthTokenFromAuthorizationHeader(String authorization) {
        if (authorization.startsWith(AUTHORIZATION_HEADER_PREFIX)) {
            return authorization.substring(SEVEN);
        }
        return authorization;
    }

    private void validateUserIsAuthorized(HttpServletRequest request, String product, String apiName, ActionConfiguration authConfig) {
        if (authConfig.getAuthorisationMode().equalsIgnoreCase("ON")) {
            authorizeUserRoleForApiCall(request, apiName, product);
        } else {
            LOGGER.warn(USER_ROLE_AUTHORIZATION_SKIPPED, product, authConfig.getAuthorisationMode());
        }
    }

    private void authorizeUserRoleForApiCall(HttpServletRequest request, String apiName, String product) {
        boolean apiRoleAccess = false;
        String httpMethod = request.getMethod();
            apiRoleAccess = cacheService.checkApiRoleAccess(product, apiName, httpMethod);

        if (!apiRoleAccess) {
            throw new ApplicationSecurityException(String.format(API_ROLE_ACCESS_CHECK_FAILED, product, apiName, httpMethod));
        }
    }


    private static final BaseResponse UNAUTHORIZED_BASE_RESPONSE = Utility.getBaseResponse(HttpStatus.UNAUTHORIZED, NON_AUTHORITATIVE_INFORMATION);


}
