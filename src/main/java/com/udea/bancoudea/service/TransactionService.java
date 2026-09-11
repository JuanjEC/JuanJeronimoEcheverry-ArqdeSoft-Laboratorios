package com.udea.bancoudea.service;

import com.udea.bancoudea.dto.TransactionDTO;
import com.udea.bancoudea.entity.Customer;
import com.udea.bancoudea.entity.Transaction;
import com.udea.bancoudea.repository.CustomerRepository;
import com.udea.bancoudea.repository.TransactionRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private CustomerRepository customerRepository;

    public TransactionDTO transferMoney(TransactionDTO transactionDTO) {

        if (transactionDTO.getSenderAccountNumber() == null
                || transactionDTO.getReceiverAccountNumber() == null) {

            throw new IllegalArgumentException(
                    "Sender Account Number or Receiver Account Number cannot be null"
            );
        }

        Customer sender = customerRepository
                .findByAccountNumber(transactionDTO.getSenderAccountNumber())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Sender Account Number not found"
                        )
                );

        Customer receiver = customerRepository
                .findByAccountNumber(transactionDTO.getReceiverAccountNumber())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Receiver Account Number not found"
                        )
                );

        if (sender.getBalance() < transactionDTO.getAmount()) {
            throw new IllegalArgumentException(
                    "Sender Balance not enough"
            );
        }

        sender.setBalance(
                sender.getBalance() - transactionDTO.getAmount()
        );

        receiver.setBalance(
                receiver.getBalance() + transactionDTO.getAmount()
        );

        customerRepository.save(sender);
        customerRepository.save(receiver);

        Transaction transaction = new Transaction();

        transaction.setSenderAccountNumber(
                sender.getAccountNumber()
        );

        transaction.setReceiverAccountNumber(
                receiver.getAccountNumber()
        );

        transaction.setAmount(
                transactionDTO.getAmount()
        );

        transaction.setTimestamp(LocalDateTime.now());

        transaction = transactionRepository.save(transaction);

        TransactionDTO savedTransaction = new TransactionDTO();

        savedTransaction.setId(transaction.getId());

        savedTransaction.setSenderAccountNumber(
                transaction.getSenderAccountNumber()
        );

        savedTransaction.setReceiverAccountNumber(
                transaction.getReceiverAccountNumber()
        );

        savedTransaction.setAmount(
                transaction.getAmount()
        );

        return savedTransaction;
    }

    public List<TransactionDTO> getTransactionsForAccount(
            String accountNumber
    ) {

        List<Transaction> transactions =
                transactionRepository
                        .findBySenderAccountNumberOrReceiverAccountNumber(
                                accountNumber,
                                accountNumber
                        );

        return transactions.stream()
                .map(transaction -> {

                    TransactionDTO dto = new TransactionDTO();

                    dto.setId(transaction.getId());

                    dto.setSenderAccountNumber(
                            transaction.getSenderAccountNumber()
                    );

                    dto.setReceiverAccountNumber(
                            transaction.getReceiverAccountNumber()
                    );

                    dto.setAmount(
                            transaction.getAmount()
                    );

                    return dto;
                })
                .collect(Collectors.toList());
    }
}