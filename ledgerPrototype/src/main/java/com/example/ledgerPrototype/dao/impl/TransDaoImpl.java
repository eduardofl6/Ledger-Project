package com.example.ledgerPrototype.dao.impl;


import com.example.ledgerPrototype.dao.TransDAO;
import com.example.ledgerPrototype.domain.AccountBalanceDTO;
import com.example.ledgerPrototype.domain.TransDTO;
import com.example.ledgerPrototype.domain.TransFraudDTO;
import com.example.ledgerPrototype.domain.TransLogDTO;
import com.example.ledgerPrototype.mappers.AccBalanceRowMapper;
import com.example.ledgerPrototype.mappers.TransDTOMapper;
import com.example.ledgerPrototype.mappers.TransDTORowMapper;
import com.example.ledgerPrototype.mappers.TransLogRowMapper;
import jakarta.transaction.Transactional;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Component
public class TransDaoImpl implements TransDAO {

    private JdbcTemplate jdbcTemplate;

    public TransDaoImpl(JdbcTemplate jdbcTemplate){
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    @Override
    public void saveEvent(TransDTO transDTO){
        jdbcTemplate.update("INSERT INTO transEventStored (accountId, payload) VALUES (?, ?::jsonb)"
                ,transDTO.getAccountId(), transDTO.getPayload());

        jdbcTemplate.update("INSERT INTO transOutBox (accountId, payload) VALUES (?, ?::jsonb)"
                            ,transDTO.getAccountId(), transDTO.getPayload());
    }

    @Override
    public void saveEventLog(TransDTO transDTO) {
        jdbcTemplate.update("INSERT INTO transAuditLog (accountId, payload) VALUES (?, ?::jsonb)"
                ,transDTO.getAccountId(), transDTO.getPayload());
    }

    @Override
    public void saveEventFraud(TransFraudDTO transFDTO){
        jdbcTemplate.update("INSERT INTO transFraudLog (accountId, payload, reason) VALUES (?, ?::jsonb, ?)"
                ,transFDTO.getAccountId(), transFDTO.getPayload(), transFDTO.getReason());
    }

    @Override
    public void publishEvent(TransDTO transDTO) {
        jdbcTemplate.update("UPDATE transOutBox SET published = true WHERE accountId = ? AND id = ?"
                ,transDTO.getAccountId(), transDTO.getId());
    }

    @Override
    public void changeFraudStatAcc(String accountId, boolean status){
        jdbcTemplate.update("INSERT INTO accountsFraud (accountId, fraudAlert) VALUES (?, ?)" +
                        " ON CONFLICT (accountId) DO UPDATE SET fraudAlert = ?"
                ,accountId, status, status);
    }

    @Override
    public Boolean checkFraud(String accountId){
        try {
            Boolean flag = jdbcTemplate.queryForObject("SELECT fraudAlert FROM accountsFraud where accountId = ?", Boolean.class, accountId);

            return flag;
        } catch (EmptyResultDataAccessException e) {
            return false;
        }
    }

    @Override
    public void balanceAdd(TransDTO transDTO) {
        BigDecimal amount = TransDTOMapper.toType(transDTO);

        jdbcTemplate.update("INSERT INTO accountsBalance (accountId, balance) VALUES (?, ?)" +
                                " ON CONFLICT (accountId) DO UPDATE SET balance = accountsBalance.balance + ?"
                ,transDTO.getAccountId(), amount, amount);
    }

    @Override
    public List<TransDTO> findAllStored(String accountId) {
        List<TransDTO> transactions = jdbcTemplate.query("SELECT accountId, date_occur, payload, id FROM transEventStored WHERE accountId = ?",
                new TransDTORowMapper() ,accountId);

        return transactions;
    }

    @Override
    public List<TransDTO> findAllOutBox(){
        List<TransDTO> transactions = jdbcTemplate.query("SELECT id, accountId, date_occur, payload FROM transOutBox WHERE published = ?",
                new TransDTORowMapper() ,false);

        return transactions;
    }

    @Override
    public List<TransDTO> findLastX(long limitReturn, String accountId){
        List<TransDTO> transactions = jdbcTemplate.query("SELECT accountId, date_occur, payload, id FROM transEventStored WHERE accountId = ?" +
                        " ORDER BY id DESC LIMIT ?",
                new TransDTORowMapper() ,accountId, limitReturn);

        return transactions;
    }

    @Override
    public List<TransDTO> findTransOneMinute(TransDTO transDTO) {
        List<TransDTO> transactions = jdbcTemplate.query(" SELECT accountId, date_occur, payload, id FROM transEventStored " +
                                            " WHERE date_occur >= (now()  - interval '1 minute') " +
                                            " and date_occur <= now() " +
                                            " and accountId = ? ",
                new TransDTORowMapper(), transDTO.getAccountId());

        return transactions;
    }

    @Override
    public Optional<AccountBalanceDTO> findBalance(String accountId){
        List<AccountBalanceDTO> transactions = jdbcTemplate.query("SELECT accountId, balance FROM accountsBalance " +
                        " WHERE accountId = ?",
                new AccBalanceRowMapper() , accountId);

        return transactions.stream().findFirst();

    }

    @Override
    public List<TransLogDTO> findAllById(String accountId){
        List<TransLogDTO> transactions = jdbcTemplate.query("SELECT accountId, done_at, payload, id FROM transAuditLog " +
                        " WHERE accountId = ?",
                new TransLogRowMapper(), accountId);

        return transactions;
    }

    @Override
    public String checkAcount(String accountId){
        String accountFoundId;
        accountFoundId = jdbcTemplate.queryForObject("SELECT max(accountId) FROM transEventStored WHERE accountId = ?", String.class, accountId);

        return accountFoundId == null ? "" : accountFoundId;
    }

}
