package com.Misbra.DTO;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SessionBundleDTO {
    private String bundleId;
    private String bundleName;   // e.g. "Single", "10-Pack", "20-Pack"
    private Long quantity;       // how many sessions are included
    private Double price;        // cost of the bundle
    private boolean active;
}
