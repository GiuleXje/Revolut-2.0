package org.poo.BankingOperations;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.BankUsers.User;
import org.poo.BankUsers.AliasDB;
import org.poo.BankUsers.BankAccount;
import org.poo.BankUsers.IBANDB;
import org.poo.ExchangeRate.ExchangeRate;
import org.poo.Merchants.Merchant;
import org.poo.Merchants.MerchantAccounts;
import org.poo.Transactions.DataForTransactions;
import org.poo.Transactions.TransactionReport;
import org.poo.fileio.CommandInput;

public final class SendMoney implements BankingOperations {
    public ObjectNode itIsAMerchant(final BankOpData command, final Merchant merchant) {
        CommandInput commandInput = command.getCommandInput();
        TransactionReport transactionReport = command.getTransactionReport();
        IBANDB ibanDB = command.getIbanDB();
        ExchangeRate exchangeRate = command.getExchangeRate();
        String giver = commandInput.getAccount();
        String email = commandInput.getEmail();
        User giverUser = ibanDB.getUserFromIBAN(giver);
        if (giverUser == null || !giverUser.getEmail().equals(email)) {
            ObjectNode output = new ObjectMapper().createObjectNode();
            output.put("command", "sendMoney");
            ObjectNode out = new ObjectMapper().createObjectNode();
            out.put("description", "User not found");
            out.put("timestamp", commandInput.getTimestamp());
            output.set("output", out);
            output.put("timestamp", commandInput.getTimestamp());
            return output;
        }

        BankAccount account = giverUser.getBankAccounts().getOrDefault(giver, null);
        if (account == null) {
            return null;
        }

        double amount = commandInput.getAmount();
        double toRON = exchangeRate.getExchangeRate(account.getCurrency(), "RON");
        double feeInRON = giverUser.getServicePlan().fee(amount * toRON);
        double fee = feeInRON * exchangeRate.getExchangeRate("RON", account.getCurrency());

        if (amount + fee > account.getBalance()) {
            DataForTransactions data =
                    new DataForTransactions().
                            withCommand("noFunds").
                            withTimestamp(commandInput.
                                    getTimestamp());
            ObjectNode output = transactionReport.
                    executeOperation(data);
            if (output != null) {
                account.
                        addReport(output);
                ibanDB.
                        getUserFromIBAN(account.
                                getIBAN()).
                        addTransactionReport(output);
            }
            return null;
        }

        account.pay(amount + fee);
        if (amount * toRON >= 300) {
            giverUser.incrementPayToWin();
        }
        if (merchant.getCashbackPlan().equals("spendingThreshold")) {
            account.increaseSpending(amount * toRON);
            merchant.spendMore(amount * toRON, account);
        } else {
            if (!merchant.getTransactions().containsKey(account)) {
                merchant.getTransactions().put(account, 1);
            } else {
                int nr = merchant.getTransactions().get(account);
                merchant.getTransactions().remove(account);
                merchant.getTransactions().put(account, nr + 1);
            }
        }
        merchant.getCashback(amount * toRON, account,
                giverUser.getPlan(), exchangeRate);
        merchant.forceCashback(amount * toRON, account);
        if (merchant.getCashbackPlan().equals("nrOfTransactions")) {
            int transactions = merchant.getTransactions().get(account);
            if (transactions >= 2 && account.getUsedFoodCB().equals("locked")) {
                account.setUsedFoodCB("unlocked");
            }
            if (transactions >= 5 && account.getUsedClothesCB().equals("locked")) {
                account.setUsedClothesCB("unlocked");
            }
            if (transactions >= 10 && account.getUsedTechCB().equals("locked")) {
                account.setUsedTechCB("unlocked");
            }
        }
        DataForTransactions data =
                new DataForTransactions().
                        withCommand("sendMoney").
                        withTimestamp(commandInput.
                                getTimestamp()).
                        withDescription(commandInput.
                                getDescription()).
                        withAmount(amount).
                        withCurrency(account.getCurrency()).
                        withPayerIBAN(account.
                                getIBAN()).
                        withReceiverIBAN(commandInput.getReceiver()).
                        withTransferType("sent");
        ObjectNode output = transactionReport.
                executeOperation(data);
        if (output != null) {
            account.addReport(output);
            ibanDB.
                    getUserFromIBAN(account.
                            getIBAN()).
                    addTransactionReport(output);
        }
        if (giverUser.getPayToWin() == 5 && giverUser.getPlan().equals("silver")) {
            giverUser.changeServicePlan("gold");
        }
        return null;
    }
    @Override
    public ObjectNode execute(final BankOpData command) {
        CommandInput commandInput = command.getCommandInput();
        String receiver = commandInput.getReceiver();

        // check to see if the receiver is a merchant
        MerchantAccounts merchantAccounts = command.getMerchantAccounts();
        Merchant merchant = merchantAccounts.getMerchAccounts().getOrDefault(receiver, null);
        if (merchant != null) {
            return itIsAMerchant(command, merchant);
        }

        TransactionReport transactionReport = command.getTransactionReport();
        IBANDB ibanDB = command.getIbanDB();
        ExchangeRate exchangeRate = command.getExchangeRate();
        AliasDB aliasDB = command.getAliasDB();
        String giver = commandInput.getAccount();
        String email = commandInput.getEmail();
        User giverUser = ibanDB.getUserFromIBAN(giver);
        if (giverUser == null || !giverUser.getEmail().equals(email)) {
            ObjectNode output = new ObjectMapper().createObjectNode();
            output.put("command", "sendMoney");
            ObjectNode out = new ObjectMapper().createObjectNode();
            out.put("description", "User not found");
            out.put("timestamp", commandInput.getTimestamp());
            output.set("output", out);
            output.put("timestamp", commandInput.getTimestamp());
            return output;
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
                        double toRON = exchangeRate.getExchangeRate(accCurrency, "RON");
                        double amountInRON = amount * toRON;
                        double feeInRON = giverUser.getServicePlan().fee(amountInRON);
                        double fee = feeInRON * exchangeRate.getExchangeRate("RON", accCurrency);
                        if (amount + fee
                                <= giverAccount.getBalance()) {
                            giverAccount.pay(amount + fee);
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
                } else { // receiver user not found
                    ObjectNode output = new ObjectMapper().createObjectNode();
                    output.put("command", "sendMoney");
                    ObjectNode out = new ObjectMapper().createObjectNode();
                    out.put("description", "User not found");
                    out.put("timestamp", commandInput.getTimestamp());
                    output.set("output", out);
                    output.put("timestamp", commandInput.getTimestamp());
                    return output;
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
        User giver = ibanDB.getUserFromIBAN(giverAccount.getIBAN());
        assert (giver != null);
        double toRON = exchangeRate.getExchangeRate(accCurrency, "RON");
        double amountInRON = amount * toRON;
        double feeInRON = giver.getServicePlan().fee(amountInRON);
        double fee = feeInRON * exchangeRate.getExchangeRate("RON", accCurrency);
        if (amount + fee
                <= giverAccount.getBalance()) {
            giverAccount.pay(amount + fee);
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
