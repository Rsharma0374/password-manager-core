package com.password.manager.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

@Document (collection = "credentialCollection")
@ToString
@Data
public class UserCredsCollection {

    @Id
    @JsonProperty("sUserName")
    private String userName;

    @JsonProperty("sEmailId")
    private String emailId;

    @JsonProperty("aCredsList")
    List<CredList> credLists;

    @JsonProperty("dtLastUpdatedDate")
    private Date lastUpdatedDate;

    @JsonProperty("bAccountActive")
    private boolean accountActive;

    @ToString
    @Data
    public static class CredList {

        @JsonProperty("sUserName")
        private String username;

        @JsonProperty("sEmail")
        private String email;

        @JsonProperty("sPassword")
        private String password;

        @JsonProperty("sService")
        private String service;

    }
}
