package com.password.manager.dao.impl;

import com.password.manager.configuration.ActionConfiguration;
import com.password.manager.dao.DynamoDbService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DynamoDbServiceImpl implements DynamoDbService {

    private static final Logger logger = LoggerFactory.getLogger(DynamoDbServiceImpl.class);

    private final DynamoDbEnhancedClient dynamoDbEnhancedClient;
    private final DynamoDbClient dynamoDbClient;

    public DynamoDbServiceImpl(DynamoDbEnhancedClient dynamoDbEnhancedClient, DynamoDbClient dynamoDbClient) {
        this.dynamoDbEnhancedClient = dynamoDbEnhancedClient;
        this.dynamoDbClient = dynamoDbClient;
    }


    @Override
    public boolean addActionConfiguration(ActionConfiguration actionConfiguration) {
        logger.info("Inside add action configuration method in dynamo db service impl for action {}", actionConfiguration.getActionName());

        try {

            // Access the table
            DynamoDbTable<ActionConfiguration> table = dynamoDbEnhancedClient.table("actionConfiguration", TableSchema.fromBean(ActionConfiguration.class));

            String actionName = actionConfiguration.getActionName();
            String productName = actionConfiguration.getProductName();
            boolean enable = actionConfiguration.isEnable();

            // Build a Scan Request with filter expression
            ScanRequest scanRequest = ScanRequest.builder()
                    .tableName("actionConfiguration")
                    .filterExpression("productName = :productName and actionName = :actionName and #enable = :enable")
                    .expressionAttributeNames(Map.of(
                            "#enable", "enable" // Use a placeholder for the reserved keyword
                    ))
                    .expressionAttributeValues(Map.of(
                            ":productName", AttributeValue.builder().s(productName).build(),
                            ":actionName", AttributeValue.builder().s(actionName).build(),
                            ":enable", AttributeValue.builder().bool(enable).build()
                    ))

                    .build();

            // Perform the scan
            ScanResponse scanResponse = dynamoDbClient.scan(scanRequest);

            // Use the DynamoDB Enhanced Client's table object to directly convert items
            List<ActionConfiguration> actionConfigurationList = scanResponse.items().stream()
                    .map(item -> table.getItem(Key.builder().partitionValue(item.get("id").s()).build())) // This uses the partition key
                    .collect(Collectors.toList());

            // If no item exists with the same actionName, proceed to insert
            if (actionConfigurationList.isEmpty()) {
                // Insert the ActionConfiguration object into the table
                table.putItem(actionConfiguration);
                logger.info("Successfully inserted action configuration with action name {}", actionConfiguration.getActionName());
                return true;
            } else {
                logger.warn("An action configuration with action name {} already exists. No insertion performed.", actionConfiguration.getActionName());
                return false;
            }
        } catch (Exception e) {
            logger.error("Exception occurred while dynamodb execution for action name {} with probable cause - ", actionConfiguration.getActionName(), e);
            return false;
        }
    }

    @Override
    public boolean updateActionConfiguration(ActionConfiguration actionConfiguration) {
        logger.info("Inside update action configuration method in dynamo db service impl for action {}", actionConfiguration.getActionName());

        String actionName = actionConfiguration.getActionName();
        String productName = actionConfiguration.getProductName();
        boolean enable = actionConfiguration.isEnable();
        Set<String> apiList = actionConfiguration.getSkipApiList();

        // Step 1: Query to get the item based on actionName
        // Create a QueryRequest for the GSI
        QueryRequest queryRequest = QueryRequest.builder()
                .tableName("actionConfiguration")
                .indexName("actionName-index")
                .keyConditionExpression("actionName = :actionNameVal")
                .expressionAttributeValues(Map.of(":actionNameVal", AttributeValue.builder().s(actionName).build()))
                .build();

        QueryResponse queryResponse = dynamoDbClient.query(queryRequest);
        List<Map<String, AttributeValue>> items = queryResponse.items();


        try {
            if (!items.isEmpty()) {
                String id = items.get(0).get("id").s();  // Get the item's id

                // Step 2: Now use this id to update the item
                Map<String, AttributeValue> key = new HashMap<>();
                key.put("id", AttributeValue.builder().s(id).build());

                String updateExpression = "ADD skipApiList :newSkipApi";
                Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
                expressionAttributeValues.put(":newSkipApi", AttributeValue.builder().ss(apiList).build());

                UpdateItemRequest updateItemRequest = UpdateItemRequest.builder()
                        .tableName("actionConfiguration")
                        .key(key)
                        .updateExpression(updateExpression)
                        .expressionAttributeValues(expressionAttributeValues)
                        .build();

                dynamoDbClient.updateItem(updateItemRequest);
            } else {
                logger.error("Action config is found empty.");
            }


            // Execute the update
//            dynamoDbClient.updateItem(updateItemRequest);
            logger.warn("Successfully added to skipApiList.");
            return true;
        } catch (Exception e) {
            logger.error("Exception occurred while updating dynamodb execution for action name {} with probable cause - ", actionConfiguration.getActionName(), e);
            return false;
        }
    }

}
