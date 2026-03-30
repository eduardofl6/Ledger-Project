package com.example.ledgerPrototype.mappers;

import com.example.ledgerPrototype.domain.TransDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;

@Component
public class TransDTOMapper {

    private static ObjectMapper objectMapper;

    public TransDTOMapper() {
        objectMapper = new ObjectMapper();
    }

    public static TransDTO mapDtoFromJson(String transJson){
        JsonNode node;

        try {
            node = objectMapper.readTree(transJson);
        }
        catch (Exception e) {
            throw new RuntimeException("Failed to parse payload",e);
        }

        Long id = node.hasNonNull("id") ? node.path("id").asLong() : null;
        Instant date_occur = node.hasNonNull("date_occur") ? Instant.parse(node.path("date_occur").textValue()) : null;

        TransDTO transDTO = TransDTO.builder()
                .id(id)
                .accountId(node.path("accountId").textValue())
                .payload(transJson)
                .date_occur(date_occur)
                .currentBalance(node.path("current-balance").decimalValue())
                .build();

        return transDTO;
    }

    public static BigDecimal toType(TransDTO transDTO){
        JsonNode tree;
        BigDecimal value;
        String type;
        ObjectMapper objectMapper =  new ObjectMapper();

        try {
            tree = objectMapper.readTree(transDTO.getPayload());
        }
        catch (Exception e) {
            throw new RuntimeException("Failed to parse payload",e);
        }

        value = tree.path("amount").decimalValue();
        type = tree.path("type").textValue();

        if (!(type.equals("credit") ||  type.equals("debit"))){
            throw new RuntimeException("Invalid type");
        }

        BigDecimal operator = BigDecimal.valueOf((type.equals("debit") ? -1 : 1));

        return value.multiply(operator);
    }
}
