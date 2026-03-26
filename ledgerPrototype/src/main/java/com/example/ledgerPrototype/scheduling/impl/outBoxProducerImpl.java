package com.example.ledgerPrototype.scheduling.impl;

import com.example.ledgerPrototype.dao.TransDAO;
import com.example.ledgerPrototype.domain.TransDTO;
import com.example.ledgerPrototype.scheduling.outBoxProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class outBoxProducerImpl implements outBoxProducer {

    private KafkaTemplate<String, String> kafkaTemplate;

    private TransDAO transDAO;

    public outBoxProducerImpl(KafkaTemplate<String, String> kafkaTemplate, TransDAO transDAO) {
        this.kafkaTemplate = kafkaTemplate;
        this.transDAO = transDAO;
    }

    @Override
    @Scheduled(fixedRate = 500)
    public void collectOutBox(){

        List<TransDTO> transactions =  transDAO.findAllOutBox();

        for(TransDTO transDTO : transactions){
            kafkaTemplate.send("ledger.transactions", transDTO.getAccountId(), transDTO.getPayload());
            transDAO.publishEvent(transDTO);
        }

    }

}
