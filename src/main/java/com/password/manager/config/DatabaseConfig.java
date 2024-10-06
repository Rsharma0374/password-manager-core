package com.password.manager.config;

import com.password.manager.utility.Utility;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.util.Properties;

@Configuration
public class DatabaseConfig {

    private static String ACCESS_KEY = "";
    private static String SECRET_KEY = "";
    private static final String DYNAMO_DB_ACCESS_KEY = "DYNAMO_DB_ACCESS_KEY";
    private static final String DYNAMO_DB_SECRET_KEY = "DYNAMO_DB_SECRET_KEY";
    private static final String PASS_MANAGER_PROPERTIES_PATH = "/opt/configs/passmanager.properties";

    static {
        Properties properties = Utility.fetchProperties(PASS_MANAGER_PROPERTIES_PATH);
        if (null != properties) {
            ACCESS_KEY = properties.getProperty(DYNAMO_DB_ACCESS_KEY);
            SECRET_KEY = properties.getProperty(DYNAMO_DB_SECRET_KEY);
        }
    }

    @Bean
    public DynamoDbClient dynamoDbClient() {
        return DynamoDbClient.builder()
                .region(Region.AP_SOUTH_1)
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(ACCESS_KEY, SECRET_KEY)))
                .build();
    }

    @Bean
    public DynamoDbEnhancedClient dynamoDbEnhancedClient(DynamoDbClient dynamoDbClient) {
        return DynamoDbEnhancedClient.builder()
                .dynamoDbClient(dynamoDbClient)
                .build();
    }

}
