package com.example.ledgerPrototype.mappers;

import com.example.ledgerPrototype.domain.TransDTO;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;

public class TransDTORowMapper implements RowMapper<TransDTO> {

    @Override
    public TransDTO mapRow(ResultSet rs, int rowNum) throws SQLException {

        return TransDTO.builder()
                .accountId(rs.getString("accountId"))
                .date_occur(rs.getTimestamp("date_occur").toInstant())
                .payload(rs.getString("payload"))
                .id(rs.getLong("id"))
                .build();
    }

}