package com.Misbra.Entity;


import com.Misbra.Enum.RecordStatus;
import com.Misbra.Enum.RoleEnum;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import software.amazon.awssdk.annotations.NotNull;

import java.util.Collection;
import java.util.List;

@Document(collection = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails { // Implement UserDetails

        @Id
        private String userId;

       @NotNull
       private String firstName;

    @NotNull
    private String lastName;

        @Indexed(unique = true)
        private String email;

        @Getter
        @Indexed(unique = true)
        private String phone;

        private String password;

        private List<String> visitedPlaces;

        private RecordStatus recordStatus;

       private RoleEnum role;

       @Builder.Default
       private boolean enabled = true;


        // Required by Spring Security
        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
                return List.of(new SimpleGrantedAuthority(role.getDescription()));
        }

    @Override
    public String getUsername() {
        return phone;
    }

    @Override
      public boolean isAccountNonExpired() {
        return recordStatus == RecordStatus.ACTIVE;
      }

        @Override
        public boolean isEnabled() {
                return enabled;
        }

}