package com.Misbra.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseBundleResponseDTO {


    private String paymentId;
    private BigDecimal amount;
    private String currency;
    private String description;

}
