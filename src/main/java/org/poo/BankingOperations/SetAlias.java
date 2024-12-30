package org.poo.BankingOperations;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.BankUsers.User;
import org.poo.BankUsers.AliasDB;
import org.poo.BankUsers.BankAccount;
import org.poo.BankUsers.EmailDB;
import org.poo.fileio.CommandInput;

public final class SetAlias implements BankingOperations {
    @Override
    public ObjectNode execute(final BankOpData command) {
        CommandInput commandInput = command.getCommandInput();
        EmailDB emailDB = command.getEmailDB();
        AliasDB aliasDB = command.getAliasDB();

        String userEmail = commandInput.getEmail();
        String alias = commandInput.getAlias();
        User user = emailDB.getUser(userEmail);
        if (user != null) {
            BankAccount bankAccount = user.
                    getBankAccounts().
                    get(commandInput.getAccount());
            if (bankAccount != null) {
                bankAccount.changeAlias(alias);
                aliasDB.addAlias(alias + userEmail, bankAccount);
            }
        }
        return null;
    }
}
