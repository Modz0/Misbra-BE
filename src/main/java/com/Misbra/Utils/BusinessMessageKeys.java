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

}