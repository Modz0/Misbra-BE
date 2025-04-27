package com.Misbra.Service;

import com.Misbra.DTO.PromoCodeDTO;
import com.Misbra.Entity.PromoCode;
import com.Misbra.Entity.User;
import com.Misbra.Enum.PromoCodeType;
import com.Misbra.Exception.Utils.ExceptionUtils;
import com.Misbra.Exception.Validation.ValidationErrorDTO;
import com.Misbra.Mapper.PromoCodeMapper;
import com.Misbra.Repository.PromoCodeRepository;
import com.Misbra.Repository.UserRepository;
import com.Misbra.Utils.BusinessMessageKeys;
import com.Misbra.Utils.AuthMessageKeys;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service

public class PromoCodeServiceImp implements PromoCodeService {

    private final PromoCodeRepository promoCodeRepository;
    private final UserRepository userRepository;
    private final PromoCodeMapper promoCodeMapper;
    private final ExceptionUtils exceptionUtils;

    public PromoCodeServiceImp(PromoCodeRepository promoCodeRepository, UserRepository userRepository, PromoCodeMapper promoCodeMapper, ExceptionUtils exceptionUtils) {
        this.promoCodeRepository = promoCodeRepository;
        this.userRepository = userRepository;
        this.promoCodeMapper = promoCodeMapper;
        this.exceptionUtils = exceptionUtils;
    }

    @Override
    @Transactional
    public PromoCodeDTO validatePromoCode(String userId, String codeInput, String bundleId) {
        List<ValidationErrorDTO> errors = new ArrayList<>();

        // Validate input parameters
        if (StringUtils.isEmpty(userId)) {
            errors.add(new ValidationErrorDTO(
                    AuthMessageKeys.USER_NOT_FOUND,
                    new String[]{"User ID is required"}
            ));
        }

        if (StringUtils.isEmpty(codeInput)) {
            errors.add(new ValidationErrorDTO(
                    BusinessMessageKeys.PROMO_CODE_MISSING,
                    new String[]{"Promo code input is required"}
            ));
        }

        if (!errors.isEmpty()) {
            exceptionUtils.throwValidationException(errors);
        }

        // Find and validate promo code
        PromoCode promoCode = promoCodeRepository.findByCode(codeInput);
        if (promoCode == null || !promoCode.isActive()) {
            errors.add(new ValidationErrorDTO(
                    BusinessMessageKeys.PROMO_CODE_INACTIVE,
                    new String[]{"The promo code is either invalid or inactive"}
            ));
            exceptionUtils.throwValidationException(errors);
        }

        // Check if the code has reached its usage limit
        if (promoCode.getMaxUses() > 0 && promoCode.getCurrentUses() >= promoCode.getMaxUses()) {
            errors.add(new ValidationErrorDTO(
                    BusinessMessageKeys.PROMO_CODE_USAGE_LIMIT_REACHED,
                    new String[]{"This promo code has reached its usage limit"}
            ));
            exceptionUtils.throwValidationException(errors);
        }

        // Process based on promo code type
        PromoCodeDTO promoCodeDTO;
        switch (promoCode.getPromoCodeType()) {
            case FREE_SESSION:
                promoCodeDTO = handleFreeSessionCode(promoCode, userId);
                break;
            case DISCOUNT:
                // Validate bundle ID for discount codes if applicable
                if (!ObjectUtils.isEmpty(promoCode.getBundleId()) && !promoCode.getBundleId().equals(bundleId)) {
                    errors.add(new ValidationErrorDTO(
                            BusinessMessageKeys.PROMO_CODE_NOT_APPLICABLE,
                            new String[]{promoCode.getCode(), "This code is not applicable for the selected bundle"}
                    ));
                    exceptionUtils.throwValidationException(errors);
                }
                promoCodeDTO = handleDiscountPercentage(promoCode);
                break;
            default:
                errors.add(new ValidationErrorDTO(
                        BusinessMessageKeys.PROMO_CODE_INVALID_TYPE,
                        new String[]{"Unsupported promo code type"}
                ));
                exceptionUtils.throwValidationException(errors);
                return null; // This line will never execute due to the exception above
        }

        return promoCodeDTO;
    }

