package com.Misbra.Authentication.Services;

import com.Misbra.Authentication.DTO.AuthRequest.PhoneLoginRequestDTO;
import com.Misbra.Authentication.DTO.AuthRequest.PhoneRequestDTO;
import com.Misbra.Authentication.DTO.AuthRequest.RegisterRequestDTO;
import com.Misbra.Authentication.DTO.AuthResponse.AuthResponseDTO;
import com.Misbra.DTO.UserDTO;
import com.Misbra.Entity.User;
import com.Misbra.Enum.RecordStatus;
import com.Misbra.Enum.RoleEnum;
import com.Misbra.Exception.Utils.ExceptionUtils;
import com.Misbra.Exception.Validation.ValidationErrorDTO;
import com.Misbra.Mapper.UserMapper;
import com.Misbra.Service.UserService;
import com.Misbra.Utils.AuthMessageKeys;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final OTPService otpService;
    private final ExceptionUtils exceptionUtils;
    private final UserMapper userMapper;
    private final AuthenticationManager authenticationManager;


    public String register(PhoneRequestDTO phoneRequest) {
        if (userService.existsByPhone(phoneRequest.getPhone())) {
            List<ValidationErrorDTO> errors = new ArrayList<>();
            errors.add(new ValidationErrorDTO(
                    AuthMessageKeys.USER_ALREADY_REGISTERED,
                    new String[]{phoneRequest.getPhone()}
            ));
            exceptionUtils.throwValidationException(errors);
        }

        // Generate OTP for phone verification before saving user
       return  otpService.generateOTP(phoneRequest.getPhone());
    }

    public AuthResponseDTO verifyRegistrationOTP(RegisterRequestDTO request) {
        if (!otpService.validateOTP(request.getPhone(), request.getOtp())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid OTP");
        }


        UserDTO user = UserDTO.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName()) // Last Name
                .phone(request.getPhone())
                .email(request.getEmail()) // Store email for reference
                .password(passwordEncoder.encode(request.getPassword()))
                .role(RoleEnum.USER)
                .recordStatus(RecordStatus.ACTIVE)
                .enabled(true)
                .build();

         userService.addUser(user);
        return generateAuthResponse(userMapper.toEntity(user));
    }

    public String generateLoginOtp(PhoneRequestDTO request) {


        // Find or create user
        Optional<User> user = userService.findByPhone(request.getPhone());

        if(user.isEmpty()) {

            List<ValidationErrorDTO> errors = new ArrayList<>();
            errors.add(new ValidationErrorDTO(
                    AuthMessageKeys.USER_NOT_FOUND,
                    new String[]{request.getPhone()}
            ));
            exceptionUtils.throwValidationException(errors);

              }
       return otpService.generateOTP(request.getPhone());

    }

    public AuthResponseDTO verifyLoginOTP(PhoneLoginRequestDTO request) {
        // Validate OTP
        if (!otpService.validateOTP(request.getPhone(), request.getOtp())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid OTP");
        }

        // Find or create user
        Optional<User> user = userService.findByPhone(request.getPhone());

        if(user.isEmpty()) {

            List<ValidationErrorDTO> errors = new ArrayList<>();
            errors.add(new ValidationErrorDTO(
                    AuthMessageKeys.USER_NOT_FOUND,
                    new String[]{request.getPhone()}
            ));
            exceptionUtils.throwValidationException(errors);
        }

        // Generate tokens
        return AuthResponseDTO.builder()
                .accessToken(jwtService.generateToken(user.get()))
                .refreshToken(jwtService.generateRefreshToken(user.get()))
                .user(userMapper.toAuthResponseDTO(user.get()))
                .build();
    }
    public AuthResponseDTO passwordLogin(PhoneLoginRequestDTO request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getPhone(),
                        request.getPassword()
                )
        );
        // Find or create user
        Optional<User> user = userService.findByPhone(request.getPhone());

        if(user.isEmpty()) {
            List<ValidationErrorDTO> errors = new ArrayList<>();
            errors.add(new ValidationErrorDTO(
                    AuthMessageKeys.USER_NOT_FOUND,
                    new String[]{request.getPhone()}
            ));
            exceptionUtils.throwValidationException(errors);
        }

        // Generate tokens
        return AuthResponseDTO.builder()
                .accessToken(jwtService.generateToken(user.get()))
                .refreshToken(jwtService.generateRefreshToken(user.get()))
                .user(userMapper.toAuthResponseDTO(user.get()))
                .build();
    }



    private AuthResponseDTO generateAuthResponse(User user) {
        return AuthResponseDTO.builder()
                .accessToken(jwtService.generateToken(user))
                .refreshToken(jwtService.generateRefreshToken(user))
                .user(userMapper.toAuthResponseDTO(user))
                .build();
    }

    public String generateNonExpiringToken (){
        return jwtService.generateNonExpiringToken();
    }


}
