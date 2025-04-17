package com.Misbra.Repository;

import com.Misbra.Entity.PromoCode;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PromoCodeRepository  extends MongoRepository<PromoCode, String> {

    PromoCode findByCode(String promoCode);
}
