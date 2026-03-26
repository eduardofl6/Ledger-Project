package com.example.ledgerPrototype.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransFraudDTO {
    private long id;
    private String accountId;
    private String reason;
    private String payload;
    private Instant flagged_at;
}
