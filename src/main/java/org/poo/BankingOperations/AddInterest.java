package org.poo.BankingOperations;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.BankUsers.BankAccount;
import org.poo.BankUsers.IBANDB;
import org.poo.BankUsers.User;
import org.poo.fileio.CommandInput;

public final class AddInterest implements BankingOperations {
    @Override
    public ObjectNode execute(final BankOpData command) {
        CommandInput commandInput = command.getCommandInput();
        IBANDB ibanDB = command.getIbanDB();

        String account = commandInput.getAccount();
        User user = ibanDB.getUserFromIBAN(account);
        if (user == null) {
            ObjectNode output = new ObjectMapper().createObjectNode();
            output.put("command", "addInterest");
            ObjectNode out = new ObjectMapper().createObjectNode();
            out.put("description", "Account not found");
            out.put("timestamp", commandInput.getTimestamp());
            output.set("output", out);
            output.put("timestamp", commandInput.getTimestamp());
            return output;
        }

        BankAccount bankAccount = user.getBankAccounts().getOrDefault(account, null);
        assert (bankAccount != null);

        if (bankAccount.getAccountType().equals("classic")) {
            ObjectNode output = new ObjectMapper().createObjectNode();
            output.put("command", "addInterest");
            ObjectNode out = new ObjectMapper().createObjectNode();
            out.put("description", "This is not a savings account");
            out.put("timestamp", commandInput.getTimestamp());
            output.set("output", out);
            output.put("timestamp", commandInput.getTimestamp());
            return output;
        }

        bankAccount.setBalance(bankAccount.calculateInterest());
        return null;
    }
}
