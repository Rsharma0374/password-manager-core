package com.password.manager.config;

import com.password.manager.response.BaseResponse;
import com.password.manager.service.transport.TransportUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class TokenValidationService {

    @Value("${token.validation.url}")
    private String validationUrl;

    public boolean validateToken(String token, String username) {
        try {


            validationUrl = validationUrl.concat("/" + token);

            BaseResponse res = (BaseResponse) TransportUtils.getRequest(validationUrl, token, username, BaseResponse.class);


            return res.getStatus().getStatusCode() == HttpStatus.OK.value();
        } catch (Exception e) {
            return false;
        }
    }
}
