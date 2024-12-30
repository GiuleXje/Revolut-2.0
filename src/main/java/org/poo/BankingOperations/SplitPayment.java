package org.poo.BankingOperations;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.BankUsers.BankAccount;
import org.poo.BankUsers.IBANDB;
import org.poo.BankUsers.User;
import org.poo.ExchangeRate.ExchangeRate;
import org.poo.Transactions.DataForTransactions;
import org.poo.Transactions.TransactionReport;
import org.poo.fileio.CommandInput;

import java.util.List;

public final class SplitPayment implements BankingOperations {
    @Override
    public ObjectNode execute(final BankOpData command) {
        CommandInput commandInput = command.getCommandInput();
        IBANDB ibanDB = command.getIbanDB();
        ExchangeRate exchangeRate = command.getExchangeRate();
        TransactionReport transactionReport = command.getTransactionReport();
        List<String> accounts = commandInput.getAccounts();
        int people = accounts.size();
        double amountPerPerson = commandInput.getAmount() / people;
        String currency = commandInput.getCurrency();
        BankAccount failedAcc = null;
        for (String account : accounts) {
            User user = ibanDB.getUserFromIBAN(account);
            if (user != null) {
                BankAccount bankAccount = user.
                        getBankAccounts().
                        getOrDefault(account, null);
                if (bankAccount != null) {
                    String accCurrency = bankAccount.getCurrency();
                    double exRate = exchangeRate.
                            getExchangeRate(currency, accCurrency);

                    if (amountPerPerson * exRate > bankAccount.getBalance()) {
                        failedAcc = bankAccount;
                    }
                } else {
                    return null;
                }
            } else {
                return null;
            }
        }

        //signal the payment error in all accounts
        if (failedAcc != null) {
            for (String acc : accounts) {
                User accUser = ibanDB.getUserFromIBAN(acc);
                if (accUser != null) {
                    BankAccount accBankAccount = accUser.
                            getBankAccounts().
                            getOrDefault(acc, null);

                    if (accBankAccount != null) {

                        DataForTransactions data =
                                new DataForTransactions().
                                        withTimestamp(commandInput.
                                                getTimestamp()).
                                        withCommand("poorFriend").
                                        withAccount(failedAcc.getIBAN()).
                                        withAmount(commandInput.
                                                getAmount()).
                                        withSplitAmount(amountPerPerson).
                                        withCurrency(currency).
                                        withAccounts(accounts);
                        ObjectNode output = transactionReport.
                                executeOperation(data);
                        if (output != null) {
                            accBankAccount.addReport(output);
                            accUser.addTransactionReport(output);
                        }

                    }
                }
            }
            return null;
        }

        for (String account : accounts) {
            User user = ibanDB.getUserFromIBAN(account);
            if (user != null) {
                BankAccount bankAccount = user.
                        getBankAccounts().
                        getOrDefault(account, null);
                if (bankAccount != null) {
                    double exRate = exchangeRate.
                            getExchangeRate(currency,
                                    bankAccount.getCurrency());
                    bankAccount.pay(amountPerPerson * exRate);
                    DataForTransactions data = new DataForTransactions().
                            withCommand("splitBill").
                            withTimestamp(commandInput.getTimestamp()).
                            withAmount(commandInput.getAmount()).
                            withCurrency(currency).
                            withPayerIBAN(account).
                            withSplitAmount(amountPerPerson).
                            withAccounts(accounts);
                    ObjectNode output = transactionReport.
                            executeOperation(data);
                    if (output != null) {
                        ibanDB.getUserFromIBAN(account).
                                addTransactionReport(output);
                        bankAccount.addReport(output);
                    }
                }
            }
        }
        return null;
    }
}
