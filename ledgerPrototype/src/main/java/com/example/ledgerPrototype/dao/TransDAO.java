package com.example.ledgerPrototype.dao;

import com.example.ledgerPrototype.domain.AccountBalanceDTO;
import com.example.ledgerPrototype.domain.TransDTO;
import com.example.ledgerPrototype.domain.TransFraudDTO;
import com.example.ledgerPrototype.domain.TransLogDTO;
import org.springframework.data.domain.Page;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface TransDAO {

    public void saveEvent(TransDTO transDTO);

    public void saveEventLog(TransDTO transDTO);

    public void saveEventFraud(TransFraudDTO transFDTO);

    public void publishEvent(TransDTO transDTO);

    public void changeFraudStatAcc(String accountId, boolean status);

    public void balanceAdd(TransDTO transDTO);

    public List<TransDTO> findAllStored(String accountId);

    public List<TransDTO> findAllOutBox();

    public List<TransDTO> findLastX(long limitReturn, String accountId);

    public List<TransDTO> findTransOneMinute(TransDTO transDTO);

    Optional<AccountBalanceDTO> findBalance(String accountId);

    public List<TransLogDTO> findAllById(String accountId);

    public Boolean checkFraud(String accountId);

    public String checkAcount(String accountId);
}
