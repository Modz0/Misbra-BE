package com.Misbra.DTO;

import com.Misbra.Enum.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDTO {

    private String paymentId;
    private String userId;
    private String bundleName;
    private Long sessionsPurchased;
    private BigDecimal amount;
    private Instant paymentDate;
    private PaymentStatus paymentStatus;
    private String paymentGatewayStatus;
    private String usedPromoCode;
    private String paymentGatewayId;
    private String paymentGatewayMessage;
}
