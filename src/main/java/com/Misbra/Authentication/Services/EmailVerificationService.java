package com.Misbra.Authentication.Service;

import com.Misbra.DTO.UserDTO;
import com.Misbra.Entity.User;
import com.Misbra.Exception.Utils.ExceptionUtils;
import com.Misbra.Exception.Validation.ValidationErrorDTO;
import com.Misbra.Service.UserService;
import com.Misbra.Utils.AuthMessageKeys;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private final JavaMailSender mailSender;
    private final JwtService jwtService;
    private final ExceptionUtils exceptionUtils;
    private final UserService userService;
    private final UserDetailsService userDetailsService;

    @Async
    public void sendEmailVerification(User user) {
        try {
            String verificationToken = jwtService.generateToken(user);
            String verificationURL = "http://localhost:8080/api/v1/auth/verify-email?token=" + verificationToken;

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

            helper.setTo(user.getEmail());
            helper.setSubject("Verify Your Email Address");
            helper.setText(buildEmailContent(user.getFirstName(), verificationURL), true);

            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send verification email", e);
        }
    }

    public void verifyEmail(String token) {
        String userEmail = jwtService.extractUserPhone(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

        if (!jwtService.isTokenValid(token, userDetails)) {
            List<ValidationErrorDTO> errors = new ArrayList<>();
            errors.add(new ValidationErrorDTO(
                    AuthMessageKeys.TOKEN_EXPIRED,
                    new String[]{}
            ));
            exceptionUtils.throwValidationException(errors);

        }

        UserDTO existingUser = userService.getUserByEmail(userEmail);
        existingUser.setEnabled(true);
        userService.updateUser(existingUser);
    }

    private String buildEmailContent(String name, String verificationUrl) {
        return String.format("""
            <html>
                <body style="font-family: Arial, sans-serif; padding: 20px;">
                    <div style="max-width: 600px; margin: 0 auto;">
                        <h2 style="color: #2563eb;">Hi %s!</h2>
                        <p style="font-size: 16px;">Please click the button below to verify your email address:</p>
                        <a href="%s" style="display: inline-block; padding: 12px 24px; background-color: #2563eb; 
                            color: white; text-decoration: none; border-radius: 4px; margin: 20px 0;">
                            Verify Email
                        </a>
                        <p style="font-size: 14px; color: #6b7280;">
                            This link will expire in 24 hours.<br>
                            If you didn't request this verification, please ignore this email.
                        </p>
                    </div>
                </body>
            </html>
            """, name, verificationUrl);
    }
}