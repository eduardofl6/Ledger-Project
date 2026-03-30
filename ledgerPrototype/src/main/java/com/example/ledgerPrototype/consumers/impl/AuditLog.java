package com.example.ledgerPrototype.consumers.impl;

import com.example.ledgerPrototype.consumers.TransConsumer;
import com.example.ledgerPrototype.dao.TransDAO;
import com.example.ledgerPrototype.domain.TransDTO;
import com.example.ledgerPrototype.mappers.TransDTOMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class AuditLog implements TransConsumer {

    private TransDAO transDAO;

    public AuditLog(TransDAO transDAO) {
        this.transDAO = transDAO;
    }

    @Override
    @KafkaListener(topics="ledger.transactions", groupId = "transaction-auditLog")
    public void listenEvent(ConsumerRecord<String, String> record) {
        TransDTO transDTO = TransDTOMapper.mapDtoFromJson(record.value());

        transDAO.saveEventLog(transDTO);
    }

}
