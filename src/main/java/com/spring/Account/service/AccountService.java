package com.spring.Account.service;

import com.spring.Account.model.Account;
import com.spring.Account.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class AccountService {
    private final AccountRepository repository;
    private final EmailService emailService;

    @Autowired
    public AccountService(AccountRepository repository, EmailService emailService) {
        this.repository = repository;
        this.emailService = emailService;
    }

    public Account save(Account account) throws IOException {
        account.setId(UUID.randomUUID());
        account.setCreatedAt(LocalDateTime.now());
        repository.save(account);
        emailService.sendAccountCreatedEmail(account.getEmail(), account.getFirstName(), account.getId());
        return account;
    }

    public Optional<Account> findById(UUID id) {
        return Optional.ofNullable(repository.findById(id));
    }

    public void deleteAll() {
        repository.deleteAll();
    }
}
