package com.example.ledgerPrototype.consumers.impl;

import com.example.ledgerPrototype.consumers.transConsumer;
import com.example.ledgerPrototype.dao.TransDAO;
import com.example.ledgerPrototype.domain.TransDTO;
import com.example.ledgerPrototype.domain.TransFraudDTO;
import com.example.ledgerPrototype.mappers.TransDTOMapper;
import lombok.extern.apachecommons.CommonsLog;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.List;

@Component
public class FraudChecker implements transConsumer {

    @Autowired
    private TransDAO transDAO;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    @KafkaListener(topics="ledger.transactions", groupId = "transaction-fraud-checker")
    public void listenEvent(ConsumerRecord<String, String> record) {
        TransDTO transDTO = TransDTOMapper.mapDtoFromJson(record.value());

        BigDecimal amount = objectMapper.readTree(transDTO.getPayload()).path("amount").decimalValue();
        BigDecimal currentBalance = transDTO.getCurrentBalance();
        String warningDesc = "";

        if (isLargeTransaction(amount, currentBalance, transDTO))
            warningDesc = warningDesc.concat("[Transaction above regular value]");

        if (repeatedTransactions(transDTO, amount))
            warningDesc = warningDesc.concat("[Transaction repeated many times]");

        if (manyTransUnderTime(transDTO))
            warningDesc = warningDesc.concat("[Many transactions under one minute]");

        if (!(warningDesc.isBlank())){
            TransFraudDTO transFDTO = TransFraudDTO.builder()
                    .accountId(transDTO.getAccountId())
                    .reason(warningDesc)
                    .payload(transDTO.getPayload())
                    .build();

            transDAO.changeFraudStatAcc(transDTO.getAccountId(), true);
            transDAO.saveEventFraud(transFDTO);
        }
    }

    public boolean manyTransUnderTime(TransDTO transDTO) {
        List<TransDTO> transactions = transDAO.findTransOneMinute(transDTO);

        if(transactions.stream().count() >= 3) {
            return true;
        }else  {
            return false;
        }
    }
    public boolean isLargeTransaction(BigDecimal amount, BigDecimal currentBalance, TransDTO transDTO) {
        if(amount.compareTo(currentBalance.multiply(BigDecimal.valueOf(0.7))) > 0 && TransDTOMapper.toType(transDTO).compareTo(BigDecimal.ZERO) < 0) {
            return true;
        }else {
            return false;
        }
    }

    public boolean repeatedTransactions(TransDTO transDTO, BigDecimal amount) {
        List<TransDTO> transactions = transDAO.findLastX(5, transDTO.getAccountId());

        if (transactions.stream().allMatch(transDtoStrm ->
                extractAmountFromTransaction(transDtoStrm).compareTo(amount) == 0)){
            return true;
        }else {
            return false;
        }
    }

    public BigDecimal extractAmountFromTransaction(TransDTO transDTO) {
        try{
            BigDecimal amount;
            JsonNode tree = objectMapper.readTree(transDTO.getPayload());
            amount = tree.path("amount").decimalValue();

            return amount;
        } catch(Exception e){
            throw new RuntimeException(e);
        }
    }

}
