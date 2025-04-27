package com.Misbra.Service;

import com.Misbra.DTO.PromoCodeResultDTO;
import com.Misbra.DTO.PurchaseBundleResponseDTO;
import com.Misbra.DTO.UserDTO;
import com.Misbra.Entity.Payment;
import com.Misbra.Entity.PromoCode;
import com.Misbra.Entity.SessionBundle;
import com.Misbra.Enum.PaymentGatewayStatus;
import com.Misbra.Enum.PaymentStatus;
import com.Misbra.Exception.Utils.ExceptionUtils;
import com.Misbra.Exception.Validation.ValidationErrorDTO;
import com.Misbra.Proxy.Payment.PaymentGatewayService;
import com.Misbra.Proxy.Payment.PaymentResponse;
import com.Misbra.Repository.PaymentRepository;
import com.Misbra.Repository.PromoCodeRepository;
import com.Misbra.Repository.SessionBundleRepository;
import com.Misbra.Utils.BusinessMessageKeys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.Misbra.Utils.BusinessMessageKeys.*;

@Service
@Slf4j
public class PaymentServiceImp implements PaymentService {

    private final UserService userService;
    private final SessionBundleRepository sessionBundleRepository;
    private final PromoCodeRepository promoCodeRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentGatewayService paymentGatewayService;
    private final ExceptionUtils exceptionUtils;

    public PaymentServiceImp(UserService userService, SessionBundleRepository sessionBundleRepository, PromoCodeRepository promoCodeRepository, PaymentRepository paymentRepository, PaymentGatewayService paymentGatewayService, ExceptionUtils exceptionUtils) {
        this.userService = userService;
        this.sessionBundleRepository = sessionBundleRepository;
        this.promoCodeRepository = promoCodeRepository;
        this.paymentRepository = paymentRepository;
        this.paymentGatewayService = paymentGatewayService;
        this.exceptionUtils = exceptionUtils;
    }

    @Transactional
    @Override
    public PurchaseBundleResponseDTO purchaseBundle(String userId, String bundleId, String promoCodeInput) {
        log.info("Initiating purchase for user: {}, bundle: {}", userId, bundleId);

        userService.getUserById(userId);  // Validate user
        SessionBundle bundle = validateBundle(bundleId);  // Validate bundle

        PromoCodeResultDTO promoResult = applyPromoCode(promoCodeInput, bundleId, bundle.getPrice(), bundle.getQuantity());

        Payment payment = Payment.builder()
                .userId(userId)
                .bundleName(bundle.getBundleName())
                .sessionsPurchased(promoResult.getFinalSessions())
                .amount(promoResult.getFinalPrice())
                .paymentDate(Instant.now())
                .paymentStatus(PaymentStatus.IN_PROGRESS)
                .usedPromoCode(promoResult.getUsedPromoCode())
                .build();

        paymentRepository.save(payment);

        log.info("Payment created with ID: {}", payment.getPaymentId());

        return PurchaseBundleResponseDTO.builder()
                .currency("SAR")
                .paymentId(payment.getPaymentId())
                .description(bundle.getBundleName())
                .amount(promoResult.getFinalPrice())
                .build();
    }

