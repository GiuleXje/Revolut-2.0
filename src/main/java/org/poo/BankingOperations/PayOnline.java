package org.poo.BankingOperations;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.BankUsers.BankAccount;
import org.poo.BankUsers.Card;
import org.poo.BankUsers.CardDB;
import org.poo.BankUsers.IBANDB;
import org.poo.ExchangeRate.ExchangeRate;
import org.poo.ExchangeRate.Pair;
import org.poo.Transactions.DataForTransactions;
import org.poo.Transactions.TransactionReport;
import org.poo.fileio.CommandInput;

public final class PayOnline implements BankingOperations {
    @Override
    public ObjectNode execute(final BankOpData command) {
        CommandInput commandInput = command.getCommandInput();
        CardDB cardDB = command.getCardDB();
        TransactionReport transactionReport = command.getTransactionReport();
        IBANDB ibanDB = command.getIbanDB();
        ExchangeRate exchangeRate = command.getExchangeRate();

        String cardNumber = commandInput.getCardNumber();
        BankAccount bankAccount = cardDB.
                getAssociatedCards().
                getOrDefault(cardNumber, null);
        String merchant = commandInput.getCommerciant();
        if (bankAccount != null) {
            double amount = commandInput.getAmount();
            String accCurrency = bankAccount.getCurrency();
            Card card = bankAccount.getCards().getOrDefault(cardNumber, null);
            if (card.getStatus().equals("frozen")) {
                DataForTransactions data = new DataForTransactions().
                        withCommand("frozen").
                        withTimestamp(commandInput.getTimestamp());
                ObjectNode output = transactionReport.executeOperation(data);
                if (output != null) {
                    ibanDB.
                            getUserFromIBAN(bankAccount.
                                    getIBAN()).
                            addTransactionReport(output);
                    bankAccount.addReport(output);
                }
                return null;
            }
            double exRate = exchangeRate.
                    getExchangeRate(commandInput.getCurrency(), accCurrency);
            if (amount * exRate
                    + bankAccount.getServicePlan().fee(amount * exRate)
                    <= bankAccount.getBalance()) {
                if (bankAccount.getBalance()
                        - amount * exRate < bankAccount.getMinAmount()) {
                    bankAccount.freezeThisCard(cardNumber);
                    DataForTransactions data = new DataForTransactions().
                            withCommand("frozen").
                            withTimestamp(commandInput.getTimestamp());
                    ObjectNode output = transactionReport.
                            executeOperation(data);
                    if (output != null) {
                        ibanDB.
                                getUserFromIBAN(bankAccount.getIBAN()).
                                addTransactionReport(output);
                        bankAccount.addReport(output);
                    }
                    return null;
                }
                bankAccount.pay(amount * exRate);
                DataForTransactions data = new DataForTransactions().
                        withCommand("payOnline").
                        withAmount(amount * exRate).
                        withMerchant(commandInput.
                                getCommerciant()).
                        withTimestamp(commandInput.getTimestamp());
                ObjectNode output = transactionReport.executeOperation(data);
                if (output != null) {
                    ibanDB.
                            getUserFromIBAN(bankAccount.
                                    getIBAN()).
                            addTransactionReport(output);
                    bankAccount.addReport(output);
                }
                // add the spending report
                bankAccount.getMerchants().
                        put(commandInput.getTimestamp(),
                                new Pair<>(merchant, amount * exRate));
                // add the transaction report
                if (bankAccount.getCards().get(cardNumber) != null) {
                    if (bankAccount.getCards().get(cardNumber).getOneTime()) {
                        bankAccount.
                                deleteCard(bankAccount.
                                        getCards().
                                        get(cardNumber));
                        // delete the card from the database
                        cardDB.deleteCard(cardNumber);
                        data = new DataForTransactions().
                                withCommand("deleteCard").
                                withAccount(bankAccount.getIBAN()).
                                withCardNumber(cardNumber).
                                withEmail(bankAccount.
                                        getEmail()).
                                withTimestamp(commandInput.
                                        getTimestamp());
                        output = transactionReport.executeOperation(data);
                        if (output != null) {
                            ibanDB.
                                    getUserFromIBAN(bankAccount.
                                            getIBAN()).
                                    addTransactionReport(output);
                            bankAccount.addReport(output);
                        }

                        Card newCard = new Card(true);
                        bankAccount.addCard(newCard);
                        cardDB.addCard(newCard, bankAccount);
                        data = new DataForTransactions().
                                withCommand("createCard").
                                withCardNumber(newCard.
                                        getNumber()).
                                withAccount(bankAccount.
                                        getIBAN()).
                                withEmail(bankAccount.
                                        getEmail()).
                                withTimestamp(commandInput.
                                        getTimestamp());
                        output = transactionReport.
                                executeOperation(data);
                        if (output != null) {
                            ibanDB.
                                    getUserFromIBAN(bankAccount.
                                            getIBAN()).
                                    addTransactionReport(output);
                            bankAccount.addReport(output);
                        }

                    }
                }
            } else {
                // no funds report
                DataForTransactions data =
                        new DataForTransactions().
                                withCommand("noFunds").
                                withTimestamp(commandInput.getTimestamp());
                ObjectNode output = transactionReport.executeOperation(data);
                if (output != null) {
                    ibanDB.
                            getUserFromIBAN(bankAccount.getIBAN()).
                            addTransactionReport(output);
                    bankAccount.addReport(output);
                }
            }
        } else {
            ObjectNode output = new ObjectMapper().createObjectNode();
            output.put("command", "payOnline");
            ObjectNode aux = new ObjectMapper().createObjectNode();
            aux.put("timestamp", commandInput.getTimestamp());
            aux.put("description", "Card not found");
            output.set("output", aux);
            output.put("timestamp", commandInput.getTimestamp());
            return output;
        }

        return null;
    }
}
