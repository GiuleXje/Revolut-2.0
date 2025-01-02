package org.poo.BankUsers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.LinkedHashMap;

@Setter
@Getter
public final class User {
    private final String firstName;
    private final String lastName;
    private final String email;
    private final String birthDate;
    private final String occupation;
    private LinkedHashMap<String, BankAccount> bankAccounts;
    private ArrayList<ObjectNode> transactionReport;

    public User(final String firstName, final String lastName, final String email,
                final String birthDate, final String occupation) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        bankAccounts = new LinkedHashMap<>();
        transactionReport = new ArrayList<>();
        this.birthDate = birthDate;
        this.occupation = occupation;
    }

    /**
     * adds a bank account
     *
     * @param bankAccount -
     */
    public void addBankAccount(final BankAccount bankAccount) {
        bankAccounts.put(bankAccount.getIBAN(), bankAccount);
    }

    /**
     * removes the bank account
     *
     * @param iban -
     */
    public void removeBankAccount(final String iban) {
        bankAccounts.remove(iban);
    }

    /**
     * adds a report
     *
     * @param report -
     */
    public void addTransactionReport(final ObjectNode report) {
        transactionReport.add(report);
    }

    /**
     * generates a report of the actions performed between 2 timestamps
     *
     * @param start   -
     * @param end     -
     * @param account the account
     * @return the report
     */
    public ArrayNode generateReport(final int start, final int end, final String account) {
        ArrayNode transactions = new ObjectMapper().createArrayNode();
        for (ObjectNode transaction : transactionReport) {
            int timestamp = Integer.parseInt(transaction.get("timestamp").asText());
            if (transaction.get("IBAN") != null) {
                String accountNumber = transaction.get("IBAN").asText();
                if (accountNumber != null) {
                    if (timestamp >= start
                            && timestamp <= end
                            && accountNumber.equals(account)) {
                        transactions.add(transaction);
                    }
                }
            }
        }
        return transactions;
    }

    /**
     * finds the first account with a given currency
     * @param currency
     * the currency we're looking for
     * @return
     * the account, if it exists
     */
    public BankAccount getFirstAccountWithCurrency(final String currency) {
        for (BankAccount bankAccount : bankAccounts.values()) {
            if (bankAccount.getCurrency().equals(currency)
                    && bankAccount.getAccountType().equals("classic")) {
                return bankAccount;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return firstName + " " + lastName + " " + email;
    }
}
