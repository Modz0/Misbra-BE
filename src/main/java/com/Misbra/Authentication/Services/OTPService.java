package com.Misbra.Authentication.Services;

import com.Misbra.Authentication.Utils.AuthConstants;
import com.Misbra.Authentication.Utils.Result;
import com.Misbra.Exception.Utils.ExceptionUtils;
import com.Misbra.Exception.Validation.ValidationErrorDTO;
import com.Misbra.Utils.AuthMessageKeys;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class OTPService {
   final private ExceptionUtils exceptionUtils;
   final private RedisTemplate<String, String> redisTemplate;
   final private SMSService smsService;

    public OTPService(ExceptionUtils exceptionUtils, RedisTemplate<String, String> redisTemplate, SMSService smsService) {
        this.exceptionUtils = exceptionUtils;
        this.redisTemplate = redisTemplate;
        this.smsService = smsService;
    }


    public void generateOTP(String phone) {
        validatePhoneNumber(phone);

        // Define keys for Redis
        String requestCountKey = getRequestCountKey(phone);

        // Check if this is a new request counter or increment existing one
        Long currentCount = redisTemplate.opsForValue().increment(requestCountKey, 1);
        if (currentCount == null) {
            // Set a default value or handle the error
            currentCount = 1L;
            // Or log this unusual condition
        }

        // If this is the first request in this time window, set expiration to 1 hour
        if (currentCount == 1) {
            redisTemplate.expire(requestCountKey, 1, TimeUnit.HOURS);
        }

        // Check if rate limit exceeded (more than 3 attempts within the hour)
        if (currentCount > 3) {
            handleRateLimitExceeded(phone);
            return;
        }

        // Generate and store OTP
        String otp = generateSecureOTP();
        String otpKey = getOTPKey(phone);
        redisTemplate.opsForValue().set(otpKey, otp, AuthConstants.OTP_EXPIRATION_MINUTES, TimeUnit.MINUTES);

        // Format message and send SMS
        String formattedMessage = String.format(AuthConstants.OTP_MESSAGE_TEMPLATE, otp);
        Result<String> result = smsService.sendSMS(formattedMessage, phone);

        if (!result.isSuccess()) {
            // Consider masking part of the phone number for security
            String maskedPhone = maskPhoneNumber(phone);
            throw new RuntimeException("Failed to send OTP to phone: " + maskedPhone + ", error: " + result.getError());
        }
    }

    // Helper method to mask phone number
    private String maskPhoneNumber(String phone) {
        if (phone == null || phone.length() < 4) {
            return "****";
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 3);
    }


    public boolean validateOTP(String phone, String otp) {
        validatePhoneNumber(phone);
        validateOTPFormat(otp);

        String otpKey = getOTPKey(phone);
        String storedOtp = redisTemplate.opsForValue().get(otpKey);

        // Early return for expired OTP
        if (storedOtp == null) {
            handleExpiredOTP(phone);
            return false;
        }

        // Early return for invalid OTP
        if (!storedOtp.equals(otp)) {
            handleInvalidOTP(phone);
            return false;
        }

        // OTP is valid, now delete the key and reset count
        redisTemplate.delete(otpKey);
        resetRequestCount(phone);
        return true;
    }
    private void validateOTPFormat(String otp) {
        if (otp == null || otp.length() != AuthConstants.OTP_LENGTH || !otp.matches("\\d+")) {
            List<ValidationErrorDTO> errors = new ArrayList<>();
            errors.add(new ValidationErrorDTO(
                    AuthMessageKeys.OTP_INVALID_FORMAT,
                    new String[]{}
            ));
            exceptionUtils.throwValidationException(errors);
        }
    }


    private String generateSecureOTP() {
        SecureRandom random = new SecureRandom();
        return String.format("%0" + AuthConstants.OTP_LENGTH + "d",
                random.nextInt((int) Math.pow(10, AuthConstants.OTP_LENGTH)));
    }

    private void validatePhoneNumber(String phone) {
        if (!phone.matches(AuthConstants.PHONE_REGEX)) {
            throw new IllegalArgumentException(AuthConstants.PHONE_VALIDATION_MESSAGE);
        }
    }

    private String getOTPKey(String phone) {
        return "otp:" + phone;
    }

    private String getRequestCountKey(String phone) {
        return "otp_attempts:" + phone;
    }

    private void resetRequestCount(String phone) {
        redisTemplate.delete(getRequestCountKey(phone));
    }



    private void handleRateLimitExceeded(String phone) {
        List<ValidationErrorDTO> errors = new ArrayList<>();
        errors.add(new ValidationErrorDTO(
                AuthMessageKeys.OTP_REQUEST_LIMIT_EXCEEDED,
                new String[]{phone}
        ));
        exceptionUtils.throwValidationException(errors);
    }

    private void handleExpiredOTP(String phone) {
        List<ValidationErrorDTO> errors = new ArrayList<>();
        errors.add(new ValidationErrorDTO(
                AuthMessageKeys.OTP_EXPIRED,
                new String[]{phone}
        ));
        exceptionUtils.throwValidationException(errors);
    }

    private void handleInvalidOTP(String phone) {
        List<ValidationErrorDTO> errors = new ArrayList<>();
        errors.add(new ValidationErrorDTO(
                AuthMessageKeys.OTP_INVALID,
                new String[]{phone}
        ));
        exceptionUtils.throwValidationException(errors);
    }

    private void sendSMS(String phone, String message) {

        Result<String> result = smsService.sendSMS(message, phone);

        if (result.isSuccess()) {
            System.out.println("SMS Sent Successfully: " + result.getData());
        } else {
            System.out.println("SMS Sending Failed: " + result.getError());
        }



    }
}