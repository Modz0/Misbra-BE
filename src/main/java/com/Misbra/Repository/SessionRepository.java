package com.Misbra.Repository;

import com.Misbra.Entity.Session;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface SessionRepository extends MongoRepository<Session,String> {
    List<Session> findSessionsByUserId(String userId);
    List<Session> findSessionsByUserIdOrderByUpdatedAtDesc(String userId);

}
