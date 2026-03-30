package com.example.ledgerPrototype.controller;

import com.example.ledgerPrototype.dao.TransDAO;
import com.example.ledgerPrototype.domain.AccountBalanceDTO;
import com.example.ledgerPrototype.domain.TransDTO;
import com.example.ledgerPrototype.domain.TransDTOWeb;
import com.example.ledgerPrototype.domain.TransLogDTO;
import com.example.ledgerPrototype.mappers.TransDTOMapper;
import com.example.ledgerPrototype.service.TransCommand;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/account")
public class AccountOperations {

    private final TransDAO transDAO;
    private ObjectMapper objectMapper = new ObjectMapper();

    private TransCommand transCommand;

    public AccountOperations(TransCommand transCommand, TransDAO transDAO) {
        this.transCommand = transCommand;
        this.transDAO = transDAO;
    }

    @PostMapping("/operation")
    public ResponseEntity<String> operation(@RequestBody TransDTOWeb transaction){
        String response = "";

        if(transaction.getAccountId().isBlank()){
            response = "AccountId can't be blank";
            return new ResponseEntity<>(response,HttpStatus.BAD_REQUEST);
        }

        if(!(transaction.getType().equals("credit") || transaction.getType().equals("debit"))){
            response = "Type must be credit or debit";
            return new ResponseEntity<>(response,HttpStatus.BAD_REQUEST);
        }

        if(transaction.getAmount().compareTo(BigDecimal.ZERO) <= 0){
            response = "Amount must greater than zero";
            return new ResponseEntity<>(response,HttpStatus.BAD_REQUEST);
        }

        String payload;
        try{
            payload = objectMapper.writeValueAsString(transaction);
        }
        catch (Exception e){
            throw new RuntimeException("Failed to parse WebDto to String",e);
        }

        TransDTO transactionDTO = TransDTO.builder()
                .payload(payload)
                .accountId(transaction.getAccountId())
                .build();

        BigDecimal balance = transCommand.reconstituteBalance(transactionDTO.getAccountId());
        BigDecimal transValue = TransDTOMapper.toType(transactionDTO);

        if(transCommand.checkFraudStatus(transactionDTO.getAccountId())){
            response += "Account Locked (Fraud Suspicion)";
            return new ResponseEntity<>(response,HttpStatus.UNPROCESSABLE_ENTITY);
        }
        if(balance.add(transValue).compareTo(BigDecimal.ZERO) <= 0){
            response = "Transaction unsuccessful (not enough balance)";
            return new ResponseEntity<>(response,HttpStatus.UNPROCESSABLE_ENTITY);
        }


        transCommand.operate(transactionDTO);

        return new ResponseEntity<>(response,HttpStatus.OK);
    }

    @GetMapping("/{accountId}/balance")
    public ResponseEntity<AccountBalanceDTO> getBalance(@PathVariable String accountId){
        if ((transCommand.checkAccount(accountId).isEmpty())){
            return new ResponseEntity<>(AccountBalanceDTO.builder()
                    .accountId("Account doesn't exist").balance(BigDecimal.valueOf(-1
                    )).build(),HttpStatus.NOT_FOUND);
        }
        Optional<AccountBalanceDTO> balance = transCommand.consultBalance(accountId);

        if(balance.isPresent()){
            return new ResponseEntity<>(balance.get(),HttpStatus.OK);
        }else{
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/{accountId}/transactions")
    public ResponseEntity<List<TransLogDTO>> getTransactions(@PathVariable String accountId){
        if ((transCommand.checkAccount(accountId).isEmpty()))
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

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
