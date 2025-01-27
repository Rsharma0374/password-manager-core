package com.password.manager.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class DashboardDetailsRequest {

    @JsonProperty("sIdentifier")
    private String identifier;

    @JsonProperty("sProductName")
    private String productName;
}
