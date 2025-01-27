package com.password.manager.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.password.manager.model.Address;
import com.password.manager.model.Name;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
public class UserCreation {

    @JsonProperty("sUserName")
    private String userName;

    @JsonProperty("sEmail")
    private String email;

    @JsonProperty("bAccountActive")
    private boolean accountActive;

}
