package com.Misbra.Service;

import com.Misbra.DTO.PurchaseBundleResponseDTO;

public interface PaymentService {

    PurchaseBundleResponseDTO purchaseBundle(String userId, String bundleId, String promoCodeInput);
    String updatePaymentStatus(String userId,String paymentId ,String Status,String paymentGatewayId,String paymentGatewayMessage) throws Exception;
    String cancelTransaction(String paymentId);

}
