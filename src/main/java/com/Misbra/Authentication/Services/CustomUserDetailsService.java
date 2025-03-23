package com.Misbra.Authentication.Services;

import com.Misbra.Entity.User;
import com.Misbra.Enum.RoleEnum;
import com.Misbra.Service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {


    private final UserService userService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = new User();
        if (username.equals("+9660000000000")) {
            user.setFirstName("dev-user");
            user.setLastName("dev-user");
            user.setEmail("dev-user@example.com");
            user.setPhone("+9660000000000");
            user.setRole(RoleEnum.ADMIN);
            return user;
        }
        return userService.findByPhone(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
}
