package org.poo.BankingOperations;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.BankUsers.BankAccount;
import org.poo.BankUsers.EmailDB;
import org.poo.BankUsers.User;
import org.poo.fileio.CommandInput;

public class ChangeDepositLimit implements BankingOperations {
    @Override
    public ObjectNode execute(final BankOpData command) {
        CommandInput commandInput = command.getCommandInput();
        String email = commandInput.getEmail();
        String iban = commandInput.getAccount();
        double newLimit = commandInput.getDepositLimit();
        int timestamp = commandInput.getTimestamp();

        EmailDB emailDB = command.getEmailDB();
        User user = emailDB.getUser(email);
        if (user == null) {
            return null;
        }
        BankAccount bankAccount = user.getBankAccounts().get(iban);
        if (bankAccount == null) {
            return null;
        }

        bankAccount.setDepositLimit(newLimit);
        return null;
    }
}
