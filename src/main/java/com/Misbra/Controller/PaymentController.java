package com.Misbra.Controller;

import com.Misbra.DTO.PaymentDTO;

import com.Misbra.Service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/purchase")
    public PaymentDTO purchaseBundle(
            @RequestParam String userId,
            @RequestParam String bundleId,
            @RequestParam(required = false) String promoCode
    ) {
        return paymentService.purchaseBundle(userId, bundleId, promoCode);
    }
}
