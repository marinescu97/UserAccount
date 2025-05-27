package com.spring.Account.repository;

import com.spring.Account.model.Account;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Repository
public class AccountRepository {
    private final Map<UUID, Account> accounts = new HashMap<>();

    public void save(Account account) {
        accounts.put(account.getId(), account);
    }

    public Account findById(UUID id) {
        return accounts.get(id);
    }
}
