package com.password.manager.service.transport;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.password.manager.utility.Utility;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class TransportUtils {
    private static final Logger logger = LoggerFactory.getLogger(TransportUtils.class);


    public static Object postJsonRequest(Object jsonRequest, String url, Class<?> name) throws Exception {

        logger.debug("postJsonRequest  started for url {} with input request {} ", url, jsonRequest);

        try {

            if (StringUtils.isBlank(url)) {


                logger.error("postJsonRequest config not setup for service fetching response for {}", name.getSimpleName());

                throw new Exception(String.format("postJsonRequest config not setup for service fetching response for {} ", name.getSimpleName()));
            }
            String inputJson = Utility.ObjectToString(jsonRequest);

            // Create an instance of HttpClient
            HttpClient httpClient = HttpClient.newHttpClient();

            // Create a POST request with JSON payload
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(inputJson))
                    .build();

            // Send the POST request and receive the response
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            String payLoadString = "";
            JSONObject jsonObject = new JSONObject(response.body());
            if (jsonObject.has("oBody")) {
                JSONObject oBody = jsonObject.getJSONObject("oBody");
                if (null != oBody && oBody.has("payLoad")) {
                    JSONObject payLoad = oBody.getJSONObject("payLoad");
                    payLoadString = payLoad.toString();
                    logger.info(payLoadString);

                }
            }
            return Utility.StringToObject(payLoadString, name);

        } catch (JsonProcessingException e) {

            logger.error("Error occured during postJsonRequest JsonProcessingException :: " + e);

            throw new Exception(e);

        } catch (InterruptedException e) {

            logger.error("Error occurred during postJsonRequest Exception :: " + e);

            throw new Exception(e);
        }
    }
}
