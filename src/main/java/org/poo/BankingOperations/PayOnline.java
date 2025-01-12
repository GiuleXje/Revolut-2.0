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

import java.util.HashMap;

public final class PayOnline implements BankingOperations {
    private static final double SILVER_TO_GOLD = 300.0;
    private static final int RANK_UP = 5;

    public void handleOneTimeCard(final BankAccount bankAccount, final Card card,
                                  final User payer, BankOpData command) {
        String cardNumber = card.getNumber();
        CommandInput commandInput = command.getCommandInput();
        CardDB cardDB = command.getCardDB();
        IBANDB ibanDB = command.getIbanDB();
        TransactionReport transactionReport = command.getTransactionReport();

        // delete the card from the database
        cardDB.deleteCard(cardNumber);
        // delete the card form the bank account
        bankAccount.deleteCard(card);
        bankAccount.getBusinessCards().remove(cardNumber);
        // add the report
        DataForTransactions data = new DataForTransactions().
                withCommand("deleteCard").
                withAccount(bankAccount.getIBAN()).
                withCardNumber(cardNumber).
                withEmail(bankAccount.
                        getEmail()).
                withTimestamp(commandInput.
                        getTimestamp());
        ObjectNode output = transactionReport.executeOperation(data);
        if (output != null) {
            ibanDB.
                    getUserFromIBAN(bankAccount.
                            getIBAN()).
                    addTransactionReport(output);
            bankAccount.addReport(output);
        }

        // create a new one time card
        Card newCard = new Card(true);
        bankAccount.addCard(newCard);
        // added by
        bankAccount.addBusinessCard(newCard.getNumber(), payer);
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
    /**
     * handles a business account online payment
     * @param bankAccount -
     * @param command -
     */
    private ObjectNode handleBusinessAccount(final BankAccount bankAccount, final BankOpData command) {
        CommandInput commandInput = command.getCommandInput();
        double amount = commandInput.getAmount();
        String email = commandInput.getEmail();
        String currency = commandInput.getCurrency();
        String cardNumber = commandInput.getCardNumber();
        int timestamp = commandInput.getTimestamp();
        MerchantsDB merchantDbB= command.getMerchantsDB();
        Merchant merchant = merchantDbB.getMerchants().get(commandInput.getCommerciant());
        EmailDB emailDB = command.getEmailDB();
        User payer = emailDB.getUser(email);
        TransactionReport transactionReport = command.getTransactionReport();
        IBANDB ibanDB = command.getIbanDB();

        if (payer == null) {
            return null;
        }

        if (merchant == null) {
            // the merchant doesn't exist
            return null;
        }

        HashMap<String, User> businessCards = bankAccount.getBusinessCards();
        if (!businessCards.containsKey(cardNumber)) { // the card does not exist
            ObjectNode output = new ObjectMapper().createObjectNode();
            output.put("command", "payOnline");
            ObjectNode aux = new ObjectMapper().createObjectNode();
            aux.put("timestamp", timestamp);
            aux.put("description", "Card not found");
            output.set("output", aux);
            output.put("timestamp", timestamp);
            return output;
        }

        Card card = bankAccount.getCards().get(cardNumber);
        if (card.getStatus().equals("frozen")) { // the card was frozen
            DataForTransactions data = new DataForTransactions().
                    withCommand("frozen").
                    withTimestamp(timestamp);
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

        ExchangeRate exchangeRate = command.getExchangeRate();
        double exRate = exchangeRate.getExchangeRate(currency, bankAccount.getCurrency());
        double toPay = amount * exRate;
        if (toPay == 0) {
            return null;
        }

        double toRon = exchangeRate.getExchangeRate(currency, "RON");
        double amountInRON = amount * toRon;
        User owner = bankAccount.getOwner();
        double feeInRon = owner.getServicePlan().fee(amountInRON);
        double fee = feeInRon * exchangeRate.getExchangeRate("RON", bankAccount.getCurrency());
        if (toPay + fee <= bankAccount.getBalance()) {
            if (bankAccount.getBalance() - toPay - fee < bankAccount.getMinAmount()) {
                // the card has to be frozen
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
            // continue with the payment, if it doesn't exceed the spending limit
            if (amount + fee <= bankAccount.getSpendingLimit()
                    || !bankAccount.getEmployees().contains(payer)) {
                if (amountInRON >= SILVER_TO_GOLD) {
                    owner.incrementPayToWin();
                }
                bankAccount.pay(toPay + fee);
                bankAccount.increaseSpending(amountInRON);
                getCashback(owner, bankAccount, amountInRON, commandInput.getCommerciant(),
                        command);
                bankAccount.spendMore(toPay, payer, timestamp);
                DataForTransactions data = new DataForTransactions().
                            withCommand("payOnline").
                            withAmount(amount * exRate).
                            withMerchant(commandInput.
                                    getCommerciant()).
                            withTimestamp(commandInput.getTimestamp());
                ObjectNode output = transactionReport.executeOperation(data);
                if (output != null) {
                    ibanDB.getUserFromIBAN(bankAccount.getIBAN()).
                                addTransactionReport(output);
                    bankAccount.addReport(output);
                }
                if (owner.getPayToWin() == RANK_UP && owner.getPlan().equals("silver")) {
                    owner.changeServicePlan("gold");
                    data = new DataForTransactions().
                            withTimestamp(commandInput.getTimestamp()).
                            withCommand("changeOfPlan").
                            withNewPlan("gold").
                            withAccount(bankAccount.getIBAN());
                    ObjectNode report = transactionReport.executeOperation(data);
                    owner.addTransactionReport(report);
                    bankAccount.addReport(report);
                }
                    // add the spending report
                bankAccount.getMerchants().
                        put(commandInput.getTimestamp(),
                                new Pair<>(merchant.getName(), amount * exRate));

                // check if the card is one time
                if (card.getOneTime()) {
                    handleOneTimeCard(bankAccount, card, payer, command);
                }
            } else { // we exceed the spending limit
                // we'll see
            }
        } else {
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
        return null;
    }

    /**
     * pays the user's bank account the respective cashback
     * @param user -
     * @param bankAccount -
     * @param paidInRon -
     * @param merchantName -
     * @param command -
     */
    private void getCashback(final User user, final BankAccount bankAccount, final double paidInRon,
                            final String merchantName, final BankOpData command) {
        MerchantsDB merchantDB = command.getMerchantsDB();
        Merchant merchant = merchantDB.merchantInfo(merchantName);

        assert merchant != null;

        if (merchant.getCashbackPlan().equals("spendingThreshold")) {
            bankAccount.increaseSpending(paidInRon);
            merchant.spendMore(paidInRon, bankAccount);
        } else { // nr of transactions
            if (!merchant.getTransactions().containsKey(bankAccount)) {
                merchant.getTransactions().put(bankAccount, 1);
            } else {
                int nr = merchant.getTransactions().get(bankAccount);
                merchant.getTransactions().remove(bankAccount);
                merchant.getTransactions().put(bankAccount, nr + 1);
            }
        }


        merchant.getCashback(paidInRon, bankAccount,
                user.getPlan(), command.getExchangeRate());
        ExchangeRate exchangeRate = command.getExchangeRate();
        double exRate = exchangeRate.getExchangeRate("RON", bankAccount.getCurrency());
        merchant.forceCashback(paidInRon * exRate, bankAccount);
        if (merchant.getCashbackPlan().equals("nrOfTransactions")) {
            int transactions = merchant.getTransactions().get(bankAccount);
            if (transactions >= 2 && bankAccount.getUsedFoodCB().equals("locked")) {
                bankAccount.setUsedFoodCB("unlocked");
            }
            if (transactions >= 5 && bankAccount.getUsedClothesCB().equals("locked")) {
                bankAccount.setUsedClothesCB("unlocked");
            }
            if (transactions >= 10 && bankAccount.getUsedClothesCB().equals("locked")) {
                bankAccount.setUsedClothesCB("unlocked");
            }
        }
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
            if (bankAccount.getAccountType().equals("business")) {
                return handleBusinessAccount(bankAccount, command);
            }
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

                if (amountInRON >= SILVER_TO_GOLD) {
                    user.incrementPayToWin();
                }
                bankAccount.pay(amount * exRate + fee);
                double toRon = exchangeRate.getExchangeRate(commandInput.getCurrency(), "RON");
                getCashback(user, bankAccount, amount * toRon,
                        commandInput.getCommerciant(), command);
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
                // if 5 payments were recorded and the user has a silver plan
                if (user.getPayToWin() == RANK_UP && user.getPlan().equals("silver")) {
                    user.changeServicePlan("gold");
                    data = new DataForTransactions().
                            withTimestamp(commandInput.getTimestamp()).
                            withCommand("changeOfPlan").
                            withNewPlan("gold").
                            withAccount(bankAccount.getIBAN());
                    ObjectNode report = transactionReport.executeOperation(data);
                    user.addTransactionReport(report);
                    bankAccount.addReport(report);
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
