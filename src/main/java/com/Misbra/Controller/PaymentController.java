package com.Misbra.Controller;

import com.Misbra.DTO.PaymentDTO;

import com.Misbra.DTO.PaymentResponseDTO;
import com.Misbra.DTO.PurchaseBundleRequestDTO;
import com.Misbra.DTO.PurchaseBundleResponseDTO;
import com.Misbra.Entity.User;
import com.Misbra.Service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/purchase")
    public ResponseEntity<PurchaseBundleResponseDTO> purchaseBundle(
            @AuthenticationPrincipal User user,
            @RequestBody PurchaseBundleRequestDTO purchaseBundleRequest

    ) {
        return ResponseEntity.ok(paymentService.purchaseBundle(user.getUserId(), purchaseBundleRequest.getBundleId(), purchaseBundleRequest.getPromoCode()));
    }

    @PutMapping("/verify")
    public ResponseEntity<String> verifyPurchase(
            @AuthenticationPrincipal User user,
        @RequestBody PaymentResponseDTO paymentResponse
    ) throws Exception {

        return ResponseEntity.ok(paymentService.updatePaymentStatus(user.getUserId(), paymentResponse.getPaymentId(),paymentResponse.getStatus(),paymentResponse.getPaymentGatewayId(),paymentResponse.getPaymentGatewayMessage()) );
    }
    @PutMapping("/cancel")
    public ResponseEntity<String> cancelPurchase(
            @RequestParam String paymentId){
        return ResponseEntity.ok(paymentService.cancelTransaction(paymentId));

    }
}
