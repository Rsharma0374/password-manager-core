package com.password.manager.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

@Document (collection = "CredsCollection")
public class UserCredsCollection {

    @Id
    @JsonProperty("sLoginUserName")
    private String loginUsername;

    @JsonProperty("aCredsList")
    List<CredList> credLists;

    @JsonProperty("dtLastUpdatedDate")
    private Date lastUpdatedDate;

    public String getLoginUsername() {
        return loginUsername;
    }

    public void setLoginUsername(String loginUsername) {
        this.loginUsername = loginUsername;
    }

    public List<CredList> getCredLists() {
        return credLists;
    }

    public void setCredLists(List<CredList> credLists) {
        this.credLists = credLists;
    }

    public Date getLastUpdatedDate() {
        return lastUpdatedDate;
    }

    public void setLastUpdatedDate(Date lastUpdatedDate) {
        this.lastUpdatedDate = lastUpdatedDate;
    }

    public static class CredList {

        @JsonProperty("sUserName")
        private String username;

        @JsonProperty("sEmail")
        private String email;

        @JsonProperty("sPassword")
        private String password;

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
    }
}
