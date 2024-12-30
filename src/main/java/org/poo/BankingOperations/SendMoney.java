package org.poo.BankingOperations;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.BankUsers.User;
import org.poo.BankUsers.AliasDB;
import org.poo.BankUsers.BankAccount;
import org.poo.BankUsers.IBANDB;
import org.poo.ExchangeRate.ExchangeRate;
import org.poo.Transactions.DataForTransactions;
import org.poo.Transactions.TransactionReport;
import org.poo.fileio.CommandInput;

public final class SendMoney implements BankingOperations {
    @Override
    public ObjectNode execute(final BankOpData command) {
        CommandInput commandInput = command.getCommandInput();
        TransactionReport transactionReport = command.getTransactionReport();
        IBANDB ibanDB = command.getIbanDB();
        ExchangeRate exchangeRate = command.getExchangeRate();
        AliasDB aliasDB = command.getAliasDB();
        String giver = commandInput.getAccount();
        String receiver = commandInput.getReceiver();
        String email = commandInput.getEmail();
        User giverUser = ibanDB.getUserFromIBAN(giver);
        if (giverUser == null) {
            return null;
        }

        BankAccount giverAccount = giverUser.
                getBankAccounts().
                getOrDefault(giver, null);
        if (giverAccount != null) {
            String receiverAlias = receiver + email;
            BankAccount receiverAccount = aliasDB.
                    getAssociatedAliases().
                    getOrDefault(receiverAlias, null);
            double amount = commandInput.getAmount();

            if (receiverAccount != null) {
                nullCase(giverAccount, exchangeRate, receiverAccount, amount,
                        commandInput, transactionReport, ibanDB);
            } else {
                User receivingUser = ibanDB.getUserFromIBAN(receiver);
                if (receivingUser != null) {
                    BankAccount receiverBAccount = receivingUser.
                            getBankAccounts().
                            getOrDefault(receiver, null);

                    if (receiverBAccount != null) {
                        String accCurrency = giverAccount.getCurrency();
                        double exRate = exchangeRate.
                                getExchangeRate(accCurrency,
                                        receiverBAccount.
                                                getCurrency());

                        if (amount < giverAccount.getBalance()) {
                            giverAccount.pay(amount);
                            receiverBAccount.
                                    addFunds(amount * exRate);

                            DataForTransactions data =
                                    new DataForTransactions().
                                            withCommand("sendMoney").
                                            withTimestamp(commandInput.
                                                    getTimestamp()).
                                            withDescription(commandInput.
                                                    getDescription()).
                                            withAmount(amount).
                                            withCurrency(accCurrency).
                                            withPayerIBAN(giverAccount.
                                                    getIBAN()).
                                            withReceiverIBAN(receiverBAccount.
                                                    getIBAN()).
                                            withTransferType("sent");
                            ObjectNode output = transactionReport.
                                    executeOperation(data);
                            if (output != null) {
                                giverAccount.addReport(output);
                                ibanDB.
                                        getUserFromIBAN(giverAccount.
                                                getIBAN()).
                                        addTransactionReport(output);
                            }
                            data =
                                    new DataForTransactions().
                                            withCommand("sendMoney").
                                            withTimestamp(commandInput.
                                                    getTimestamp()).
                                            withDescription(commandInput.
                                                    getDescription()).
                                            withAmount(amount * exRate).
                                            withCurrency(receiverBAccount.
                                                    getCurrency()).
                                            withPayerIBAN(giverAccount.
                                                    getIBAN()).
                                            withReceiverIBAN(receiverBAccount.
                                                    getIBAN()).
                                            withTransferType("r"
                                                    + "eceived");
                            output =
                                    transactionReport.
                                            executeOperation(data);
                            if (output != null) {
                                receiverBAccount.addReport(output);
                                ibanDB.
                                        getUserFromIBAN(receiverBAccount.
                                                getIBAN()).
                                        addTransactionReport(output);
                            }
                        } else {
                            DataForTransactions data =
                                    new DataForTransactions().
                                            withCommand("noFunds").
                                            withTimestamp(commandInput.
                                                    getTimestamp());
                            ObjectNode output = transactionReport.
                                    executeOperation(data);
                            if (output != null) {
                                giverAccount.
                                        addReport(output);
                                ibanDB.
                                        getUserFromIBAN(giverAccount.
                                                getIBAN()).
                                        addTransactionReport(output);
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    private static void nullCase(final BankAccount giverAccount, final ExchangeRate exchangeRate,
                                 final BankAccount receiverAccount, final double amount,
                                 final CommandInput commandInput,
                                 final TransactionReport transactionReport, final IBANDB ibanDB) {
        String accCurrency = giverAccount.getCurrency();
        double exRate = exchangeRate.
                getExchangeRate(accCurrency,
                        receiverAccount.getCurrency());

        if (amount < giverAccount.getBalance()) {
            giverAccount.pay(amount);
            receiverAccount.addFunds(amount * exRate);

            DataForTransactions data = new DataForTransactions().
                    withCommand("sendMoney").
                    withTimestamp(commandInput.getTimestamp()).
                    withDescription(commandInput.
                            getDescription()).
                    withAmount(amount).
                    withCurrency(accCurrency).
                    withPayerIBAN(giverAccount.getIBAN()).
                    withReceiverIBAN(receiverAccount.
                            getIBAN()).
                    withTransferType("sent");

            ObjectNode output = transactionReport.
                    executeOperation(data);
            if (output != null) {
                ibanDB.
                        getUserFromIBAN(giverAccount.getIBAN()).
                        addTransactionReport(output);
                giverAccount.addReport(output);
            }

            data = new DataForTransactions().withCommand("sendMoney").
                    withTimestamp(commandInput.getTimestamp()).
                    withDescription(commandInput.
                            getDescription()).
                    withAmount(amount * exRate).
                    withCurrency(receiverAccount.getCurrency()).
                    withPayerIBAN(giverAccount.getIBAN()).
                    withReceiverIBAN(receiverAccount.
                            getIBAN()).
                    withTransferType("received");
            output = transactionReport.executeOperation(data);
            if (output != null) {
                ibanDB.
                        getUserFromIBAN(receiverAccount.
                                getIBAN()).
                        addTransactionReport(output);
                receiverAccount.addReport(output);
            }
        } else {
            DataForTransactions data =
                    new DataForTransactions().
                            withCommand("noFunds").
                            withTimestamp(commandInput.getTimestamp());
            ObjectNode output = transactionReport.
                    executeOperation(data);
            if (output != null) {
                ibanDB.
                        getUserFromIBAN(giverAccount.getIBAN()).
                        addTransactionReport(output);
            }
        }
    }
}
