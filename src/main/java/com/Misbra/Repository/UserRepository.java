package com.Misbra.Repository;

import com.Misbra.Entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
    boolean existsByEmail(String email);
    Optional<User> findByEmail(String email);
    Optional<User> findByPhone(String phone);
    boolean existsByPhone(String phone);
    Optional<User> findByUserId(String userId);
}
