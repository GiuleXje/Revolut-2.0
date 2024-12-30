package org.poo.BankingOperations;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.BankUsers.BankAccount;
import org.poo.BankUsers.CardDB;
import org.poo.BankUsers.IBANDB;
import org.poo.Transactions.DataForTransactions;
import org.poo.Transactions.TransactionReport;
import org.poo.fileio.CommandInput;

public final class CheckCardStatus implements BankingOperations {
    private static final int LIMIT = 30;
    @Override
    public ObjectNode execute(final BankOpData command) {
        CommandInput commandInput = command.getCommandInput();
        CardDB cardDB = command.getCardDB();
        IBANDB ibanDB = command.getIbanDB();
        TransactionReport transactionReport = command.getTransactionReport();
        String cardNumber = commandInput.getCardNumber();
        BankAccount bankAccount = cardDB.
                getAssociatedCards().
                getOrDefault(cardNumber, null);
        if (bankAccount == null) {
            ObjectNode output = new ObjectMapper().createObjectNode();
            output.put("command", commandInput.getCommand());
            ObjectNode aux = new ObjectMapper().createObjectNode();
            aux.put("timestamp", commandInput.getTimestamp());
            aux.put("description", "Card not found");
            output.set("output", aux);
            output.put("timestamp", commandInput.getTimestamp());
            return output;
        }

        if (bankAccount.getBalance() - bankAccount.getMinAmount() <= LIMIT) {
            DataForTransactions data = new DataForTransactions().
                    withCommand("minAmount").
                    withTimestamp(commandInput.
                            getTimestamp());
            ObjectNode output = transactionReport.executeOperation(data);
            if (output != null) {
                ibanDB.
                        getUserFromIBAN(bankAccount.getIBAN()).
                        addTransactionReport(output);
                bankAccount.addReport(output);
            }
        } else if (bankAccount.getBalance() < bankAccount.getMinAmount()) {
            DataForTransactions data = new DataForTransactions().withCommand("frozen").
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
}
