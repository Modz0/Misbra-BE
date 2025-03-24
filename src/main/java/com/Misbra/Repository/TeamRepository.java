package com.Misbra.Repository;

import com.Misbra.Entity.Team;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TeamRepository extends MongoRepository<Team,String> {
}
