package com.Misbra.Controller;

import com.Misbra.DTO.PromoCodeDTO;
import com.Misbra.DTO.SessionBundleDTO;
import com.Misbra.Entity.User;
import com.Misbra.Service.PromoCodeService;
import io.github.bucket4j.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/promo-codes")
@RequiredArgsConstructor
public class PromoCodeController {
    private final PromoCodeService promoCodeService;

    // Store a rate limiter bucket per userId
    private final Map<String, Bucket> userRateLimiters = new ConcurrentHashMap<>();

    // Create a per-user rate limiter: 5 requests per second
    private Bucket createRateLimiterBucket() {
        // Using greedy refill strategy instead of the deprecated intervally method
        Bandwidth limit = Bandwidth.classic(5, Refill.greedy(5, Duration.ofSeconds(1)));

        return Bucket4j.builder()
                .addLimit(limit)
                .build();
    }

    @GetMapping("/validate")
    public ResponseEntity<?> validatePromoCode(
            @AuthenticationPrincipal User user,
            @RequestParam String code,
            @RequestParam(required = false) String bundleId
    ) {
        Bucket bucket = userRateLimiters.computeIfAbsent(user.getUserId(), id -> createRateLimiterBucket());

        // Check if request can be consumed
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

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