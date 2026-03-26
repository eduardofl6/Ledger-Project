package com.example.ledgerPrototype.mappers;

import com.example.ledgerPrototype.domain.AccountBalanceDTO;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class AccBalanceRowMapper implements RowMapper<AccountBalanceDTO> {

    @Override
        public AccountBalanceDTO mapRow(ResultSet rs, int rowNum) throws SQLException {

            return AccountBalanceDTO.builder()
                    .accountId(rs.getString("accountId"))
                    .balance(rs.getBigDecimal("balance"))
                    .build();
        }

}
