package org.poo.BankingOperations;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.BankUsers.*;
import org.poo.ExchangeRate.ExchangeRate;
import org.poo.Transactions.DataForTransactions;
import org.poo.Transactions.TransactionReport;
import org.poo.fileio.CommandInput;


public final class AddAccount implements BankingOperations {
    /**
     * creates a new business account
     * @param command -
     */
    private void createBusinessAccount(final BankOpData command) {
        CommandInput commandInput = command.getCommandInput();
        String email = commandInput.getEmail();
        String currency = commandInput.getCurrency();
        EmailDB emailDB = command.getEmailDB();
        int timestamp = commandInput.getTimestamp();
        User owner = emailDB.getUser(email);
        if (owner == null) {
            return;
        }
        // business account constructor
        BankAccount bankAccount = new BankAccount(email, currency, owner,
                commandInput.getTimestamp());
        ExchangeRate exchangeRate = command.getExchangeRate();
        double exRate = exchangeRate.getExchangeRate("RON", currency);
        bankAccount.setDepositLimit(bankAccount.getDepositLimit() * exRate);
        bankAccount.setSpendingLimit(bankAccount.getSpendingLimit() * exRate);
        IBANDB ibanDB = command.getIbanDB();
        ibanDB.addIBANReference(bankAccount.getIBAN(), owner);
        owner.addBankAccount(bankAccount);
        AliasDB aliasDB = command.getAliasDB();
        aliasDB.addAlias(bankAccount.getIBAN() + email, bankAccount);
        AccountDB accountDB = command.getAccountDB();
        accountDB.addAccount(bankAccount.getIBAN(), bankAccount);
        DataForTransactions data = new DataForTransactions().
                withTimestamp(timestamp).
                withCommand("addAccount");
        TransactionReport transactionReport = command.getTransactionReport();
        ObjectNode report = transactionReport.executeOperation(data);
        bankAccount.addReport(report);
        owner.addTransactionReport(report);
    }
    @Override
    public ObjectNode execute(final BankOpData command) {
        CommandInput commandInput = command.getCommandInput();
        if (commandInput.getAccountType().equals("business")) {
            // in case of a new business account
            createBusinessAccount(command);
            return null;
        }
        AliasDB aliasDB = command.getAliasDB();
        IBANDB ibanDB = command.getIbanDB();
        TransactionReport transactionReport = command.getTransactionReport();
        EmailDB emailDB = command.getEmailDB();
        if (commandInput.getAccountType().equals("classic")) {
            commandInput.setInterestRate(0);
        }


        User user = emailDB.getUser(commandInput.getEmail());
        BankAccount bankAccount = new BankAccount(commandInput.getEmail(),
                commandInput.getCurrency(),
                commandInput.getAccountType(), commandInput.getTimestamp(),
                commandInput.getInterestRate());
        user.addBankAccount(bankAccount);
        aliasDB.addAlias(bankAccount.getIBAN() + user.getEmail(), bankAccount);
        ibanDB.addIBANReference(bankAccount.getIBAN(), user);
        AccountDB accountDB = command.getAccountDB();
        accountDB.addAccount(bankAccount.getIBAN(), bankAccount);
        DataForTransactions data = new DataForTransactions().
                withCommand("addAccount").
                withTimestamp(commandInput.getTimestamp());
        ObjectNode output = transactionReport.executeOperation(data);
        if (output != null) {
            user.addTransactionReport(output);
            bankAccount.addReport(output);
        }
        return null;
    }
}
