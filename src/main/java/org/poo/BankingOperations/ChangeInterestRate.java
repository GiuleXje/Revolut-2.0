package org.poo.BankingOperations;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.BankUsers.BankAccount;
import org.poo.BankUsers.IBANDB;
import org.poo.BankUsers.User;
import org.poo.Transactions.DataForTransactions;
import org.poo.Transactions.TransactionReport;
import org.poo.fileio.CommandInput;

public final class ChangeInterestRate implements BankingOperations {
    @Override
    public ObjectNode execute(final BankOpData command) {
        CommandInput commandInput = command.getCommandInput();
        IBANDB ibanDB = command.getIbanDB();
        TransactionReport transactionReport = command.getTransactionReport();
        String account = commandInput.getAccount();
        User user = ibanDB.getUserFromIBAN(account);
        if (user == null) {
            ObjectNode output = new ObjectMapper().createObjectNode();
            output.put("command", "changeInterestRate");
            ObjectNode out = new ObjectMapper().createObjectNode();
            out.put("description", "Account not found");
            out.put("timestamp", commandInput.getTimestamp());
            output.set("output", out);
            output.put("timestamp", commandInput.getTimestamp());
            return output;
        }

        BankAccount bankAccount = user.getBankAccounts().getOrDefault(account, null);
        if (bankAccount == null) {
            return null;
        }

        if (bankAccount.getAccountType().equals("classic")) {
            ObjectNode output = new ObjectMapper().createObjectNode();
            output.put("command", "changeInterestRate");
            ObjectNode out = new ObjectMapper().createObjectNode();
            out.put("description", "This is not a savings account");
            out.put("timestamp", commandInput.getTimestamp());
            output.set("output", out);
            output.put("timestamp", commandInput.getTimestamp());
            return output;
        }

        bankAccount.changeInterest(commandInput.getInterestRate());
        DataForTransactions data = new DataForTransactions().
                withInterestRate(commandInput.getInterestRate()).
                withTimestamp(commandInput.getTimestamp()).
                withCommand("changeInterest");
        ObjectNode output = transactionReport.executeOperation(data);
        if (output != null) {
            user.addTransactionReport(output);
            bankAccount.addReport(output);
        }

        return null;
    }
}
