package com.example.ledgerPrototype.consumers.impl;

import com.example.ledgerPrototype.consumers.transConsumer;
import com.example.ledgerPrototype.dao.TransDAO;
import com.example.ledgerPrototype.domain.TransDTO;
import com.example.ledgerPrototype.mappers.TransDTOMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.HashSet;

@Component
public class BalanceProjector implements transConsumer {

    HashSet<String> transactionsHash = new HashSet<>();

    @Autowired
    public TransDAO transDAO;

    @Override
    @KafkaListener(topics="ledger.transactions", groupId = "transaction-balance-projector")
    public void listenEvent(ConsumerRecord<String, String> record) {
        String strHash = record.partition() + "-" + record.offset();
        TransDTO transDTO = TransDTOMapper.mapDtoFromJson(record.value());

        if(!(transactionsHash.contains(strHash))){
            transDAO.balanceAdd(transDTO);
            transactionsHash.add(strHash);
        }

    }
}
