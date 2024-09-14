package com.iyzico.challenge.repository;


import java.util.List;

import com.iyzico.challenge.entity.OutboxMessage;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface OutboxRepository extends MongoRepository<OutboxMessage, String> {
    List<OutboxMessage> findByStatus(String status);
}
