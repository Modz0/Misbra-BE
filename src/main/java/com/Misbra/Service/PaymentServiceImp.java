package com.Misbra.Service;

import com.Misbra.DTO.PaymentDTO;
import com.Misbra.Entity.Payment;
import com.Misbra.Entity.PromoCode;
import com.Misbra.Entity.SessionBundle;
import com.Misbra.Entity.User;
import com.Misbra.Mapper.PaymentMapper;
import com.Misbra.Repository.PaymentRepository;
import com.Misbra.Repository.PromoCodeRepository;
import com.Misbra.Repository.SessionBundleRepository;
import com.Misbra.Repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
public class PaymentServiceImp implements PaymentService {


    private final UserRepository userRepository;
    private final SessionBundleRepository sessionBundleRepository;
    private final PromoCodeRepository promoCodeRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;

    public PaymentServiceImp(UserRepository userRepository, SessionBundleRepository sessionBundleRepository, PromoCodeRepository promoCodeRepository, PaymentRepository paymentRepository, PaymentMapper paymentMapper) {
        this.userRepository = userRepository;
        this.sessionBundleRepository = sessionBundleRepository;
        this.promoCodeRepository = promoCodeRepository;
        this.paymentRepository = paymentRepository;
        this.paymentMapper = paymentMapper;
    }

    public PaymentDTO purchaseBundle(String userId, String bundleId, String promoCodeInput) {
        // 1) Find user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2) Find bundle
        SessionBundle bundle = sessionBundleRepository.findById(bundleId)
                .orElseThrow(() -> new RuntimeException("Bundle not found"));

        if (!bundle.isActive()) {
            throw new RuntimeException("This bundle is not active or for sale right now.");
        }

        // 3) Calculate final price & sessions based on promo code (if provided)
        double finalPrice = bundle.getPrice();
        long finalSessions = bundle.getQuantity();
        String usedPromoCode = null;

        if (promoCodeInput != null && !promoCodeInput.isEmpty()) {
            // Attempt to find promo code in DB
            PromoCode promoCode = promoCodeRepository.findByCode(promoCodeInput);

            if (promoCode != null && promoCode.isActive()) {
                // Check if promo is specific to a bundle
                if (promoCode.getBundleId() == null
                        || promoCode.getBundleId().equals(bundleId)) {

                    usedPromoCode = promoCode.getCode();

                    // Apply discount if any
                    if (promoCode.getDiscountPercentage() != null && promoCode.getDiscountPercentage() > 0) {
                        double discount = finalPrice * promoCode.getDiscountPercentage();
                        finalPrice -= discount;
                    }

                    // Add free extra session if flagged
                    if (promoCode.isFreeExtraSession()) {
                        finalSessions += 1;  // or as many sessions as you want to grant
                    }
                }
            }
        }

        // 4) Update the user's available sessions
        Long currentSessions = Optional.ofNullable(user.getNumberOfGamesRemaining()).orElse(0L);
        user.setNumberOfGamesRemaining(currentSessions + finalSessions);
        userRepository.save(user);

        // 5) Create a payment record
        Payment payment = Payment.builder()
                .userId(userId)
                .bundleName(bundle.getBundleName())
                .sessionsPurchased(finalSessions)
                .amount(finalPrice)
                .paymentDate(Instant.now())
                .successful(true)           // in a real system, confirm via gateway
                .usedPromoCode(usedPromoCode)
                .build();

        payment = paymentRepository.save(payment);
        return paymentMapper.toDTO(payment);
    }

}
