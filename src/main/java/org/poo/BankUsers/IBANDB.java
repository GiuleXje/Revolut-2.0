package org.poo.BankUsers;

import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;

@Getter
@Setter
public final class IBANDB {
    // the key here is the IBAN
    private LinkedHashMap<String, User> associatedIBANs;

    public IBANDB() {
        associatedIBANs = new LinkedHashMap<>();
    }

    /**
     * inserts an IBAN to the database
     *
     * @param iBAN the IBAN of the account
     * @param user the user info
     */
    public void addIBANReference(final String iBAN, final User user) {
        associatedIBANs.put(iBAN, user);
    }

    /**
     * gets an existing user based on the IBAN
     *
     * @param iBAN the bank account number
     * @return the user, if it exists
     */
    public User getUserFromIBAN(final String iBAN) {
        return associatedIBANs.getOrDefault(iBAN, null);
    }

    /**
     * deletes a bank account
     *
     * @param iBAN the account number
     */
    public void deleteAccount(final String iBAN) {
        associatedIBANs.remove(iBAN);
    }
}
