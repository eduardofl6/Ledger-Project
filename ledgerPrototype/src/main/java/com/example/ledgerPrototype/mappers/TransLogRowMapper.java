package com.example.ledgerPrototype.mappers;

import com.example.ledgerPrototype.domain.TransDTO;
import com.example.ledgerPrototype.domain.TransLogDTO;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class TransLogRowMapper implements RowMapper<TransLogDTO> {

    @Override
    public TransLogDTO mapRow(ResultSet rs, int rowNum) throws SQLException {

        return TransLogDTO.builder()
                .accountId(rs.getString("accountId"))
                .timestamp(rs.getTimestamp("done_at"))
                .payload(rs.getString("payload"))
                .id(rs.getLong("id"))
                .build();
    }


}
