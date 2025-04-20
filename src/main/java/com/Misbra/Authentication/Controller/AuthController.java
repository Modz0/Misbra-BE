package com.Misbra.Authentication.Controller;


import com.Misbra.Authentication.DTO.AuthRequest.PhoneLoginRequestDTO;
import com.Misbra.Authentication.DTO.AuthRequest.PhoneRequestDTO;
import com.Misbra.Authentication.DTO.AuthRequest.RegisterRequestDTO;
import com.Misbra.Authentication.DTO.AuthResponse.AuthResponseDTO;
import com.Misbra.Authentication.Services.AuthService;
import com.Misbra.Entity.User;
import com.Misbra.Service.RateLimitService;
import io.github.bucket4j.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication API", description = "Operations related to Authentication user API")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    private final RateLimitService rateLimitService;


    @PostMapping("/verify-register")
    public ResponseEntity<String> register(
            @Valid @RequestBody PhoneRequestDTO phoneRequest,
            @RequestHeader(value = "X-Forwarded-For", required = false) String forwardedIp,
            HttpServletRequest request) {

        // Check IP-based rate limit
        ConsumptionProbe probe = rateLimitService.checkIpRateLimit(request, forwardedIp);

        if (!probe.isConsumed()) {
            String clientIp = rateLimitService.extractClientIp(forwardedIp, request);
            return ResponseEntity
                    .status(HttpStatus.TOO_MANY_REQUESTS)
                    .header("X-Rate-Limit-Retry-After-Seconds",
                            String.valueOf(probe.getNanosToWaitForRefill() / 1_000_000_000))
                    .body("Rate limit exceeded for IP address: " + rateLimitService.maskIpAddress(clientIp));
        }

        // Process the registration request
        authService.register(phoneRequest);
        return ResponseEntity.ok().body("success");
    }



    @PostMapping("/register-user")
    public ResponseEntity<AuthResponseDTO> registerUser( @Valid @RequestBody RegisterRequestDTO registerRequestDTO){
        return  ResponseEntity.ok(authService.verifyRegistrationOTP(registerRequestDTO));
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(   @Valid @RequestBody PhoneRequestDTO request){
        authService.generateLoginOtp(request);
        return ResponseEntity.ok("login successful");

    }
//    @GetMapping("/verify-email")
//    public ResponseEntity<String> verifyEmail(@RequestParam String token) {
//        emailVerificationService.veriflyEmail(token);
//        return ResponseEntity.ok("Email verified successfully");
//    }
    @PostMapping("/verify-login")
    public ResponseEntity<AuthResponseDTO> verifyLoginOTP(
            @Valid @RequestBody PhoneLoginRequestDTO request
    ) {
        return ResponseEntity.ok(authService.verifyLoginOTP(request));
    }
    @PostMapping("/password-login")
    public ResponseEntity<AuthResponseDTO> verifyLoginPassword(
            @Valid @RequestBody PhoneLoginRequestDTO request
    ) {
        return ResponseEntity.ok(authService.passwordLogin(request));
    }
    @GetMapping("/generate-token")
    public ResponseEntity<String> generateToken() {
        return ResponseEntity.ok(authService.generateNonExpiringToken()) ;

    }

}
