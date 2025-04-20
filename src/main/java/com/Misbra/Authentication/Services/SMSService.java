package com.Misbra.Authentication.Services;

import com.Misbra.Authentication.Utils.Result;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class SMSService {

    @Value("${sms.api.url}")
    private String baseUrl;

    @Value("${sms.api.token}")
    private String apiToken;

    @Value("${sms.sender}")
    private String senderName;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    public Result<String> sendSMS(String message, String phoneNumber) {
        try {
            // Prepare JSON payload
            JSONObject jsonMessage = new JSONObject();
            jsonMessage.put("src", senderName);
            jsonMessage.put("dests", new JSONArray().put(phoneNumber));
            jsonMessage.put("body", message);
            jsonMessage.put("priority", 0);
            jsonMessage.put("delay", 0);
            jsonMessage.put("validity", 0);
            jsonMessage.put("maxParts", 0);
            jsonMessage.put("dlr", 0);
            jsonMessage.put("prevDups", 0);
            jsonMessage.put("msgClass", "transactional");

            // Prepare HTTP Request
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/msgs/sms"))
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiToken)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonMessage.toString()))
                    .build();

            // Send request
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            // Handle Response
            if (response.statusCode() == 200) {
                return new Result<>(response.body(), null);
            } else {
                return new Result<>(null, "HTTP Error: " + response.statusCode() + " - " + response.body());
            }

        } catch (Exception e) {
            return new Result<>(null, "Exception: " + e.getMessage());
        }
    }
}
