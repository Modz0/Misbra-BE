package com.Misbra.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseBundleRequestDTO {

    private String bundleId ;
    private  String promoCode ;
}
