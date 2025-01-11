package org.poo.BankingOperations;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.BankUsers.BankAccount;
import org.poo.BankUsers.IBANDB;
import org.poo.BankUsers.User;
import org.poo.fileio.CommandInput;

public final class AddFunds implements BankingOperations {
    private void handleBusinessAccount(final BankAccount bankAccount, final BankOpData command) {
        CommandInput commandInput = command.getCommandInput();
        double funds = commandInput.getAmount();
        double depositLimit = bankAccount.getDepositLimit();
        if (funds > depositLimit) {
            // can't add the funds
            return;
        }
        bankAccount.addFunds(funds);
    }
    @Override
    public ObjectNode execute(final BankOpData command) {
        CommandInput commandInput = command.getCommandInput();
        IBANDB ibanDB = command.getIbanDB();
        double funds = commandInput.getAmount();
        String iban = commandInput.getAccount();
        User user = ibanDB.getUserFromIBAN(iban);
        if (user != null) {
            BankAccount bankAccount = user.getBankAccounts().get(iban);
            if (bankAccount != null) {
                if (bankAccount.getAccountType().equals("business")) {
                    handleBusinessAccount(bankAccount, command);
                    return null;
                }
                bankAccount.addFunds(funds);
            }
        }
        return null;
    }
}
