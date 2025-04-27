package com.Misbra.DTO;


import com.Misbra.Enum.PromoCodeType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PromoCodeDTO {

    private String promoId;
    private String code;
    private Double discountPercentage;
    private boolean freeExtraSession;
    private String bundleId;
    private boolean active;
    private boolean used;
    private Instant validFrom;
    private Instant validTo;
    private PromoCodeType promoCodeType;
    private int maxUses;
    private int currentUses;

}
