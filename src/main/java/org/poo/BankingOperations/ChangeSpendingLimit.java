package org.poo.BankingOperations;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.BankUsers.BankAccount;
import org.poo.BankUsers.IBANDB;
import org.poo.BankUsers.User;
import org.poo.fileio.CommandInput;

public class ChangeSpendingLimit implements BankingOperations {
    @Override
    public ObjectNode execute(final BankOpData command) {
        CommandInput commandInput = command.getCommandInput();
        String email = commandInput.getEmail();
        String iban = commandInput.getAccount();
        double newLimit = commandInput.getAmount();
        //int timestamp = commandInput.getTimestamp();

        IBANDB ibanDB = command.getIbanDB();
        User owner = ibanDB.getUserFromIBAN(iban);
        if (owner == null) {
            return null;
        }
        if (!email.equals(owner.getEmail())) {
            // this is not the business account's owner
            return null;
        }

        BankAccount bankAccount = owner.getBankAccounts().get(iban);
        if (bankAccount == null) { // bank account doesn't exist
            return null;
        }

        bankAccount.setSpendingLimit(newLimit);
        return null;
    }
}
