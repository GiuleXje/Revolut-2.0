package org.poo.BankingOperations;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.BankUsers.BankAccount;
import org.poo.BankUsers.EmailDB;
import org.poo.BankUsers.IBANDB;
import org.poo.BankUsers.User;
import org.poo.ExchangeRate.ExchangeRate;
import org.poo.fileio.CommandInput;

public final class AddFunds implements BankingOperations {
    /**
     * handles a business account
     * @param bankAccount -
     * @param command -
     */
    private void handleBusinessAccount(final BankAccount bankAccount, final BankOpData command) {
        CommandInput commandInput = command.getCommandInput();
        double funds = commandInput.getAmount();
        double depositLimit = bankAccount.getDepositLimit();
        String email = commandInput.getEmail();
        EmailDB emailDB = command.getEmailDB();
        User user = emailDB.getUser(email);
        if (bankAccount.getEmployees().contains(user)) {
            ExchangeRate exchangeRate = command.getExchangeRate();
            double exRate = exchangeRate.getExchangeRate(bankAccount.getCurrency(), "RON");
           if (funds * exRate > depositLimit) {
               return;
           }
        }
        bankAccount.addFunds(funds);
        bankAccount.addMore(funds, user, commandInput.getTimestamp());
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
