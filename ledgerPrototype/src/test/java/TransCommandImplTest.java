package com.example.ledgerPrototype.service.Impl;

import com.example.ledgerPrototype.dao.TransDAO;
import com.example.ledgerPrototype.domain.AccountBalanceDTO;
import com.example.ledgerPrototype.domain.TransDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransCommandImplTest {

    @Mock
    private TransDAO transDAO;

    @InjectMocks
    private TransCommandImpl transCommand;

    @Test
    void reconstituteBalance_ShouldCalculateCorrectSum_WhenTransactionsExist() {
        String accountId = "acc-12345";

        TransDTO creditTx = TransDTO.builder()
                .accountId(accountId)
                .payload("{\"amount\": 150.00, \"type\": \"credit\"}")
                .build();

        TransDTO debitTx = TransDTO.builder()
                .accountId(accountId)
                .payload("{\"amount\": 50.00, \"type\": \"debit\"}")
                .build();

        when(transDAO.findAllStored(accountId)).thenReturn(Arrays.asList(creditTx, debitTx));

        BigDecimal actualBalance = transCommand.reconstituteBalance(accountId);

        assertEquals(0, new BigDecimal("100.00").compareTo(actualBalance));

        verify(transDAO, times(1)).findAllStored(accountId);
    }

    @Test
    void reconstituteBalance_ShouldReturnZero_WhenNoTransactionsExist() {
        String accountId = "acc-empty";
        when(transDAO.findAllStored(accountId)).thenReturn(Collections.emptyList());

        BigDecimal actualBalance = transCommand.reconstituteBalance(accountId);

        assertEquals(BigDecimal.ZERO, actualBalance);
        verify(transDAO).findAllStored(accountId);
    }

    @Test
    void consultBalance_ShouldReturnOptionalBalance_WhenAccountExists() {
        String accountId = "acc-12345";
        AccountBalanceDTO mockBalance = AccountBalanceDTO.builder()
                .accountId(accountId)
                .balance(new BigDecimal("250.00"))
                .build();

        when(transDAO.findBalance(accountId)).thenReturn(Optional.of(mockBalance));

        Optional<AccountBalanceDTO> result = transCommand.consultBalance(accountId);

        assertTrue(result.isPresent());
        assertEquals(new BigDecimal("250.00"), result.get().getBalance());
    }
}