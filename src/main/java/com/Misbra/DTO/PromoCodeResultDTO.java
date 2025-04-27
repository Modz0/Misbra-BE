package com.Misbra.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class PromoCodeResultDTO {
    private  BigDecimal finalPrice;
    private  long finalSessions;
    private  String usedPromoCode;
}
