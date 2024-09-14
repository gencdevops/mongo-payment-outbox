package com.iyzico.challenge.entity;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;


import javax.persistence.Id;
import java.time.LocalDateTime;
@Document(collection = "outbox")
@Data
public class OutboxMessage {
    @Id
    private String id;
    private String requestId;
    private String eventType;
    private String payload;
    private LocalDateTime createdAt;
    private String status;
}