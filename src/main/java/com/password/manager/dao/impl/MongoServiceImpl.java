package com.password.manager.dao.impl;

import com.mongodb.BasicDBObject;
import com.password.manager.configuration.ActionConfiguration;
import com.password.manager.dao.MongoService;
import com.password.manager.model.UserCredsCollection;
import com.password.manager.model.master.ApiRoleAuthorisationMaster;
import com.password.manager.request.UserCredsRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class MongoServiceImpl implements MongoService {

    private static final Logger logger = LoggerFactory.getLogger(MongoServiceImpl.class);

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private GridFsTemplate gridFsTemplate;

    @Override
    public UserCredsCollection getUserData(UserCredsRequest userCredsRequest) {
        logger.info("Inside getUserData for user {}", userCredsRequest.getLoginUser());
        try {
            Query query = new Query();
            query.addCriteria(Criteria.where("loginUsername").is(userCredsRequest.getLoginUser()));

            return mongoTemplate.findOne(query, UserCredsCollection.class);
        } catch (Exception e) {
            logger.error("Exception occurred due to - ", e);
            return null;
        }
    }

    @Override
    public boolean saveCredsCollection(UserCredsCollection userCredsCollection) {
        logger.info("Inside save cred collection method for user - {}", userCredsCollection.getLoginUsername());

        try {
            Query query = new Query();
            query.addCriteria(Criteria.where("loginUsername").is(userCredsCollection.getLoginUsername()));

            boolean isExist = mongoTemplate.exists(query, UserCredsCollection.class);

            if (!isExist) {
                mongoTemplate.insert(userCredsCollection);
            } else {
                // If the document exists, update it using upsert logic
                Query query1 = new Query(Criteria.where("loginUsername").is(userCredsCollection.getLoginUsername()));
                Update update = new Update();
                update.set("credLists", userCredsCollection.getCredLists());
                update.set("lastUpdatedDate", new Date());
                mongoTemplate.updateFirst(query1, update, UserCredsCollection.class);
            }
            return true;

        } catch (Exception e) {
            logger.error("Exception occurred in method save creds collection for user {} with probable cause - ", userCredsCollection.getLoginUsername(), e);
            return false;
        }
    }

    @Override
    public ActionConfiguration getActionConfigByProductAndActionName(String product, String apiSkipAuthentication) {
        try {
            Query query = new Query();
            query.addCriteria(Criteria.where("productName").is(product)
                    .and("actionName").is(apiSkipAuthentication)
                    .and("enable").is(true));

            return mongoTemplate.findOne(query, ActionConfiguration.class);
        } catch (Exception e) {
            logger.error("Exception occurred while fetching action config with probable cause - ", e);
            return null;
        }
    }

    @Override
    public ApiRoleAuthorisationMaster getApiAuthMasterByApiName(String product, String apiName, String httpMethod) {
        try {
            Query query = new Query();
            query.addCriteria(Criteria.where("product").is(product)
                    .and("apiName").is(apiName)
                    .and("methodType").is(httpMethod)
                    .and("active").is(true));

            return mongoTemplate.findOne(query, ApiRoleAuthorisationMaster.class);

        } catch (Exception e) {
            logger.error("Exception occurred while fetching authentication master for api {}, product {} and method {} with probable cause - ",apiName, product, httpMethod, e);
            return null;
        }
    }
}
