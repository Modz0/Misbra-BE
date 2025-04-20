package com.Misbra.Authentication.Services;

import com.Misbra.Authentication.Utils.AuthConstants;
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

    public OTPService(ExceptionUtils exceptionUtils, RedisTemplate<String, String> redisTemplate) {
        this.exceptionUtils = exceptionUtils;
        this.redisTemplate = redisTemplate;
    }

    public String generateOTP(String phone) {
        validatePhoneNumber(phone);

        String requestCountKey = getRequestCountKey(phone);
        Long currentCount = redisTemplate.opsForValue().increment(requestCountKey, 1);

        if (currentCount != null && currentCount > AuthConstants.MAX_OTP_ATTEMPTS) {
            handleRateLimitExceeded(phone);
        }

        redisTemplate.expire(requestCountKey, AuthConstants.MAX_OTP_ATTEMPTS, TimeUnit.HOURS);

        String otp = generateSecureOTP();
        String otpKey = getOTPKey(phone);

        redisTemplate.opsForValue().set(otpKey, otp, AuthConstants.OTP_EXPIRATION_MINUTES, TimeUnit.MINUTES);

        sendSMS(phone, AuthConstants.OTP_MESSAGE_PREFIX + otp);

        // Don't return the OTP - this is more secure
        // Return success status instead
        return "OTP_SENT";
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
        // Implement actual SMS sending logic
        System.out.println("[SMS] To: " + phone + " | Message: " + message);
    }
}