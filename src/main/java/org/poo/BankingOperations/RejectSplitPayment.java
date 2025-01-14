package org.poo.BankingOperations;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.BankUsers.*;
import org.poo.Transactions.DataForTransactions;
import org.poo.Transactions.TransactionReport;
import org.poo.fileio.CommandInput;

import java.util.LinkedHashSet;
import java.util.List;

public class RejectSplitPayment implements BankingOperations {
    @Override
    public ObjectNode execute(final BankOpData command) {
        CommandInput commandInput = command.getCommandInput();
        String email = commandInput.getEmail();
        int timestamp = commandInput.getTimestamp();
        EmailDB emailDB = command.getEmailDB();
        if (!emailDB.getAssociatedEmails().containsKey(email)) {
            // the user does not exist
            ObjectNode output = new ObjectMapper().createObjectNode();
            output.put("command", "rejectSplitPayment");
            ObjectNode out = new ObjectMapper().createObjectNode();
            out.put("description", "User not found");
            out.put("timestamp", timestamp);
            output.set("output", out);
            output.put("timestamp", timestamp);
            return output;
        }

        User user = emailDB.getUser(email);
        LinkedHashSet<CustomSplit> activeSplits = command.getActiveSplits();
        for (CustomSplit split : activeSplits) {
            List<String> accounts = split.getAccounts();
            for (String account : accounts) {
                if (user.getBankAccounts().containsKey(account)) {
                    // we found the account that rejected the split payment
                    for (String acc : accounts) {
                        // for each account add the rejection report
                        IBANDB ibanDB = command.getIbanDB();
                        User currUser = ibanDB.getUserFromIBAN(acc);
                        assert currUser != null;
                        BankAccount bankAccount = currUser.getBankAccounts().get(acc);
                        assert bankAccount != null;
                        DataForTransactions data = new DataForTransactions().
                                withTimestamp(split.getTimestamp()).
                                withCommand("rejectSplit").
                                withAccount(account).
                                withAccounts(accounts).
                                withPayEach(split.getSplitBill()).
                                withCurrency(split.getCurrency()).
                                withSplitType(split.getType()).
                                withSplitAmount(split.getTotalAmount());
                        TransactionReport transactionReport = command.getTransactionReport();
                        ObjectNode report = transactionReport.executeOperation(data);
                        bankAccount.addReport(report);
                        currUser.addTransactionReport(report);
                    }
                    activeSplits.remove(split);
                    return null;
                }
            }
        }
        return null;
    }
}
