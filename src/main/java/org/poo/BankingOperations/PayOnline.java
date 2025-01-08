package org.poo.BankingOperations;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.BankUsers.*;
import org.poo.ExchangeRate.ExchangeRate;
import org.poo.ExchangeRate.Pair;
import org.poo.Merchants.Merchant;
import org.poo.Merchants.MerchantsDB;
import org.poo.Transactions.DataForTransactions;
import org.poo.Transactions.TransactionReport;
import org.poo.fileio.CommandInput;

public final class PayOnline implements BankingOperations {
    private static final double SILVER_TO_GOLD = 300.0;
    private static final int RANK_UP = 5;
    public void getCashback(final User user, final BankAccount bankAccount, final double paid,
                            final String merchantName, final BankOpData command,
                            final double amount) {
        MerchantsDB merchantDB = command.getMerchantsDB();
        Merchant merchant = merchantDB.merchantInfo(merchantName);

        assert merchant != null;

        if (merchant.getCashbackPlan().equals("spendingThreshold")) {
            merchant.spendMore(paid, bankAccount);
        }

        merchant.getCashback(paid, bankAccount,
                user.getPlan(), command.getExchangeRate());
        //merchant.forceCashback(amount, bankAccount);
    }
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
            User user = ibanDB.getUserFromIBAN(bankAccount.getIBAN());
            assert user != null;
            if (amount == 0) {
                return null;
            }
            double toRON = exchangeRate.getExchangeRate(commandInput.getCurrency(), "RON");
            double amountInRON = amount * toRON;
            if (amountInRON >= SILVER_TO_GOLD) {
                user.incrementPayToWin();
            }
            double feeInRON = user.getServicePlan().fee(amountInRON);
            double fee = feeInRON * exchangeRate.getExchangeRate("RON", accCurrency);
            if (amount * exRate
                    + fee
                    <= bankAccount.getBalance()) {
                if (bankAccount.getBalance()
                        - amount * exRate - fee < bankAccount.getMinAmount()) {
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
                bankAccount.pay(amount * exRate + fee);
                double toRon = exchangeRate.getExchangeRate(commandInput.getCurrency(), "RON");
                bankAccount.increaseTransactions(); // add this transaction
                getCashback(user, bankAccount, amount * toRon,
                        commandInput.getCommerciant(), command, amount);
                // if 5 payments were recorded and the user has a silver plan
                if (amount * toRON >= 300) {
                    user.incrementPayToWin();
                }
                if (user.getPayToWin() == RANK_UP && user.getPlan().equals("silver")) {
                    user.changeServicePlan("gold");
                }
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
