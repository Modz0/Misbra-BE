package com.Misbra.Repository;

import com.Misbra.Entity.SessionBundle;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SessionBundleRepository  extends MongoRepository<SessionBundle, String> {
    SessionBundle findBybundleId(String sessionBundleId);
}
