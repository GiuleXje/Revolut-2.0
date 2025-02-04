package org.poo.BankingOperations;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.jdi.ObjectReference;
import org.poo.BankUsers.BankAccount;
import org.poo.BankUsers.EmailDB;
import org.poo.BankUsers.IBANDB;
import org.poo.BankUsers.User;
import org.poo.ExchangeRate.ExchangeRate;
import org.poo.Transactions.DataForTransactions;
import org.poo.Transactions.TransactionReport;
import org.poo.fileio.CommandInput;
import org.poo.BankUsers.CustomSplit;

import java.awt.geom.NoninvertibleTransformException;
import java.util.LinkedHashSet;
import java.util.List;

public class AcceptSplitPayment implements BankingOperations {
    /**
     * handles a case where one of the payers does not have enough money to pay
     * @param split -
     * @param bankAccount -
     * @param command -
     */
    public void poorFriend(final CustomSplit split, final BankAccount bankAccount,
                           final BankOpData command) {
        DataForTransactions data;
        List<String> accounts = split.getAccounts();
        IBANDB ibanDB = command.getIbanDB();

        if (split.getType().equals("custom")) {
            int idx = 0;
            for (String account : accounts) {
                User user = ibanDB.getUserFromIBAN(account);
                BankAccount bkAccount = user.getBankAccounts().get(account);
                data = new DataForTransactions().
                        withCommand("poorFriendV2").
                        withTimestamp(split.getTimestamp()).
                        withAccount(bankAccount.getIBAN()).
                        withAccounts(split.getAccounts()).
                        withCurrency(split.getCurrency()).
                        withPayEach(split.getSplitBill()).
                        withAmount(split.getTotalAmount()).
                        withSplitType(split.getType());
                TransactionReport transactionReport = command.getTransactionReport();
                ObjectNode report = transactionReport.executeOperation(data);
                bkAccount.addReport(report);
                user.addTransactionReport(report);
            }

        } else {
            data = new DataForTransactions().
                    withCommand("poorFriend").
                    withAccounts(split.getAccounts()).
                    withAccount(bankAccount.getIBAN()).
                    withAmount(split.getTotalAmount()).
                    withSplitAmount(split.getSplitBill().getFirst()).
                    withTimestamp(split.getTimestamp()).
                    withCurrency(split.getCurrency()).
                    withSplitType(split.getType());
            TransactionReport transactionReport = command.getTransactionReport();
            ObjectNode report = transactionReport.executeOperation(data);
            for (String account : accounts) {
                User user = ibanDB.getUserFromIBAN(account);
                BankAccount bkAccount = user.getBankAccounts().get(account);
                bkAccount.addReport(report);
                user.addTransactionReport(report);
            }
        }
    }
    @Override
    public ObjectNode execute(final BankOpData command) {
        LinkedHashSet<CustomSplit> activeSplits = command.getActiveSplits();
        CommandInput commandInput = command.getCommandInput();
        String email = commandInput.getEmail();
        EmailDB emailDB = command.getEmailDB();
        User user = emailDB.getUser(email);
        if (user == null) {
            ObjectNode output = new ObjectMapper().createObjectNode();
            output.put("command", commandInput.getCommand());
            ObjectNode out = new ObjectMapper().createObjectNode();
            out.put("description", "User not found");
            out.put("timestamp", commandInput.getTimestamp());
            output.set("output", out);
            output.put("timestamp", commandInput.getTimestamp());
            return output;
        }

        for (CustomSplit split : activeSplits) { // go through all active splits
            List<String> accounts = split.getAccounts();
            int idx = 0;
            List<Boolean> hasPayed = split.getHasPayed();
            for (String account : accounts) { // go through each account involved
                if (user.getBankAccounts().containsKey(account)) { // see if the account belongs
                    if (!hasPayed.get(idx)) { // see if he already accepted this split
                        hasPayed.set(idx, true);
                        split.incrementAccountsFound();
                    }
                }
                idx++;
            }
        }

        for (CustomSplit split : activeSplits) {
            if (split.getAccountsFound() == split.getAccounts().size()) {
                // if we have a payment that all the accounts have agreed upon
                List<String> accounts = split.getAccounts();
                List<Double> eachPay = split.getSplitBill();
                IBANDB ibanDB = command.getIbanDB();
                ExchangeRate exchangeRate = command.getExchangeRate();
                int idx = 0;
                for (String account : accounts) {
                    User currUser = ibanDB.getUserFromIBAN(account);
                    BankAccount bankAccount = currUser.getBankAccounts().get(account);
                    assert bankAccount != null;
                    double exRate = exchangeRate.
                            getExchangeRate(split.getCurrency(), bankAccount.getCurrency());
                    double toPay = eachPay.get(idx);
                    if (toPay * exRate > bankAccount.getBalance()) {
                        poorFriend(split, bankAccount, command);
                        activeSplits.remove(split);
                        return null;
                    }
                    idx++;
                }

                // now we know all have enough to pay
                idx = 0;
                for (String account : accounts) {
                    User currUser = ibanDB.getUserFromIBAN(account);
                    BankAccount bankAccount = currUser.getBankAccounts().get(account);
                    assert bankAccount != null;
                    double exRate = exchangeRate.
                            getExchangeRate(split.getCurrency(), bankAccount.getCurrency());
                    double toPay = eachPay.get(idx);
                    bankAccount.pay(toPay * exRate);
                    if (split.getType().equals("custom")) {
                        DataForTransactions data = new DataForTransactions().
                                withTimestamp(split.getTimestamp()).
                                withAmount(split.getTotalAmount()).
                                withCurrency(split.getCurrency()).
                                withAccounts(accounts).
                                withPayEach(eachPay).
                                withCommand("customSplit").
                                withSplitType(split.getType());
                        TransactionReport transactionReport = command.getTransactionReport();
                        ObjectNode report = transactionReport.executeOperation(data);
                        currUser.addTransactionReport(report);
                        bankAccount.addReport(report);
                    } else {
                        DataForTransactions data = new DataForTransactions().
                            withCommand("splitBill").
                            withAmount(split.getTotalAmount()).
                            withCurrency(split.getCurrency()).
                            withPayerIBAN(account).
                            withSplitAmount(toPay).
                            withAccounts(accounts).
                            withTimestamp(split.getTimestamp()).
                                withSplitType(split.getType());
                        TransactionReport transactionReport = command.getTransactionReport();
                        ObjectNode report = transactionReport.executeOperation(data);
                        currUser.addTransactionReport(report);
                        bankAccount.addReport(report);
                    }

                    idx++;
                }

                activeSplits.remove(split);
                return null;
            }
        }
        return null;
    }
}
