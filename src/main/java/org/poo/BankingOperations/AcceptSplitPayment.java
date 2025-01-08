package org.poo.BankingOperations;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.BankUsers.BankAccount;
import org.poo.BankUsers.EmailDB;
import org.poo.BankUsers.IBANDB;
import org.poo.BankUsers.User;
import org.poo.ExchangeRate.ExchangeRate;
import org.poo.Transactions.DataForTransactions;
import org.poo.Transactions.TransactionReport;
import org.poo.fileio.CommandInput;

import java.util.List;

public class AcceptSplitPayment implements BankingOperations {
    @Override
    public ObjectNode execute(BankOpData command) {
        CustomSplit customSplit = command.getSplit();
        if (customSplit == null) {
            System.out.println("Custom Split is null");
            return null;
        }
        CommandInput commandInput = command.getCommandInput();
        String email = commandInput.getEmail();
        int timestamp = commandInput.getTimestamp();

        EmailDB emailDB = command.getEmailDB();
        User user = emailDB.getUser(email);
        if (user == null) {
            return null;
        }

        List<String> accounts = customSplit.getAccounts();
        List<Double> payEach = customSplit.getSplitBill();
        for(String account : accounts) {
            if (user.getBankAccounts().getOrDefault(account, null) != null) {
                customSplit.incrementAccountsFound();
                break;
            }
        }

        // check to see if all accounts have accepted the payment
        if (accounts.size() == customSplit.getAccountsFound()) {
            IBANDB ibanDB = command.getIbanDB();
            ExchangeRate exchangeRate = command.getExchangeRate();
            int index = 0;
            String currency = customSplit.getCurrency();
            boolean canAllPay = true;
            for (String account : accounts) {
                User currUser = ibanDB.getUserFromIBAN(account);
                BankAccount bankAccount = currUser.getBankAccounts().getOrDefault(account, null);
                assert bankAccount != null;

                // get the amount that has to be paid
                double toPay = payEach.get(index);
                index++;

                double exRate = exchangeRate.getExchangeRate(bankAccount.getCurrency(), currency);
                if (toPay * exRate > bankAccount.getBalance()) {
                    canAllPay = false;
                }
            }

            if (canAllPay) {
                index = 0;
                for (String account : accounts) {
                    System.out.println("Da");
                    User currUser = ibanDB.getUserFromIBAN(account);
                    BankAccount bankAccount = currUser.getBankAccounts().getOrDefault(account, null);
                    assert bankAccount != null;
                    double toPay = payEach.get(index);
                    index++;
                    double exRate = exchangeRate.getExchangeRate(bankAccount.getCurrency(), currency);

                    bankAccount.pay(toPay * exRate);
                    DataForTransactions data = new DataForTransactions().
                            withTimestamp(timestamp).
                            withPayEach(payEach).
                            withAccounts(accounts).
                            withCurrency(currency).
                            withAmount(customSplit.getTotalAmount()).
                            withCommand("customSplit");
                    TransactionReport transactionReport = command.getTransactionReport();
                    ObjectNode report = transactionReport.executeOperation(data);
                    user.addTransactionReport(report);
                    bankAccount.addReport(report);
                }
            }
        }

        return null;
    }
}
