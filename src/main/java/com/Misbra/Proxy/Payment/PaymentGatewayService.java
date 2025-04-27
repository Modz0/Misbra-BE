package com.Misbra.Proxy.Payment;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
public class PaymentGatewayService {

    @Value("${moyasar.api.key}")
    private String apiKey;

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newHttpClient();


    public PaymentGatewayService( ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public PaymentResponse fetchPayment(String paymentId) throws Exception {
        String url = "https://api.moyasar.com/v1/payments/" + paymentId;

        String base64Creds = Base64.getEncoder().encodeToString((apiKey + ":").getBytes(StandardCharsets.UTF_8));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Basic " + base64Creds)
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        int statusCode = response.statusCode();
        if (statusCode == 200) {
            return objectMapper.readValue(response.body(), PaymentResponse.class);
        } else if (statusCode == 401 || statusCode == 403 || statusCode == 404) {
            throw new RuntimeException("Payment fetch failed with status: " + statusCode);
        } else {
            throw new RuntimeException("Unexpected error, status: " + statusCode);
        }
    }
}
