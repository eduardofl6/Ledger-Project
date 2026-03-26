package com.example.ledgerPrototype.controller;

import com.example.ledgerPrototype.dao.TransDAO;
import com.example.ledgerPrototype.domain.AccountBalanceDTO;
import com.example.ledgerPrototype.domain.TransDTO;
import com.example.ledgerPrototype.domain.TransDTOWeb;
import com.example.ledgerPrototype.domain.TransLogDTO;
import com.example.ledgerPrototype.mappers.TransDTOMapper;
import com.example.ledgerPrototype.service.transCommand;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/account")
public class AccountOperations {

    private final TransDAO transDAO;
    private ObjectMapper objectMapper = new ObjectMapper();

    private transCommand transCommand;

    public AccountOperations(transCommand transCommand, TransDAO transDAO) {
        this.transCommand = transCommand;
        this.transDAO = transDAO;
    }

    @PostMapping("/operation")
    public ResponseEntity<String> operation(@RequestBody TransDTOWeb transaction){
        TransDTO transactionDTO = TransDTO.builder()
                .payload(objectMapper.writeValueAsString(transaction))
                .accountId(transaction.getAccountId())
                .build();
        String response = "";

        BigDecimal balance = transCommand.reconstituteBalance(transactionDTO.getAccountId());
        BigDecimal transValue = TransDTOMapper.toType(transactionDTO);

        if(transCommand.checkFraudStatus(transactionDTO.getAccountId())){
            response = "Account Locked (Fraud Suspicion)";
            return new ResponseEntity<>(response,HttpStatus.UNPROCESSABLE_ENTITY);
        }else if(balance.add(transValue).compareTo(BigDecimal.ZERO) <= 0){
            response = "Transaction unsuccessful (not enough balance)";
            return new ResponseEntity<>(response,HttpStatus.UNPROCESSABLE_ENTITY);
        }

        transCommand.operate(transactionDTO);

        return new ResponseEntity<>(response,HttpStatus.OK);
    }

    @GetMapping("/{accountId}/balance")
    public ResponseEntity<AccountBalanceDTO> getBalance(@PathVariable String accountId){
        Optional<AccountBalanceDTO> balance = transCommand.consultBalance(accountId);

        if(balance.isPresent()){
            return new ResponseEntity<>(balance.get(),HttpStatus.OK);
        }else{
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/{accountId}/transactions")
    public ResponseEntity<List<TransLogDTO>> getTransactions(@PathVariable String accountId){
        List<TransLogDTO> transactions = transCommand.consultHistory(accountId);

        if(transactions.isEmpty()){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }else{
            return new ResponseEntity<>(transactions,HttpStatus.OK);
        }
    }

    @PostMapping("/{accountId}/fraud")
    public ResponseEntity<String> changeFraudStatus(@PathVariable String accountId){
        if ((transCommand.checkAccount(accountId).isEmpty())){
            return new ResponseEntity<>("Account not found",HttpStatus.NOT_FOUND);
        }

        transCommand.changeFraudStatus(accountId);
        return new ResponseEntity<>("Fraud Status successfully changed",HttpStatus.GONE);
    }

    @GetMapping("/{accountId}/fraud")
    public ResponseEntity<String> checkFraudStatus(@PathVariable String accountId){
        if ((transCommand.checkAccount(accountId).isEmpty()))
            return new ResponseEntity<>("Account not found",HttpStatus.NOT_FOUND);

        if (!transCommand.checkFraudStatus(accountId))
            return new ResponseEntity<>("Account without fraud",HttpStatus.OK);
        else
            return new ResponseEntity<>("Account with fraud ",HttpStatus.OK);

    }

}
