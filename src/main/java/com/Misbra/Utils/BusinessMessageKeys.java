package com.Misbra.Utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BusinessMessageKeys {
    public static final String PHOTO_UPDATE_FAILED = "BUSS-ERR-0001";
    // From AUTH-ERR-0018 → BUSS-ERR-0010
    public static final String PHOTO_NOT_FOUND = "BUSS-ERR-0002";
    public static final String NO_REMAINING_SESSION = "BUSS-ERR-0003";
    public static final String PROMO_CODE_MISSING = "BUSS-ERR-0004";
    public static final String PROMO_CODE_INACTIVE = "BUSS-ERR-0005";
    public static final String PROMO_CODE_NOT_APPLICABLE = "BUSS-ERR-0006";
    public static final String PROMO_CODE_ALREADY_USED = "BUSS-ERR-0007";

    // New error codes
    public static final String PROMO_CODE_USAGE_LIMIT_REACHED = "BUSS-ERR-0008";
    public static final String PROMO_CODE_INVALID_TYPE = "BUSS-ERR-0009";
    public static final String INVALID_INPUT = "BUSS-ERR-0010";
    public static final String PROMO_CODE_TYPE_MISSING = "BUSS-ERR-0011";
    public static final String PROMO_CODE_ALREADY_EXISTS = "BUSS-ERR-0012";
    public static final String INVALID_DISCOUNT_PERCENTAGE = "BUSS-ERR-0013";

    public static final String PAYMENT_NOT_FOUND = "BUSS-ERR-0014";
    public static final String PAYMENT_ALREADY_COMPLETED = "BUSS-ERR-0015";
    public static final String PAYMENT_AMOUNT_MISMATCH = "BUSS-ERR-0016";
    public static final String PAYMENT_USER_MISMATCH = "BUSS-ERR-0017";
    public static final String PAYMENT_ERROR = "BUSS-ERR-0018";
    public static final String BUNDLE_NOT_FOUND = "BUSS-ERR-0019";

    // Question report error codes
    public static final String QUESTION_NOT_FOUND = "BUSS-ERR-0020";
    public static final String REPORT_NOT_FOUND = "BUSS-ERR-0021";
    public static final String UNAUTHORIZED_ACCESS = "BUSS-ERR-0022";
    public static final String REPORT_ALREADY_REVIEWED = "BUSS-ERR-0023";
}