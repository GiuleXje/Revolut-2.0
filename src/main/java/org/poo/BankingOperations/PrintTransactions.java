package org.poo.BankingOperations;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.BankUsers.EmailDB;
import org.poo.BankUsers.User;
import org.poo.fileio.CommandInput;

import java.util.ArrayList;

public final class PrintTransactions implements BankingOperations {
    @Override
    public ObjectNode execute(final BankOpData command) {
        CommandInput commandInput = command.getCommandInput();
        EmailDB emailDB = command.getEmailDB();
        int timestamp = commandInput.getTimestamp();
        String email = commandInput.getEmail();
        User user = emailDB.getUser(email);
        if (user != null) {
            ArrayList<ObjectNode> transactions = user.getTransactionReport();
            ObjectNode out = new ObjectMapper().createObjectNode();
            out.put("command", "printTransactions");
            ArrayNode output = new ObjectMapper().createArrayNode();
            for (ObjectNode transaction : transactions) {
                output.add(transaction);
            }
            out.set("output", output);
            out.put("timestamp", timestamp);
            return out;
        }
        return null;
    }
}
