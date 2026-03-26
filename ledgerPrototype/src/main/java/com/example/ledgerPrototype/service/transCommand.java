package com.example.ledgerPrototype.service;

import com.example.ledgerPrototype.domain.AccountBalanceDTO;
import com.example.ledgerPrototype.domain.TransDTO;
import com.example.ledgerPrototype.domain.TransLogDTO;
import org.springframework.data.domain.Page;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface transCommand {

    public boolean operate(TransDTO transDTO);

    public BigDecimal reconstituteBalance(String accountId);

    public Optional<AccountBalanceDTO> consultBalance(String accountId);

    public List<TransLogDTO> consultHistory(String accountId);

    public boolean checkFraudStatus(String accountId);

    public void changeFraudStatus(String accountId);

    public String checkAccount(String accountId);
}

