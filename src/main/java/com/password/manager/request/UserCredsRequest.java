package com.password.manager.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.ToString;

@ToString
public class UserCredsRequest {

    @JsonProperty("sUsername")
    private String username;

    @JsonProperty("sEmail")
    private String email;

    @JsonProperty("sPassword")
    private String password;

    @JsonProperty("sLoginUser")
    private String loginUser;

    @JsonProperty("sPlatformName")
    private String platformName;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getLoginUser() {
        return loginUser;
    }

    public void setLoginUser(String loginUser) {
        this.loginUser = loginUser;
    }

    public String getPlatformName() {
        return platformName;
    }

    public void setPlatformName(String platformName) {
        this.platformName = platformName;
    }
}
