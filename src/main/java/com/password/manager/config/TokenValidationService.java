package com.password.manager.config;

import com.password.manager.response.BaseResponse;
import com.password.manager.service.transport.TransportUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class TokenValidationService {
    private final Logger logger = LoggerFactory.getLogger(TokenValidationService.class);

    @Value("${token.validation.url}")
    private String validationUrl;

    public boolean validateToken(String token, String username) {
        try {

            String url = validationUrl + "/" + token;
            logger.warn("Validating token: " + url);


            int responseCode = TransportUtils.validateToken(url, token, username);
            logger.warn("Response code: " + responseCode);

            return responseCode == HttpStatus.OK.value();
        } catch (Exception e) {
            logger.error("Exception occurred while validating token with probable cause: ", e);
            return false;
        }
    }
}
