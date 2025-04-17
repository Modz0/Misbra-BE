package com.Misbra.Service;

import com.Misbra.DTO.PaymentDTO;

public interface PaymentService {

    PaymentDTO purchaseBundle(String userId, String bundleId, String promoCodeInput);
}
