package com.Misbra.Entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "payments")
public class Payment {
    @Id
    private String paymentId;
    private String userId;
    private String bundleName;        // e.g. "10-Pack"
    private Long sessionsPurchased;   // e.g. 10
    private Double amount;            // final amount paid
    private Instant paymentDate;
    private boolean successful;
    private String usedPromoCode;     // the code used, if any
}
