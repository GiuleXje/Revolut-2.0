package org.poo.BankUsers;

import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;

@Getter
@Setter
public final class AliasDB {
    // hash by the alias + the email address of the user to ensure that
    // we select the correct user(in case of duplicate aliases for different users)
    private final LinkedHashMap<String, BankAccount> associatedAliases;

    public AliasDB() {
        associatedAliases = new LinkedHashMap<>();
    }

    /**
     * sets up an alias for a bank account
     *
     * @param alias   -
     * @param account -
     */
    public void addAlias(final String alias, final BankAccount account) {
        associatedAliases.put(alias, account);
    }

    /**
     * removes an alias from the database(happens when deleting a bank account)
     *
     * @param alias -
     */
    public void removeAlias(final String alias) {
        associatedAliases.remove(alias);
    }

}
