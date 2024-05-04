package com.password.manager.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.ToString;

/**
 * @author rahul
 */

@ToString
@Data
public class Name {

    @JsonProperty("sFirstName")
    private String firstName;

    @JsonProperty("sMiddleName")
    private String middleName;

    @JsonProperty("sLastName")
    private String lastName;

    @JsonProperty("sPrefix")
    private String prefix;

    @JsonProperty("sSuffix")
    private String suffix;
}
