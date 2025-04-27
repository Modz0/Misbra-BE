package com.Misbra.Proxy.Payment;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
public class PaymentResponse {
    private String id;
    private String status;
    private int amount;
    private int fee;
    private String currency;
    private int refunded;

    @JsonProperty("refunded_at")
    private LocalDateTime refundedAt;

    private int captured;

    @JsonProperty("captured_at")
    private LocalDateTime capturedAt;

    @JsonProperty("voided_at")
    private LocalDateTime voidedAt;

    private String description;

    @JsonProperty("amount_format")
    private String amountFormat;

    @JsonProperty("fee_format")
    private String feeFormat;

    @JsonProperty("refunded_format")
    private String refundedFormat;

    @JsonProperty("captured_format")
    private String capturedFormat;

    @JsonProperty("invoice_id")
    private String invoiceId;

    private String ip;

    @JsonProperty("callback_url")
    private String callbackUrl;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    private Map<String, Object> metadata;
    private Map<String, Object> source;
}

