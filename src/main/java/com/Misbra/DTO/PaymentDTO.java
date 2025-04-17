package com.Misbra.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDTO {

    private String paymentId;
    private String userId;
    private String bundleName;        // e.g. "10-Pack"
    private Long sessionsPurchased;   // e.g. 10
    private Double amount;            // final amount paid
    private Instant paymentDate;
    private boolean successful;
    private String usedPromoCode;
}
