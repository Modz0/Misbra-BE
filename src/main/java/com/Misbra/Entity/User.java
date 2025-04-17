package com.Misbra.Entity;

import com.Misbra.Enum.RecordStatus;
import com.Misbra.Enum.RoleEnum;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import software.amazon.awssdk.annotations.NotNull;

import java.time.Instant;
import java.util.ArrayList;
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

    // New field for tracking answered questions only
    @Builder.Default
    private List<String> answeredQuestionIds = new ArrayList<>();

    private RecordStatus recordStatus;

    private RoleEnum role;
    @Builder.Default
    private Long numberOfGamesPlayed =0L;
    @Builder.Default
    private Long numberOfGamesRemaining=0L;

    @Builder.Default
    private boolean enabled = true;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    // Helper method to add answered question
    public void addAnsweredQuestion(String questionId) {
        if (answeredQuestionIds == null) {
            answeredQuestionIds = new ArrayList<>();
        }
        if (!answeredQuestionIds.contains(questionId)) {
            answeredQuestionIds.add(questionId);
        }
    }

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

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
}