    private PromoCodeDTO handleFreeSessionCode(PromoCode promoCode, String userId) {
        List<ValidationErrorDTO> errors = new ArrayList<>();

        // Find user and validate
        User user = userRepository.findById(userId)
                .orElse(null);

        if (user == null) {
            errors.add(new ValidationErrorDTO(
                    AuthMessageKeys.USER_NOT_FOUND,
                    new String[]{"User not found"}
            ));
            exceptionUtils.throwValidationException(errors);
        }

        // Increment usage counter and check if code should be marked as used
        incrementUsageCounter(promoCode);

        // Grant user 1 free session
        long currentSessions = (user.getNumberOfGamesRemaining() == null) ? 0L : user.getNumberOfGamesRemaining();
        user.setNumberOfGamesRemaining(currentSessions + 1);
        userRepository.save(user);

        return promoCodeMapper.toDTO(promoCode);
    }

    private PromoCodeDTO handleDiscountPercentage(PromoCode promoCode) {
        // Increment usage counter and check if code should be marked as used
        incrementUsageCounter(promoCode);

        // Return the DTO
        return promoCodeMapper.toDTO(promoCode);
    }

    /**
     * Increments the usage counter of a promo code and marks it as used if necessary
     */
    private void incrementUsageCounter(PromoCode promoCode) {
        // Increment usage counter
        promoCode.setCurrentUses(promoCode.getCurrentUses() + 1);

        // Check if usage limit is reached
        if (promoCode.getMaxUses() > 0 && promoCode.getCurrentUses() >= promoCode.getMaxUses()) {
            // If it has reached max uses, mark as used (inactive)
            promoCode.setUsed(true);
        }

        // Save changes
        promoCodeRepository.save(promoCode);
    }

    @Override
    @Transactional
    public PromoCodeDTO createPromoCode(PromoCodeDTO promoCodeDTO) {
        List<ValidationErrorDTO> errors = new ArrayList<>();

        // Validate DTO
        if (promoCodeDTO == null) {
            errors.add(new ValidationErrorDTO(
                    BusinessMessageKeys.INVALID_INPUT,
                    new String[]{"Promo code data cannot be null"}
            ));
            exceptionUtils.throwValidationException(errors);
        }

        if (StringUtils.isEmpty(promoCodeDTO.getCode())) {
            errors.add(new ValidationErrorDTO(
                    BusinessMessageKeys.PROMO_CODE_MISSING,
                    new String[]{"Promo code is required"}
            ));
        }

        if (promoCodeDTO.getPromoCodeType() == null) {
            errors.add(new ValidationErrorDTO(
                    BusinessMessageKeys.PROMO_CODE_TYPE_MISSING,
                    new String[]{"Promo code type is required"}
            ));
        }

        // Check for duplicate code
        PromoCode existingCode = promoCodeRepository.findByCode(promoCodeDTO.getCode());
        if (existingCode != null) {
            errors.add(new ValidationErrorDTO(
                    BusinessMessageKeys.PROMO_CODE_ALREADY_EXISTS,
                    new String[]{promoCodeDTO.getCode()}
            ));
        }

        if (!errors.isEmpty()) {
            exceptionUtils.throwValidationException(errors);
        }

        // Specific validations based on type
        if (promoCodeDTO.getPromoCodeType() == PromoCodeType.DISCOUNT &&
                (promoCodeDTO.getDiscountPercentage() == null ||
                        promoCodeDTO.getDiscountPercentage() <= 0 ||
                        promoCodeDTO.getDiscountPercentage() > 100)) {
            errors.add(new ValidationErrorDTO(
                    BusinessMessageKeys.INVALID_DISCOUNT_PERCENTAGE,
                    new String[]{"Discount percentage must be between 1 and 100"}
            ));
            exceptionUtils.throwValidationException(errors);
        }

        // Convert to entity and save
        PromoCode promoCode = promoCodeMapper.toEntity(promoCodeDTO);

        // Set default values for counters
        if (promoCode.getCurrentUses() < 0) {
            promoCode.setCurrentUses(0);
        }

        // maxUses = 0 means unlimited uses
        if (promoCode.getMaxUses() < 0) {
            promoCode.setMaxUses(0); // Set to 0 to indicate unlimited use
        }

        promoCodeRepository.save(promoCode);
        return promoCodeMapper.toDTO(promoCode);
    }
}