package com.example.ledgerPrototype.service.Impl;

import com.example.ledgerPrototype.dao.TransDAO;
import com.example.ledgerPrototype.domain.AccountBalanceDTO;
import com.example.ledgerPrototype.domain.TransDTO;
import com.example.ledgerPrototype.domain.TransLogDTO;
import com.example.ledgerPrototype.mappers.TransDTOMapper;
import com.example.ledgerPrototype.service.transCommand;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import java.awt.print.Pageable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class transCommandImpl implements transCommand {

    @Autowired
    private TransDAO transDAO;
    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public boolean operate(TransDTO transDTO) {
        BigDecimal preBalance = reconstituteBalance(transDTO.getAccountId());;
        JsonNode payload = objectMapper.readTree(transDTO.getPayload());

        ((ObjectNode) payload).put("current-balance", preBalance);
        transDTO.setPayload(objectMapper.writeValueAsString(payload));

        String type = payload.get("type").asText();
        preBalance = preBalance.add(TransDTOMapper.toType(transDTO));

        if(preBalance.compareTo(BigDecimal.ZERO) >= 0 || type.equals("credit")){
            transDAO.saveEvent(transDTO);
        }

        return true;
    }

    @Override
    public BigDecimal reconstituteBalance(String accountId){
        List<TransDTO> transactions = transDAO.findAllStored(accountId);

        BigDecimal balance = new BigDecimal(0);

        for(TransDTO transDTO : transactions){
            balance = balance.add(TransDTOMapper.toType(transDTO));
        }

        return balance;
    }

    @Override
    public Optional<AccountBalanceDTO> consultBalance(String accountId){
        Optional<AccountBalanceDTO> balance = transDAO.findBalance(accountId);

        return balance;
    }

    @Override
    public List<TransLogDTO> consultHistory(String accountId){
        List<TransLogDTO> transactions = transDAO.findAllById(accountId);

        return transactions;
    }

    @Override
    public boolean checkFraudStatus(String accountId){
        return transDAO.checkFraud(accountId);
    }

    @Override
    public void changeFraudStatus(String accountId){
        transDAO.changeFraudStatAcc(accountId,transDAO.checkFraud(accountId).booleanValue() ? false : true);
    }

    @Override
    public String checkAccount(String accountId) {
        return transDAO.checkAcount(accountId);
    }

}
