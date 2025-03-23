package com.Misbra.Utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AuthMessageKeys {

    // -------------------------------------------------------------------------
    // Pure authentication-related errors
    // -------------------------------------------------------------------------
    public static final String INVALID_CREDENTIALS = "AUTH-ERR-0001";
    public static final String USER_LOCKED         = "AUTH-ERR-0002";
    public static final String TOKEN_EXPIRED       = "AUTH-ERR-0003";

    // -------------------------------------------------------------------------
    // User-related errors (commonly part of auth flows)
    // -------------------------------------------------------------------------
    public static final String USER_ALREADY_REGISTERED = "AUTH-ERR-0004";
    public static final String USER_NOT_FOUND          = "AUTH-ERR-0005";

    // -------------------------------------------------------------------------
    // OTP-related errors (also often tied to authentication)
    // -------------------------------------------------------------------------
    public static final String OTP_REQUEST_LIMIT_EXCEEDED = "AUTH-ERR-0007";
    public static final String OTP_EXPIRED                = "AUTH-ERR-0008";
    public static final String OTP_INVALID                = "AUTH-ERR-0009";
}
