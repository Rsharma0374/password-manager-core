package com.password.manager.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.Set;

@Document(collection = "actionConfiguration")
public class ActionConfiguration {

    @JsonProperty("sProductName")
    private String  productName;

    @JsonProperty("sActionName")
    private String  actionName;

    @JsonProperty("bEnable")
    private boolean enable;

    @JsonProperty("dtCreatedDate")
    private Date createdDate = new Date();

    @JsonProperty("dtLastUpdateDate")
    private Date lastUpdateDate;

    @JsonProperty("sAuthenticationMode")
    private String authenticationMode;

    @JsonProperty("sAuthorisationMode")
    private String authorisationMode;

    @JsonProperty("aSkipApiList")
    private Set<String> skipApiList;

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getActionName() {
        return actionName;
    }

    public void setActionName(String actionName) {
        this.actionName = actionName;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Date getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(Date lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    public String getAuthenticationMode() {
        return authenticationMode;
    }

    public void setAuthenticationMode(String authenticationMode) {
        this.authenticationMode = authenticationMode;
    }

    public String getAuthorisationMode() {
        return authorisationMode;
    }

    public void setAuthorisationMode(String authorisationMode) {
        this.authorisationMode = authorisationMode;
    }

    public Set<String> getSkipApiList() {
        return skipApiList;
    }

    public void setSkipApiList(Set<String> skipApiList) {
        this.skipApiList = skipApiList;
    }
}
