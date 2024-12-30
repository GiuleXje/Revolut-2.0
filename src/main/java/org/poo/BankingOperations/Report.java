package org.poo.BankingOperations;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.BankUsers.BankAccount;
import org.poo.BankUsers.IBANDB;
import org.poo.BankUsers.User;
import org.poo.fileio.CommandInput;

public final class Report implements BankingOperations {
    @Override
    public ObjectNode execute(final BankOpData command) {
        CommandInput commandInput = command.getCommandInput();
        IBANDB ibanDB = command.getIbanDB();

        int start = commandInput.getStartTimestamp();
        int end = commandInput.getEndTimestamp();
        String account = commandInput.getAccount();
        User user = ibanDB.getUserFromIBAN(account);
        if (user == null) {
            ObjectNode output = new ObjectMapper().createObjectNode();
            output.put("command", "report");
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

        ArrayNode transactions = bankAccount.generateReport(start, end);
        ObjectNode output = new ObjectMapper().createObjectNode();
        ObjectNode out = new ObjectMapper().createObjectNode();
        output.put("command", "report");
        out.put("IBAN", account);
        out.put("balance", bankAccount.getBalance());
        out.put("currency", bankAccount.getCurrency());
        out.set("transactions", transactions);
        output.set("output", out);
        output.put("timestamp", commandInput.getTimestamp());
        return output;
    }
}
