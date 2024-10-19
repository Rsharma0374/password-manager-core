package com.password.manager.configuration;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "actionConfiguration")
public class ActionConfiguration {

    @Id
    private String id;

    private String  productName;

    private String  actionName;

    private boolean enable;

    private Date createdDate;

    private Date lastUpdateDate;

    private String authenticationMode;

    private String authorisationMode;

    private Set<String> skipApiList;

}