    @Transactional
    @Override
    public String updatePaymentStatus(String userId, String paymentId, String status, String paymentGatewayId, String paymentGatewayMessage) throws Exception {
        log.info("Updating payment status for paymentId: {}, userId: {}", paymentId, userId);

        UserDTO user = userService.getUserById(userId);  // Validate user
        Payment payment = paymentRepository.findByPaymentId(paymentId);

        if (ObjectUtils.isEmpty(payment)){

            List<ValidationErrorDTO> errors = new ArrayList<>();
            errors.add(new ValidationErrorDTO(BusinessMessageKeys.PAYMENT_NOT_FOUND, new String[]{"Payment not found with ID: " + paymentId}));
            exceptionUtils.throwPaymentException(errors);

        }


        if (!payment.getUserId().equals(user.getUserId())) {
            List<ValidationErrorDTO> errors = new ArrayList<>();
            errors.add(new ValidationErrorDTO(BusinessMessageKeys.PAYMENT_USER_MISMATCH, new String[]{"Payment does not belong to user: " + userId}));
            exceptionUtils.throwPaymentException(errors);
        }

        if (payment.getPaymentStatus() == PaymentStatus.COMPLETED) {
            List<ValidationErrorDTO> errors = new ArrayList<>();
            errors.add(new ValidationErrorDTO(BusinessMessageKeys.PAYMENT_ALREADY_COMPLETED, new String[]{"Payment already completed with ID: " + paymentId}));
            exceptionUtils.throwPaymentException(errors);
        }

        // Idempotency protection
        if (paymentRepository.existsByPaymentGatewayId(paymentGatewayId) && payment.getPaymentStatus() == PaymentStatus.COMPLETED) {
            log.warn("Duplicate payment notification received for gateway ID: {}", paymentGatewayId);
            return PaymentStatus.COMPLETED.toString();
        }

        payment.setPaymentGatewayStatus(status);
        payment.setPaymentGatewayId(paymentGatewayId);
        payment.setPaymentGatewayMessage(paymentGatewayMessage);

        PaymentGatewayStatus gatewayStatus = PaymentGatewayStatus.valueOf(status.toUpperCase());
        if (gatewayStatus == PaymentGatewayStatus.PAID || gatewayStatus == PaymentGatewayStatus.CAPTURED) {
            PaymentResponse paymentResponse = new PaymentResponse();
            try {
                paymentResponse = paymentGatewayService.fetchPayment(paymentGatewayId);
            } catch (Exception e) {
                log.error("Error fetching payment details from gateway for ID: {}", paymentGatewayId, e);
                List<ValidationErrorDTO> errors = new ArrayList<>();
                errors.add(new ValidationErrorDTO(BusinessMessageKeys.PAYMENT_ERROR, new String[]{"Failed to fetch payment details from gateway"}));
                exceptionUtils.throwPaymentException(errors);
            }

            BigDecimal responseAmount = BigDecimal.valueOf(paymentResponse.getAmount()).setScale(0, BigDecimal.ROUND_HALF_UP);
            BigDecimal localAmountInCents = payment.getAmount().multiply(BigDecimal.valueOf(100)).setScale(0, BigDecimal.ROUND_HALF_UP);

            if (responseAmount.compareTo(localAmountInCents) != 0) {
                List<ValidationErrorDTO> errors = new ArrayList<>();
                errors.add(new ValidationErrorDTO(BusinessMessageKeys.PAYMENT_AMOUNT_MISMATCH, new String[]{
                        "Expected amount (in cents): " + localAmountInCents,
                        "Received amount (in cents): " + responseAmount
                }));
                exceptionUtils.throwPaymentException(errors);
            }

            payment.setPaymentStatus(PaymentStatus.COMPLETED);
            Long currentSessions = Optional.ofNullable(user.getNumberOfGamesRemaining()).orElse(0L);
            user.setNumberOfGamesRemaining(currentSessions + payment.getSessionsPurchased());
            userService.updateUser(user);

            log.info("Payment {} completed successfully for user {}", paymentId, userId);
        } else {
            payment.setPaymentStatus(PaymentStatus.FAILED);
            log.warn("Payment {} failed for user {}", paymentId, userId);
        }

        paymentRepository.save(payment);
        return payment.getPaymentStatus().toString();
    }
    @Override
     public String cancelTransaction(String paymentId)  {
         Payment payment = paymentRepository.findByPaymentId(paymentId);

         if (ObjectUtils.isEmpty(payment)){
             List<ValidationErrorDTO> errors = new ArrayList<>();
             errors.add(new ValidationErrorDTO(BusinessMessageKeys.PAYMENT_NOT_FOUND, new String[]{"Payment not found with ID: " + paymentId}));
             exceptionUtils.throwPaymentException(errors);

         }
         if (payment.getPaymentStatus()!=PaymentStatus.IN_PROGRESS) {
             List<ValidationErrorDTO> errors = new ArrayList<>();
             errors.add(new ValidationErrorDTO(BusinessMessageKeys.PAYMENT_ALREADY_COMPLETED, new String[]{"Payment already completed with ID: " + paymentId}));
             exceptionUtils.throwPaymentException(errors);
         }
         payment.setPaymentStatus(PaymentStatus.CANCELED);
         paymentRepository.save(payment);
         log.info("Payment {} cancelled successfully", paymentId);
         return payment.getPaymentStatus().toString();
     }



    private SessionBundle validateBundle(String bundleId) {
        SessionBundle sessionBundle=   sessionBundleRepository.findBybundleId(bundleId);
        if(ObjectUtils.isEmpty(sessionBundle)||!sessionBundle.isActive()){
            List<ValidationErrorDTO> errors = new ArrayList<>();
            errors.add(new ValidationErrorDTO(BusinessMessageKeys.BUNDLE_NOT_FOUND, new String[]{"Bundle not found with ID: " + bundleId}));
            exceptionUtils.throwPaymentException(errors);
        }
        return sessionBundle;
    }


    private PromoCodeResultDTO applyPromoCode(String promoCodeInput, String bundleId, BigDecimal price, long sessions) {
        BigDecimal finalPrice = price;
        long finalSessions = sessions;
        String usedPromoCode = null;

        if (promoCodeInput != null && !promoCodeInput.isEmpty()) {
            PromoCode promoCode = promoCodeRepository.findByCode(promoCodeInput);
            if (promoCode != null && promoCode.isActive()) {
                if ( ObjectUtils.isEmpty(promoCode.getBundleId())|| promoCode.getBundleId().equals(bundleId)) {
                    usedPromoCode = promoCode.getCode();
                    if (promoCode.getDiscountPercentage() != null && promoCode.getDiscountPercentage() > 0) {
                        BigDecimal discount = finalPrice.multiply(BigDecimal.valueOf(promoCode.getDiscountPercentage()));
                        finalPrice = finalPrice.subtract(discount);
                    }
                    if (promoCode.isFreeExtraSession()) {
                        finalSessions += 1;
                    }
                }
            }
        }
        return new PromoCodeResultDTO(finalPrice, finalSessions, usedPromoCode);
    }

}