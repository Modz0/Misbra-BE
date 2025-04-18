package com.Misbra.Controller;

import com.Misbra.DTO.PaymentDTO;

import com.Misbra.DTO.PurchaseBundleRequest;
import com.Misbra.Entity.User;
import com.Misbra.Service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/purchase")
    public PaymentDTO purchaseBundle(
            @AuthenticationPrincipal User user,
            @RequestBody PurchaseBundleRequest  purchaseBundleRequest

    ) {
        return paymentService.purchaseBundle(user.getUserId(), purchaseBundleRequest.getBundleId(), purchaseBundleRequest.getPromoCode());
    }
}
