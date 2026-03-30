package com.example.ledgerPrototype.config;

import com.example.ledgerPrototype.domain.TransDTO;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaConfig {

    @Bean
    public DefaultErrorHandler errorHandler(KafkaTemplate<String, String> kafka) {
        // where failed messages go after all retries are exhausted
        var dlt = new DeadLetterPublishingRecoverer(kafka);

        // retry 3 times, waiting 1 second between each attempt
        var backoff = new FixedBackOff(1000L, 3);

        return new DefaultErrorHandler(dlt, backoff);
    }
}