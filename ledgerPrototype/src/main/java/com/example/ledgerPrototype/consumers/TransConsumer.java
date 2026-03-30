package com.example.ledgerPrototype.consumers;

import org.apache.kafka.clients.consumer.ConsumerRecord;

public interface TransConsumer {

    public void listenEvent(ConsumerRecord<String, String> record);
}
