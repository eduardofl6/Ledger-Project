package com.example.ledgerPrototype.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransLogDTO {
    private String accountId;
    private long id;
    private String payload;
    private Timestamp timestamp;
    private BigDecimal currentBalance;
}
