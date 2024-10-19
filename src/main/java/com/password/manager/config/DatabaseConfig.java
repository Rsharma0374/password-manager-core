package com.password.manager.config;

import com.password.manager.utility.Utility;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.mongodb.core.convert.DbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;

import java.util.Properties;

@Configuration
public class DatabaseConfig {

    //  Mongo DB connection
    private static String mongoUri = "";
    public static final String MONGO_URI = "MONGO_URI";
    private static final String PASS_MANAGER_PROPERTIES_PATH = "/opt/configs/passmanager.properties";

    static {
        Properties properties = Utility.fetchProperties(PASS_MANAGER_PROPERTIES_PATH);
        if (null != properties) {
            mongoUri = properties.getProperty(MONGO_URI);
        }
    }

    @Bean
    public MongoDatabaseFactory mongoDbFactory() {
        return new SimpleMongoClientDatabaseFactory(mongoUri);
    }

    @Bean
    public MongoTemplate mongoTemplate() {
        return new MongoTemplate(mongoDbFactory());
    }

    @Bean
    public GridFsTemplate gridFsTemplate() throws Exception {
        return new GridFsTemplate(mongoDbFactory(), mappingMongoConverter());
    }

    @Bean
    public MappingMongoConverter mappingMongoConverter() throws Exception {
        DbRefResolver dbRefResolver = new DefaultDbRefResolver(mongoDbFactory());
        // Add any custom conversions if required
        return new MappingMongoConverter(dbRefResolver, new MongoMappingContext());
    }

}
