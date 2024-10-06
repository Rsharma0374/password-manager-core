package com.password.manager.configuration;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

import java.time.Instant;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@DynamoDbBean
public class ActionConfiguration {

    private String id;

    private String  productName;

    private String  actionName;

    private boolean enable;

    private Instant createdDate = Instant.now();

    private Instant lastUpdateDate;

    private String authenticationMode;

    private String authorisationMode;

    private Set<String> skipApiList;

    @DynamoDbPartitionKey
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @DynamoDbAttribute("productName")
    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    @DynamoDbAttribute("actionName")
    public String getActionName() {
        return actionName;
    }

    public void setActionName(String actionName) {
        this.actionName = actionName;
    }

    @DynamoDbAttribute("enable")
    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    @DynamoDbAttribute("createdDate")
    public Instant getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Instant createdDate) {
        this.createdDate = createdDate;
    }

    @DynamoDbAttribute("lastUpdateDate")
    public Instant getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(Instant lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    @DynamoDbAttribute("authenticationMode")
    public String getAuthenticationMode() {
        return authenticationMode;
    }

    public void setAuthenticationMode(String authenticationMode) {
        this.authenticationMode = authenticationMode;
    }

    @DynamoDbAttribute("authorisationMode")
    public String getAuthorisationMode() {
        return authorisationMode;
    }

    public void setAuthorisationMode(String authorisationMode) {
        this.authorisationMode = authorisationMode;
    }

    @DynamoDbAttribute("skipApiList")
    public Set<String> getSkipApiList() {
        return skipApiList;
    }

    public void setSkipApiList(Set<String> skipApiList) {
        this.skipApiList = skipApiList;
    }
}
