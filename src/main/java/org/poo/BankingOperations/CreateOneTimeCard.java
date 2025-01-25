package org.poo.BankingOperations;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.BankUsers.User;
import org.poo.BankUsers.BankAccount;
import org.poo.BankUsers.IBANDB;
import org.poo.BankUsers.CardDB;
import org.poo.BankUsers.Card;
import org.poo.Transactions.DataForTransactions;
import org.poo.Transactions.TransactionReport;
import org.poo.fileio.CommandInput;

public final class CreateOneTimeCard implements BankingOperations {
    private void handleBusinessAccount(final BankAccount bankAccount, final User user,
                                       final BankOpData command) {
        Card card = new Card(true);
        CardDB cardDB = command.getCardDB();
        TransactionReport transactionReport = command.getTransactionReport();
        CommandInput commandInput = command.getCommandInput();
        cardDB.addCard(card, bankAccount);
        bankAccount.addCard(card);
        bankAccount.addBusinessCard(card.getNumber(), user);
        DataForTransactions data = new DataForTransactions().
                withCommand("createCard").
                withCardNumber(card.getNumber()).
                withAccount(bankAccount.getIBAN()).
                withEmail(bankAccount.getEmail()).
                withTimestamp(commandInput.getTimestamp());
        ObjectNode output = transactionReport.executeOperation(data);
        bankAccount.addReport(output);
    }
    @Override
    public ObjectNode execute(final BankOpData command) {
        CommandInput commandInput = command.getCommandInput();
        CardDB cardDB = command.getCardDB();
        TransactionReport transactionReport = command.getTransactionReport();
        IBANDB ibanDB = command.getIbanDB();
        String iban = commandInput.getAccount();
        User user = ibanDB.getUserFromIBAN(iban);
        if (user != null) {
            BankAccount bankAccount = user.getBankAccounts().get(iban);
            if (bankAccount != null) {
                if (bankAccount.getAccountType().equals("business")) {
                    handleBusinessAccount(bankAccount, user, command);
                    return null;
                }
                Card card = new Card(true);
                bankAccount.addCard(card);
                cardDB.addCard(card, bankAccount);
                DataForTransactions data = new DataForTransactions().
                        withCommand("createCard").
                        withCardNumber(card.getNumber()).
                        withAccount(bankAccount.getIBAN()).
                        withEmail(bankAccount.getEmail()).
                        withTimestamp(commandInput.
                                getTimestamp());
                ObjectNode output = transactionReport.executeOperation(data);
                if (output != null) {
                    user.addTransactionReport(output);
                    bankAccount.addReport(output);
                }
            }
        }

        return null;
    }
}
