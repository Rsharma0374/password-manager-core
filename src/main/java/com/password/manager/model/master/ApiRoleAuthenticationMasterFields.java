package com.password.manager.model.master;

public enum ApiRoleAuthenticationMasterFields {

    API_NAME("apiName"),
    METHOD("methodType"),
    PRODUCT("product");

    private String value;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    ApiRoleAuthenticationMasterFields(String value) {
        this.value = value;
    }
}
