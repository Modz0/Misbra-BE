package com.Misbra.Controller;

import com.Misbra.DTO.PromoCodeDTO;
import com.Misbra.Entity.User;
import com.Misbra.Service.PromoCodeService;
import com.Misbra.Service.RateLimitService;
import io.github.bucket4j.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


import java.util.Optional;

@RestController
@RequestMapping("/api/promo-codes")
@RequiredArgsConstructor
public class PromoCodeController {
    private final PromoCodeService promoCodeService;

    // Store a rate limiter bucket per userId
    private final RateLimitService rateLimitService;

    @GetMapping("/validate")
    public ResponseEntity<?> validatePromoCode(
            @AuthenticationPrincipal User user,
            @RequestParam String code,
            @RequestParam(required = false) String bundleId) {

        // Check user-based rate limit
        ConsumptionProbe probe = rateLimitService.checkUserRateLimit(user.getUserId());

        if (!probe.isConsumed()) {
            return ResponseEntity
                    .status(HttpStatus.TOO_MANY_REQUESTS)
                    .header("X-Rate-Limit-Retry-After-Seconds",
                            String.valueOf(probe.getNanosToWaitForRefill() / 1_000_000_000))
                    .body("Rate limit exceeded for user: " + user.getUserId());
        }

        Optional<PromoCodeDTO> promoDto = promoCodeService.validatePromoCode(user.getUserId(), code, bundleId);
        return promoDto.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/add")
    public ResponseEntity<PromoCodeDTO> createBundle(@RequestBody PromoCodeDTO promoCodeDTO) {
        return ResponseEntity.ok(promoCodeService.createPromoCode(promoCodeDTO));
    }
}