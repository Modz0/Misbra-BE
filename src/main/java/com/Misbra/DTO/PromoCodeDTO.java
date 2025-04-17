package com.Misbra.DTO;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PromoCodeDTO {

    private String promoId;
    private String code;                // The actual text code (e.g. "RAMADAN2025")
    private Double discountPercentage;  // e.g. 0.2 for 20% discount
    private boolean freeExtraSession;   // if true, add +1 (or more) free session
    private String bundleId;            // if not null, discount applies to a specific bundle only
    private boolean active;
    private boolean used;

}
