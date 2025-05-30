package com.Misbra.Service;

import com.Misbra.DTO.PromoCodeDTO;

import java.util.Optional;

public interface PromoCodeService {
    PromoCodeDTO validatePromoCode(String userId, String codeInput, String bundleId);
    PromoCodeDTO createPromoCode(PromoCodeDTO promoCodeDTO);
}
