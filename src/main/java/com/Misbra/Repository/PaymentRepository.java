package com.Misbra.Repository;

import com.Misbra.Entity.Payment;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PaymentRepository extends MongoRepository<Payment, String> {
    boolean existsByPaymentGatewayId(String paymentGatewayId);
    Payment findByPaymentId(String paymentId);
}
