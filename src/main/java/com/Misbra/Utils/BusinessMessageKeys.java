package com.Misbra.Utils;


import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BusinessMessageKeys {


    public static final String PHOTO_UPDATE_FAILED = "BUSS-ERR-0001";
    // From AUTH-ERR-0018 → BUSS-ERR-0010
    public static final String PHOTO_NOT_FOUND = "BUSS-ERR-0002";
}