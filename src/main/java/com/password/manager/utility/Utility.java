package com.password.manager.utility;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.password.manager.constant.FieldSeparator;
import com.password.manager.response.BaseResponse;
import com.password.manager.response.Error;
import com.password.manager.response.Payload;
import com.password.manager.response.Status;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerMapping;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class Utility {


    private static final Logger logger = LoggerFactory.getLogger(Utility.class);

    private static ObjectMapper mapper = new ObjectMapper().registerModule(new JodaModule());

    public static final String NO_DATA_FOUND = "No data found against provided request.";

    public static BaseResponse getBaseResponse(HttpStatus httpStatus, Object buzResponse) {
        logger.info("Inside getBaseResponse method");

        if (null == buzResponse)
            buzResponse = Collections.emptyMap();

        return BaseResponse.builder()
                .payload(new Payload<>(buzResponse))
                .status(
                        Status.builder()
                                .statusCode(httpStatus.value())
                                .statusValue(httpStatus.name()).build())
                .build();
    }

    public static BaseResponse getBaseResponse(HttpStatus httpStatus, Collection<Error> errors) {
        return BaseResponse.builder()
                .status(
                        Status.builder()
                                .statusCode(httpStatus.value())
                                .statusValue(httpStatus.name()).build())
                .errors(errors)
                .build();
    }

    public static Collection<Error> getNoContentErrorList() {
        Collection<Error> errors = new ArrayList<>();
        errors.add(Error.builder()
                .message(NO_DATA_FOUND)
                .errorCode(String.valueOf(Error.ERROR_TYPE.DATABASE.toCode()))
                .errorType(Error.ERROR_TYPE.DATABASE.toValue())
                .level(Error.SEVERITY.LOW.name())
                .build());
        return errors;
    }

    public static Collection<Error> getBadRequestErrorList(String errorMsg){
        Collection<Error> errors = new ArrayList<>();
        errors.add(Error.builder()
                .errorType(Error.ERROR_TYPE.BAD_REQUEST.toValue())
                .errorCode(String.valueOf(Error.ERROR_TYPE.BAD_REQUEST.toCode()))
                .message(errorMsg)
                .build());
        return errors;
    }

    public static Collection<Error> getInterServerErrorList(String errorMsg){
        Collection<Error> errors = new ArrayList<>();
        errors.add(Error.builder()
                .errorType(Error.ERROR_TYPE.SYSTEM.toValue())
                .errorCode(String.valueOf(Error.ERROR_TYPE.SYSTEM.toCode()))
                .message(errorMsg)
                .build());
        return errors;
    }

    public static String encryptThisString(String input) {


        try {
            // getInstance() method is called with algorithm SHA-1
            MessageDigest md = MessageDigest.getInstance("SHA-1");

            // digest() method is called to calculate message digest of the input string returned as array of byte
            byte[] messageDigest = md.digest(input.getBytes());

            // Convert byte array into signum representation
            BigInteger no = new BigInteger(1, messageDigest);

            // Convert message digest into hex value
            String hashtext = no.toString(16);

            // Add preceding 0s to make it 40 bit
            while (hashtext.length() < 40) {
                hashtext = "0" + hashtext;
            }

            // return the HashText
            return hashtext;
        }

        // For specifying wrong message digest algorithms
        catch (NoSuchAlgorithmException e) {
            logger.error("Exception occurred at sha conversion due to - ", e);
            throw new RuntimeException(e);
        }
    }

    public static String generateOtpAgainstLength(int length) {
        // Using numeric values
        String numbers = "0123456789";

        // Using random method
        Random rndm_method = new Random();

        StringBuilder  otp=new StringBuilder();

        for (int i = 0; i < length; i++) {
            otp.append(numbers.charAt(rndm_method.nextInt(numbers.length())));
        }
        return otp.toString();
    }


    public static String generateStringAgainstLength(int length) {
        // Using numeric values
        String numbers = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

        // Using random method
        Random rndm_method = new Random();

        StringBuilder  string = new StringBuilder();

        for (int i = 0; i < length; i++) {
            string.append(numbers.charAt(rndm_method.nextInt(numbers.length())));
        }
        return string.toString();
    }

    public static String ObjectToString(Object object) throws JsonProcessingException {
        // mapper.configure(MapperFeature.CAN_OVERRIDE_ACCESS_MODIFIERS,false);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper.writeValueAsString(object);
    }

    public static <T> T StringToObject(String jsonString, Class<?> type) throws IOException {
        return (T)mapper.readValue(jsonString, type);

    }

    public static String getApiName(HttpServletRequest request) {

        String apiName = request.getRequestURI();

        Map object = (Map) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        if(object != null && object.size() > 0){
            for(int i = 0 ; i < object.size(); i++){
                apiName = StringUtils.substringBeforeLast(apiName, FieldSeparator.FORWARD_SLASH);
            }
        }
        apiName = StringUtils.substringAfterLast(apiName, FieldSeparator.FORWARD_SLASH);

        return apiName;
    }

    public static Map<String, Object> convertToMap(Object redisObj) {
        Map<String, Object> redisClass = new HashMap<>();
        Field[] fields = redisObj.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                redisClass.put(field.getName(), field.get(redisObj));
            } catch (IllegalAccessException e) {
                logger.error("Exception occurred while converting object to map with probable cause -", e);
            }
        }
        return redisClass;
    }

    public static Properties fetchProperties(String passManagerPropertiesPath) {
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(passManagerPropertiesPath));
            return properties;
        } catch (IOException e) {
            logger.error("Exception occurred while getting pass manager config with probable cause - ", e);
            return null;
        }
    }
}
