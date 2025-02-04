package org.poo.BankingOperations;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.DataFormatReaders;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.BankUsers.BankAccount;
import org.poo.BankUsers.EmailDB;
import org.poo.BankUsers.User;
import org.poo.ExchangeRate.ExchangeRate;
import org.poo.Transactions.DataForTransactions;
import org.poo.Transactions.TransactionReport;
import org.poo.fileio.CommandInput;
import org.poo.BankUsers.CardDB;


public class CashWithdrawal implements BankingOperations {
    @Override
    public ObjectNode execute(final BankOpData command) {
        CommandInput commandInput = command.getCommandInput();
        String cardNumber = commandInput.getCardNumber();
        double amount = commandInput.getAmount();
        String email = commandInput.getEmail();
        String location = commandInput.getLocation();
        int timestamp = commandInput.getTimestamp();

        EmailDB emailDB = command.getEmailDB();
        User user = emailDB.getAssociatedEmails().get(email);
        if (user == null) { // user not found
            ObjectNode output = new ObjectMapper().createObjectNode();
            output.put("command", "cashWithdrawal");
            ObjectNode out = new ObjectMapper().createObjectNode();
            out.put("description", "User not found");
            out.put("timestamp", timestamp);
            output.set("output", out);
            output.put("timestamp", timestamp);
            return output;
        }
        CardDB cardDB = command.getCardDB();
        BankAccount bankAccount = cardDB.getAssociatedCards().get(cardNumber);
        if (bankAccount == null) { // account not found
            ObjectNode output = new ObjectMapper().createObjectNode();
            output.put("command", "cashWithdrawal");
            ObjectNode out = new ObjectMapper().createObjectNode();
            out.put("description", "Card not found");
            out.put("timestamp", timestamp);
            output.set("output", out);
            output.put("timestamp", timestamp);
            return output;
        }
        if (timestamp == 439) {
            System.out.println(bankAccount.getTimestamp());
        }
        if (bankAccount.getAccountType().equals("business")) {
            if (!bankAccount.getEmployees().contains(user)
            && !bankAccount.getManagers().contains(user) &&
            !bankAccount.getOwner().equals(user)) {
                ObjectNode output = new ObjectMapper().createObjectNode();
                output.put("command", "cashWithdrawal");
                ObjectNode out = new ObjectMapper().createObjectNode();
                out.put("description", "Card not found");
                out.put("timestamp", timestamp);
                output.set("output", out);
                output.put("timestamp", timestamp);
                return output;
            }
        }
        String currency = bankAccount.getCurrency();
        ExchangeRate exchangeRate = command.getExchangeRate();
        double exRate = exchangeRate.getExchangeRate("RON", currency);
        double withdrawAmount = amount * exRate;
        double feeInRON = user.getServicePlan().fee(amount);
        double rateInRON = exchangeRate.getExchangeRate("RON", currency);
        double fee = feeInRON * rateInRON;

        if (withdrawAmount + fee > bankAccount.getBalance()) { // not enough money
            DataForTransactions data = new DataForTransactions().
                    withCommand("noFunds").
                    withTimestamp(timestamp);
            TransactionReport transactionReport = command.getTransactionReport();
            ObjectNode output = transactionReport.executeOperation(data);
            user.addTransactionReport(output);
            bankAccount.addReport(output);
            return null;
        }

        bankAccount.pay(withdrawAmount + fee);
        DataForTransactions data = new DataForTransactions().
                withTimestamp(timestamp).
                withAmount(amount).
                withCommand("cashWithdrawal");
        TransactionReport transactionReport = command.getTransactionReport();
        ObjectNode report = transactionReport.executeOperation(data);
        user.addTransactionReport(report);
        bankAccount.addReport(report);
        return null;
    }
}
