package com.Misbra.Entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "session_bundles")
public class SessionBundle {
    @Id
    private String bundleId;
    private String bundleName;   // e.g. "Single", "10-Pack", "20-Pack"
    private Long quantity;       // how many sessions are included
    private BigDecimal price;        // cost of the bundle
    private boolean active;      // whether this bundle is currently for sale
}
