package org.poo.BankingOperations;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.BankUsers.BankAccount;
import org.poo.BankUsers.EmailDB;
import org.poo.BankUsers.IBANDB;
import org.poo.BankUsers.User;
import org.poo.fileio.CommandInput;

public class ChangeDepositLimit implements BankingOperations {
    @Override
    public ObjectNode execute(final BankOpData command) {
        CommandInput commandInput = command.getCommandInput();
        String email = commandInput.getEmail();
        String iban = commandInput.getAccount();
        double newLimit = commandInput.getAmount();

        IBANDB ibanDB = command.getIbanDB();
        User owner = ibanDB.getUserFromIBAN(iban);
        if (owner == null) {
            return null;
        }
        if (!email.equals(owner.getEmail())) {
            // this is not the business account's owner
            ObjectNode output = new ObjectMapper().createObjectNode();
            output.put("command", commandInput.getCommand());
            ObjectNode out = new ObjectMapper().createObjectNode();
            out.put("description", "You must be owner in order to change spending limit.");
            out.put("timestamp", commandInput.getTimestamp());
            output.set("output", out);
            output.put("timestamp", commandInput.getTimestamp());
            return output;
        }

        BankAccount bankAccount = owner.getBankAccounts().get(iban);
        if (bankAccount == null) { // bank account doesn't exist
            return null;
        }

        bankAccount.setDepositLimit(newLimit);
        return null;
    }
}
