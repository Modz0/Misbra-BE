package com.Misbra.Service;

import com.Misbra.DTO.PromoCodeDTO;
import com.Misbra.Entity.PromoCode;
import com.Misbra.Entity.User;
import com.Misbra.Exception.Utils.ExceptionUtils;
import com.Misbra.Exception.Validation.ValidationErrorDTO;
import com.Misbra.Mapper.PromoCodeMapper;
import com.Misbra.Repository.PromoCodeRepository;
import com.Misbra.Repository.UserRepository;
import com.Misbra.Utils.BusinessMessageKeys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PromoCodeServiceImp implements PromoCodeService {

    private final PromoCodeRepository promoCodeRepository;
    private final UserRepository userRepository;
    private final PromoCodeMapper promoCodeMapper;
    private final ExceptionUtils exceptionUtils;
    @Override
    @Transactional
    public Optional<PromoCodeDTO> validatePromoCode(String userId, String codeInput, String bundleId) {
        List<ValidationErrorDTO> errors = new ArrayList<>();

        if (codeInput == null || codeInput.isEmpty()) {
            errors.add(new ValidationErrorDTO(
                    BusinessMessageKeys.PROMO_CODE_MISSING,
                    new String[]{"Promo code input is null or empty"}
            ));
            exceptionUtils.throwValidationException(errors);
        }

        PromoCode promoCode = promoCodeRepository.findByCode(codeInput);
        if (promoCode == null || !promoCode.isActive()) {
            errors.add(new ValidationErrorDTO(
                    BusinessMessageKeys.PROMO_CODE_INACTIVE,
                    new String[]{}
            ));
            exceptionUtils.throwValidationException(errors);
        }


        // If code is bound to a specific bundle, check it
        if (promoCode.getBundleId() != null
                && !promoCode.getBundleId().isEmpty()
                && !promoCode.getBundleId().equals(bundleId)) {
            errors.add(new ValidationErrorDTO(
                    BusinessMessageKeys.PROMO_CODE_NOT_APPLICABLE,
                    new String[]{promoCode.getCode()}
            ));
            exceptionUtils.throwValidationException(errors);
        }

        // If it's already used, no further action
        if (promoCode.isUsed()) {
            errors.add(new ValidationErrorDTO(
                    BusinessMessageKeys.PROMO_CODE_ALREADY_USED,
                    new String[]{promoCode.getCode()}
            ));
            exceptionUtils.throwValidationException(errors);
        }

        // Check if it's a "free game" code, i.e. freeExtraSession = true but discount = 0
        boolean isFreeGameCode = promoCode.isFreeExtraSession()
                && (promoCode.getDiscountPercentage() == null
                || promoCode.getDiscountPercentage() == 0.0);

        if (isFreeGameCode) {
            // 1) Mark code as used
            promoCode.setUsed(true);
            promoCodeRepository.save(promoCode);

            // 2) Immediately grant user 1 free session
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            long currentSessions = (user.getNumberOfGamesRemaining() == null) ? 0L : user.getNumberOfGamesRemaining();
            user.setNumberOfGamesRemaining(currentSessions + 1);
            userRepository.save(user);
        }

        // Return the DTO with updated state
        return Optional.of(promoCodeMapper.toDTO(promoCode));
    }
    @Override
    public    PromoCodeDTO createPromoCode(PromoCodeDTO promoCodeDTO){
        PromoCode promoCode = promoCodeMapper.toEntity(promoCodeDTO);
        promoCodeRepository.save(promoCode);
        return promoCodeMapper.toDTO(promoCode);
    }
}
