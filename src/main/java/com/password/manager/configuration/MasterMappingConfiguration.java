package com.password.manager.configuration;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.Map;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "masterMappingConfiguration")
public class MasterMappingConfiguration {

    private String product;

    private String sourceMasterName;

    private String destinationMasterName;

    private Set<String> sourceColumnName;

    private Set<String> destinationColumnName;

    private Map<String, String> columnMapping;

    private boolean enable = true;

    private Date insertDate = new Date();

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public String getSourceMasterName() {
        return sourceMasterName;
    }

    public void setSourceMasterName(String sourceMasterName) {
        this.sourceMasterName = sourceMasterName;
    }

    public String getDestinationMasterName() {
        return destinationMasterName;
    }

    public void setDestinationMasterName(String destinationMasterName) {
        this.destinationMasterName = destinationMasterName;
    }

    public Set<String> getSourceColumnName() {
        return sourceColumnName;
    }

    public void setSourceColumnName(Set<String> sourceColumnName) {
        this.sourceColumnName = sourceColumnName;
    }

    public Set<String> getDestinationColumnName() {
        return destinationColumnName;
    }

    public void setDestinationColumnName(Set<String> destinationColumnName) {
        this.destinationColumnName = destinationColumnName;
    }

    public Map<String, String> getColumnMapping() {
        return columnMapping;
    }

    public void setColumnMapping(Map<String, String> columnMapping) {
        this.columnMapping = columnMapping;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public Date getInsertDate() {
        return insertDate;
    }

    public void setInsertDate(Date insertDate) {
        this.insertDate = insertDate;
    }
}
