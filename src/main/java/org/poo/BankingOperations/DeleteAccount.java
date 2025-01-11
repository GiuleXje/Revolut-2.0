package org.poo.BankingOperations;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.BankUsers.User;
import org.poo.BankUsers.AliasDB;
import org.poo.BankUsers.BankAccount;
import org.poo.BankUsers.CardDB;
import org.poo.BankUsers.Card;
import org.poo.BankUsers.IBANDB;
import org.poo.fileio.CommandInput;

public final class DeleteAccount implements BankingOperations {
    private ObjectNode handleBusinessAccount(final BankAccount bankAccount,
                                             final BankOpData command) {
        CommandInput commandInput = command.getCommandInput();
        IBANDB ibanDB = command.getIbanDB();
        String iban = bankAccount.getIBAN();
        User user = ibanDB.getUserFromIBAN(iban);
        if (!bankAccount.getOwner().equals(user)) {
            // bank account can only be deleted by its owner
            return null;
        }

        if (bankAccount.getBalance() > 0) {
            // can't delete while the balance is not 0
            ObjectNode output = new ObjectMapper().createObjectNode();
            output.put("command", commandInput.getCommand());
            ObjectNode status = new ObjectMapper().createObjectNode();
            status.put("error", "Account couldn't be deleted "
                    + "- see org.poo.transactions for details");
            ObjectNode tran = new ObjectMapper().createObjectNode();
            tran.put("description",
                    "Account couldn't be deleted - there are funds remaining");
            tran.put("timestamp", commandInput.getTimestamp());
            user.addTransactionReport(tran);
            bankAccount.addReport(tran);
            status.put("timestamp", commandInput.getTimestamp());
            output.set("status", status);
            output.put("timestamp", commandInput.getTimestamp());
            return output;
        }

        CardDB cardDB = command.getCardDB();
        // delete all the card and info about the bank account
        for (String cardNumber : bankAccount.getBusinessCards().keySet()) {
            cardDB.deleteCard(cardNumber);
        }
        AliasDB aliasDB = command.getAliasDB();
        aliasDB.removeAlias(bankAccount.getAlias() + user.getEmail());
        aliasDB.removeAlias(bankAccount.getIBAN() + user.getEmail());
        user.removeBankAccount(iban);
        ibanDB.deleteAccount(iban);

        ObjectNode output = new ObjectMapper().createObjectNode();
        output.put("command", commandInput.getCommand());
        ObjectNode status = new ObjectMapper().createObjectNode();
        status.put("success", "Account deleted");
        status.put("timestamp", commandInput.getTimestamp());
        output.set("status", status);
        output.put("timestamp", commandInput.getTimestamp());
        return output;
    }
    @Override
    public ObjectNode execute(final BankOpData command) {
        CommandInput commandInput = command.getCommandInput();
        AliasDB aliasDB = command.getAliasDB();
        CardDB cardDB = command.getCardDB();
        IBANDB ibanDB = command.getIbanDB();
        ObjectNode output = new ObjectMapper().createObjectNode();
        output.put("command", "deleteAccount");
        ObjectNode status = new ObjectMapper().createObjectNode();
        String iban = commandInput.getAccount();
        User user = ibanDB.getUserFromIBAN(iban);
        if (user != null) {
            BankAccount bankAccount = user.getBankAccounts().get(iban);
            if (bankAccount.getAccountType().equals("business")) {
                return handleBusinessAccount(bankAccount, command);
            }
            if (bankAccount.getBalance() == 0) {
                // delete each card from the database
                for (Card card : bankAccount.getCards().values()) {
                    cardDB.deleteCard(card.getNumber());
                }
                // delete all card related to the account
                bankAccount.deleteAllCards();
                // delete the alias from the alias database
                aliasDB.removeAlias(bankAccount.getAlias() + user.getEmail());
                aliasDB.removeAlias(bankAccount.getIBAN() + user.getEmail());
                // delete the bank account
                user.removeBankAccount(iban);
                /* remove the account form the database */
                ibanDB.deleteAccount(iban);
                status.put("success", "Account deleted");
            } else {
                status.put("error", "Account couldn't be deleted "
                        + "- see org.poo.transactions for details");
                ObjectNode tran = new ObjectMapper().createObjectNode();
                tran.put("description",
                        "Account couldn't be deleted - there are funds remaining");
                tran.put("timestamp", commandInput.getTimestamp());
                user.addTransactionReport(tran);
                bankAccount.addReport(tran);
            }
            status.put("timestamp", commandInput.getTimestamp());
            output.set("output", status);
        }
        output.put("timestamp", commandInput.getTimestamp());
        return output;
    }
}
