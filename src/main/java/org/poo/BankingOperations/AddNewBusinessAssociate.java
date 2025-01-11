package org.poo.BankingOperations;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.BankUsers.*;
import org.poo.fileio.CommandInput;

public class AddNewBusinessAssociate implements BankingOperations {
    @Override
    public ObjectNode execute(final BankOpData command) {
        CommandInput commandInput = command.getCommandInput();
        String iban = commandInput.getAccount();
        String role = commandInput.getRole();
        String email = commandInput.getEmail();

        EmailDB emailDB = command.getEmailDB();
        IBANDB ibanDB = command.getIbanDB();
        User user = emailDB.getUser(email);
        if (user == null) {
            return null;
        }
        BankAccount bankAccount = user.getBankAccounts().get(iban);
        if (!bankAccount.getAccountType().equals("business")) {
            return null;
        }
        if (role.equals("manager")) {
            bankAccount.addManager(user);
        } else {
            bankAccount.addEmployee(user);
        }
        return null;
    }
}
