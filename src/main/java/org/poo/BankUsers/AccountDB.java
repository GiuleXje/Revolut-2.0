package org.poo.BankUsers;

import java.util.LinkedHashMap;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public final class AccountDB {
    private LinkedHashMap<String, BankAccount> accounts;

    public AccountDB() {
        accounts = new LinkedHashMap<>();
    }

    /**
     * adds a bankAccount
     *
     * @param alias   alias can either be the IBAN + email, or the alias + email
     * @param account -
     */
    public void addAccount(final String alias, final BankAccount account) {
        accounts.put(alias, account);
    }

    /**
     * checks for an account
     *
     * @param alias -
     */
    public void getAccount(final String alias) {
        accounts.getOrDefault(alias, null);
    }
}
