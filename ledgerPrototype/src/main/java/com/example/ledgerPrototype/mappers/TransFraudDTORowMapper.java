package com.example.ledgerPrototype.mappers;

import com.example.ledgerPrototype.domain.TransDTO;
import com.example.ledgerPrototype.domain.TransFraudDTO;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class TransFraudDTORowMapper implements RowMapper<TransFraudDTO> {

    @Override
    public TransFraudDTO mapRow(ResultSet rs, int rowNum) throws SQLException {

        return TransFraudDTO.builder()
                .accountId(rs.getString("accountId"))
                .flagged_at(rs.getTimestamp("flagged_at").toInstant())
                .payload(rs.getString("payload"))
                .reason(rs.getString("reason"))
                .id(rs.getLong("id"))
                .build();
    }
}
