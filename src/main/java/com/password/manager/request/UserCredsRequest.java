package com.password.manager.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.ToString;

@ToString
@Data
public class UserCredsRequest {

    @JsonProperty("sUserName")
    private String username;

    @JsonProperty("sEmail")
    private String email;

    @JsonProperty("sPassword")
    private String password;

    @JsonProperty("sService")
    private String service;

    @JsonProperty("sUrl")
    private String url;
}
