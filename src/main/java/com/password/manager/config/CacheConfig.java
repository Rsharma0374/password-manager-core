package com.password.manager.config;

import com.password.manager.utility.Utility;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class CacheConfig {

    public static Map<String , Object> CACHE = new ConcurrentHashMap<String,Object>();
    private static final String SECRET_KEY = "SECRET_KEY";

    static {
        try {
            Map config = InfisicalConfig.fetchConfig("PasswordManager");
            CACHE.put(SECRET_KEY, null != config ? config.get(SECRET_KEY) : null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
