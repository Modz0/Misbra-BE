package com.Misbra.Authentication.Utils;

public class AuthConstants {

    // JWT Configuration
    public static final String JWT_ISSUER = "MisbraApp";
    public static final long ACCESS_TOKEN_EXPIRATION_MS = 86400000; // 24 hours
    public static final long REFRESH_TOKEN_EXPIRATION_MS = 604800000; // 7 days
    public static final String TOKEN_HEADER = "Authorization";
    public static final String TOKEN_PREFIX = "Bearer ";

    // OTP Configuration
    public static final int OTP_LENGTH = 6;
    public static final int OTP_EXPIRATION_MINUTES = 5;
    public static final int MAX_OTP_ATTEMPTS = 5;

    // Security Constants
    public static final String[] PUBLIC_ENDPOINTS = {
            "/api/v1/auth/**",
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/v3/api-docs",
            "/v3/api-docs/**",
            "/swagger-resources/**",
            "/webjars/**"
    };
    // Error Messages
    public static final String OTP_EXPIRED = "OTP has expired";
    public static final String OTP_INVALID = "Invalid OTP code";


    // Phone Validation
    public static final String PHONE_REGEX = "^\\+?[1-9]\\d{1,14}$";
    public static final String PHONE_VALIDATION_MESSAGE =
            "Phone number must be in E.164 format";

    // SMS Related
    public static final String OTP_MESSAGE_PREFIX = "Your verification code is: ";




    // Timing Constants (if missing)
    public static final int OTP_ATTEMPT_EXPIRATION_HOURS = 1;
}
