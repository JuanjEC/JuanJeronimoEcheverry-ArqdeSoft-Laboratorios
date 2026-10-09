package com.udea.bancoudea;

import com.udea.bancoudea.dto.CustomerDTO;
import com.udea.bancoudea.dto.TransactionDTO;
import com.udea.bancoudea.service.CustomerService;
import com.udea.bancoudea.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class BancoServicesTests {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private TransactionService transactionService;

    private CustomerDTO newCustomer(String account, double balance) {
        CustomerDTO dto = new CustomerDTO();
        dto.setFirstName("Ana");
        dto.setLastName("Lopez");
        dto.setAccountNumber(account);
        dto.setBalance(balance);
        return customerService.createCustomer(dto);
    }

    private TransactionDTO newTransfer(String from, String to, double amount) {
        TransactionDTO dto = new TransactionDTO();
        dto.setSenderAccountNumber(from);
        dto.setReceiverAccountNumber(to);
        dto.setAmount(amount);
        return dto;
    }

    @Test
    void createAndFindCustomer() {
        CustomerDTO created = newCustomer("T-0001", 500.0);
        assertNotNull(created.getId());
        CustomerDTO found = customerService.getCustomerById(created.getId());
        assertEquals("T-0001", found.getAccountNumber());
        assertEquals(500.0, found.getBalance());
    }

    @Test
    void listCustomers() {
        newCustomer("T-0002", 100.0);
        assertTrue(customerService.getAllCustomer().size() >= 1);
    }

    @Test
    void customerNotFound() {
        assertThrows(RuntimeException.class, () -> customerService.getCustomerById(-1L));
    }

    @Test
    void transferMoney() {
        CustomerDTO sender = newCustomer("T-0003", 1000.0);
        CustomerDTO receiver = newCustomer("T-0004", 200.0);

        TransactionDTO saved = transactionService.transferMoney(newTransfer("T-0003", "T-0004", 300.0));

        assertNotNull(saved.getId());
        assertEquals(700.0, customerService.getCustomerById(sender.getId()).getBalance());
        assertEquals(500.0, customerService.getCustomerById(receiver.getId()).getBalance());
    }

    @Test
    void transferWithInsufficientBalance() {
        newCustomer("T-0005", 50.0);
        newCustomer("T-0006", 0.0);
        assertThrows(IllegalArgumentException.class,
                () -> transactionService.transferMoney(newTransfer("T-0005", "T-0006", 100.0)));
    }

    @Test
    void transferWithUnknownAccount() {
        newCustomer("T-0007", 50.0);
        assertThrows(IllegalArgumentException.class,
                () -> transactionService.transferMoney(newTransfer("T-0007", "NO-EXISTE", 10.0)));
    }

    @Test
    void transactionsByAccount() {
        newCustomer("T-0008", 400.0);
        newCustomer("T-0009", 0.0);
        transactionService.transferMoney(newTransfer("T-0008", "T-0009", 100.0));
        List<TransactionDTO> list = transactionService.getTransactionsForAccount("T-0008");
        assertEquals(1, list.size());
    }
}
