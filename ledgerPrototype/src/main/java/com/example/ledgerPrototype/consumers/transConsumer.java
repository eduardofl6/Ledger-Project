package com.example.ledgerPrototype.consumers;

import org.apache.kafka.clients.consumer.ConsumerRecord;

public interface transConsumer {

    public void listenEvent(ConsumerRecord<String, String> record);
}
