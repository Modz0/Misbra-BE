package com.Misbra.Authentication.Controller;


import com.Misbra.Authentication.DTO.AuthRequest.PhoneLoginRequestDTO;
import com.Misbra.Authentication.DTO.AuthRequest.PhoneRequestDTO;
import com.Misbra.Authentication.DTO.AuthRequest.RegisterRequestDTO;
import com.Misbra.Authentication.DTO.AuthResponse.AuthResponseDTO;
import com.Misbra.Authentication.Service.AuthService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication API", description = "Operations related to Authentication user API")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @GetMapping("/verify-register")
    public ResponseEntity<String> register(   @Valid @RequestBody PhoneRequestDTO phoneRequest){

        return ResponseEntity.ok(authService.register(phoneRequest));

    }
    @PostMapping("/register-user")
    public ResponseEntity<AuthResponseDTO> registerUser( @Valid @RequestBody RegisterRequestDTO registerRequestDTO){
        return  ResponseEntity.ok(authService.verifyRegistrationOTP(registerRequestDTO));
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(   @Valid @RequestBody PhoneRequestDTO request){
        return ResponseEntity.ok(authService.generateLoginOtp(request));

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
